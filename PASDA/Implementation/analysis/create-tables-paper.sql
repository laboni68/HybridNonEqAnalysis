DROP TABLE IF EXISTS __paper__tools;
CREATE TABLE __paper__tools
(
    position INTEGER NOT NULL,

    tool_original TEXT PRIMARY KEY NOT NULL,
    tool_paper TEXT UNIQUE NOT NULL,
    tool_latex TEXT UNIQUE NOT NULL
);

INSERT INTO __paper__tools VALUES (1, 'PASDA-diff', 'PASDA', '\ToolPASDA{}');
INSERT INTO __paper__tools VALUES (2, 'ARDiff-base', 'ARDiff', '\ToolARDiff{}');
INSERT INTO __paper__tools VALUES (3, 'DSE-base', 'DSE', '\ToolDSE{}');
INSERT INTO __paper__tools VALUES (4, 'SE-diff', 'PRV', '\ToolSE{}');

DROP TABLE IF EXISTS __paper__classifications;
CREATE TABLE __paper__classifications
(
    position INTEGER NOT NULL,

    class_original TEXT PRIMARY KEY NOT NULL,
    class_paper TEXT UNIQUE NOT NULL,
    class_latex TEXT UNIQUE NOT NULL
);

INSERT INTO __paper__classifications VALUES (1, 'EQ', 'EQ', '\scEq{}');
INSERT INTO __paper__classifications VALUES (2, 'NEQ', 'NEQ', '\scNeq{}');
INSERT INTO __paper__classifications VALUES (3, 'MAYBE_EQ', 'MAYBE_EQ', '\scMaybeEq{}');
INSERT INTO __paper__classifications VALUES (4, 'MAYBE_NEQ', 'MAYBE_NEQ', '\scMaybeNeq{}');
INSERT INTO __paper__classifications VALUES (5, 'UNKNOWN', 'UNKNOWN', '\scUnknown{}');
INSERT INTO __paper__classifications VALUES (6, 'DEPTH_LIMITED', 'DEPTH_LIMITED', '\scDepthLimited{}');
INSERT INTO __paper__classifications VALUES (7, 'TIMEOUT', 'TIMEOUT', '\scTimeout{}');
INSERT INTO __paper__classifications VALUES (8, 'ERROR', 'ERROR', '\scError{}');
INSERT INTO __paper__classifications VALUES (9, '*', '*', '*');

DROP TABLE IF EXISTS __paper__table__program_level_classifications_part_1;
CREATE TABLE __paper__table__program_level_classifications_part_1 AS
SELECT
    t.tool_paper AS tool,
    c.class_paper AS expected,
    t.tool_latex AS tool_latex,
    c.class_latex AS expected_latex,
    coalesce(max(CASE WHEN run_timeout = 10 THEN EQ END), 'n/a') AS EQ_10,
    '',
    coalesce(max(CASE WHEN run_timeout = 30 THEN EQ END), 'n/a') AS EQ_30,
    '',
    coalesce(max(CASE WHEN run_timeout = 90 THEN EQ END), 'n/a') AS EQ_90,
    '',
    coalesce(max(CASE WHEN run_timeout = 300 THEN EQ END), 'n/a') AS EQ_300,
    '',
    coalesce(max(CASE WHEN run_timeout = 900 THEN EQ END), 'n/a') AS EQ_900,
    '',
    coalesce(max(CASE WHEN run_timeout = 3600 THEN EQ END), 'n/a') AS EQ_3600,
    '',
    coalesce(max(CASE WHEN run_timeout = 10 THEN NEQ END), 'n/a') AS NEQ_10,
    '',
    coalesce(max(CASE WHEN run_timeout = 30 THEN NEQ END), 'n/a') AS NEQ_30,
    '',
    coalesce(max(CASE WHEN run_timeout = 90 THEN NEQ END), 'n/a') AS NEQ_90,
    '',
    coalesce(max(CASE WHEN run_timeout = 300 THEN NEQ END), 'n/a') AS NEQ_300,
    '',
    coalesce(max(CASE WHEN run_timeout = 900 THEN NEQ END), 'n/a') AS NEQ_900,
    '',
    coalesce(max(CASE WHEN run_timeout = 3600 THEN NEQ END), 'n/a') AS NEQ_3600,
    '',
    coalesce(max(CASE WHEN run_timeout = 10 THEN UNKNWN END), 'n/a') AS UNKNOWN_10,
    '',
    coalesce(max(CASE WHEN run_timeout = 30 THEN UNKNWN END), 'n/a') AS UNKNOWN_30,
    '',
    coalesce(max(CASE WHEN run_timeout = 90 THEN UNKNWN END), 'n/a') AS UNKNOWN_90,
    '',
    coalesce(max(CASE WHEN run_timeout = 300 THEN UNKNWN END), 'n/a') AS UNKNOWN_300,
    '',
    coalesce(max(CASE WHEN run_timeout = 900 THEN UNKNWN END), 'n/a') AS UNKNOWN_900,
    '',
    coalesce(max(CASE WHEN run_timeout = 3600 THEN UNKNWN END), 'n/a') AS UNKNOWN_3600
