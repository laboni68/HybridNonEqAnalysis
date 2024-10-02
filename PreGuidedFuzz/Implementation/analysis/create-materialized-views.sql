DROP TABLE IF EXISTS run_;
CREATE TABLE run_ AS
SELECT
    r.*
FROM run AS r INNER JOIN settings AS s ON r.id = s.run_id
WHERE
    s.tool != 'PASDA-base' -- PASDA-base is not fully implemented
    AND r.benchmark != 'Ell/zbrent/NEq'; -- ARDiff's instrumentation produces incompilable code for this case

CREATE UNIQUE INDEX IF NOT EXISTS run___id ON run_ (id);
CREATE INDEX IF NOT EXISTS run___benchmark ON run_ (benchmark);
CREATE INDEX IF NOT EXISTS run___result ON run_ (result);

DROP TABLE IF EXISTS iteration_;
CREATE TABLE iteration_ AS
SELECT i.*
FROM iteration AS i
INNER JOIN run_ AS r ON i.run_id = r.id;

CREATE UNIQUE INDEX IF NOT EXISTS iteration___id ON iteration_ (id);
CREATE INDEX IF NOT EXISTS iteration___run_id ON iteration_ (run_id);
CREATE INDEX IF NOT EXISTS iteration___result ON iteration_ (result);

DROP TABLE IF EXISTS partition_;
CREATE TABLE partition_ AS
SELECT p.*
FROM partition AS p
INNER JOIN iteration_ AS i ON p.iteration_id = i.id
WHERE p.result IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS partition___id ON partition_ (id);
CREATE INDEX IF NOT EXISTS partition___iteration_id ON partition_ (iteration_id);
CREATE INDEX IF NOT EXISTS partition___result ON partition_ (result);

WITH partition_counts AS (
    SELECT i.id, count(p.id) AS partition_count
    FROM iteration_ AS i
    LEFT JOIN partition_ AS p ON i.id = p.iteration_id
    WHERE i.partition_count IS NOT NULL
    GROUP BY i.id
)
UPDATE iteration_
SET partition_count = (SELECT partition_count FROM partition_counts WHERE partition_counts.id = iteration_.id)
WHERE exists(SELECT 1 FROM partition_counts WHERE partition_counts.id = iteration_.id);

UPDATE iteration_
SET result = 'TIMEOUT'
WHERE
    partition_count = 0
    AND has_timed_out = 1
    AND result = 'UNKNOWN';

WITH run_results AS (
    SELECT r.id, 'TIMEOUT' AS result
    FROM run_ AS r
    INNER JOIN iteration_ AS i ON r.id = i.run_id
    WHERE
        r.iteration_count = 1
        AND i.iteration = 1
        AND r.has_timed_out = 1
        AND r.result = 'UNKNOWN'
        AND i.result = 'TIMEOUT'
)
UPDATE run_
SET result = (SELECT result FROM run_results WHERE run_results.id = run_.id)
WHERE exists(SELECT 1 FROM run_results WHERE run_results.id = run_.id);

DROP TABLE IF EXISTS _run_result_runtime;
CREATE TABLE _run_result_runtime AS
SELECT
    id,
    r_runtime,
    i_runtime,
    p_runtime,
    i_runtime + p_runtime AS result_runtime
FROM (
    SELECT
        r.id,
        r.r_runtime,
        r.i_runtime,
        p.runtime AS p_runtime
    FROM (
        SELECT
            r.id,
            r.result_iteration,
            r.runtime AS r_runtime,
            coalesce(sum(i.runtime), 0) AS i_runtime
        FROM run_ AS r
        LEFT JOIN iteration_ AS i ON r.id = i.run_id AND r.result_iteration AND r.result_iteration > i.iteration
        WHERE r.result = 'NEQ'
        GROUP BY r.id
    ) AS r
    INNER JOIN iteration_ AS i ON r.id = i.run_id AND r.result_iteration = i.iteration
    INNER JOIN partition_ AS p ON i.id = p.iteration_id AND p.result = 'NEQ'
    ORDER BY r.id, p.id
)
GROUP BY id;

DROP TABLE IF EXISTS _run_result_iteration;
CREATE TABLE _run_result_iteration AS
SELECT
    id,
    coalesce(result, coalesce(result_2, coalesce(result_1, 'NULL'))) AS result,
    coalesce(iteration_count, iteration_count_) AS iteration_count,
    coalesce(result_iteration, coalesce(iteration_2, coalesce(iteration_1, 1))) AS result_iteration,
    coalesce(has_timed_out, result IS NULL) AS has_timed_out,
    coalesce(has_uif, FALSE) AS has_uif
FROM (
    SELECT
        id,
        result,
        iteration_count,
        result_iteration,
        count(*) AS iteration_count_,
        min(CASE WHEN row = 1 THEN iteration_result END) AS result_1, -- last iteration result
        min(CASE WHEN row = 2 THEN iteration_result END) AS result_2, -- second-to-last iteration result
        min(CASE WHEN row = 1 THEN iteration END) AS iteration_1,     -- last iteration
        min(CASE WHEN row = 2 THEN iteration END) AS iteration_2,     -- second-to-last iteration
        has_timed_out,
        has_uif
    FROM (
        SELECT
            r.*,
            i.result AS iteration_result,
            i.iteration,
            row_number() OVER (PARTITION BY r.id ORDER BY i.iteration DESC) AS row
        FROM run_ r
        LEFT JOIN iteration_ i ON r.id = i.run_id)
    GROUP BY id
);

DROP TABLE IF EXISTS _run_;
CREATE TABLE _run_ AS
SELECT
    benchmark,
    tool,
    run_timeout,
    depth_limit,
    row_number() OVER (PARTITION BY benchmark, tool, run_timeout, depth_limit) AS run,
    run_id,
    expected,
    result,
    result = expected AS is_correct,
    result IN ('EQ', 'NEQ') AND result != expected AS is_incorrect,
    result NOT IN ('EQ', 'NEQ') AS is_undecided,
    result LIKE 'MAYBE_%' AND result = 'MAYBE_' || expected AS is_maybe_correct,
    result LIKE 'MAYBE_%' AND result != 'MAYBE_' || expected AS is_maybe_incorrect,
    iteration_count,
    result_iteration,
    has_timed_out,
    has_uif,
    runtime,
    result_runtime,
    errors
FROM (
    SELECT
        b.benchmark,
        s.tool,
        s.run_timeout,
        s.depth_limit,
        r.id AS run_id,
        b.expected,
        rri.result,
        rri.iteration_count,
        rri.result_iteration,
        rri.has_timed_out,
        rri.has_uif,
        coalesce(r.runtime, s.run_timeout) AS runtime,
        coalesce(rrr.result_runtime, coalesce(r.runtime, s.run_timeout)) AS result_runtime,
        r.errors
    FROM run_ AS r
    INNER JOIN settings AS s ON r.id = s.run_id
    INNER JOIN benchmark AS b ON r.benchmark = b.benchmark
    LEFT JOIN _run_result_runtime AS rrr USING (id)
    INNER JOIN _run_result_iteration rri USING (id)
)
ORDER BY benchmark, tool, run_timeout, run_id;

