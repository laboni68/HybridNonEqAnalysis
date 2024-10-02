-- DROP VIEW IF EXISTS run_result_crosstab_true;
-- DROP VIEW IF EXISTS run_result_crosstab_lenient;
-- DROP VIEW IF EXISTS run_result_crosstab_strict;
-- DROP VIEW IF EXISTS run_runtime_overview;
-- DROP VIEW IF EXISTS runtime_per_task;
-- DROP VIEW IF EXISTS run_reducibility_statistics;
-- DROP VIEW IF EXISTS __paper__classification_crosstab;
-- DROP VIEW IF EXISTS __paper__runtime_per_expected;
-- DROP VIEW IF EXISTS __paper__runtime_per_step;
-- DROP VIEW IF EXISTS __paper__partitions_per_classification;

CREATE VIEW IF NOT EXISTS run_result_crosstab_true AS
SELECT run.tool, benchmark.expected,
    count(CASE run.result WHEN 'EQ' THEN 1 END) AS 'EQ',
    count(CASE run.result WHEN 'NEQ' THEN 1 END) AS 'NEQ',
    count(CASE run.result WHEN 'MAYBE_EQ' THEN 1 END) AS 'MAYBE_EQ',
    count(CASE run.result WHEN 'MAYBE_NEQ' THEN 1 END) AS 'MAYBE_NEQ',
    count(CASE run.result WHEN 'UNKNOWN' THEN 1 END) AS 'UNKNOWN',
    count(CASE run.result WHEN 'DEPTH_LIMITED' THEN 1 END) AS 'DEPTH_LIMITED',
    count(CASE WHEN run.result = 'TIMEOUT' OR run.result IS NULL THEN 1 END) AS 'TIMEOUT',
    count(CASE run.result WHEN 'UNREACHABLE' THEN 1 END) AS 'UNREACHABLE',
    count(CASE run.result WHEN 'ERROR' THEN 1 END) AS 'ERROR',
    count(CASE run.result WHEN 'BASE_TOOL_MISSING' THEN 1 END) AS 'BASE_TOOL_MISSING',
    count(CASE run.result WHEN 'MISSING' THEN 1 END) AS 'MISSING'
FROM run
INNER JOIN benchmark on benchmark.benchmark = run.benchmark
GROUP BY run.tool, benchmark.expected ORDER BY benchmark.expected, run.tool;

CREATE VIEW IF NOT EXISTS run_result_crosstab_lenient AS
SELECT run.tool, benchmark.expected,
    count(CASE run.result WHEN 'EQ' THEN 1 WHEN 'MAYBE_EQ' THEN 1 END) AS 'EQ',
    count(CASE run.result WHEN 'NEQ' THEN 1 WHEN 'MAYBE_NEQ' THEN 1 END) AS 'NEQ',
    count(CASE run.result WHEN 'UNKNOWN' THEN 1 END) AS 'UNKNOWN',
    count(CASE run.result WHEN 'DEPTH_LIMITED' THEN 1 END) AS 'DEPTH_LIMITED',
    count(CASE run.result WHEN 'TIMEOUT' THEN 1 END) AS 'TIMEOUT',
    count(CASE run.result WHEN 'UNREACHABLE' THEN 1 END) AS 'UNREACHABLE',
    count(CASE run.result WHEN 'ERROR' THEN 1 END) AS 'ERROR',
    count(CASE run.result WHEN 'BASE_TOOL_MISSING' THEN 1 END) AS 'BASE_TOOL_MISSING',
    count(CASE run.result WHEN 'MISSING' THEN 1 END) AS 'MISSING'
FROM run
INNER JOIN benchmark on benchmark.benchmark = run.benchmark
GROUP BY run.tool, benchmark.expected ORDER BY benchmark.expected, run.tool;

CREATE VIEW IF NOT EXISTS run_result_crosstab_strict AS
SELECT run.tool, benchmark.expected,
    count(CASE run.result WHEN 'EQ' THEN 1 END) AS 'EQ',
    count(CASE run.result WHEN 'NEQ' THEN 1 END) AS 'NEQ',
    count(CASE run.result WHEN 'UNKNOWN' THEN 1 WHEN 'MAYBE_EQ' THEN 1 WHEN 'MAYBE_NEQ' THEN 1 END) AS 'UNKNOWN',
    count(CASE run.result WHEN 'DEPTH_LIMITED' THEN 1 END) AS 'DEPTH_LIMITED',
    count(CASE run.result WHEN 'TIMEOUT' THEN 1 END) AS 'TIMEOUT',
    count(CASE run.result WHEN 'UNREACHABLE' THEN 1 END) AS 'UNREACHABLE',
    count(CASE run.result WHEN 'ERROR' THEN 1 END) AS 'ERROR',
    count(CASE run.result WHEN 'BASE_TOOL_MISSING' THEN 1 END) AS 'BASE_TOOL_MISSING',
    count(CASE run.result WHEN 'MISSING' THEN 1 END) AS 'MISSING'
FROM run
INNER JOIN benchmark on benchmark.benchmark = run.benchmark
GROUP BY run.tool, benchmark.expected ORDER BY benchmark.expected, run.tool;

CREATE VIEW IF NOT EXISTS run_runtime_overview AS
SELECT run.benchmark, benchmark.expected,
    max(CASE WHEN run.tool = 'ARDiff-base' THEN run.runtime END) AS 'ARDiff-base',
    max(CASE WHEN run.tool = 'ARDiff-diff' THEN run.runtime END) AS 'ARDiff-diff',
    max(CASE WHEN run.tool = 'DSE-base' THEN run.runtime END) AS 'DSE-base',
    max(CASE WHEN run.tool = 'DSE-diff' THEN run.runtime END) AS 'DSE-diff',
    max(CASE WHEN run.tool = 'SE-base' THEN run.runtime END) AS 'SE-base',
    max(CASE WHEN run.tool = 'SE-diff' THEN run.runtime END) AS 'SE-diff'