FROM (
    SELECT
        tool,
        run_timeout,
        depth_limit,
        expected,
        sum(CASE WHEN result = 'EQ' THEN 1 ELSE 0 END) AS 'EQ',
        sum(CASE WHEN result = 'NEQ' THEN 1 ELSE 0 END) AS 'NEQ',
        sum(CASE WHEN result IN ('MAYBE_EQ', 'MAYBE_NEQ', 'UNKNOWN') THEN 1 ELSE 0 END) AS 'UNKNWN'
    FROM _run_group_result AS r
    GROUP BY tool, run_timeout, expected
    ORDER BY tool, run_timeout, expected
) AS r
INNER JOIN __paper__tools AS t ON r.tool = t.tool_original
INNER JOIN __paper__classifications AS c ON r.expected = c.class_original
GROUP BY t.position, c.position
ORDER BY t.position, c.position;

DROP TABLE IF EXISTS __paper__table__program_level_classifications_part_2;
CREATE TABLE __paper__table__program_level_classifications_part_2 AS
SELECT
    t.tool_paper AS tool,
    c.class_paper AS expected,
    t.tool_latex AS tool_latex,
    c.class_latex AS expected_latex,
    coalesce(max(CASE WHEN run_timeout = 10 THEN DEPTH_LIMITED END), 'n/a') AS DEPTH_LIMITED_10,
    '',
    coalesce(max(CASE WHEN run_timeout = 30 THEN DEPTH_LIMITED END), 'n/a') AS DEPTH_LIMITED_30,
    '',
    coalesce(max(CASE WHEN run_timeout = 90 THEN DEPTH_LIMITED END), 'n/a') AS DEPTH_LIMITED_90,
    '',
    coalesce(max(CASE WHEN run_timeout = 300 THEN DEPTH_LIMITED END), 'n/a') AS DEPTH_LIMITED_300,
    '',
    coalesce(max(CASE WHEN run_timeout = 900 THEN DEPTH_LIMITED END), 'n/a') AS DEPTH_LIMITED_900,
    '',
    coalesce(max(CASE WHEN run_timeout = 3600 THEN DEPTH_LIMITED END), 'n/a') AS DEPTH_LIMITED_3600,
    '',
    coalesce(max(CASE WHEN run_timeout = 10 THEN TIMEOUT END), 'n/a') AS TIMEOUT_10,
    '',
    coalesce(max(CASE WHEN run_timeout = 30 THEN TIMEOUT END), 'n/a') AS TIMEOUT_30,
    '',
    coalesce(max(CASE WHEN run_timeout = 90 THEN TIMEOUT END), 'n/a') AS TIMEOUT_90,
    '',
    coalesce(max(CASE WHEN run_timeout = 300 THEN TIMEOUT END), 'n/a') AS TIMEOUT_300,
    '',
    coalesce(max(CASE WHEN run_timeout = 900 THEN TIMEOUT END), 'n/a') AS TIMEOUT_900,
    '',
    coalesce(max(CASE WHEN run_timeout = 3600 THEN TIMEOUT END), 'n/a') AS TIMEOUT_3600,
    '',
    coalesce(max(CASE WHEN run_timeout = 10 THEN ERROR END), 'n/a') AS ERROR_10,
    '',
    coalesce(max(CASE WHEN run_timeout = 30 THEN ERROR END), 'n/a') AS ERROR_30,
    '',
    coalesce(max(CASE WHEN run_timeout = 90 THEN ERROR END), 'n/a') AS ERROR_90,
    '',
    coalesce(max(CASE WHEN run_timeout = 300 THEN ERROR END), 'n/a') AS ERROR_300,
    '',
    coalesce(max(CASE WHEN run_timeout = 900 THEN ERROR END), 'n/a') AS ERROR_900,
    '',
    coalesce(max(CASE WHEN run_timeout = 3600 THEN ERROR END), 'n/a') AS ERROR_3600
FROM (
    SELECT
        tool,
        run_timeout,
        depth_limit,
        expected,
        sum(CASE WHEN result = 'DEPTH_LIMITED' THEN 1 ELSE 0 END) AS 'DEPTH_LIMITED',
        sum(CASE WHEN result = 'TIMEOUT' THEN 1 ELSE 0 END) AS 'TIMEOUT',
        sum(CASE WHEN result = 'ERROR' THEN 1 ELSE 0 END) AS 'ERROR'
    FROM _run_group_result AS r
    GROUP BY tool, run_timeout, expected
    ORDER BY tool, run_timeout, expected
) AS r
INNER JOIN __paper__tools AS t ON r.tool = t.tool_original
INNER JOIN __paper__classifications c ON r.expected = c.class_original
GROUP BY t.position, c.position
ORDER BY t.position, c.position;