CREATE UNIQUE INDEX IF NOT EXISTS _run___run_id ON _run_ (run_id);
CREATE INDEX IF NOT EXISTS _run___result ON _run_ (result);

DROP TABLE IF EXISTS _run_group_result;
CREATE TABLE _run_group_result AS
SELECT *
FROM (
    SELECT
        benchmark,
        tool,
        run_timeout,
        depth_limit,
        expected,
        result,
        count(result) AS cnt_result,
        is_correct,
        is_incorrect,
        is_undecided,
        is_maybe_correct,
        is_maybe_incorrect
    FROM _run_
    GROUP BY benchmark, tool, run_timeout, result
    ORDER BY
        benchmark, tool, run_timeout, cnt_result DESC,
        CASE WHEN result = 'EQ' THEN 1 WHEN result = 'NEQ' THEN 2 ELSE 3 END
)
GROUP BY benchmark, tool, run_timeout;

DROP TABLE IF EXISTS _run_group_result_summary;
CREATE TABLE _run_group_result_summary AS
SELECT
    tool,
    expected,
    group_concat("ALL", ' / ') AS 'ALL',
    group_concat(EQ, ' / ') AS 'EQ',
    group_concat(NEQ, ' / ') AS 'NEQ',
    group_concat(MAYBE_EQ, ' / ') AS 'MAYBE_EQ',
    group_concat(MAYBE_NEQ, ' / ') AS 'MAYBE_NEQ',
    group_concat(UNKNWN, ' / ') AS 'UNKNOWN',
    group_concat(DEPTH_LIMITED, ' / ') AS 'DEPTH_LIMITED',
    group_concat(TIMEOUT, ' / ') AS 'TIMEOUT',
    group_concat(ERROR, ' / ') AS 'ERROR',
    group_concat(INVALID, ' / ') AS 'INVALID'
FROM (
    SELECT
        tool,
        run_timeout,
        expected,
        count(*) AS 'ALL',
        sum(CASE WHEN result = 'EQ' THEN 1 ELSE 0 END) AS 'EQ',
        sum(CASE WHEN result = 'NEQ' THEN 1 ELSE 0 END) AS 'NEQ',
        sum(CASE WHEN result = 'MAYBE_EQ' THEN 1 ELSE 0 END) AS 'MAYBE_EQ',
        sum(CASE WHEN result = 'MAYBE_NEQ' THEN 1 ELSE 0 END) AS 'MAYBE_NEQ',
        sum(CASE WHEN result = 'UNKNOWN' THEN 1 ELSE 0 END) AS 'UNKNWN',
        sum(CASE WHEN result = 'DEPTH_LIMITED' THEN 1 ELSE 0 END) AS 'DEPTH_LIMITED',
        sum(CASE WHEN result = 'TIMEOUT' THEN 1 ELSE 0 END) AS 'TIMEOUT',
        sum(CASE WHEN result = 'ERROR' THEN 1 ELSE 0 END) AS 'ERROR',
        sum(
            CASE
                WHEN result NOT IN (
                    'EQ', 'NEQ', 'MAYBE_EQ', 'MAYBE_NEQ', 'UNKNOWN', 'DEPTH_LIMITED', 'TIMEOUT', 'ERROR'
                ) THEN 1
                ELSE 0
            END
        ) AS 'INVALID'
    FROM _run_group_result
    GROUP BY tool, run_timeout, expected
    ORDER BY tool, run_timeout, expected
)
GROUP BY tool, expected;

DROP TABLE IF EXISTS _run_group;
CREATE TABLE _run_group AS
SELECT *
FROM (
    SELECT
        benchmark,
        tool,
        run_timeout,
        depth_limit,
        --expected,
        -- result stats:
        group_concat(result, ', ') AS results,
        group_concat(run_id, ', ') AS run_ids,
        count(*) AS cnt_runs,
        sum(CASE WHEN NOT has_timed_out THEN 1 ELSE 0 END) AS cnt_runs_no_timeout,
        sum(CASE WHEN has_timed_out THEN 1 ELSE 0 END) AS cnt_runs_timeout,
        sum(has_timed_out) BETWEEN 1 AND count(*) - 1 AS some_timeout,
        count(DISTINCT result) AS cnt_distinct_results,
        --
        sum(is_correct) = count(*) AS all_correct,
        sum(is_correct) BETWEEN 1 AND count(*) - 1 AS some_correct,
        sum(is_correct) = 0 AS none_correct,
        sum(is_correct) AS sum_correct,
        --
        sum(is_incorrect) = count(*) AS all_incorrect,
        sum(is_incorrect) BETWEEN 1 AND count(*) - 1 AS some_incorrect,
        sum(is_incorrect) = 0 AS none_incorrect,
        sum(is_incorrect) AS sum_incorrect,
        -- maybe result stats:
        sum(is_maybe_correct) = count(*) AS all_maybe_correct,
        sum(is_maybe_incorrect) = count(*) AS all_maybe_incorrect,
        sum(is_maybe_correct) BETWEEN 1 AND count(*) - 1 AS some_maybe_correct,
        sum(is_maybe_incorrect) BETWEEN 1 AND count(*) - 1 AS some_maybe_incorrect,
        sum(is_maybe_correct) = 0 AS none_maybe_correct,
        sum(is_maybe_incorrect) = 0 AS none_maybe_incorrect,
        sum(is_maybe_correct) AS sum_maybe_correct,
        sum(is_maybe_incorrect) AS sum_maybe_incorrect,
        -- UIF stats:
        sum(has_uif) > 0 AS has_uif,
        sum(has_uif) AS sum_has_uif,
        -- runtime stats:
        sum(has_timed_out) > 0 AS has_timed_out,
        sum(has_timed_out) AS sum_timed_out,
        --
        group_concat(printf('%.2f', runtime), ', ') AS runtimes,
        min(runtime) AS min_runtime,
        max(runtime) AS max_runtime,
        max(runtime) - min(runtime) AS range_runtime,
        sum(runtime) AS sum_runtime,
        sum(CASE WHEN NOT has_timed_out THEN runtime ELSE 0 END) AS sum_runtime_no_timeout,
        sum(CASE WHEN has_timed_out THEN runtime ELSE 0 END) AS sum_runtime_timeout,
        avg(runtime) AS avg_runtime,
        median(runtime) AS median_runtime,
        avg(CASE WHEN NOT has_timed_out THEN runtime END) AS avg_runtime_no_timeout,
        avg(CASE WHEN has_timed_out THEN runtime END) AS avg_runtime_timeout,
        stddev(runtime) AS stddev_runtime,
        stddev(runtime) / avg(runtime) AS stddev_runtime_fraction,
        --
        group_concat(printf('%.2f', runtime), ', ') AS result_runtimes,
        min(result_runtime) AS min_result_runtime,
        max(result_runtime) AS max_result_runtime,
        max(result_runtime) - min(result_runtime) AS range_result_runtime,
        sum(result_runtime) AS sum_result_runtime,
        sum(CASE WHEN NOT has_timed_out THEN result_runtime ELSE 0 END) AS sum_res_runtime_no_timeout,
        sum(CASE WHEN has_timed_out THEN result_runtime ELSE 0 END) AS sum_res_runtime_timeout,
        avg(result_runtime) AS avg_result_runtime,
        median(result_runtime) AS median_result_runtime,
        avg(CASE WHEN NOT has_timed_out THEN result_runtime END) AS avg_res_runtime_no_timeout,
        avg(CASE WHEN has_timed_out THEN result_runtime END) AS avg_res_runtime_timeout,
        stddev(result_runtime) AS stddev_result_runtime,
        stddev(result_runtime) / avg(result_runtime) AS stddev_result_runtime_fraction,
        -- iteration stats:
        group_concat(result_iteration, ', ') AS result_iterations,
        count(DISTINCT result_iteration) AS cnt_distinc_iterations,
        min(result_iteration) AS min_iterations,
        max(result_iteration) AS max_iterations,
        max(result_iteration) - min(result_iteration) AS range_iterations,
        sum(result_iteration) AS sum_iterations,
        sum(CASE WHEN NOT has_timed_out THEN result_iteration ELSE 0 END) AS sum_iterations_no_timeout,
        sum(CASE WHEN has_timed_out THEN result_iteration ELSE 0 END) AS sum_iterations_timeout,
        avg(result_iteration) AS avg_iterations
    FROM _run_
    GROUP BY benchmark, tool, run_timeout
) AS r
INNER JOIN _run_group_result AS rr USING (benchmark, tool, run_timeout);