FROM run
INNER JOIN benchmark ON run.benchmark = benchmark.benchmark
GROUP BY run.benchmark;

CREATE VIEW IF NOT EXISTS runtime_per_task AS
WITH
    all_runtimes AS (
        WITH
            run_runtimes AS (
                SELECT
                    runtime.tool AS 'tool',
                    topic AS 'topic',
                    task AS 'task',
                    printf('%.2f', avg(runtime.runtime)) AS 'avg_runtime',
                    step AS 'step'
                FROM runtime
                INNER JOIN run on runtime.benchmark = run.benchmark AND runtime.tool = run.tool
                WHERE
                    topic = 'run'
                    AND run.has_timed_out = 0
                    AND run.result != 'ERROR'
                    AND run.result != 'MISSING'
                    AND run.result != 'BASE_TOOL_MISSING'
                GROUP BY runtime.tool, topic, task, step
                ORDER BY runtime.tool, topic, step
            ),
            iterations_runtimes AS (
                SELECT tool, topic, task, printf('%.2f', avg(runtime)) AS 'avg_runtime', step
                FROM (
                    SELECT
                        runtime.benchmark AS 'benchmark',
                        runtime.tool AS 'tool',
                        'iterations' AS 'topic',
                        task AS 'task',
                        sum(runtime.runtime) AS 'runtime',
                        step AS 'step'
                    FROM runtime
                    INNER JOIN run on runtime.benchmark = run.benchmark AND runtime.tool = run.tool
                    WHERE
                        topic LIKE 'iteration-%'
                        AND run.has_timed_out = 0
                        AND run.result != 'ERROR'
                        AND run.result != 'MISSING'
                        AND run.result != 'BASE_TOOL_MISSING'
                    GROUP BY runtime.benchmark, runtime.tool, runtime.step
                )
                GROUP BY tool, task, step
                ORDER BY tool, step
            ),
            iteration_runtimes AS (
                SELECT
                    runtime.tool AS 'tool',
                    'iteration' AS 'topic',
                    task AS 'ask',
                    printf('%.2f', avg(runtime.runtime)) AS 'avg_runtime',
                    step AS 'step'
                FROM runtime
                INNER JOIN run on runtime.benchmark = run.benchmark AND runtime.tool = run.tool
                WHERE
                    topic LIKE 'iteration-%'
                    AND run.has_timed_out = 0
                    AND run.result != 'ERROR'
                    AND run.result != 'MISSING'
                    AND run.result != 'BASE_TOOL_MISSING'
                GROUP BY runtime.tool, task, step
                ORDER BY runtime.tool, step
            )
            SELECT * FROM run_runtimes
            UNION ALL
            SELECT * FROM iterations_runtimes
            UNION ALL
            SELECT * FROM iteration_runtimes
            ORDER BY tool, topic, step
    ),
    ardiff_base_runtimes AS (SELECT * FROM all_runtimes WHERE tool = 'ARDiff-base'),
    ardiff_diff_runtimes AS (SELECT * FROM all_runtimes WHERE tool = 'ARDiff-diff'),
    dse_base_runtimes AS (SELECT * FROM all_runtimes WHERE tool = 'DSE-base'),
    dse_diff_runtimes AS (SELECT * FROM all_runtimes WHERE tool = 'DSE-diff'),
    se_base_runtimes AS (SELECT * FROM all_runtimes WHERE tool = 'SE-base'),
    se_diff_runtimes AS (SELECT * FROM all_runtimes WHERE tool = 'SE-diff')
SELECT
    ardiff_base_runtimes.topic as topic,
    ardiff_base_runtimes.task as task,
    ardiff_base_runtimes.avg_runtime AS 'avg(ARDiff-base)',
    ardiff_diff_runtimes.avg_runtime AS 'avg(ARDiff-diff)',
    dse_base_runtimes.avg_runtime AS 'avg(DSE-base)',
    dse_diff_runtimes.avg_runtime AS 'avg(DSE-diff)',
    se_base_runtimes.avg_runtime AS 'avg(SE-base)',
    se_diff_runtimes.avg_runtime AS 'avg(SE-diff)'
FROM ardiff_base_runtimes
INNER JOIN ardiff_diff_runtimes
    ON ardiff_base_runtimes.topic = ardiff_diff_runtimes.topic
    AND ardiff_base_runtimes.task = ardiff_diff_runtimes.task
INNER JOIN dse_base_runtimes
    ON ardiff_base_runtimes.topic = dse_base_runtimes.topic
    AND ardiff_base_runtimes.task = dse_base_runtimes.task
INNER JOIN dse_diff_runtimes
    ON ardiff_base_runtimes.topic = dse_diff_runtimes.topic
    AND ardiff_base_runtimes.task = dse_diff_runtimes.task
INNER JOIN se_base_runtimes
    ON ardiff_base_runtimes.topic = se_base_runtimes.topic
    AND ardiff_base_runtimes.task = se_base_runtimes.task
INNER JOIN se_diff_runtimes
    ON ardiff_base_runtimes.topic = se_diff_runtimes.topic
    AND ardiff_base_runtimes.task = se_diff_runtimes.task
ORDER BY ardiff_base_runtimes.topic DESC, ardiff_base_runtimes.step;