DROP TABLE IF EXISTS __paper__table__program_level_best_effort_classifications;
CREATE TABLE __paper__table__program_level_best_effort_classifications AS
SELECT
    t.tool_paper AS tool,
    c.class_paper AS expected,
    t.tool_latex AS tool_latex,
    c.class_latex AS expected_latex,
    coalesce(max(CASE WHEN run_timeout = 10 THEN MAYBE_EQ END), 'n/a') AS MAYBE_EQ_10,
    '',
    coalesce(max(CASE WHEN run_timeout = 30 THEN MAYBE_EQ END), 'n/a') AS MAYBE_EQ_30,
    '',
    coalesce(max(CASE WHEN run_timeout = 90 THEN MAYBE_EQ END), 'n/a') AS MAYBE_EQ_90,
    '',
    coalesce(max(CASE WHEN run_timeout = 300 THEN MAYBE_EQ END), 'n/a') AS MAYBE_EQ_300,
    '',
    coalesce(max(CASE WHEN run_timeout = 900 THEN MAYBE_EQ END), 'n/a') AS MAYBE_EQ_900,
    '',
    coalesce(max(CASE WHEN run_timeout = 3600 THEN MAYBE_EQ END), 'n/a') AS MAYBE_EQ_3600,
    '',
    coalesce(max(CASE WHEN run_timeout = 10 THEN MAYBE_NEQ END), 'n/a') AS MAYBE_NEQ_10,
    '',
    coalesce(max(CASE WHEN run_timeout = 30 THEN MAYBE_NEQ END), 'n/a') AS MAYBE_NEQ_30,
    '',
    coalesce(max(CASE WHEN run_timeout = 90 THEN MAYBE_NEQ END), 'n/a') AS MAYBE_NEQ_90,
    '',
    coalesce(max(CASE WHEN run_timeout = 300 THEN MAYBE_NEQ END), 'n/a') AS MAYBE_NEQ_300,
    '',
    coalesce(max(CASE WHEN run_timeout = 900 THEN MAYBE_NEQ END), 'n/a') AS MAYBE_NEQ_900,
    '',
    coalesce(max(CASE WHEN run_timeout = 3600 THEN MAYBE_NEQ END), 'n/a') AS MAYBE_NEQ_3600,
    '',
    coalesce(max(CASE WHEN run_timeout = 10 THEN UNKNWN END), 'n/a') AS UNKNOWN_10,
    '',
    coalesce(max(CASE WHEN run_timeout = 30 THEN UNKNWN END), 'n/a') AS UNKNOWN_30,
    '',
    coalesce(max(CASE WHEN run_timeout = 90 THEN UNKNWN END), 'n/a') AS UNKNOWN_90,
    '',
    coalesce(max(CASE WHEN run_timeout = 300 THEN UNKNWN END), 'n/a') AS UNKNOWN_300,
    '',
    coalesce(max(CASE WHEN run_timeout = 900 THEN UNKNWN END), 'n/a') AS UNKNOWN_900,
    '',
    coalesce(max(CASE WHEN run_timeout = 3600 THEN UNKNWN END), 'n/a') AS UNKNOWN_3600
FROM (
    SELECT
        tool,
        run_timeout,
        depth_limit,
        expected,
        sum(CASE WHEN result = 'MAYBE_EQ' THEN 1 ELSE 0 END) AS 'MAYBE_EQ',
        sum(CASE WHEN result = 'MAYBE_NEQ' THEN 1 ELSE 0 END) AS 'MAYBE_NEQ',
        sum(CASE WHEN result = 'UNKNOWN' THEN 1 ELSE 0 END) AS 'UNKNWN'
    FROM _run_group_result AS r
    GROUP BY tool, run_timeout, expected
    ORDER BY tool, run_timeout, expected
) AS r
INNER JOIN __paper__tools AS t ON r.tool = t.tool_original
INNER JOIN __paper__classifications c ON r.expected = c.class_original
GROUP BY t.position, c.position
ORDER BY t.position, c.position;