DROP TABLE IF EXISTS _run_group_result_list;
CREATE TABLE _run_group_result_list AS
SELECT
    tool,
    run_timeout,
    --
    sum(sum_runtime) / sum(cnt_runs) AS avg_runtime,
    sum(sum_runtime_no_timeout) / sum(cnt_runs_no_timeout) AS avg_runtime_no_timeout,
    sum(sum_runtime_timeout) / sum(cnt_runs_timeout) AS avg_runtime_timeout,
    --
    sum(sum_result_runtime) / sum(cnt_runs) AS avg_result_runtime,
    sum(sum_res_runtime_no_timeout) / sum(cnt_runs_no_timeout) AS avg_result_runtime_no_timeout,
    sum(sum_res_runtime_timeout) / sum(cnt_runs_timeout) AS avg_result_runtime_timeout,
    --
    sum(cnt_runs) AS cnt_runs,
    sum(cnt_runs) - sum(sum_timed_out) AS cnt_runs_no_timeout,
    sum(sum_timed_out) AS cnt_runs_timeout,
    --
    count(*) AS cnt_cases,
    sum(is_correct) AS cnt_cases_correct,
    sum(is_incorrect) AS cnt_cases_incorrect,
    sum(is_undecided) AS cnt_cases_undecided,
    --
    count(*) - sum(has_timed_out) AS cnt_cases_no_timeout,
    sum(has_timed_out) AS cnt_cases_timeout,
    --
    sum(sum_iterations) AS cnt_iterations,
    sum(1.0 * sum_iterations) / sum(cnt_runs) AS avg_iterations,
    sum(1.0 * sum_iterations_no_timeout) / sum(cnt_runs_no_timeout) AS avg_iterations_no_timeout,
    sum(1.0 * sum_iterations_no_timeout) / sum(cnt_runs_timeout) AS avg_iterations_timeout
FROM _run_group
GROUP BY tool, run_timeout;

DROP TABLE IF EXISTS _run_group_result_list_eq_neq;
CREATE TABLE _run_group_result_list_eq_neq AS
SELECT
    tool,
    run_timeout,
    expected,
    --
    sum(sum_runtime) / sum(cnt_runs) AS avg_runtime,
    sum(sum_runtime_no_timeout) / sum(cnt_runs_no_timeout) AS avg_runtime_no_timeout,
    sum(sum_runtime_timeout) / sum(cnt_runs_timeout) AS avg_runtime_timeout,
    --
    sum(sum_result_runtime) / sum(cnt_runs) AS avg_result_runtime,
    sum(sum_res_runtime_no_timeout) / sum(cnt_runs_no_timeout) AS avg_result_runtime_no_timeout,
    sum(sum_res_runtime_timeout) / sum(cnt_runs_timeout) AS avg_result_runtime_timeout,
    --
    sum(cnt_runs) AS cnt_runs,
    sum(cnt_runs) - sum(sum_timed_out) AS cnt_runs_no_timeout,
    sum(sum_timed_out) AS cnt_runs_timeout,
    --
    count(*) AS cnt_cases,
    sum(is_correct) AS cnt_cases_correct,
    sum(is_incorrect) AS cnt_cases_incorrect,
    sum(is_undecided) AS cnt_cases_undecided,
    --
    count(*) - sum(has_timed_out) AS cnt_cases_no_timeout,
    sum(has_timed_out) AS cnt_cases_timeout,
    --
    sum(sum_iterations) AS cnt_iterations,
    sum(1.0 * sum_iterations) / sum(cnt_runs) AS avg_iterations,
    sum(1.0 * sum_iterations_no_timeout) / sum(cnt_runs_no_timeout) AS avg_iterations_no_timeout,
    sum(1.0 * sum_iterations_no_timeout) / sum(cnt_runs_timeout) AS avg_iterations_timeout
FROM _run_group
GROUP BY tool, run_timeout, expected;

DROP TABLE IF EXISTS _run_group_runtime_summary;
CREATE TABLE _run_group_runtime_summary AS
SELECT
    tool AS tool_a,
    expected AS expected_a,
    group_concat(printf('%.2f', avg_runtime), ' / ') AS avg_runtime,
    group_concat(printf('%.2f', avg_runtime_no_timeout), ' / ') AS avg_runtime_no_timeout,
    group_concat(printf('%.2f', avg_runtime_timeout), ' / ') AS avg_runtime_timeout,
    '|',
    tool AS tool_b,
    expected AS expected_b,
    group_concat(cnt_runs, ' / ') AS cnt_runs,
    group_concat(cnt_runs_no_timeout, ' / ') AS cnt_runs_no_timeout,
    group_concat(cnt_runs_timeout, ' / ') AS cnt_runs_timeout,
    '|',
    tool AS tool_c,
    expected AS expected_c,
    group_concat(cnt_cases, ' / ') AS cnt_cases,
    group_concat(cnt_cases_no_timeout, ' / ') AS cnt_cases_no_timeout,
    group_concat(cnt_cases_timeout, ' / ') AS cnt_cases_timeout,
    '|',
    tool AS tool_d,
    expected AS expected_d,
    group_concat(cnt_iterations, ' / ') AS cnt_iterations,
    group_concat(printf('%.2f', avg_iterations), ' / ') AS avg_iterations,
    group_concat(printf('%.2f', avg_iterations_no_timeout), ' / ') AS avg_iterations_no_timeout,
    group_concat(printf('%.2f', avg_iterations_timeout), ' / ') AS avg_iterations_timeout