CREATE VIEW IF NOT EXISTS run_reducibility_statistics AS
WITH reducibility_overview AS
(
    WITH
        run_reducibility_by_tool_and_expected_and_is_fully_analyzed AS
        (
            SELECT
                tool AS tool,
                expected AS expected,
                is_fully_analyzed AS is_fully_analyzed,
                count(*) AS '#_runs',
                coalesce(sum(CASE WHEN result = 'EQ' THEN 1 END), 0) AS '#_EQ',
                sum(CASE WHEN result != 'EQ' THEN 1 END) AS '#_!EQ',
                sum(CASE WHEN result != 'EQ' AND is_reducible = 0 THEN 1 END) AS '#_!is_reducible',
                sum(has_only_NEQ) AS "#_has_only_NEQ",
                sum(has_only_UNDECIDED) AS "#_has_only_UNDECIDED",
                sum(is_mixed_NEQ_UNDECIDED) AS "#_is_mixed_NEQ_UNDECIDED",
                sum("#_partitions") AS '#_partitions',
                sum("#_lines") AS "#_lines",
                sum("#_partitions_EQ") AS '#_partitions_EQ',
                sum("#_partitions_NEQ") AS '#_partitions_NEQ',
                sum("#_partitions_UNDECIDED") AS '#_partitions_UNDECIDED',
                sum("#_partitions_MAYBE_EQ") AS '#_partitions_MAYBE_EQ',
                sum("#_partitions_MAYBE_NEQ") AS '#_partitions_MAYBE_NEQ',
                sum("#_partitions_UNKNOWN") AS '#_partitions_UNKNOWN',
                sum("#_partitions_DEPTH_LIMITED") AS '#_partitions_DEPTH_LIMITED',
                sum("#_lines_all_partitions") AS "#_lines_all_partitions",
                sum("#_lines_EQ_partitions") AS '#_lines_EQ_partitions',
                sum("#_lines_NEQ_partitions") AS '#_lines_NEQ_partitions',
                sum("#_lines_UNDECIDED_partitions") AS '#_lines_UNDECIDED_partitions',
                sum("#_lines_MAYBE_EQ_partitions") AS '#_lines_MAYBE_EQ_partitions',
                sum("#_lines_MAYBE_NEQ_partitions") AS '#_lines_MAYBE_NEQ_partitions',
                sum("#_lines_UNKNOWN_partitions") AS '#_lines_UNKNOWN_partitions',
                sum("#_lines_DEPTH_LIMITED_partitions") AS '#_lines_DEPTH_LIMITED_partitions',
                sum("%_line_coverage") AS '%_line_coverage_all_partitions',
                sum("%_line_coverage_EQ_partitions") AS '%_line_coverage_EQ_partitions',
                sum("%_line_coverage_NEQ_partitions") AS '%_line_coverage_NEQ_partitions',
                sum("%_line_coverage_UNDECIDED_partitions") AS '%_line_coverage_UNDECIDED_partitions',
                sum("%_line_coverage_MAYBE_EQ_partitions") AS '%_line_coverage_MAYBE_EQ_partitions',
                sum("%_line_coverage_MAYBE_NEQ_partitions") AS '%_line_coverage_MAYBE_NEQ_partitions',
                sum("%_line_coverage_UNKNOWN_partitions") AS '%_line_coverage_UNKNOWN_partitions',
                sum("%_line_coverage_DEPTH_LIMITED_partitions") AS '%_line_coverage_DEPTH_LIMITED_partitions',
                sum(is_reducible) AS '#_is_reducible',
                sum(are_partitions_reducible) AS '#_are_partitions_reducible',
                sum(CASE WHEN are_partitions_reducible = 1 THEN "#_partitions" END) AS '#_partitions_in_reducible',
                nullif(sum(CASE WHEN are_partitions_reducible = 1 THEN "#_partitions_EQ" ELSE 0 END), 0) AS '#_partitions_reducible',
                nullif(sum(CASE WHEN are_partitions_reducible = 1 THEN "#_partitions" - "#_partitions_EQ" ELSE 0 END), 0) AS '#_partitions_!reducible',
                sum(are_lines_reducible) AS '#_are_lines_reducible',
                sum(CASE WHEN are_lines_reducible = 1 THEN "#_lines" END) AS '#_lines_in_reducible',
                nullif(sum(CASE WHEN are_lines_reducible = 1 THEN "#_lines_only_EQ" ELSE 0 END), 0) AS '#_lines_reducible',
                nullif(sum(CASE WHEN are_lines_reducible = 1 THEN "#_lines" - "#_lines_only_EQ" ELSE 0 END), 0) AS '#_lines_!reducible'
            FROM
            (
                SELECT r.*,
                    sum("%_line_coverage") AS '%_line_coverage',
                    sum(CASE WHEN p.result = 'EQ' THEN "%_line_coverage" END) AS '%_line_coverage_EQ_partitions',
                    sum(CASE WHEN p.result = 'NEQ' THEN "%_line_coverage" END) AS '%_line_coverage_NEQ_partitions',
                    sum(CASE WHEN p.result IS NULL OR (p.result != 'EQ' AND p.result != 'NEQ') THEN "%_line_coverage" END) AS '%_line_coverage_UNDECIDED_partitions',
                    sum(CASE WHEN p.result = 'MAYBE_EQ' THEN "%_line_coverage" END) AS '%_line_coverage_MAYBE_EQ_partitions',
                    sum(CASE WHEN p.result = 'MAYBE_NEQ' THEN "%_line_coverage" END) AS '%_line_coverage_MAYBE_NEQ_partitions',
                    sum(CASE WHEN p.result IS NULL OR p.result = 'UNKNOWN' THEN "%_line_coverage" END) AS '%_line_coverage_UNKNOWN_partitions',
                    sum(CASE WHEN p.result = 'DEPTH_LIMITED' THEN "%_line_coverage" END) AS '%_line_coverage_DEPTH_LIMITED_partitions'
                FROM mv_run_features AS r
                LEFT JOIN mv_partition_features AS p ON r.benchmark = p.benchmark AND r.tool = p.tool AND r.result_iteration = p.iteration
                GROUP BY r.benchmark, r.tool
                ORDER BY benchmark, tool DESC
            )
            GROUP BY tool, expected, is_fully_analyzed
            ORDER BY tool, expected, is_fully_analyzed DESC
        ),
        run_reducibility_by_tool_and_is_fully_analyzed AS
        (
            SELECT
                tool,
                '' AS expected,
                is_fully_analyzed AS is_fully_analyzed,
                sum("#_runs") AS '#_runs',
                sum("#_EQ") AS '#_EQ',
                sum("#_!EQ") AS '#_!EQ',
                sum("#_!is_reducible") AS '#_!is_reducible',
                sum("#_has_only_NEQ") AS '#_has_only_NEQ',
                sum("#_has_only_UNDECIDED") AS '#_has_only_UNDECIDED',
                sum("#_is_mixed_NEQ_UNDECIDED") AS '#_is_mixed_NEQ_UNDECIDED',
                sum("#_partitions") AS '#_partitions',
                sum("#_lines") AS '#_lines',
                sum("#_partitions_EQ") AS '#_partitions_EQ',
                sum("#_partitions_NEQ") AS '#_partitions_NEQ',
                sum("#_partitions_UNDECIDED") AS '#_partitions_UNDECIDED',
                sum("#_partitions_MAYBE_EQ") AS '#_partitions_MAYBE_EQ',
                sum("#_partitions_MAYBE_NEQ") AS '#_partitions_MAYBE_NEQ',
                sum("#_partitions_UNKNOWN") AS '#_partitions_UNKNOWN',
                sum("#_partitions_DEPTH_LIMITED") AS '#_partitions_DEPTH_LIMITED',
                sum("#_lines_all_partitions") AS '#_lines_all_partitions',
                sum("#_lines_EQ_partitions") AS '#_lines_EQ_partitions',
                sum("#_lines_NEQ_partitions") AS '#_lines_NEQ_partitions',
                sum("#_lines_UNDECIDED_partitions") AS '#_lines_UNDECIDED_partitions',
                sum("#_lines_MAYBE_EQ_partitions") AS '#_lines_MAYBE_EQ_partitions',
                sum("#_lines_MAYBE_NEQ_partitions") AS '#_lines_MAYBE_NEQ_partitions',
                sum("#_lines_UNKNOWN_partitions") AS '#_lines_UNKNOWN_partitions',
                sum("#_lines_DEPTH_LIMITED_partitions") AS '#_lines_DEPTH_LIMITED_partitions',
                sum("%_line_coverage_all_partitions") AS '%_line_coverage_all_partitions',
                sum("%_line_coverage_EQ_partitions") AS '%_line_coverage_EQ_partitions',
                sum("%_line_coverage_NEQ_partitions") AS '%_line_coverage_NEQ_partitions',
                sum("%_line_coverage_UNDECIDED_partitions") AS '%_line_coverage_UNDECIDED_partitions',
                sum("%_line_coverage_MAYBE_EQ_partitions") AS '%_line_coverage_MAYBE_EQ_partitions',
                sum("%_line_coverage_MAYBE_NEQ_partitions") AS '%_line_coverage_MAYBE_NEQ_partitions',
                sum("%_line_coverage_UNKNOWN_partitions") AS '%_line_coverage_UNKNOWN_partitions',
                sum("%_line_coverage_DEPTH_LIMITED_partitions") AS '%_line_coverage_DEPTH_LIMITED_partitions',
                sum("#_is_reducible") AS '#_is_reducible',
                sum("#_are_partitions_reducible") AS '#_are_partitions_reducible',
                sum("#_partitions_in_reducible") AS '#_partitions_in_reducible',
                sum("#_partitions_reducible") AS '#_partitions_reducible',
                sum("#_partitions_!reducible") AS '#_partitions_!reducible',
                sum("#_are_lines_reducible") AS '#_are_lines_reducible',
                sum("#_lines_in_reducible") AS '#_lines_in_reducible',
                sum("#_lines_reducible") AS '#_lines_reducible',
                sum("#_lines_!reducible") AS '#_lines_!reducible'
            FROM run_reducibility_by_tool_and_expected_and_is_fully_analyzed
            GROUP BY tool, is_fully_analyzed
            ORDER BY tool, is_fully_analyzed DESC
        ),
        run_reducibility_by_tool_and_expected AS
        (
            SELECT
                tool,
                expected AS expected,
                '' AS is_fully_analyzed,
                sum("#_runs") AS '#_runs',
                sum("#_EQ") AS '#_EQ',
                sum("#_!EQ") AS '#_!EQ',
                sum("#_!is_reducible") AS '#_!is_reducible',
                sum("#_has_only_NEQ") AS '#_has_only_NEQ',
                sum("#_has_only_UNDECIDED") AS '#_has_only_UNDECIDED',
                sum("#_is_mixed_NEQ_UNDECIDED") AS '#_is_mixed_NEQ_UNDECIDED',
                sum("#_partitions") AS '#_partitions',
                sum("#_lines") AS '#_lines',
                sum("#_partitions_EQ") AS '#_partitions_EQ',
                sum("#_partitions_NEQ") AS '#_partitions_NEQ',
                sum("#_partitions_UNDECIDED") AS '#_partitions_UNDECIDED',
                sum("#_partitions_MAYBE_EQ") AS '#_partitions_MAYBE_EQ',
                sum("#_partitions_MAYBE_NEQ") AS '#_partitions_MAYBE_NEQ',
                sum("#_partitions_UNKNOWN") AS '#_partitions_UNKNOWN',
                sum("#_partitions_DEPTH_LIMITED") AS '#_partitions_DEPTH_LIMITED',
                sum("#_lines_all_partitions") AS '#_lines_all_partitions',
                sum("#_lines_EQ_partitions") AS '#_lines_EQ_partitions',
                sum("#_lines_NEQ_partitions") AS '#_lines_NEQ_partitions',
                sum("#_lines_UNDECIDED_partitions") AS '#_lines_UNDECIDED_partitions',
                sum("#_lines_MAYBE_EQ_partitions") AS '#_lines_MAYBE_EQ_partitions',
                sum("#_lines_MAYBE_NEQ_partitions") AS '#_lines_MAYBE_NEQ_partitions',
                sum("#_lines_UNKNOWN_partitions") AS '#_lines_UNKNOWN_partitions',
                sum("#_lines_DEPTH_LIMITED_partitions") AS '#_lines_DEPTH_LIMITED_partitions',
                sum("%_line_coverage_all_partitions") AS '%_line_coverage_all_partitions',
                sum("%_line_coverage_EQ_partitions") AS '%_line_coverage_EQ_partitions',
                sum("%_line_coverage_NEQ_partitions") AS '%_line_coverage_NEQ_partitions',
                sum("%_line_coverage_UNDECIDED_partitions") AS '%_line_coverage_UNDECIDED_partitions',
                sum("%_line_coverage_MAYBE_EQ_partitions") AS '%_line_coverage_MAYBE_EQ_partitions',
                sum("%_line_coverage_MAYBE_NEQ_partitions") AS '%_line_coverage_MAYBE_NEQ_partitions',
                sum("%_line_coverage_UNKNOWN_partitions") AS '%_line_coverage_UNKNOWN_partitions',
                sum("%_line_coverage_DEPTH_LIMITED_partitions") AS '%_line_coverage_DEPTH_LIMITED_partitions',
                sum("#_is_reducible") AS '#_is_reducible',
                sum("#_are_partitions_reducible") AS '#_are_partitions_reducible',
                sum("#_partitions_in_reducible") AS '#_partitions_in_reducible',
                sum("#_partitions_reducible") AS '#_partitions_reducible',
                sum("#_partitions_!reducible") AS '#_partitions_!reducible',
                sum("#_are_lines_reducible") AS '#_are_lines_reducible',
                sum("#_lines_in_reducible") AS '#_lines_in_reducible',
                sum("#_lines_reducible") AS '#_lines_reducible',
                sum("#_lines_!reducible") AS '#_lines_!reducible'
            FROM run_reducibility_by_tool_and_expected_and_is_fully_analyzed
            GROUP BY tool, expected
            ORDER BY tool, expected
        ),
        run_reducibility_by_tool AS
        (
            SELECT
                tool,
                '' AS expected,
                '' AS is_fully_analyzed,
                sum("#_runs") AS '#_runs',
                sum("#_EQ") AS '#_EQ',
                sum("#_!EQ") AS '#_!EQ',
                sum("#_!is_reducible") AS '#_!is_reducible',
                sum("#_has_only_NEQ") AS '#_has_only_NEQ',
                sum("#_has_only_UNDECIDED") AS '#_has_only_UNDECIDED',
                sum("#_is_mixed_NEQ_UNDECIDED") AS '#_is_mixed_NEQ_UNDECIDED',
                sum("#_partitions") AS '#_partitions',
                sum("#_lines") AS '#_lines',
                sum("#_partitions_EQ") AS '#_partitions_EQ',
                sum("#_partitions_NEQ") AS '#_partitions_NEQ',
                sum("#_partitions_UNDECIDED") AS '#_partitions_UNDECIDED',
                sum("#_partitions_MAYBE_EQ") AS '#_partitions_MAYBE_EQ',
                sum("#_partitions_MAYBE_NEQ") AS '#_partitions_MAYBE_NEQ',
                sum("#_partitions_UNKNOWN") AS '#_partitions_UNKNOWN',
                sum("#_partitions_DEPTH_LIMITED") AS '#_partitions_DEPTH_LIMITED',
                sum("#_lines_all_partitions") AS '#_lines_all_partitions',
                sum("#_lines_EQ_partitions") AS '#_lines_EQ_partitions',
                sum("#_lines_NEQ_partitions") AS '#_lines_NEQ_partitions',
                sum("#_lines_UNDECIDED_partitions") AS '#_lines_UNDECIDED_partitions',
                sum("#_lines_MAYBE_EQ_partitions") AS '#_lines_MAYBE_EQ_partitions',
                sum("#_lines_MAYBE_NEQ_partitions") AS '#_lines_MAYBE_NEQ_partitions',
                sum("#_lines_UNKNOWN_partitions") AS '#_lines_UNKNOWN_partitions',
                sum("#_lines_DEPTH_LIMITED_partitions") AS '#_lines_DEPTH_LIMITED_partitions',
                sum("%_line_coverage_all_partitions") AS '%_line_coverage_all_partitions',
                sum("%_line_coverage_EQ_partitions") AS '%_line_coverage_EQ_partitions',
                sum("%_line_coverage_NEQ_partitions") AS '%_line_coverage_NEQ_partitions',
                sum("%_line_coverage_UNDECIDED_partitions") AS '%_line_coverage_UNDECIDED_partitions',
                sum("%_line_coverage_MAYBE_EQ_partitions") AS '%_line_coverage_MAYBE_EQ_partitions',
                sum("%_line_coverage_MAYBE_NEQ_partitions") AS '%_line_coverage_MAYBE_NEQ_partitions',
                sum("%_line_coverage_UNKNOWN_partitions") AS '%_line_coverage_UNKNOWN_partitions',
                sum("%_line_coverage_DEPTH_LIMITED_partitions") AS '%_line_coverage_DEPTH_LIMITED_partitions',
                sum("#_is_reducible") AS '#_is_reducible',
                sum("#_are_partitions_reducible") AS '#_are_partitions_reducible',
                sum("#_partitions_in_reducible") AS '#_partitions_in_reducible',
                sum("#_partitions_reducible") AS '#_partitions_reducible',
                sum("#_partitions_!reducible") AS '#_partitions_!reducible',
                sum("#_are_lines_reducible") AS '#_are_lines_reducible',
                sum("#_lines_in_reducible") AS '#_lines_in_reducible',
                sum("#_lines_reducible") AS '#_lines_reducible',
                sum("#_lines_!reducible") AS '#_lines_!reducible'
            FROM run_reducibility_by_tool_and_expected_and_is_fully_analyzed
            GROUP BY tool
            ORDER BY tool
        )
    SELECT * FROM run_reducibility_by_tool
    UNION ALL
    SELECT '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', ''
    UNION ALL
    SELECT * FROM run_reducibility_by_tool_and_expected
    UNION ALL
    SELECT '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', ''
    UNION ALL
    SELECT * FROM run_reducibility_by_tool_and_is_fully_analyzed
    UNION ALL
    SELECT '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', ''
    UNION ALL
    SELECT * FROM run_reducibility_by_tool_and_expected_and_is_fully_analyzed
)
SELECT
    tool AS tool,
    expected AS expected,
    is_fully_analyzed AS is_fully_analyzed,
    "#_runs",
    "#_EQ",
    "#_!EQ",
    "#_!is_reducible" AS "#_!is_reducible",
    "#_is_reducible",
    coalesce(round(("#_!is_reducible" * 1.0 / "#_!EQ") * 100, 2), '') AS '%_!i_r',
    coalesce(round(("#_is_reducible" * 1.0 / "#_!EQ") * 100, 2), '') AS '%_i_r',
    '' AS '|',
    tool AS tool,
    expected AS expected,
    is_fully_analyzed AS is_fully_analyzed,
    "#_!is_reducible" AS "#_!is_reducible",
    coalesce(round(("#_!is_reducible" * 1.0 / "#_!EQ") * 100, 2), '') AS "%_!i_r",
    "#_has_only_NEQ",
    "#_has_only_UNDECIDED",
    "#_is_mixed_NEQ_UNDECIDED",
    coalesce(round(("#_has_only_NEQ" * 1.0 / "#_!is_reducible") * 100, 2), '') AS '%_o_N',
    coalesce(round(("#_has_only_UNDECIDED" * 1.0 / "#_!is_reducible") * 100, 2), '') AS '%_o_U',
    coalesce(round(("#_is_mixed_NEQ_UNDECIDED" * 1.0 / "#_!is_reducible") * 100, 2), '') AS '%_m_N_U',
    '' AS '|',
    tool AS tool,
    expected AS expected,
    is_fully_analyzed AS is_fully_analyzed,
    "#_are_partitions_reducible",
    coalesce(round(("#_are_partitions_reducible" * 1.0 / "#_!EQ") * 100, 2), '') AS '%_a_p_r',
    "#_partitions_in_reducible",
    "#_partitions_reducible",
    "#_partitions_!reducible",
    coalesce(round(("#_partitions_reducible" * 1.0 / "#_partitions_in_reducible") * 100, 2), '') AS '%_p_r',
    coalesce(round(("#_partitions_!reducible" * 1.0 / "#_partitions_in_reducible") * 100, 2), '') AS '%_p_!r',
    '' AS '|',
    tool AS tool,
    expected AS expected,
    is_fully_analyzed AS is_fully_analyzed,
    coalesce(round("#_lines_all_partitions" * 1.0 / "#_partitions", 2), '') AS '#_lines_per_partition',
    coalesce(round("#_lines_EQ_partitions" * 1.0 / "#_partitions_EQ", 2), '') AS '#_lp_EQ_p',
    coalesce(round("#_lines_NEQ_partitions" * 1.0 / "#_partitions_NEQ", 2), '') AS '#_lp_NEQ_p',
    coalesce(round("#_lines_UNDECIDED_partitions" * 1.0 / "#_partitions_UNDECIDED", 2), '') AS '#_lp_UNDECIDED_p',
    coalesce(round("#_lines_MAYBE_EQ_partitions" * 1.0 / "#_partitions_MAYBE_EQ", 2), '') AS '#_lp_MAYBE_EQ_p',
    coalesce(round("#_lines_MAYBE_NEQ_partitions" * 1.0 / "#_partitions_MAYBE_NEQ", 2), '') AS '#_lp_MAYBE_NEQ_p',
    coalesce(round("#_lines_UNKNOWN_partitions" * 1.0 / "#_partitions_UNKNOWN", 2), '') AS '#_lp_UNKNOWN_p',
    coalesce(round("#_lines_DEPTH_LIMITED_partitions" * 1.0 / "#_partitions_DEPTH_LIMITED", 2), '') AS '#_lp_DEPTH_LIMITED_p',
    coalesce(round(("%_line_coverage_all_partitions" * 1.0 / "#_partitions"), 2), '') AS '%_coverage_per_partition',
    coalesce(round(("%_line_coverage_EQ_partitions" * 1.0 / "#_partitions_EQ"), 2), '') AS '%_cp_EQ_p',
    coalesce(round(("%_line_coverage_NEQ_partitions" * 1.0 / "#_partitions_NEQ"), 2), '') AS '%_cp_NEQ_p',
    coalesce(round(("%_line_coverage_UNDECIDED_partitions" * 1.0 / "#_partitions_UNDECIDED"), 2), '') AS '%_cp_UNDECIDED_p',
    coalesce(round(("%_line_coverage_MAYBE_EQ_partitions" * 1.0 / "#_partitions_MAYBE_EQ"), 2), '') AS '%_cp_MAYBE_EQ_p',
    coalesce(round(("%_line_coverage_MAYBE_NEQ_partitions" * 1.0 / "#_partitions_MAYBE_NEQ"), 2), '') AS '%_cp_MAYBE_NEQ_p',
    coalesce(round(("%_line_coverage_UNKNOWN_partitions" * 1.0 / "#_partitions_UNKNOWN"), 2), '') AS '%_cp_UNKNOWN_p',
    coalesce(round(("%_line_coverage_DEPTH_LIMITED_partitions" * 1.0 / "#_partitions_DEPTH_LIMITED"), 2), '') AS '%_cp_DEPTH_LIMITED_p',
    '' AS '|',
    tool AS tool,
    expected AS expected,
    is_fully_analyzed AS is_fully_analyzed,
    "#_are_lines_reducible",
    coalesce(round(("#_are_lines_reducible" * 1.0 / "#_!EQ") * 100, 2), '') AS '%_a_l_r',
    "#_lines_in_reducible",
    "#_lines_reducible",
    "#_lines_!reducible",
    coalesce(round(("#_lines_reducible" * 1.0 / "#_lines_in_reducible") * 100, 2), '') AS '%_l_r',
    coalesce(round(("#_lines_!reducible" * 1.0 / "#_lines_in_reducible") * 100, 2), '') AS '%_l_!r'