DROP TABLE IF EXISTS __paper__table__runtime_overall;
CREATE TABLE __paper__table__runtime_overall AS
SELECT
    t.tool_paper AS tool,
    c.class_original AS expected,
    t.tool_latex AS tool_latex,
    c.class_latex AS expected_latex,
    coalesce(max(CASE WHEN run_timeout = 10 THEN runtime END), 'n/a') AS runtime_10,
    '',
    coalesce(max(CASE WHEN run_timeout = 30 THEN runtime END), 'n/a') AS runtime_30,
    '',
    coalesce(max(CASE WHEN run_timeout = 90 THEN runtime END), 'n/a') AS runtime_90,
    '',
    coalesce(max(CASE WHEN run_timeout = 300 THEN runtime END), 'n/a') AS runtime_300,
    '',
    coalesce(max(CASE WHEN run_timeout = 900 THEN runtime END), 'n/a') AS runtime_900,
    '',
    coalesce(max(CASE WHEN run_timeout = 3600 THEN runtime END), 'n/a') AS runtime_3600,
    '',
    coalesce(max(CASE WHEN run_timeout = 10 THEN timeouts END), 'n/a') AS timeouts_10,
    '',
    coalesce(max(CASE WHEN run_timeout = 30 THEN timeouts END), 'n/a') AS timeouts_30,
    '',
    coalesce(max(CASE WHEN run_timeout = 90 THEN timeouts END), 'n/a') AS timeouts_90,
    '',
    coalesce(max(CASE WHEN run_timeout = 300 THEN timeouts END), 'n/a') AS timeouts_300,
    '',
    coalesce(max(CASE WHEN run_timeout = 900 THEN timeouts END), 'n/a') AS timeouts_900,
    '',
    coalesce(max(CASE WHEN run_timeout = 3600 THEN timeouts END), 'n/a') AS timeouts_3600,
    '',
    coalesce(max(CASE WHEN run_timeout = 10 THEN iterations END), 'n/a') AS iterations_10,
    '',
    coalesce(max(CASE WHEN run_timeout = 30 THEN iterations END), 'n/a') AS iterations_30,
    '',
    coalesce(max(CASE WHEN run_timeout = 90 THEN iterations END), 'n/a') AS iterations_90,
    '',
    coalesce(max(CASE WHEN run_timeout = 300 THEN iterations END), 'n/a') AS iterations_300,
    '',
    coalesce(max(CASE WHEN run_timeout = 900 THEN iterations END), 'n/a') AS iterations_900,
    '',
    coalesce(max(CASE WHEN run_timeout = 3600 THEN iterations END), 'n/a') AS iterations_3600
FROM (
    SELECT
        r.tool,
        r.run_timeout,
        r.depth_limit,
        r.expected,
        printf('%.1f', r.runtime) AS runtime,
        g.cnt_timeout AS timeouts,
        printf('%.1f', r.iterations) AS iterations
    FROM (
        SELECT
            r.tool,
            r.run_timeout,
            r.depth_limit,
            r.expected,
            avg(rt.runtime) AS runtime_,
            avg(rt2.runtime) AS runtime,
            avg(r.iteration_count) AS iterations
        FROM runtime AS rt
        INNER JOIN (SELECT run_id, sum(runtime) AS runtime FROM runtime WHERE task != '' GROUP BY run_id) AS rt2 ON rt.run_id = rt2.run_id
        INNER JOIN _run_ AS r ON rt.run_id = r.run_id
        WHERE rt.topic = 'run' AND rt.task = ''
        GROUP BY r.tool, r.run_timeout, r.depth_limit, r.expected
    ) AS r INNER JOIN (
        SELECT
            tool, run_timeout, depth_limit, expected,
            sum(CASE WHEN has_timed_out THEN 1 ELSE 0 END) AS cnt_timeout
        FROM _run_group
        GROUP BY tool, run_timeout, depth_limit, expected
    ) AS g ON
            r.tool = g.tool
        AND r.run_timeout = g.run_timeout
        AND r.depth_limit = g.depth_limit
        AND r.expected = g.expected
) AS r
INNER JOIN __paper__tools AS t ON r.tool = t.tool_original
INNER JOIN __paper__classifications AS c ON r.expected = c.class_original
GROUP BY t.position, c.position
ORDER BY t.position, c.position;

DROP TABLE IF EXISTS __paper__table__runtime_per_step;
CREATE TABLE __paper__table__runtime_per_step AS
WITH
    step_runtimes AS (
        SELECT *, 100 * step_runtime / total_runtime AS step_percent FROM (
            SELECT *, sum(step_runtime) OVER (PARTITION BY run_id) as total_runtime
            FROM (
                SELECT
                    r.run_id,
                    r.benchmark,
                    t.tool_original,
                    t.tool_paper,
                    t.position,
                    r.run_timeout,
                    r.depth_limit,
                    r.expected,
                    rt.task,
                    rt.step,
                    sum(rt.runtime) AS step_runtime
                FROM runtime AS rt
                INNER JOIN _run_ AS r ON rt.run_id = r.run_id
                INNER JOIN __paper__tools AS t ON r.tool = t.tool_original
                WHERE rt.task != ''
                GROUP BY rt.run_id, rt.task, r.run_timeout, r.depth_limit
                ORDER BY rt.run_id, max(rt.step)
            )
        )
    ),
    step_runtimes_grouped AS (
        SELECT
            tool_paper AS tool, position, run_timeout, depth_limit, task, step,
            avg(step_runtime) AS avg_step_runtime
        FROM step_runtimes
        GROUP BY position, run_timeout, depth_limit, step
        ORDER BY position, run_timeout, depth_limit, step
    ),
    step_runtimes_grouped_with_totals AS (
        SELECT * FROM step_runtimes_grouped
        --
        UNION ALL
        --
        SELECT
            tool, position, run_timeout, depth_limit,
            '*', 99, sum(avg_step_runtime)
        FROM step_runtimes_grouped
        GROUP BY position, run_timeout, depth_limit
    )