FROM _run_group_result_list_eq_neq
GROUP BY tool, expected;

DROP TABLE IF EXISTS _run_group_step_runtime_list;
CREATE TABLE _run_group_step_runtime_list AS
SELECT
    r.tool,
    r.run_timeout,
    rt.task,
    rt.step,
    -- per step, all runs
    avg(rt.runtime) AS avg_runtime_step,
    avg(CASE WHEN r.expected = 'EQ' THEN rt.runtime END) AS avg_runtime_step_eq,
    avg(CASE WHEN r.expected = 'NEQ' THEN rt.runtime END) AS avg_runtime_step_neq,
    -- per step, non-timed-out runs
    avg(CASE WHEN r.has_timed_out = 0 THEN rt.runtime END) AS avg_runtime_step_no_timeout,
    avg(CASE WHEN r.expected = 'EQ' AND r.has_timed_out = 0 THEN rt.runtime END) AS avg_runtime_step_eq_no_timeout,
    avg(CASE WHEN r.expected = 'NEQ' AND r.has_timed_out = 0 THEN rt.runtime END) AS avg_runtime_step_neq_no_timeout,
    -- per step, timed-out runs
    avg(CASE WHEN r.has_timed_out = 1 THEN rt.runtime END) AS avg_runtime_step_timeout,
    avg(CASE WHEN r.expected = 'EQ' AND r.has_timed_out = 1 THEN rt.runtime END) AS avg_runtime_step_eq_timeout,
    avg(CASE WHEN r.expected = 'NEQ' AND r.has_timed_out = 1 THEN rt.runtime END) AS avg_runtime_step_neq_timeout,
    -- across all steps, all runs
    avg(r.runtime) AS avg_runtime_r,
    avg(CASE WHEN r.expected = 'EQ' THEN r.runtime END) AS avg_runtime_r_eq,
    avg(CASE WHEN r.expected = 'NEQ' THEN r.runtime END) AS avg_runtime_r_neq
FROM (
    SELECT *
    FROM (
        SELECT
            run_id,
            task,
            sum(runtime) AS runtime,
            max(step) AS step,
            count(*) OVER (PARTITION BY run_id) AS count_
        FROM runtime
        WHERE task != ''
        GROUP BY run_id, task
        ORDER BY run_id, max(step)
    )
    WHERE count_ == (SELECT count(DISTINCT r.task) FROM runtime r WHERE r.task != '')
    ORDER BY run_id, step
) AS rt
INNER JOIN _run_ r ON rt.run_id = r.run_id
GROUP BY r.tool, r.run_timeout, rt.task
ORDER BY r.tool, r.run_timeout, rt.step;

DROP TABLE IF EXISTS _run_group_step_runtime_summary;
CREATE TABLE _run_group_step_runtime_summary AS
SELECT
    tool AS tool_a,
    task AS task_a,
    group_concat(printf('%.2f', avg_runtime_step), ' / ') AS avg_runtime_step,
    group_concat(printf('%.2f', avg_runtime_step_eq), ' / ') AS avg_runtime_step_eq,
    group_concat(printf('%.2f', avg_runtime_step_neq), ' / ') AS avg_runtime_step_neq,
    '|',
    tool AS tool_b,
    task AS task_b,
    group_concat(printf('%.2f', avg_runtime_step_no_timeout), ' / ') AS avg_runtime_step_no_timeout,
    group_concat(printf('%.2f', avg_runtime_step_eq_no_timeout), ' / ') AS avg_runtime_step_eq_no_timeout,
    group_concat(printf('%.2f', avg_runtime_step_neq_no_timeout), ' / ') AS avg_runtime_step_neq_no_timeout,
    '|',
    tool AS tool_c,
    task AS task_c,
    group_concat(printf('%.2f', avg_runtime_step_timeout), ' / ') AS avg_runtime_step_timeout,
    group_concat(printf('%.2f', avg_runtime_step_eq_timeout), ' / ') AS avg_runtime_step_eq_timeout,
    group_concat(printf('%.2f', avg_runtime_step_neq_timeout), ' / ') AS avg_runtime_step_neq_timeout,
    '|',
    tool AS tool_d,
    group_concat(printf('%.2f', avg_runtime_r), ' / ') AS avg_runtime_r,
    group_concat(printf('%.2f', avg_runtime_r_eq), ' / ') AS avg_runtime_r_eq,
    group_concat(printf('%.2f', avg_runtime_r_neq), ' / ') AS avg_runtime_r_neq
FROM _run_group_step_runtime_list
GROUP BY tool, task, step
ORDER BY tool, step;

DROP TABLE IF EXISTS _run_group_result_tool_comparison;
CREATE TABLE _run_group_result_tool_comparison AS
SELECT *,
    CASE
        WHEN is_correct_1 AND is_correct_2 THEN 'BOTH_CORRECT'
        WHEN is_correct_1 AND NOT is_correct_2 THEN 'ONLY_1_CORRECT'
        WHEN NOT is_correct_1 AND is_correct_2 THEN 'ONLY_2_CORRECT'
        WHEN NOT is_correct_1 AND NOT is_correct_2 THEN 'NONE_CORRECT'
    END AS correctness,
    CASE
        WHEN is_maybe_correct_1 AND is_maybe_correct_2 THEN 'BOTH_CORRECT'
        WHEN is_maybe_correct_1 AND NOT is_maybe_correct_2 THEN 'ONLY_1_CORRECT'
        WHEN NOT is_maybe_correct_1 AND is_maybe_correct_2 THEN 'ONLY_2_CORRECT'
        WHEN NOT is_maybe_correct_1 AND NOT is_maybe_correct_2 THEN 'NONE_CORRECT'
    END AS maybe_correctness,
    CASE
        WHEN is_maybe_incorrect_1 AND is_maybe_incorrect_2 THEN 'BOTH_INCORRECT'
        WHEN is_maybe_incorrect_1 AND NOT is_maybe_incorrect_2 THEN 'ONLY_1_INCORRECT'
        WHEN NOT is_maybe_incorrect_1 AND is_maybe_incorrect_2 THEN 'ONLY_2_INCORRECT'
        WHEN NOT is_maybe_incorrect_1 AND NOT is_maybe_incorrect_2 THEN 'NONE_INCORRECT'
    END AS maybe_incorrectness,
    CASE
        WHEN has_timed_out_1 AND has_timed_out_2 THEN 'TIE'
        WHEN avg_runtime_1 = avg_runtime_2 THEN 'TIE'
        WHEN avg_runtime_2 > avg_runtime_1 THEN '1_FASTER'
        WHEN avg_runtime_1 > avg_runtime_2 THEN '2_FASTER'
    END AS faster_avg_runtime,
    CASE
        WHEN has_timed_out_1 AND has_timed_out_2 THEN 'TIE'
        WHEN avg_result_runtime_1 = avg_result_runtime_2 THEN 'TIE'
        WHEN avg_result_runtime_2 > avg_result_runtime_1 THEN '1_FASTER'
        WHEN avg_runtime_1 > avg_result_runtime_2 THEN '2_FASTER'
    END AS faster_avg_result_runtime,
    CASE
        WHEN has_timed_out_1 AND has_timed_out_2 THEN 'TIE'
        WHEN median_runtime_1 = median_runtime_2 THEN 'TIE'
        WHEN median_runtime_2 > median_runtime_1 THEN '1_FASTER'
        WHEN median_runtime_1 > median_runtime_2 THEN '2_FASTER'
    END AS faster_median_runtime,
    CASE
        WHEN has_timed_out_1 AND has_timed_out_2 THEN 'TIE'
        WHEN median_result_runtime_1 = median_result_runtime_2 THEN 'TIE'
        WHEN median_result_runtime_2 > median_result_runtime_1 THEN '1_FASTER'
        WHEN median_runtime_1 > median_result_runtime_2 THEN '2_FASTER'
    END AS faster_median_result_runtime