FROM reducibility_overview;

CREATE VIEW IF NOT EXISTS __paper__classification_crosstab AS
SELECT
    tool,
    expected,
    EQ,
    NEQ,
    (MAYBE_EQ + MAYBE_NEQ + "UNKNOWN" + "TIMEOUT" + DEPTH_LIMITED + UNREACHABLE + ERROR + BASE_TOOL_MISSING + MISSING) AS 'UNKNOWN'
FROM run_result_crosstab_true
ORDER BY
    CASE
        WHEN tool = 'PASDA-diff' AND expected = 'EQ' THEN 0
        WHEN tool = 'PASDA-diff' AND expected = 'NEQ' THEN 1
        WHEN tool = 'ARDiff-diff' AND expected = 'EQ' THEN 2
        WHEN tool = 'ARDiff-diff' AND expected = 'NEQ' THEN 3
        WHEN tool = 'DSE-diff' AND expected = 'EQ' THEN 4
        WHEN tool = 'DSE-diff' AND expected = 'NEQ' THEN 5
        WHEN tool = 'SE-diff' AND expected = 'EQ' THEN 6
        WHEN tool = 'SE-diff' AND expected = 'NEQ' THEN 7
        WHEN tool = 'PASDA-base' AND expected = 'EQ' THEN 8
        WHEN tool = 'PASDA-base' AND expected = 'NEQ' THEN 9
        WHEN tool = 'ARDiff-base' AND expected = 'EQ' THEN 10
        WHEN tool = 'ARDiff-base' AND expected = 'NEQ' THEN 11
        WHEN tool = 'DSE-base' AND expected = 'EQ' THEN 12
        WHEN tool = 'DSE-base' AND expected = 'NEQ' THEN 13
        WHEN tool = 'SE-base' AND expected = 'EQ' THEN 14
        WHEN tool = 'SE-base' AND expected = 'NEQ' THEN 15
    END;