SELECT
    depth_limit, task, --step,
    --
    printf('%.1f', max(CASE WHEN tool = 'PASDA' AND run_timeout = 10 THEN avg_step_runtime END)) AS PASDA_10,
    printf('%.1f', max(CASE WHEN tool = 'PASDA' AND run_timeout = 30 THEN avg_step_runtime END)) AS PASDA_30,
    printf('%.1f', max(CASE WHEN tool = 'PASDA' AND run_timeout = 90 THEN avg_step_runtime END)) AS PASDA_90,
    printf('%.1f', max(CASE WHEN tool = 'PASDA' AND run_timeout = 300 THEN avg_step_runtime END)) AS PASDA_300,
    printf('%.1f', max(CASE WHEN tool = 'PASDA' AND run_timeout = 900 THEN avg_step_runtime END)) AS PASDA_900,
    printf('%.1f', max(CASE WHEN tool = 'PASDA' AND run_timeout = 3600 THEN avg_step_runtime END)) AS PASDA_3600,
    --
    printf('%.1f', max(CASE WHEN tool = 'ARDiff' AND run_timeout = 10 THEN avg_step_runtime END)) AS ARDIFF_10,
    printf('%.1f', max(CASE WHEN tool = 'ARDiff' AND run_timeout = 30 THEN avg_step_runtime END)) AS ARDIFF_30,
    printf('%.1f', max(CASE WHEN tool = 'ARDiff' AND run_timeout = 90 THEN avg_step_runtime END)) AS ARDIFF_90,
    printf('%.1f', max(CASE WHEN tool = 'ARDiff' AND run_timeout = 300 THEN avg_step_runtime END)) AS ARDIFF_300,
    printf('%.1f', max(CASE WHEN tool = 'ARDiff' AND run_timeout = 900 THEN avg_step_runtime END)) AS ARDIFF_900,
    printf('%.1f', max(CASE WHEN tool = 'ARDiff' AND run_timeout = 3600 THEN avg_step_runtime END)) AS ARDIFF_3600,
    --
    printf('%.1f', max(CASE WHEN tool = 'DSE' AND run_timeout = 10 THEN avg_step_runtime END)) AS DSE_10,
    printf('%.1f', max(CASE WHEN tool = 'DSE' AND run_timeout = 30 THEN avg_step_runtime END)) AS DSE_30,
    printf('%.1f', max(CASE WHEN tool = 'DSE' AND run_timeout = 90 THEN avg_step_runtime END)) AS DSE_90,
    printf('%.1f', max(CASE WHEN tool = 'DSE' AND run_timeout = 300 THEN avg_step_runtime END)) AS DSE_300,
    printf('%.1f', max(CASE WHEN tool = 'DSE' AND run_timeout = 900 THEN avg_step_runtime END)) AS DSE_900,
    printf('%.1f', max(CASE WHEN tool = 'DSE' AND run_timeout = 3600 THEN avg_step_runtime END)) AS DSE_3600,
    --
    printf('%.1f', max(CASE WHEN tool = 'PRV' AND run_timeout = 10 THEN avg_step_runtime END)) AS SE_10,
    printf('%.1f', max(CASE WHEN tool = 'PRV' AND run_timeout = 30 THEN avg_step_runtime END)) AS SE_30,
    printf('%.1f', max(CASE WHEN tool = 'PRV' AND run_timeout = 90 THEN avg_step_runtime END)) AS SE_90,
    printf('%.1f', max(CASE WHEN tool = 'PRV' AND run_timeout = 300 THEN avg_step_runtime END)) AS SE_300,
    printf('%.1f', max(CASE WHEN tool = 'PRV' AND run_timeout = 900 THEN avg_step_runtime END)) AS SE_900,
    printf('%.1f', max(CASE WHEN tool = 'PRV' AND run_timeout = 3600 THEN avg_step_runtime END)) AS SE_3600
FROM step_runtimes_grouped_with_totals
GROUP BY depth_limit, step;

DROP TABLE IF EXISTS __paper__table__partition_level_classifications;
CREATE TABLE __paper__table__partition_level_classifications AS
SELECT
    t.tool_paper AS tool,
    run_timeout AS run_timeout,
    depth_limit AS depth_limit,
    c_exp.class_paper AS expected,
    c_res.class_paper AS run_result,
    t.tool_latex AS tool_latex,
    run_timeout AS run_timeout_latex,
    c_exp.class_latex AS expected_latex,
    c_res.class_latex AS run_result_latex,
    cnt_runs, cnt_partitions,
    percent_eq, percent_neq,
    percent_maybe_eq, percent_maybe_neq, percent_unknown,
    percent_depth_limited, percent_timeout, percent_error,
    cnt_eq, cnt_neq,
    cnt_maybe_eq, cnt_maybe_neq, cnt_unknown,
    cnt_depth_limited, cnt_timeout, cnt_error