FROM (
    SELECT
        s1.benchmark AS benchmark,
        s1.run_timeout AS run_timeout,
        s1.depth_limit AS depth_limit,
        s1.tool AS tool_1,
        s2.tool AS tool_2,
        s1.expected AS expected,
        s1.result AS result_1,
        s2.result AS result_2,
        s1.results AS results_1,
        s2.results AS results_2,
        --
        s1.is_correct AS is_correct_1,
        s2.is_correct AS is_correct_2,
        s1.is_incorrect AS is_incorrect_1,
        s2.is_incorrect AS is_incorrect_2,
        --
        s1.is_maybe_correct AS is_maybe_correct_1,
        s2.is_maybe_correct AS is_maybe_correct_2,
        s1.is_maybe_incorrect AS is_maybe_incorrect_1,
        s2.is_maybe_incorrect AS is_maybe_incorrect_2,
        --
        s1.result_iterations AS result_iterations_1,
        s2.result_iterations AS result_iterations_2,
        --
        s1.has_uif AS has_uif_1,
        s2.has_uif AS has_uif_2,
        --
        s1.has_timed_out AS has_timed_out_1,
        s2.has_timed_out AS has_timed_out_2,
        s1.avg_runtime AS avg_runtime_1,
        s2.avg_runtime AS avg_runtime_2,
        s1.avg_result_runtime AS avg_result_runtime_1,
        s2.avg_result_runtime AS avg_result_runtime_2,
        s1.median_runtime AS median_runtime_1,
        s2.median_runtime AS median_runtime_2,
        s1.median_result_runtime AS median_result_runtime_1,
        s2.median_result_runtime AS median_result_runtime_2
    FROM _run_group AS s1
    INNER JOIN _run_group AS s2 USING (benchmark, run_timeout)
    WHERE s1.tool != s2.tool
    ORDER BY s1.tool, s2.tool, benchmark, run_timeout
);

DROP TABLE IF EXISTS _run_group_result_tool_comparison_summary;
CREATE TABLE _run_group_result_tool_comparison_summary AS
SELECT *
FROM (
    SELECT
        tool_1,
        tool_2,
        run_timeout,
        depth_limit,
        ex AS expected,
        '|',
        -- provable results
        sum(is_correct_1) AS cnt_1_correct,
        sum(is_correct_2) AS cnt_2_correct,
        sum(CASE WHEN correctness = 'BOTH_CORRECT' THEN 1 ELSE 0 END) AS cnt_both_correct,
        sum(CASE WHEN correctness = 'ONLY_1_CORRECT' THEN 1 ELSE 0 END) AS cnt_only_1_correct,
        sum(CASE WHEN correctness = 'ONLY_2_CORRECT' THEN 1 ELSE 0 END) AS cnt_only_2_correct,
        sum(CASE WHEN correctness = 'NONE_CORRECT' THEN 1 ELSE 0 END) AS cnt_none_correct,
        '|',
        -- non-provable results
        sum(is_maybe_correct_1) AS cnt_1_maybe_correct,
        sum(is_maybe_incorrect_1) AS cnt_1_maybe_incorrect,
        sum(is_maybe_correct_2) AS cnt_2_maybe_correct,
        sum(is_maybe_incorrect_2) AS cnt_2_maybe_incorrect,
        '|',
        --
        group_concat(CASE WHEN correctness = 'BOTH_CORRECT' THEN benchmark END, ', ') AS both_correct,
        group_concat(CASE WHEN correctness = 'ONLY_1_CORRECT' THEN benchmark END, ', ') AS only_1_correct,
        group_concat(CASE WHEN correctness = 'ONLY_2_CORRECT' THEN benchmark END, ', ') AS only_2_correct,
        group_concat(CASE WHEN correctness = 'NONE_CORRECT' THEN benchmark END, ', ') AS none_correct,
        '|',
        -- avg runtime results
        sum(CASE WHEN faster_avg_runtime = '1_FASTER' THEN 1 ELSE 0 END) AS cnt_avg_runtime_1_faster,
        sum(CASE WHEN faster_avg_runtime = '2_FASTER' THEN 1 ELSE 0 END) AS cnt_avg_runtime_2_faster,
        sum(CASE WHEN faster_avg_runtime = 'TIE' THEN 1 ELSE 0 END) AS cnt_avg_runtime_time,
        '|',
        -- median runtime results
        sum(CASE WHEN faster_median_runtime = '1_FASTER' THEN 1 ELSE 0 END) AS cnt_median_runtime_1_faster,
        sum(CASE WHEN faster_median_runtime = '2_FASTER' THEN 1 ELSE 0 END) AS cnt_median_runtime_2_faster,
        sum(CASE WHEN faster_median_runtime = 'TIE' THEN 1 ELSE 0 END) AS cnt_median_runtime_tie,
        '|',
        -- avg result-runtime results
        sum(CASE WHEN faster_avg_result_runtime = '1_FASTER' THEN 1 ELSE 0 END) AS cnt_avg_result_runtime_1_faster,
        sum(CASE WHEN faster_avg_result_runtime = '2_FASTER' THEN 1 ELSE 0 END) AS cnt_avg_result_runtime_2_faster,
        sum(CASE WHEN faster_avg_result_runtime = 'TIE' THEN 1 ELSE 0 END) AS cnt_avg_result_runtime_tie,
        '|',
        -- median result-runtime results
        sum(CASE WHEN faster_median_result_runtime = '1_FASTER' THEN 1 ELSE 0 END) AS cnt_median_result_runtime_1_faster,
        sum(CASE WHEN faster_median_result_runtime = '2_FASTER' THEN 1 ELSE 0 END) AS cnt_median_result_runtime_2_faster,
        sum(CASE WHEN faster_median_result_runtime = 'TIE' THEN 1 ELSE 0 END) AS cnt_median_result_runtime_tie,
        --
        group_concat(CASE WHEN faster_median_result_runtime = '1_FASTER' THEN benchmark END, ', ') AS '1_faster',
        group_concat(CASE WHEN faster_median_result_runtime = '2_FASTER' THEN benchmark END, ', ') AS '2_faster',
        group_concat(CASE WHEN faster_median_result_runtime = 'TIE' THEN benchmark END, ', ') AS 'tie'
    FROM (
        SELECT expected AS ex, *
        FROM _run_group_result_tool_comparison
        UNION ALL
        SELECT '*' AS ex, *
        FROM _run_group_result_tool_comparison
    )
    GROUP BY tool_1, tool_2, run_timeout, ex
);