CREATE VIEW IF NOT EXISTS __paper__runtime_per_expected AS
SELECT
    tool,
    expected,
    --count(*),
    sum(has_timed_out) as timed_out,
    --printf('%.2f', avg(CASE WHEN has_timed_out = 1 THEN runtime END)) AS avg_runtime,
    sum(NOT has_timed_out) as not_timed_out,
    printf('%.2f', avg(CASE WHEN has_timed_out = 0 THEN "#_iterations" END)) AS avg_iterations,
    printf('%.2f', avg(CASE WHEN has_timed_out = 0 THEN runtime END)) AS avg_runtime
    --printf('%.2f', median(CASE WHEN has_timed_out = 0 THEN runtime END)) median_runtime,
    --printf('%.2f', stddev(CASE WHEN has_timed_out = 0 THEN runtime END)) stddev_runtime
FROM mv_run_features
WHERE
    result IS NOT NULL
    AND result != 'ERROR'
    AND result != 'MISSING'
    AND result != 'BASE_TOOL_MISSING'
GROUP BY tool, expected
ORDER BY
    CASE
        WHEN tool = 'ARDiff-diff' AND expected = 'EQ' THEN 0
        WHEN tool = 'ARDiff-diff' AND expected = 'NEQ' THEN 1
        WHEN tool = 'DSE-diff' AND expected = 'EQ' THEN 2
        WHEN tool = 'DSE-diff' AND expected = 'NEQ' THEN 3
        WHEN tool = 'SE-diff' AND expected = 'EQ' THEN 4
        WHEN tool = 'SE-diff' AND expected = 'NEQ' THEN 5
        WHEN tool = 'ARDiff-base' AND expected = 'EQ' THEN 6
        WHEN tool = 'ARDiff-base' AND expected = 'NEQ' THEN 7
        WHEN tool = 'DSE-base' AND expected = 'EQ' THEN 8
        WHEN tool = 'DSE-base' AND expected = 'NEQ' THEN 9
        WHEN tool = 'SE-base' AND expected = 'EQ' THEN 10
        WHEN tool = 'SE-base' AND expected = 'NEQ' THEN 11
    END;