FROM _partition_result_summary_ AS r
INNER JOIN __paper__tools AS t ON r.tool = t.tool_original
INNER JOIN __paper__classifications AS c_exp ON r.expected = c_exp.class_original
INNER JOIN __paper__classifications AS c_res ON r.run_result = c_res.class_original
WHERE t.tool_paper IN ('PASDA', 'PRV');

DROP TABLE IF EXISTS __paper__stats__runtime_until_solve;
CREATE TABLE __paper__stats__runtime_until_solve AS
SELECT
    t.tool_paper AS tool,
    run_timeout AS run_timeout,
    depth_limit AS depth_limit,
    c.class_paper AS result,
    avg_result_runtime_percent
FROM (
    SELECT * FROM (
        SELECT
            tool,
            '*' AS run_timeout,
            depth_limit,
            result,
            printf('%.2f', avg(runtime)) AS runtime,
            printf('%.2f', avg(result_runtime)) AS result_runtime,
            printf('%.2f', 100 * avg(result_runtime / runtime)) AS avg_result_runtime_percent
        FROM _run_ AS r
        WHERE result = 'NEQ'
        GROUP BY tool
        ORDER BY tool
    )
    UNION ALL
    SELECT * FROM (
        SELECT
            tool,
            run_timeout,
            depth_limit,
            result,
            printf('%.2f', avg(runtime)) AS runtime,
            printf('%.2f', avg(result_runtime)) AS result_runtime,
            printf('%.2f', 100 * avg(result_runtime / runtime)) AS avg_result_runtime_percent
        FROM _run_ AS r
        WHERE result = 'NEQ'
        GROUP BY tool, run_timeout
        ORDER BY tool
    )
) AS r
INNER JOIN __paper__tools AS t ON r.tool = t.tool_original
INNER JOIN __paper__classifications AS c ON r.result = c.class_original
ORDER BY t.position;

DROP TABLE IF EXISTS __paper__stats__refinement;
CREATE TABLE __paper__stats__refinement AS
SELECT
    tool,
    CASE WHEN grp_1 IN (1, 2) THEN run_timeout ELSE '*' END AS run_timeout,
    CASE WHEN grp_1 IN (1, 3) THEN expected ELSE '*' END AS expected,
    printf('%.2f', 100.0 * cnt_timeout / cnt_overall) AS percent_timeout,
    printf('%.2f', 100.0 * cnt_one_iteration / cnt_overall) AS percent_one_iteration,
    printf('%.2f', 100.0 * cnt_more_iterations / cnt_overall) AS percent_more_iterations,
    printf('%.2f', 100.0 * cnt_refinement / cnt_overall) AS percent_refinement,
    printf('%.2f', 100.0 * cnt_no_refinement / cnt_overall) AS percent_no_refinement
FROM (
    SELECT
        grp_1,
        grp_2,
        position,
        tool_paper AS tool,
        run_timeout,
        expected,
        count(*) AS cnt_overall,
        sum(CASE WHEN has_timed_out = 1 THEN 1 ELSE 0 END) AS cnt_timeout,
        sum(CASE WHEN result_iteration = 1 THEN 1 ELSE 0 END) AS cnt_one_iteration,
        sum(CASE WHEN result_iteration > 1 THEN 1 ELSE 0 END) AS cnt_more_iterations,
        sum(
            CASE
                WHEN
                    (tool = 'PASDA-diff' AND result_iteration > 2) OR
                    (tool != 'PASDA-diff' AND result_iteration > 1)
                THEN 1
                ELSE 0
            END
        ) AS cnt_refinement,
        sum(
            CASE
                WHEN
                    (tool = 'PASDA-diff' AND result_iteration <= 2) OR
                    (tool != 'PASDA-diff' AND result_iteration <= 1)
                THEN 1
                ELSE 0
            END
        ) AS cnt_no_refinement
    FROM (
        SELECT 1 AS grp_1, min(run_id) OVER (PARTITION BY tool, run_timeout, expected) AS grp_2, * FROM _run_
        UNION ALL
        SELECT 2 AS grp_1, min(run_id) OVER (PARTITION BY tool, run_timeout) AS grp_2, * FROM _run_
        UNION ALL
        SELECT 3 AS grp_1, min(run_id) OVER (PARTITION BY tool, expected) AS grp_2, * FROM _run_
        UNION ALL
        SELECT 4 AS grp_2, min(run_id) OVER (PARTITION BY tool) AS grp_2, * FROM _run_
    ) AS r
    INNER JOIN __paper__tools AS t ON r.tool = t.tool_original
    GROUP BY grp_1, grp_2
)
ORDER BY grp_1, position, run_timeout, expected;