DROP TABLE IF EXISTS _run_group_result_runtime_comparison;
CREATE TABLE _run_group_result_runtime_comparison AS
SELECT *,
    CASE
        WHEN is_correct_1 AND is_correct_2 THEN 'BOTH_CORRECT'
        WHEN is_correct_1 AND NOT is_correct_2 THEN 'ONLY_1_CORRECT'
        WHEN NOT is_correct_1 AND is_correct_2 THEN 'ONLY_2_CORRECT'
        WHEN NOT is_correct_1 AND NOT is_correct_2 THEN 'NONE_CORRECT'
    END AS correctness,
    CASE
        WHEN is_maybe_correct_1 AND is_maybe_correct_2 THEN 'BOTH_CORRECT'
        WHEN is_maybe_correct_1 AND NOT is_maybe_correct_2 THEN 'ONLY_1_CORRECT'
        WHEN NOT is_maybe_correct_1 AND is_maybe_correct_2 THEN 'ONLY_2_CORRECT'
        WHEN NOT is_maybe_correct_1 AND NOT is_maybe_correct_2 THEN 'NONE_CORRECT'
    END AS maybe_correctness,
    CASE
        WHEN is_maybe_incorrect_1 AND is_maybe_incorrect_2 THEN 'BOTH_INCORRECT'
        WHEN is_maybe_incorrect_1 AND NOT is_maybe_incorrect_2 THEN 'ONLY_1_INCORRECT'
        WHEN NOT is_maybe_incorrect_1 AND is_maybe_incorrect_2 THEN 'ONLY_2_INCORRECT'
        WHEN NOT is_maybe_incorrect_1 AND NOT is_maybe_incorrect_2 THEN 'NONE_INCORRECT'
    END AS maybe_incorrectness
FROM (
    SELECT
        s1.tool AS tool,
        s1.benchmark AS benchmark,
        s1.run_timeout AS run_timeout_1,
        s2.run_timeout AS run_timeout_2,
        s1.expected AS expected,
        s1.result AS result_1,
        s2.result AS result_2,
        s1.results AS results_1,
        s2.results AS results_2,
        s1.run_ids AS run_ids_1,
        s2.run_ids AS run_ids_2,
        --
        s1.is_correct AS is_correct_1,
        s2.is_correct AS is_correct_2,
        s1.is_incorrect AS is_incorrect_1,
        s2.is_incorrect AS is_incorrect_2,
        --
        s1.is_maybe_correct AS is_maybe_correct_1,
        s2.is_maybe_correct AS is_maybe_correct_2,
        s1.is_maybe_incorrect AS is_maybe_incorrect_1,
        s2.is_maybe_incorrect AS is_maybe_incorrect_2
    FROM _run_group AS s1
    INNER JOIN _run_group AS s2 USING (benchmark, tool)
    WHERE s1.run_timeout != s2.run_timeout
    ORDER BY tool, benchmark, s1.run_timeout DESC, s2.run_timeout DESC
);

DROP TABLE IF EXISTS _run_group_result_runtime_comparison_summary;
CREATE TABLE _run_group_result_runtime_comparison_summary AS
SELECT *
FROM (
    SELECT
        tool,
        run_timeout_1,
        run_timeout_2,
        expected,
        '|',
        -- provable results
        sum(is_correct_1) AS cnt_1_correct,
        sum(is_correct_2) AS cnt_2_correct,
        sum(CASE WHEN correctness = 'BOTH_CORRECT' THEN 1 ELSE 0 END) AS cnt_both_correct,
        sum(CASE WHEN correctness = 'ONLY_1_CORRECT' THEN 1 ELSE 0 END) AS cnt_only_1_correct,
        sum(CASE WHEN correctness = 'ONLY_2_CORRECT' THEN 1 ELSE 0 END) AS cnt_only_2_correct,
        sum(CASE WHEN correctness = 'NONE_CORRECT' THEN 1 ELSE 0 END) AS cnt_none_correct,
        '|',
        -- non-provable results
        sum(is_maybe_correct_1) AS cnt_1_maybe_correct,
        sum(is_maybe_incorrect_1) AS cnt_1_maybe_incorrect,
        sum(is_maybe_correct_2) AS cnt_2_maybe_correct,
        sum(is_maybe_incorrect_2) AS cnt_2_maybe_incorrect,
        '|',
        --
        group_concat(CASE WHEN correctness = 'BOTH_CORRECT' THEN benchmark END, ', ') AS both_correct,
        group_concat(CASE WHEN correctness = 'ONLY_1_CORRECT' THEN benchmark END, ', ') AS only_1_correct,
        group_concat(CASE WHEN correctness = 'ONLY_2_CORRECT' THEN benchmark END, ', ') AS only_2_correct,
        group_concat(CASE WHEN correctness = 'NONE_CORRECT' THEN benchmark END, ', ') AS none_correct
    FROM _run_group_result_runtime_comparison
    GROUP BY tool, run_timeout_1, run_timeout_2, expected
);

DROP TABLE IF EXISTS _run_group_runtime_comparison;
CREATE TABLE _run_group_runtime_comparison AS
SELECT *
FROM (
    SELECT
        s1.benchmark AS benchmark,
        s1.run_timeout AS run_timeout,
        s1.tool AS tool_1,
        s2.tool AS tool_2,
        s1.expected AS expected,
        s1.result AS result_1,
        s2.result AS result_2,
        --
        s1.cnt_runs AS cnt_runs_1,
        s2.cnt_runs AS cnt_runs_2,
        s1.cnt_runs_no_timeout AS cnt_runs_no_timeout_1,
        s2.cnt_runs_no_timeout AS cnt_runs_no_timeout_2,
        s1.cnt_runs_timeout AS cnt_runs_timeout_1,
        s2.cnt_runs_timeout AS cnt_runs_timeout_2,
        --
        s1.sum_runtime AS sum_runtime_1,
        s2.sum_runtime AS sum_runtime_2,
        s1.sum_runtime_no_timeout AS sum_runtime_no_timeout_1,
        s2.sum_runtime_no_timeout AS sum_runtime_no_timeout_2,
        s1.sum_runtime_timeout AS sum_runtime_timeout_1,
        s2.sum_runtime_timeout AS sum_runtime_timeout_2,
        --
        s1.avg_runtime AS avg_runtime_1,
        s2.avg_runtime AS avg_runtime_2,
        s1.avg_result_runtime AS avg_result_runtime_1,
        s2.avg_result_runtime AS avg_result_runtime_2,
        --
        CASE
            WHEN s1.has_timed_out AND s2.has_timed_out THEN 'TIE'
            WHEN s1.avg_runtime < s2.avg_runtime THEN '1_FASTER'
            WHEN s1.avg_runtime > s2.avg_runtime THEN '2_FASTER'
            ELSE 'TIE'
        END AS runtime_comparison,
        CASE
            WHEN s1.has_timed_out AND s2.has_timed_out THEN 'TIE'
            WHEN s1.avg_result_runtime < s2.avg_result_runtime THEN '1_FASTER'
            WHEN s1.avg_result_runtime > s2.avg_result_runtime THEN '2_FASTER'
            ELSE 'TIE'
        END AS result_runtime_comparison
    FROM _run_group AS s1
    INNER JOIN _run_group AS s2 USING (benchmark, run_timeout)
    WHERE s1.tool != s2.tool
    ORDER BY s1.tool, s2.tool, benchmark, run_timeout
);