CREATE VIEW IF NOT EXISTS __paper__runtime_per_step AS
SELECT
    CASE task WHEN '' THEN 'overall' ELSE task END as task,
    sum("avg(ARDiff-diff)"),
    sum("avg(ARDiff-base)"),
    sum("avg(DSE-diff)"),
    sum("avg(DSE-base)"),
    sum("avg(SE-diff)"),
    sum("avg(SE-base)")
FROM runtime_per_task
WHERE
    topic in ('run', 'iterations')
    AND NOT (topic = 'iterations' AND task = '')
GROUP BY task
ORDER BY
    CASE task
        WHEN 'initialization' THEN 0
        WHEN 'instrumentation' THEN 1
        WHEN 'symbolic-execution' THEN 2
        WHEN 'partition-classification' THEN 3
        WHEN 'program-classification' THEN 4
        WHEN 'refinement' THEN 5
        WHEN 'finalization' THEN 6
        WHEN '' THEN 7
    END;

CREATE VIEW IF NOT EXISTS __paper__partitions_per_classification AS
WITH partitions_per_classification AS
(
    SELECT
        rf.benchmark,
        rf.tool,
        rf.expected,
        rf.result,
        rf.has_timed_out,
        count(*) AS "#_Partitions",
        sum(CASE WHEN pf.result = 'EQ' THEN 1 ELSE 0 END) AS '#_EQ',
        sum(CASE WHEN pf.result = 'NEQ' THEN 1 ELSE 0 END) AS '#_NEQ',
        sum(CASE WHEN pf.result = 'MAYBE_EQ' THEN 1 ELSE 0 END) AS '#_MAYBE_EQ',
        sum(CASE WHEN pf.result = 'MAYBE_NEQ' THEN 1 ELSE 0 END) AS '#_MAYBE_NEQ',
        sum(CASE WHEN pf.result = 'UNKNOWN' THEN 1 ELSE 0 END) AS '#_UNKNOWN',
        sum(CASE WHEN pf.result = 'DEPTH_LIMITED' THEN 1 ELSE 0 END) AS '#_DEPTH_LIMITED',
        sum(CASE WHEN pf.result IS NULL THEN 1 ELSE 0 END) AS '#_TIMEOUT'
    FROM mv_run_features AS rf
    LEFT JOIN mv_partition_features AS pf
        ON rf.benchmark = pf.benchmark
        AND rf.tool = pf.tool
        AND rf.result_iteration = pf.iteration
    WHERE rf.tool = 'ARDiff-diff'
    GROUP BY rf.benchmark, rf.tool
)
SELECT
    ppe.tool,
    ppe.expected,
    ppe.result,
    count(*) AS '#_Runs',
    sum(ppe.has_timed_out) AS "#_Timeouts",
    printf('%.2f', avg(ppe."#_Partitions")) AS 'avg(#_Partitions)',
    printf('%.2f', 100 * avg(ppe."#_EQ") / avg(ppe."#_Partitions")) AS 'avg(%_EQ)',
    printf('%.2f', 100 * avg(ppe."#_NEQ") / avg(ppe."#_Partitions")) AS 'avg(%_NEQ)',
    printf('%.2f', 100 * avg(ppe."#_MAYBE_EQ") / avg(ppe."#_Partitions")) AS 'avg(%_MAYBE_EQ)',
    printf('%.2f', 100 * avg(ppe."#_MAYBE_NEQ") / avg(ppe."#_Partitions")) AS 'avg(%_MAYBE_NEQ)',
    printf('%.2f', 100 * avg(ppe."#_UNKNOWN" + "#_TIMEOUT") / avg(ppe."#_Partitions")) AS 'avg(%_UNKNOWN)',
    printf('%.2f', 100 * avg(ppe."#_DEPTH_LIMITED") / avg(ppe."#_Partitions")) AS 'avg(%_DEPTH_LIMITED)'