DROP TABLE IF EXISTS __paper__stats__iterations_until_solve;
CREATE TABLE __paper__stats__iterations_until_solve AS
SELECT
    *,
    printf('%.2f', (100.0 * solved_at_iteration_1 / runs_solved)) AS 's_at_1_%',
    printf('%.2f', (100.0 * solved_above_iteration_1 / runs_solved)) AS 's_above_1_%',
    printf('%.2f', (100.0 * solved_at_iteration_2 / runs_solved)) AS 's_at_2_%',
    printf('%.2f', (100.0 * solved_above_iteration_2 / runs_solved)) AS 's_above_2_%'
FROM (
    SELECT
        tool_paper, run_timeout, depth_limit, expected,
        count(*) AS runs_solved,
        sum(CASE WHEN result_iteration = 1 THEN 1 ELSE 0 END) AS solved_at_iteration_1,    -- fully refined for PASDA, fully abstracted for ARDiff
        sum(CASE WHEN result_iteration > 1 THEN 1 ELSE 0 END) AS solved_above_iteration_1, -- partially refined for ARDiff
        sum(CASE WHEN result_iteration = 2 THEN 1 ELSE 0 END) AS solved_at_iteration_2,    -- fully abstracted for PASDA
        sum(CASE WHEN result_iteration > 2 THEN 1 ELSE 0 END) AS solved_above_iteration_2  -- partially refined for PASDA
    FROM _run_ AS r INNER JOIN __paper__tools AS t ON r.tool = t.tool_original WHERE result = expected
    GROUP BY position, run_timeout, depth_limit, expected
    ORDER BY position, run_timeout, depth_limit, expected
);

DROP TABLE IF EXISTS __paper__stats__group_result_fluctuations;
CREATE TABLE __paper__stats__group_result_fluctuations AS
SELECT
    CASE WHEN grp_1 = 1 THEN tool_paper ELSE '*' END AS tool,
    count(*) AS 'cnt_groups',
    sum(CASE WHEN cnt_distinct_results = 1 THEN 1 ELSE 0 END) AS 'cnt_1_distinct',
    sum(CASE WHEN cnt_distinct_results = 2 THEN 1 ELSE 0 END) AS 'cnt_2_distinct',
    sum(CASE WHEN cnt_distinct_results > 2 THEN 1 ELSE 0 END) AS 'cnt_3+_distinct',
    sum(some_correct) AS cnt_some_correct,
    sum(CASE WHEN sum_correct = 5 THEN 1 ELSE 0 END) AS '5_of_5_correct',
    sum(CASE WHEN sum_correct = 4 THEN 1 ELSE 0 END) AS '4_of_5_correct',
    sum(CASE WHEN sum_correct = 3 THEN 1 ELSE 0 END) AS '3_of_5_correct',
    sum(CASE WHEN sum_correct = 2 THEN 1 ELSE 0 END) AS '2_of_5_correct',
    sum(CASE WHEN sum_correct = 1 THEN 1 ELSE 0 END) AS '1_of_5_correct',
    sum(CASE WHEN sum_correct = 0 THEN 1 ELSE 0 END) AS '0_of_5_correct',
    sum(some_timeout) AS cnt_some_timeout
FROM (
    SELECT 1 AS grp_1, tool AS grp_2, * FROM _run_group
    UNION ALL
    SELECT 2 AS grp_1, '*' AS grp_2, * FROM _run_group
) AS r
INNER JOIN __paper__tools AS t ON r.tool = t.tool_original
WHERE cnt_runs = 5
GROUP BY grp_1, grp_2
ORDER BY CASE WHEN grp_1 = 1 THEN position ELSE -1 END;

DROP TABLE IF EXISTS __paper__stats__group_runtime_fluctuations;
CREATE TABLE __paper__stats__group_runtime_fluctuations AS
WITH
    r AS (
        SELECT
            t.position, r.benchmark, t.tool_paper AS tool, r.run_timeout, r.depth_limit,
            min(runtime) AS min_runtime,
            max(runtime) AS max_runtime,
            max(runtime) - min(runtime) AS range_runtime,
            100 * min(runtime) / max(runtime) As min_as_percent_of_max,
            100 * (max(runtime) - min(runtime)) / max(runtime) AS range_as_percent_of_max
        FROM (
            SELECT benchmark, tool, run_timeout, depth_limit, expected, run_id, sum(rt.runtime) AS runtime
            FROM _run_ AS r
            INNER JOIN runtime AS rt USING (run_id)
            WHERE rt.task != ''
            GROUP BY r.run_id
            ORDER BY sum(rt.runtime) DESC
        ) AS r
        INNER JOIN __paper__tools AS t ON r.tool = t.tool_original
        GROUP BY benchmark, tool, run_timeout, depth_limit
    ),
    r_per_tool_timeout AS (
        SELECT tool , r.run_timeout, r.depth_limit, avg(min_as_percent_of_max), avg(range_as_percent_of_max) FROM r
        GROUP BY position, run_timeout, depth_limit
    ),
    r_per_tool AS (
        SELECT tool , '*', r.depth_limit, avg(min_as_percent_of_max), avg(range_as_percent_of_max) FROM r
        GROUP BY position, depth_limit
    ),
    r_overall AS (
        SELECT '*' , '*', r.depth_limit, avg(min_as_percent_of_max), avg(range_as_percent_of_max) FROM r
        GROUP BY depth_limit
    )