DROP TABLE IF EXISTS _run_group_runtime_comparison_summary;
CREATE TABLE _run_group_runtime_comparison_summary AS
SELECT *
FROM (
    SELECT
        tool_1,
        tool_2,
        run_timeout,
        expected,
        '|',
        sum(sum_runtime_1) / sum(cnt_runs_1) AS avg_runtime_1,
        sum(sum_runtime_2) / sum(cnt_runs_2) AS avg_runtime_2,
        sum(sum_runtime_no_timeout_1) / sum(cnt_runs_no_timeout_1) AS avg_runtime_no_timeout_1,
        sum(sum_runtime_no_timeout_2) / sum(cnt_runs_no_timeout_2) AS avg_runtime_no_timeout_2,
        sum(sum_runtime_timeout_1) / sum(cnt_runs_timeout_1) AS avg_runtime_timeout_1,
        sum(sum_runtime_timeout_2) / sum(cnt_runs_timeout_2) AS avg_runtime_timeout_2,
        '|',
        sum(CASE WHEN runtime_comparison = '1_FASTER' THEN 1 ELSE 0 END) AS cnt_1_faster,
        sum(CASE WHEN runtime_comparison = '2_FASTER' THEN 1 ELSE 0 END) AS cnt_2_faster,
        sum(CASE WHEN runtime_comparison = 'TIE' THEN 1 ELSE 0 END) AS cnt_tie,
        '|',
        sum(CASE WHEN result_runtime_comparison = '1_FASTER' THEN 1 ELSE 0 END) AS cnt_1_result_faster,
        sum(CASE WHEN result_runtime_comparison = '2_FASTER' THEN 1 ELSE 0 END) AS cnt_2_result_faster,
        sum(CASE WHEN result_runtime_comparison = 'TIE' THEN 1 ELSE 0 END) AS cnt_result_tie,
        '|',
        group_concat(CASE WHEN runtime_comparison = '1_FASTER' THEN benchmark END, ', ') AS _1_faster,
        group_concat(CASE WHEN runtime_comparison = '2_FASTER' THEN benchmark END, ', ') AS _2_faster,
        group_concat(CASE WHEN runtime_comparison = 'TIE' THEN benchmark END, ', ') AS _tie
    FROM _run_group_runtime_comparison
    GROUP BY tool_1, tool_2, run_timeout, expected
)
WHERE tool_1 = 'PASDA-diff';

DROP TABLE IF EXISTS _runtime_total_summary;
CREATE TABLE _runtime_total_summary AS
SELECT
    r.rto AS run_timeout,
    round(printf('%.2f', sum(r.runtime)), 1) AS runtime_s,
    round(printf('%.2f', sum(r.runtime) / 3600), 1) AS runtime_h,
    round(printf('%.2f', sum(r.runtime) / (3600 * 24)), 1) AS runtime_d,
    count(DISTINCT r.tool) AS tool_count
FROM (SELECT *, run_timeout AS rto FROM _run_ AS r UNION ALL SELECT *, '*' AS rto FROM _run_ AS r) AS r
GROUP BY r.rto;

DROP TABLE IF EXISTS _runtime_total_list;
CREATE TABLE _runtime_total_list AS
SELECT
    r.run_timeout,
    r.tool,
    sum(r.runtime) AS runtime_s,
    sum(r.runtime) / 3600 AS runtime_h,
    sum(r.runtime) / (3600 * 24) AS runtime_d,
    count(DISTINCT r.benchmark) AS benchmark_count,
    count(*) AS run_count,
    (count(*) * 1.0 / (count(DISTINCT r.tool) * count(DISTINCT r.benchmark))) AS full_run_count,
    count(DISTINCT r.tool) AS tool_count
FROM _run_ AS r
GROUP BY r.run_timeout, r.tool;

DROP TABLE IF EXISTS _iteration_partition_results;
CREATE TABLE _iteration_partition_results AS
SELECT
    i.id AS iteration_id,
    sum(
        CASE
            WHEN p.result IN ('EQ', 'NEQ', 'MAYBE_EQ', 'MAYBE_NEQ', 'UNKNOWN', 'DEPTH_LIMITED', 'TIMEOUT', 'ERROR')
            THEN 1
            ELSE 0
        END
    ) AS cnt_partitions,
    sum(CASE WHEN p.result = 'EQ' THEN 1 ELSE 0 END) AS cnt_eq,
    sum(CASE WHEN p.result = 'NEQ' THEN 1 ELSE 0 END) AS cnt_neq,
    sum(CASE WHEN p.result = 'MAYBE_EQ' THEN 1 ELSE 0 END) AS cnt_maybe_eq,
    sum(CASE WHEN p.result = 'MAYBE_NEQ' THEN 1 ELSE 0 END) AS cnt_maybe_neq,
    sum(CASE WHEN p.result = 'UNKNOWN' THEN 1 ELSE 0 END) AS cnt_unknown,
    sum(CASE WHEN p.result = 'DEPTH_LIMITED' THEN 1 ELSE 0 END) AS cnt_depth_limited,
    sum(CASE WHEN p.result = 'TIMEOUT' THEN 1 ELSE 0 END) AS cnt_timeout,
    sum(CASE WHEN p.result = 'ERROR' THEN 1 ELSE 0 END) AS cnt_error,
    group_concat(
        CASE
            WHEN p.result NOT IN ('EQ', 'NEQ', 'MAYBE_EQ', 'MAYBE_NEQ', 'UNKNOWN', 'DEPTH_LIMITED', 'TIMEOUT', 'ERROR')
            THEN p.result
        END,
        ', '
    ) AS other_results,
    group_concat(
        CASE
            WHEN p.result NOT IN ('EQ', 'NEQ', 'MAYBE_EQ', 'MAYBE_NEQ', 'UNKNOWN', 'DEPTH_LIMITED', 'TIMEOUT', 'ERROR')
            THEN p.id
        END,
        ', '
    ) AS other_results_partition_ids
FROM iteration_ AS i
LEFT JOIN partition_ AS p ON i.id = p.iteration_id
GROUP BY i.id;

CREATE INDEX IF NOT EXISTS _iteration_partition_results___iteration_id ON _iteration_partition_results (iteration_id);