FROM partitions_per_classification AS ppe
GROUP BY ppe.expected, ppe.result
ORDER BY
    CASE
        WHEN ppe.expected = 'EQ' AND ppe.result = 'EQ' THEN 0
        WHEN ppe.expected = 'EQ' AND ppe.result = 'MAYBE_EQ' THEN 1
        WHEN ppe.expected = 'EQ' AND ppe.result = 'MAYBE_NEQ' THEN 2
        WHEN ppe.expected = 'EQ' AND ppe.result = 'UNKNOWN' THEN 3
        WHEN ppe.expected = 'EQ' AND ppe.result = 'DEPTH_LIMITED' THEN 4
        WHEN ppe.expected = 'EQ' AND ppe.result = 'ERROR' THEN 5
        WHEN ppe.expected = 'NEQ' AND ppe.result = 'NEQ' THEN 6
        WHEN ppe.expected = 'NEQ' AND ppe.result = 'MAYBE_EQ' THEN 7
        WHEN ppe.expected = 'NEQ' AND ppe.result = 'MAYBE_NEQ' THEN 8
        WHEN ppe.expected = 'NEQ' AND ppe.result = 'UNKNOWN' THEN 9
        WHEN ppe.expected = 'NEQ' AND ppe.result = 'DEPTH_LIMITED' THEN 10
    END;