SELECT * FROM (
    SELECT * FROM r_per_tool_timeout
    UNION ALL
    SELECT * FROM r_per_tool
    UNION ALL
    SELECT * FROM r_overall
);

DROP TABLE IF EXISTS __paper__stats__errors;
CREATE TABLE __paper__stats__errors AS
SELECT t.tool_paper AS tool, s.run_timeout, s.depth_limit, r.result,
    CASE
        WHEN r.errors LIKE '%java.lang.OutOfMemoryError%' THEN 'java.lang.OutOfMemoryError'
        WHEN r.errors LIKE '%java.lang.StackOverflowError%' THEN 'java.lang.StackOverflowError'
        WHEN r.errors LIKE '%Compilation error: ''else'' without ''if''%' THEN 'Compilation error'
        ELSE r.errors
    END AS error_type,
    count(*) AS cnt, group_concat(r.benchmark, ', ') AS benchmarks, group_concat(r.id, ', ') AS ids
FROM run AS r
INNER JOIN settings AS s ON r.id = s.run_id
INNER JOIN __paper__tools AS t ON s.tool = t.tool_original
WHERE r.result = 'ERROR' AND s.tool != 'PASDA-base'
GROUP BY t.position, s.run_timeout, s.depth_limit
ORDER BY t.position, s.run_timeout, s.depth_limit;

DROP TABLE IF EXISTS __paper__stats__time_to_correct_result;
CREATE TABLE __paper__stats__time_to_correct_result AS
SELECT
    run_timeout,
    t1.tool_paper AS tool_1,
    t2.tool_paper AS tool_2,
    expected,
    count(*) AS cnt,
    '1 : ' || printf('%.2f', avg(median_runtime_2 / median_runtime_1)) AS avg_runtime_ratio,
    '1 : ' || printf('%.2f', avg(median_result_runtime_2 / median_result_runtime_1)) AS avg_result_runtime_ratio
FROM _run_group_result_tool_comparison AS r
    INNER JOIN __paper__tools AS t1 ON r.tool_1 = t1.tool_original
    INNER JOIN __paper__tools AS t2 ON r.tool_2 = t2.tool_original
--WHERE correctness = 'BOTH_CORRECT'
--AND tool_1 = 'PASDA-diff' AND tool_2 = 'ARDiff-base'
GROUP BY
    run_timeout, expected, t1.position, t2.position
ORDER BY
    t1.position, t2.position, expected, run_timeout;

DROP TABLE IF EXISTS __paper__stats__tool_result_comparison;
CREATE TABLE __paper__stats__tool_result_comparison AS
SELECT
    t1.tool_paper AS tool_1, t2.tool_paper AS tool_2, run_timeout, depth_limit, expected,
    cnt_both_correct, cnt_1_correct, cnt_2_correct, cnt_only_1_correct, cnt_only_2_correct, cnt_none_correct,
    both_correct, only_1_correct, only_2_correct, none_correct
FROM _run_group_result_tool_comparison_summary AS r
INNER JOIN __paper__tools AS t1 ON r.tool_1 = t1.tool_original
INNER JOIN __paper__tools AS t2 ON r.tool_2 = t2.tool_original
ORDER BY t1.position, t2.position, run_timeout, depth_limit, expected;

DROP TABLE IF EXISTS __paper__stats__tool_runtime_comparison;
CREATE TABLE __paper__stats__tool_runtime_comparison AS
SELECT
    t1.tool_paper AS tool_1, t2.tool_paper AS tool_2, run_timeout, depth_limit, expected,
    cnt_median_result_runtime_1_faster AS cnt_1_faster,
    cnt_median_result_runtime_2_faster AS cnt_2_faster,
    cnt_median_result_runtime_tie AS cnt_tie,
    "1_faster", "2_faster", "tie"
FROM _run_group_result_tool_comparison_summary AS r
INNER JOIN __paper__tools AS t1 ON r.tool_1 = t1.tool_original
INNER JOIN __paper__tools AS t2 ON r.tool_2 = t2.tool_original
ORDER BY t1.position, t2.position, run_timeout, depth_limit, expected;