DROP TABLE IF EXISTS _iteration_;
CREATE TABLE _iteration_ AS
SELECT *
FROM (
    SELECT
        r.run_id AS run_id,
        i.id AS iteration_id,
        i.iteration,
        r.benchmark,
        r.tool,
        r.run_timeout,
        r.depth_limit,
        r.expected,
        r.result AS run_result,
        i.result AS iteration_result
    FROM iteration_ AS i
    INNER JOIN _run_ AS r ON i.run_id = r.run_id
    WHERE i.iteration = r.result_iteration
) AS i
INNER JOIN _iteration_partition_results AS ipr USING (iteration_id);

CREATE INDEX IF NOT EXISTS _iteration___run_id ON _iteration_ (run_id);
CREATE UNIQUE INDEX IF NOT EXISTS _iteration___iteration_id ON _iteration_ (iteration_id);
CREATE INDEX IF NOT EXISTS _iteration___benchmark ON _iteration_ (benchmark);
CREATE INDEX IF NOT EXISTS _iteration___tool ON _iteration_ (tool);
CREATE INDEX IF NOT EXISTS _iteration___expected ON _iteration_ (expected);
CREATE INDEX IF NOT EXISTS _iteration___run_result ON _iteration_ (run_result);
CREATE INDEX IF NOT EXISTS _iteration___iteration_result ON _iteration_ (iteration_result);

DROP TABLE IF EXISTS _partition_;
CREATE TABLE _partition_ AS
SELECT
    i.run_id,
    i.iteration_id AS iteration_id,
    p.id AS partition_id,
    i.iteration,
    p.partition,
    i.benchmark,
    i.tool,
    i.run_timeout,
    i.depth_limit,
    i.expected,
    i.run_result,
    i.iteration_result,
    p.result AS partition_result,
    p.pc_status,
    p.neq_status,
    p.eq_status
FROM _iteration_ AS i
LEFT JOIN partition_ AS p ON i.iteration_id = p.iteration_id
WHERE i.tool LIKE '%-diff';

CREATE INDEX IF NOT EXISTS _partition___run_id ON _partition_ (run_id);
CREATE INDEX IF NOT EXISTS _partition___iteration_id ON _partition_ (iteration_id);
CREATE UNIQUE INDEX IF NOT EXISTS _partition___partition_id ON _partition_ (partition_id);
CREATE INDEX IF NOT EXISTS _partition___benchmark ON _partition_ (benchmark);
CREATE INDEX IF NOT EXISTS _partition___tool ON _partition_ (tool);
CREATE INDEX IF NOT EXISTS _partition___expected ON _partition_ (expected);
CREATE INDEX IF NOT EXISTS _partition___run_result ON _partition_ (run_result);
CREATE INDEX IF NOT EXISTS _partition___iteration_result ON _partition_ (iteration_result);
CREATE INDEX IF NOT EXISTS _partition___partition_result ON _partition_ (partition_result);

DROP TABLE IF EXISTS _partition_result_summary_;
CREATE TABLE _partition_result_summary_ AS
SELECT
    tool,
    run_timeout,
    depth_limit,
    expected,
    run_result,
    cnt_runs,
    cnt_partitions,
    CASE
        WHEN cnt_eq = 0 THEN '-'
        ELSE printf('%.1f', 100.0 * cnt_eq / cnt_partitions)
    END AS percent_eq,
    CASE
        WHEN cnt_neq = 0 THEN '-'
        ELSE printf('%.1f', 100.0 * cnt_neq / cnt_partitions)
    END AS percent_neq,
    CASE
        WHEN cnt_maybe_eq = 0 THEN '-'
        ELSE printf('%.1f', 100.0 * cnt_maybe_eq / cnt_partitions)
    END AS percent_maybe_eq,
    CASE
        WHEN cnt_maybe_neq = 0 THEN '-'
        ELSE printf('%.1f', 100.0 * cnt_maybe_neq / cnt_partitions)
    END AS percent_maybe_neq,
    CASE
        WHEN cnt_unknown = 0 THEN '-'
        ELSE printf('%.1f', 100.0 * cnt_unknown / cnt_partitions)
    END AS percent_unknown,
    CASE
        WHEN cnt_depth_limited = 0 THEN '-'
        ELSE printf('%.1f', 100.0 * cnt_depth_limited / cnt_partitions)
    END AS percent_depth_limited,
    CASE
        WHEN cnt_timeout = 0 THEN '-'
        ELSE printf('%.1f', 100.0 * cnt_timeout / cnt_partitions)
    END AS percent_timeout,
    CASE
        WHEN cnt_error = 0 THEN '-'
        ELSE printf('%.1f', 100.0 * cnt_error / cnt_partitions)
    END AS percent_error,
    cnt_eq,
    cnt_neq,
    cnt_maybe_eq,
    cnt_maybe_neq,
    cnt_unknown,
    cnt_depth_limited,
    cnt_timeout,
    cnt_error
FROM (
    SELECT
        tool,
        run_timeout,
        depth_limit,
        CASE WHEN grp_1 = 1 THEN expected WHEN grp_1 = 2 THEN expected WHEN grp_1 = 3 THEN '*' END AS expected,
        CASE WHEN grp_1 = 1 THEN run_result WHEN grp_1 IN (2, 3) THEN '*' END AS run_result,
        count(*) AS cnt_runs,
        sum(cnt_partitions) AS cnt_partitions,
        sum(cnt_eq) AS cnt_eq,
        sum(cnt_neq) AS cnt_neq,
        sum(cnt_maybe_eq) AS cnt_maybe_eq,
        sum(cnt_maybe_neq) AS cnt_maybe_neq,
        sum(cnt_unknown) AS cnt_unknown,
        sum(cnt_depth_limited) AS cnt_depth_limited,
        sum(cnt_timeout) AS cnt_timeout,
        sum(cnt_error) AS cnt_error
    FROM (
        SELECT
            1 AS grp_1,
            min(run_id) OVER (PARTITION BY tool, run_timeout, depth_limit, expected, run_result) AS grp_2,
            *
        FROM _iteration_
        UNION ALL
        SELECT
            2 AS grp_1,
            min(run_id) OVER (PARTITION BY tool, run_timeout, depth_limit, expected) AS grp_2,
            *
        FROM _iteration_
        UNION ALL
        SELECT
            3 AS grp_2,
            min(run_id) OVER (PARTITION BY tool, run_timeout, depth_limit) AS grp_2,
            *
        FROM _iteration_
    )
    WHERE tool LIKE '%-diff'
    GROUP BY grp_1, grp_2
    ORDER BY
        tool, run_timeout, depth_limit, grp_1, expected,
        CASE
            WHEN run_result = 'EQ' THEN 1
            WHEN run_result = 'NEQ' THEN 2
            WHEN run_result = 'MAYBE_EQ' THEN 3
            WHEN run_result = 'MAYBE_NEQ' THEN 4
            WHEN run_result = 'UNKNOWN' THEN 5
            WHEN run_result = 'DEPTH_LIMITED' THEN 6
            WHEN run_result = 'TIMEOUT' THEN 7
            WHEN run_result = 'ERROR' THEN 8
        END
);
