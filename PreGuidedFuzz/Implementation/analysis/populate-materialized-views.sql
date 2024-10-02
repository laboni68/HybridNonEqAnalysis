INSERT INTO mv_line
(
    benchmark,
    tool,
    iteration,
    source_file,
    source_line,
    ---
    '#_instructions'
)
SELECT
    i.benchmark,
    i.tool,
    i.iteration,
    i.source_file,
    i.source_line,
    count(*) AS '#instructions'
FROM instruction AS i
GROUP BY i.benchmark, i.tool, i.iteration, i.source_file, i.source_line;

INSERT INTO mv_partition_line
(
    benchmark,
    tool,
    iteration,
    partition,
    source_file,
    source_line
)
SELECT
    pi.benchmark,
    pi.tool,
    pi.iteration,
    pi.partition,
    i.source_file,
    i.source_line
FROM partition_instruction AS pi
INNER JOIN instruction AS i
    ON pi.benchmark = i.benchmark
    AND pi.tool = i.tool
    AND pi.iteration = i.iteration
    AND pi.method = i.method
    AND pi.instruction_index = i.instruction_index
GROUP BY pi.benchmark, pi.tool, pi.iteration, pi.partition, i.source_file, i.source_line;

INSERT INTO mv_line_features
(
    benchmark,
    tool,
    iteration,
    source_file,
    source_line,
    ---
    is_base,
    is_diff,
    ---
    '#_instructions',
    ---
    '#_partitions',
    '#_partitions_EQ',
    '#_partitions_NEQ',
    '#_partitions_UNDECIDED',
    '#_partitions_MAYBE_EQ',
    '#_partitions_MAYBE_NEQ',
    '#_partitions_UNKNOWN',
    '#_partitions_DEPTH_LIMITED',
    ---
    has_EQ,
    has_NEQ,
    has_UNDECIDED,
    has_MAYBE_EQ,
    has_MAYBE_NEQ,
    has_UNKNOWN,
    has_DEPTH_LIMITED,
    ---
    is_non_mixed,
    has_only_EQ,
    has_only_NEQ,
    has_only_UNDECIDED,
    ---
    is_mixed,
    is_mixed_EQ_NEQ,
    is_mixed_EQ_UNDECIDED,
    is_mixed_NEQ_UNDECIDED,
    is_mixed_EQ_NEQ_UNDECIDED
)
WITH l_features AS (
    WITH l_features AS (
        SELECT l.*,
            l.tool LIKE '%-base' AS is_base,
            l.tool LIKE '%-diff' AS is_diff,
            count(*) AS "#_partitions",
            coalesce(sum(CASE p.result WHEN 'EQ' THEN 1 END), 0) AS "#_partitions_EQ",
            coalesce(sum(CASE p.result WHEN 'NEQ' THEN 1 END), 0) AS "#_partitions_NEQ",
            coalesce(sum(CASE WHEN p.result == null OR (p.result != 'EQ' AND p.result != 'NEQ') THEN 1 END), 0) AS "#_partitions_UNDECIDED",
            coalesce(sum(CASE p.result WHEN 'MAYBE_EQ' THEN 1 END), 0) AS "#_partitions_MAYBE_EQ",
            coalesce(sum(CASE p.result WHEN 'MAYBE_NEQ' THEN 1 END), 0) AS "#_partitions_MAYBE_NEQ",
            coalesce(sum(CASE WHEN p.result == null OR p.result = 'UNKNOWN' THEN 1 END), 0) AS "#_partitions_UNKNOWN",
            coalesce(sum(CASE p.result WHEN 'DEPTH_LIMITED' THEN 1 END), 0) AS "#_partitions_DEPTH_LIMITED"
        FROM mv_line AS l
        INNER JOIN mv_partition_line AS pl USING (benchmark, tool, iteration, source_file, source_line)
        INNER JOIN partition AS p USING (benchmark, tool, iteration, partition)
        GROUP BY l.benchmark, l.tool, l.iteration, l.source_file, l.source_line
    )
    SELECT lf.*,
        lf."#_partitions_EQ" > 0 AS has_EQ,
        lf."#_partitions_NEQ" > 0 AS has_NEQ,
        lf."#_partitions_UNDECIDED" > 0 AS has_UNDECIDED,
        lf."#_partitions_MAYBE_EQ" > 0 AS has_MAYBE_EQ,
        lf."#_partitions_MAYBE_NEQ" > 0 AS has_MAYBE_NEQ,
        lf."#_partitions_UNKNOWN" > 0 AS has_UNKNOWN,
        lf."#_partitions_DEPTH_LIMITED" > 0 AS has_DEPTH_LIMITED,
        lf."#_partitions" = lf."#_partitions_EQ" AS has_only_EQ,
        lf."#_partitions" = lf."#_partitions_NEQ" AS has_only_NEQ,
        lf."#_partitions" = lf."#_partitions_UNDECIDED" AS has_only_UNDECIDED
    FROM l_features AS lf
)
SELECT
    lf.benchmark,
    lf.tool,
    lf.iteration,
    lf.source_file,
    lf.source_line,
    ---
    lf.is_base,
    lf.is_diff,
    ---
    lf."#_instructions",
    ---
    lf."#_partitions",
    lf."#_partitions_EQ",
    lf."#_partitions_NEQ",
    lf."#_partitions_UNDECIDED",
    lf."#_partitions_MAYBE_EQ",
    lf."#_partitions_MAYBE_NEQ",
    lf."#_partitions_UNKNOWN",
    lf."#_partitions_DEPTH_LIMITED",
    ---
    lf.has_EQ,
    lf.has_NEQ,
    lf.has_UNDECIDED,
    lf.has_MAYBE_EQ,
    lf.has_MAYBE_NEQ,
    lf.has_UNKNOWN,
    lf.has_DEPTH_LIMITED,
    ---
    lf.has_only_EQ OR lf.has_only_NEQ OR lf.has_only_UNDECIDED AS is_non_mixed,
    lf.has_only_EQ,
    lf.has_only_NEQ,
    lf.has_only_UNDECIDED,
    ---
    NOT lf.has_only_EQ AND NOT lf.has_only_NEQ AND NOT lf.has_only_UNDECIDED AS is_mixed,
    (lf.has_EQ AND lf.has_NEQ) AND (NOT lf.has_UNDECIDED) AS is_mixed_EQ_NEQ,
    (lf.has_EQ AND lf.has_UNDECIDED) AND (NOT lf.has_NEQ) AS is_mixed_EQ_UNDECIDED,
    (lf.has_NEQ AND lf.has_UNDECIDED) AND (NOT lf.has_EQ) AS is_mixed_NEQ_UNDECIDED,
    (lf.has_EQ AND lf.has_NEQ AND lf.has_UNDECIDED) AS is_mixed_EQ_NEQ_UNDECIDED
FROM l_features AS lf;

INSERT INTO mv_partition_features
(
    benchmark,
    tool,
    iteration,
    partition,
    ---
    result,
    pc_status,
    neq_status,
    eq_status,
    has_uif,
    has_uif_pc,
    has_uif_v1,
    has_uif_v2,
    constraint_count,
    runtime,
    errors,
    ---
    is_base,
    is_diff,
    ---
    "#_lines_iteration",
    "#_lines_partition",
    "%_line_coverage",
    "#_instructions_partition"
)
WITH p_features AS
(
    SELECT
    p.benchmark,
    p.tool,
    p.iteration,
    p.partition,
    ---
    p.result,
    p.pc_status,
    p.neq_status,
    p.eq_status,
    p.has_uif,
    p.has_uif_pc,
    p.has_uif_v1,
    p.has_uif_v2,
    p.constraint_count,
    p.runtime,
    p.errors,
    ---
    p.tool LIKE '%-base' AS is_base,
    p.tool LIKE '%-diff' AS is_diff,
    ---
    count(DISTINCT i.source_file || ':' || i.source_line) AS '#_lines_partition',
    count(pi.execution_index) AS '#_instructions_partition'
    FROM partition AS p
    LEFT JOIN partition_instruction AS pi USING(benchmark, tool, iteration, partition)
    LEFT JOIN instruction AS i USING(benchmark, tool, iteration, method, instruction_index)
    GROUP BY p.benchmark, p.tool, p.iteration, p.partition
)
SELECT
    p.benchmark,
    p.tool,
    p.iteration,
    p.partition,
    ---
    p.result,
    p.pc_status,
    p.neq_status,
    p.eq_status,
    p.has_uif,
    p.has_uif_pc,
    p.has_uif_v1,
    p.has_uif_v2,
    p.constraint_count,
    p.runtime,
    p.errors,
    ---
    p.is_base,
    p.is_diff,
    ---
    count(*) AS '#_lines_iteration',
    p."#_lines_partition",
    (p."#_lines_partition" * 1.0 / count(*)) * 100 AS '%_line_coverage',
    p."#_instructions_partition"
FROM p_features AS p
LEFT JOIN mv_line_features AS l USING (benchmark, tool, iteration)
GROUP BY p.benchmark, p.tool, p.iteration, p.partition;

---

INSERT INTO mv_iteration_features
(
    benchmark,
    tool,
    iteration,
    ---
    has_timed_out,
    is_depth_limited,
    has_uif,
    runtime,
    errors,
    ---
    is_base,
    is_diff,
    ---
    expected,
    result,
    ---
    is_correct,
    is_incorrect,
    is_undecided,
    ---
    is_fully_analyzed,
    ---
    is_reducible,
    are_partitions_reducible,
    are_lines_reducible,
    ---
    has_EQ,
    has_NEQ,
    has_UNDECIDED,
    ---
    is_non_mixed,
    has_only_EQ,
    has_only_NEQ,
    has_only_UNDECIDED,
    ---
    is_mixed,
    is_mixed_EQ_NEQ,
    is_mixed_EQ_UNDECIDED,
    is_mixed_NEQ_UNDECIDED,
    is_mixed_EQ_NEQ_UNDECIDED,
    ---
    "#_partitions",
    ---
    "#_partitions_EQ",
    "#_partitions_NEQ",
    "#_partitions_UNDECIDED",
    "#_partitions_MAYBE_EQ",
    "#_partitions_MAYBE_NEQ",
    "#_partitions_UNKNOWN",
    "#_partitions_DEPTH_LIMITED",
    ---
    "%_partitions_EQ",
    "%_partitions_NEQ",
    "%_partitions_UNDECIDED",
    "%_partitions_MAYBE_EQ",
    "%_partitions_MAYBE_NEQ",
    "%_partitions_UNKNOWN",
    "%_partitions_DEPTH_LIMITED",
    ---
    "#_lines_all_partitions",
    ---
    "#_lines_EQ_partitions",
    "#_lines_NEQ_partitions",
    "#_lines_UNDECIDED_partitions",
    "#_lines_MAYBE_EQ_partitions",
    "#_lines_MAYBE_NEQ_partitions",
    "#_lines_UNKNOWN_partitions",
    "#_lines_DEPTH_LIMITED_partitions",
    ---
    "#_lines_per_partition",
    "#_lines_per_EQ_partition",
    "#_lines_per_NEQ_partition",
    "#_lines_per_UNDECIDED_partition",
    "#_lines_per_MAYBE_EQ_partition",
    "#_lines_per_MAYBE_NEQ_partition",
    "#_lines_per_UNKNOWN_partition",
    "#_lines_per_DEPTH_LIMITED_partition",
    ---
    "%_line_coverage_per_partition",
    "%_line_coverage_per_EQ_partition",
    "%_line_coverage_per_NEQ_partition",
    "%_line_coverage_per_UNDECIDED_partition",
    "%_line_coverage_per_MAYBE_EQ_partition",
    "%_line_coverage_per_MAYBE_NEQ_partition",
    "%_line_coverage_per_UNKNOWN_partition",
    "%_line_coverage_per_DEPTH_LIMITED_partition",
    ---
    "#_lines",
    ---
    "#_lines_EQ",
    "#_lines_NEQ",
    "#_lines_UNDECIDED",
    "#_lines_non_mixed",
    "#_lines_only_EQ",
    "#_lines_only_NEQ",
    "#_lines_only_UNDECIDED",
    "#_lines_mixed",
    "#_lines_mixed_EQ_NEQ",
    "#_lines_mixed_EQ_UNDECIDED",
    "#_lines_mixed_NEQ_UNDECIDED",
    "#_lines_mixed_EQ_NEQ_UNDECIDED",
    ---
    "%_lines_EQ",
    "%_lines_NEQ",
    "%_lines_UNDECIDED",
    "%_lines_non_mixed",
    "%_lines_only_EQ",
    "%_lines_only_NEQ",
    "%_lines_only_UNDECIDED",
    "%_lines_mixed",
    "%_lines_mixed_EQ_NEQ",
    "%_lines_mixed_EQ_UNDECIDED",
    "%_lines_mixed_NEQ_UNDECIDED",
    "%_lines_mixed_EQ_NEQ_UNDECIDED"
)
WITH i_features_5 AS
(
    WITH i_features_4 AS
    (
        WITH i_features_3 AS
        (
            WITH i_features_2 AS
            (
                WITH i_features_1 AS
                (
                    SELECT
                        i.benchmark AS benchmark,
                        i.tool AS tool,
                        i.iteration AS iteration,
                        ---
                        i.has_timed_out AS has_timed_out,
                        i.is_depth_limited AS is_depth_limited,
                        i.has_uif AS has_uif,
                        i.runtime AS runtime,
                        i.errors AS errors,
                        ---
                        i.tool LIKE '%-base' AS is_base,
                        i.tool LIKE '%-diff' AS is_diff,
                        ---
                        b.expected AS expected,
                        i.result AS result,
                        ---
                        b.expected = i.result AS is_correct,
                        (b.expected = 'EQ' AND i.result = 'NEQ') OR (b.expected = 'NEQ' AND i.result = 'EQ') AS is_incorrect,
                        i.result IS NULL OR (i.result != 'EQ' AND i.result != 'NEQ') AS is_undecided,
                        coalesce(result IN ('EQ', 'NEQ', 'MAYBE_EQ', 'MAYBE_NEQ', 'UNKNOWN') AND i.has_timed_out = 0 AND i.is_depth_limited = 0, 0) AS is_fully_analyzed
                    FROM iteration AS i
                    INNER JOIN benchmark AS b USING(benchmark)
                )
                SELECT if_1.*,
                    nullif(count(pf.partition), 0) AS '#_partitions',
                    CASE WHEN pf.partition IS NULL THEN NULL ELSE coalesce(sum(CASE pf.result WHEN 'EQ' THEN 1 END), 0) END AS '#_partitions_EQ',
                    CASE WHEN pf.partition IS NULL THEN NULL ELSE coalesce(sum(CASE pf.result WHEN 'NEQ' THEN 1 END), 0) END AS '#_partitions_NEQ',
                    CASE WHEN pf.partition IS NULL THEN NULL ELSE coalesce(sum(CASE WHEN pf.result IS NULL OR (pf.result != 'EQ' AND pf.result != 'NEQ') THEN 1 END), 0) END AS "#_partitions_UNDECIDED",
                    CASE WHEN pf.partition IS NULL THEN NULL ELSE coalesce(sum(CASE pf.result WHEN 'MAYBE_EQ' THEN 1 END), 0) END AS '#_partitions_MAYBE_EQ',
                    CASE WHEN pf.partition IS NULL THEN NULL ELSE coalesce(sum(CASE pf.result WHEN 'MAYBE_NEQ' THEN 1 END), 0) END AS '#_partitions_MAYBE_NEQ',
                    CASE WHEN pf.partition IS NULL THEN NULL ELSE coalesce(sum(CASE WHEN pf.result IS NULL OR pf.result = 'UNKNOWN' THEN 1 END), 0) END AS "#_partitions_UNKNOWN",
                    CASE WHEN pf.partition IS NULL THEN NULL ELSE coalesce(sum(CASE pf.result WHEN 'DEPTH_LIMITED' THEN 1 END), 0) END AS '#_partitions_DEPTH_LIMITED',
                    nullif(sum(pf."#_lines_partition"), 0) AS '#_lines_all_partitions',
                    CASE WHEN pf.partition IS NULL THEN NULL ELSE coalesce(sum(CASE pf.result WHEN 'EQ' THEN pf."#_lines_partition" END), 0) END AS '#_lines_EQ_partitions',
                    CASE WHEN pf.partition IS NULL THEN NULL ELSE coalesce(sum(CASE pf.result WHEN 'NEQ' THEN pf."#_lines_partition" END), 0) END AS '#_lines_NEQ_partitions',
                    CASE WHEN pf.partition IS NULL THEN NULL ELSE coalesce(sum(CASE WHEN pf.result != 'EQ' AND pf.result != 'NEQ' THEN pf."#_lines_partition" END), 0) END AS "#_lines_UNDECIDED_partitions",
                    CASE WHEN pf.partition IS NULL THEN NULL ELSE coalesce(sum(CASE pf.result WHEN 'MAYBE_EQ' THEN pf."#_lines_partition" END), 0) END AS '#_lines_MAYBE_EQ_partitions',
                    CASE WHEN pf.partition IS NULL THEN NULL ELSE coalesce(sum(CASE pf.result WHEN 'MAYBE_NEQ' THEN pf."#_lines_partition" END), 0) END AS '#_lines_MAYBE_NEQ_partitions',
                    CASE WHEN pf.partition IS NULL THEN NULL ELSE coalesce(sum(CASE WHEN pf.result IS NULL OR pf.result = 'UNKNOWN' THEN pf."#_lines_partition" END), 0) END AS "#_lines_UNKNOWN_partitions",
                    CASE WHEN pf.partition IS NULL THEN NULL ELSE coalesce(sum(CASE pf.result WHEN 'DEPTH_LIMITED' THEN pf."#_lines_partition" END), 0) END AS '#_lines_DEPTH_LIMITED_partitions'
                FROM i_features_1 AS if_1
                LEFT JOIN mv_partition_features AS pf USING(benchmark, tool, iteration)
                GROUP BY if_1.benchmark, if_1.tool, if_1.iteration
            )
            SELECT if_2.*,
                nullif(count(lf.source_line), 0) AS '#_lines',
                CASE WHEN lf.source_line IS NULL THEN NULL ELSE coalesce(sum(lf.has_EQ), 0) END AS '#_lines_EQ',
                CASE WHEN lf.source_line IS NULL THEN NULL ELSE coalesce(sum(lf.has_NEQ), 0) END AS '#_lines_NEQ',
                CASE WHEN lf.source_line IS NULL THEN NULL ELSE coalesce(sum(lf.has_UNDECIDED), 0) END AS "#_lines_UNDECIDED",
                CASE WHEN lf.source_line IS NULL THEN NULL ELSE coalesce(sum(lf.has_MAYBE_EQ), 0) END AS "#_lines_MAYBE_EQ",
                CASE WHEN lf.source_line IS NULL THEN NULL ELSE coalesce(sum(lf.has_MAYBE_NEQ), 0) END AS "#_lines_MAYBE_NEQ",
                CASE WHEN lf.source_line IS NULL THEN NULL ELSE coalesce(sum(lf.has_UNKNOWN), 0) END AS "#_lines_UNKNOWN",
                CASE WHEN lf.source_line IS NULL THEN NULL ELSE coalesce(sum(lf.has_DEPTH_LIMITED), 0) END AS "#_lines_DEPTH_LIMITED",
                CASE WHEN lf.source_line IS NULL THEN NULL ELSE coalesce(sum(lf.is_non_mixed), 0) END AS '#_lines_non_mixed',
                CASE WHEN lf.source_line IS NULL THEN NULL ELSE coalesce(sum(lf.has_only_EQ), 0) END AS '#_lines_only_EQ',
                CASE WHEN lf.source_line IS NULL THEN NULL ELSE coalesce(sum(lf.has_only_NEQ), 0) END AS '#_lines_only_NEQ',
                CASE WHEN lf.source_line IS NULL THEN NULL ELSE coalesce(sum(lf.has_only_UNDECIDED), 0) END AS "#_lines_only_UNDECIDED",
                CASE WHEN lf.source_line IS NULL THEN NULL ELSE coalesce(sum(lf.is_mixed), 0) END AS '#_lines_mixed',
                CASE WHEN lf.source_line IS NULL THEN NULL ELSE coalesce(sum(lf.is_mixed_EQ_NEQ), 0) END AS '#_lines_mixed_EQ_NEQ',
                CASE WHEN lf.source_line IS NULL THEN NULL ELSE coalesce(sum(lf.is_mixed_EQ_UNDECIDED), 0) END AS "#_lines_mixed_EQ_UNDECIDED",
                CASE WHEN lf.source_line IS NULL THEN NULL ELSE coalesce(sum(lf.is_mixed_NEQ_UNDECIDED), 0) END AS "#_lines_mixed_NEQ_UNDECIDED",
                CASE WHEN lf.source_line IS NULL THEN NULL ELSE coalesce(sum(lf.is_mixed_EQ_NEQ_UNDECIDED), 0) END AS "#_lines_mixed_EQ_NEQ_UNDECIDED"
            FROM i_features_2 AS if_2
            LEFT JOIN mv_line_features AS lf USING(benchmark, tool, iteration)
            GROUP BY if_2.benchmark, if_2.tool, if_2.iteration
        )
        SELECT if_3.*,
            CASE WHEN if_3."#_partitions" IS NULL THEN if_3.result = 'EQ' ELSE if_3."#_partitions_EQ" > 0 END AS has_EQ,
            CASE WHEN if_3."#_partitions" IS NULL THEN if_3.result = 'NEQ' ELSE if_3."#_partitions_NEQ" > 0 END AS has_NEQ,
            CASE WHEN if_3."#_partitions" IS NULL THEN if_3.result != 'EQ' AND if_3.result != 'NEQ' ELSE if_3."#_partitions_UNDECIDED" > 0 END AS has_UNDECIDED,
            CASE WHEN if_3."#_partitions" IS NULL THEN if_3.result = 'EQ' ELSE if_3."#_partitions" = if_3."#_partitions_EQ" END AS has_only_EQ,
            CASE WHEN if_3."#_partitions" IS NULL THEN if_3.result = 'NEQ' ELSE if_3."#_partitions" = if_3."#_partitions_NEQ" END AS has_only_NEQ,
            CASE WHEN if_3."#_partitions" IS NULL THEN if_3.result != 'EQ' AND if_3.result != 'NEQ' ELSE if_3."#_partitions" = if_3."#_partitions_UNDECIDED" END AS has_only_UNDECIDED
        FROM i_features_3 AS if_3
    )
    SELECT if_4.*,
    CASE WHEN if_4."#_partitions_EQ" IS NULL THEN 0 ELSE (
        (
            if_4.is_fully_analyzed
            AND if_4."#_partitions_EQ" > 0
            AND if_4."#_partitions_EQ" < if_4."#_partitions"
        )
        OR
        (
            NOT if_4.is_fully_analyzed
            AND if_4."#_partitions_EQ" > 0
        )
    ) END AS are_partitions_reducible,
    CASE WHEN if_4."#_lines_only_EQ" IS NULL THEN 0 ELSE (
        if_4.is_fully_analyzed
        AND if_4."#_lines_only_EQ" > 0
        AND if_4."#_lines_only_EQ" < if_4."#_lines"
    ) END AS are_lines_reducible,
    ---
    if_4.has_only_EQ OR if_4.has_only_NEQ OR if_4.has_only_UNDECIDED AS is_non_mixed,
    ---
    NOT if_4.has_only_EQ AND NOT if_4.has_only_NEQ AND NOT if_4.has_only_UNDECIDED AS is_mixed,
    (if_4.has_EQ AND if_4.has_NEQ) AND (NOT if_4.has_UNDECIDED) AS is_mixed_EQ_NEQ,
    (if_4.has_EQ AND if_4.has_UNDECIDED) AND (NOT if_4.has_NEQ) AS is_mixed_EQ_UNDECIDED,
    (if_4.has_NEQ AND if_4.has_UNDECIDED) AND (NOT if_4.has_EQ) AS is_mixed_NEQ_UNDECIDED,
    (if_4.has_EQ AND if_4.has_NEQ AND if_4.has_UNDECIDED) AS is_mixed_EQ_NEQ_UNDECIDED,
    ---
    (if_4."#_partitions_EQ" * 1.0 / if_4."#_partitions") * 100 AS '%_partitions_EQ',
    (if_4."#_partitions_NEQ" * 1.0 / if_4."#_partitions") * 100 AS '%_partitions_NEQ',
    (if_4."#_partitions_UNDECIDED" * 1.0 / if_4."#_partitions") * 100 AS "%_partitions_UNDECIDED",
    (if_4."#_partitions_MAYBE_EQ" * 1.0 / if_4."#_partitions") * 100 AS "%_partitions_MAYBE_EQ",
    (if_4."#_partitions_MAYBE_NEQ" * 1.0 / if_4."#_partitions") * 100 AS "%_partitions_MAYBE_NEQ",
    (if_4."#_partitions_UNKNOWN" * 1.0 / if_4."#_partitions") * 100 AS "%_partitions_UNKNOWN",
    (if_4."#_partitions_DEPTH_LIMITED" * 1.0 / if_4."#_partitions") * 100 AS "%_partitions_DEPTH_LIMITED",
    ---
    if_4."#_lines_all_partitions" / "#_partitions" AS '#_lines_per_partition',
    ---
    if_4."#_lines_EQ_partitions" / "#_partitions_EQ" AS '#_lines_per_EQ_partition',
    if_4."#_lines_NEQ_partitions" / "#_partitions_NEQ" AS '#_lines_per_NEQ_partition',
    if_4."#_lines_UNDECIDED_partitions" / "#_partitions_UNDECIDED" AS '#_lines_per_UNDECIDED_partition',
    if_4."#_lines_MAYBE_EQ_partitions" / "#_partitions_MAYBE_EQ" AS '#_lines_per_MAYBE_EQ_partition',
    if_4."#_lines_MAYBE_NEQ_partitions" / "#_partitions_MAYBE_NEQ" AS '#_lines_per_MAYBE_NEQ_partition',
    if_4."#_lines_UNKNOWN_partitions" / "#_partitions_UNKNOWN" AS '#_lines_per_UNKNOWN_partition',
    if_4."#_lines_DEPTH_LIMITED_partitions" / "#_partitions_DEPTH_LIMITED" AS '#_lines_per_DEPTH_LIMITED_partition',
    ---
    (if_4."#_lines_EQ" * 1.0 / if_4."#_lines") * 100 AS '%_lines_EQ',
    (if_4."#_lines_NEQ" * 1.0 / if_4."#_lines") * 100 AS '%_lines_NEQ',
    (if_4."#_lines_UNDECIDED" * 1.0 / if_4."#_lines") * 100 AS "%_lines_UNDECIDED",
    (if_4."#_lines_MAYBE_EQ" * 1.0 / if_4."#_lines") * 100 AS "%_lines_MAYBE_EQ",
    (if_4."#_lines_MAYBE_NEQ" * 1.0 / if_4."#_lines") * 100 AS "%_lines_MAYBE_NEQ",
    (if_4."#_lines_UNKNOWN" * 1.0 / if_4."#_lines") * 100 AS "%_lines_UNKNOWN",
    (if_4."#_lines_DEPTH_LIMITED" * 1.0 / if_4."#_lines") * 100 AS "%_lines_DEPTH_LIMITED",
    (if_4."#_lines_non_mixed" * 1.0 / if_4."#_lines") * 100 AS '%_lines_non_mixed',
    (if_4."#_lines_only_EQ" * 1.0 / if_4."#_lines") * 100 AS '%_lines_only_EQ',
    (if_4."#_lines_only_NEQ" * 1.0 / if_4."#_lines") * 100 AS '%_lines_only_NEQ',
    (if_4."#_lines_only_UNDECIDED" * 1.0 / if_4."#_lines") * 100 AS "%_lines_only_UNDECIDED",
    (if_4."#_lines_mixed" * 1.0 / if_4."#_lines") * 100 AS '%_lines_mixed',
    (if_4."#_lines_mixed_EQ_NEQ" * 1.0 / if_4."#_lines") * 100 AS '%_lines_mixed_EQ_NEQ',
    (if_4."#_lines_mixed_EQ_UNDECIDED" * 1.0 / if_4."#_lines") * 100 AS "%_lines_mixed_EQ_UNDECIDED",
    (if_4."#_lines_mixed_NEQ_UNDECIDED" * 1.0 / if_4."#_lines") * 100 AS "%_lines_mixed_NEQ_UNDECIDED",
    (if_4."#_lines_mixed_EQ_NEQ_UNDECIDED" * 1.0 / if_4."#_lines") * 100 AS "%_lines_mixed_EQ_NEQ_UNDECIDED"
    FROM i_features_4 AS if_4
)
SELECT
    if_5.benchmark,
    if_5.tool,
    if_5.iteration,
    ---
    if_5.has_timed_out,
    if_5.is_depth_limited,
    if_5.has_uif,
    if_5.runtime,
    if_5.errors,
    ---
    if_5.is_base,
    if_5.is_diff,
    ---
    if_5.expected,
    if_5.result,
    ---
    if_5.is_correct,
    if_5.is_incorrect,
    if_5.is_undecided,
    ---
    if_5.is_fully_analyzed,
    ---
    if_5.are_partitions_reducible OR if_5.are_lines_reducible AS is_reducible,
    if_5.are_partitions_reducible,
    if_5.are_lines_reducible,
    ---
    if_5.has_EQ,
    if_5.has_NEQ,
    if_5.has_UNDECIDED,
    ---
    if_5.is_non_mixed,
    if_5.has_only_EQ,
    if_5.has_only_NEQ,
    if_5.has_only_UNDECIDED,
    ---
    if_5.is_mixed,
    if_5.is_mixed_EQ_NEQ,
    if_5.is_mixed_EQ_UNDECIDED,
    if_5.is_mixed_NEQ_UNDECIDED,
    if_5.is_mixed_EQ_NEQ_UNDECIDED,
    ---
    if_5."#_partitions",
    ---
    if_5."#_partitions_EQ",
    if_5."#_partitions_NEQ",
    if_5."#_partitions_UNDECIDED",
    if_5."#_partitions_MAYBE_EQ",
    if_5."#_partitions_MAYBE_NEQ",
    if_5."#_partitions_UNKNOWN",
    if_5."#_partitions_DEPTH_LIMITED",
    ---
    if_5.'%_partitions_EQ',
    if_5.'%_partitions_NEQ',
    if_5."%_partitions_UNDECIDED",
    if_5."%_partitions_MAYBE_EQ",
    if_5."%_partitions_MAYBE_NEQ",
    if_5."%_partitions_UNKNOWN",
    if_5."%_partitions_DEPTH_LIMITED",
    ---
    if_5."#_lines_all_partitions",
    ---
    if_5."#_lines_EQ_partitions",
    if_5."#_lines_NEQ_partitions",
    if_5."#_lines_UNDECIDED_partitions",
    if_5."#_lines_MAYBE_EQ_partitions",
    if_5."#_lines_MAYBE_NEQ_partitions",
    if_5."#_lines_UNKNOWN_partitions",
    if_5."#_lines_DEPTH_LIMITED_partitions",
    ---
    if_5."#_lines_per_partition",
    if_5."#_lines_per_EQ_partition",
    if_5."#_lines_per_NEQ_partition",
    if_5."#_lines_per_UNDECIDED_partition",
    if_5."#_lines_per_MAYBE_EQ_partition",
    if_5."#_lines_per_MAYBE_NEQ_partition",
    if_5."#_lines_per_UNKNOWN_partition",
    if_5."#_lines_per_DEPTH_LIMITED_partition",
    ---
    (if_5."#_lines_per_partition" * 1.0 / "#_lines") * 100 AS '%_line_coverage_per_partition',
    (if_5."#_lines_per_EQ_partition" * 1.0 / "#_lines") * 100 AS '#_line_coverage_per_EQ_partition',
    (if_5."#_lines_per_NEQ_partition" * 1.0 / "#_lines") * 100 AS '#_lines_coverage_per_NEQ_partition',
    (if_5."#_lines_per_UNDECIDED_partition" * 1.0 / "#_lines") * 100 AS '#_line_coverage_per_UNDECIDED_partition',
    (if_5."#_lines_per_MAYBE_EQ_partition" * 1.0 / "#_lines") * 100 AS '#_line_coverage_per_MAYBE_EQ_partition',
    (if_5."#_lines_per_MAYBE_NEQ_partition" * 1.0 / "#_lines") * 100 AS '#_line_coverage_per_MAYBE_NEQ_partition',
    (if_5."#_lines_per_UNKNOWN_partition" * 1.0 / "#_lines") * 100 AS '#_line_coverage_per_UNKNOWN_partition',
    (if_5."#_lines_per_DEPTH_LIMITED_partition" * 1.0 / "#_lines") * 100 AS '#_line_coverage_per_DEPTH_LIMITED_partition',
    ---
    if_5."#_lines",
    ---
    if_5."#_lines_EQ",
    if_5."#_lines_NEQ",
    if_5."#_lines_UNDECIDED",
    if_5."#_lines_non_mixed",
    if_5."#_lines_only_EQ",
    if_5."#_lines_only_NEQ",
    if_5."#_lines_only_UNDECIDED",
    if_5."#_lines_mixed",
    if_5."#_lines_mixed_EQ_NEQ",
    if_5."#_lines_mixed_EQ_UNDECIDED",
    if_5."#_lines_mixed_NEQ_UNDECIDED",
    if_5."#_lines_mixed_EQ_NEQ_UNDECIDED",
    ---
    if_5.'%_lines_EQ',
    if_5.'%_lines_NEQ',
    if_5."%_lines_UNDECIDED",
    if_5.'%_lines_non_mixed',
    if_5.'%_lines_only_EQ',
    if_5.'%_lines_only_NEQ',
    if_5."%_lines_only_UNDECIDED",
    if_5.'%_lines_mixed',
    if_5.'%_lines_mixed_EQ_NEQ',
    if_5."%_lines_mixed_EQ_UNDECIDED",
    if_5."%_lines_mixed_NEQ_UNDECIDED",
    if_5."%_lines_mixed_EQ_NEQ_UNDECIDED"
FROM i_features_5 AS if_5;

INSERT INTO mv_run_features
(
    benchmark,
    tool,
    ---
    has_timed_out,
    is_depth_limited,
    has_uif,
    runtime,
    errors,
    ---
    is_base,
    is_diff,
    ---
    "#_iterations",
    result_iteration,
    ---
    expected,
    result,
    ---
    is_correct,
    is_incorrect,
    is_undecided,
    ---
    is_fully_analyzed,
    ---
    is_reducible,
    are_partitions_reducible,
    are_lines_reducible,
    ---
    has_EQ,
    has_NEQ,
    has_UNDECIDED,
    ---
    is_non_mixed,
    has_only_EQ,
    has_only_NEQ,
    has_only_UNDECIDED,
    ---
    is_mixed,
    is_mixed_EQ_NEQ,
    is_mixed_EQ_UNDECIDED,
    is_mixed_NEQ_UNDECIDED,
    is_mixed_EQ_NEQ_UNDECIDED,
    ---
    "#_partitions",
    ---
    "#_partitions_EQ",
    "#_partitions_NEQ",
    "#_partitions_UNDECIDED",
    "#_partitions_MAYBE_EQ",
    "#_partitions_MAYBE_NEQ",
    "#_partitions_UNKNOWN",
    "#_partitions_DEPTH_LIMITED",
    ---
    "%_partitions_EQ",
    "%_partitions_NEQ",
    "%_partitions_UNDECIDED",
    "%_partitions_MAYBE_EQ",
    "%_partitions_MAYBE_NEQ",
    "%_partitions_UNKNOWN",
    "%_partitions_DEPTH_LIMITED",
    ---
    "#_lines_all_partitions",
    ---
    "#_lines_EQ_partitions",
    "#_lines_NEQ_partitions",
    "#_lines_UNDECIDED_partitions",
    "#_lines_MAYBE_EQ_partitions",
    "#_lines_MAYBE_NEQ_partitions",
    "#_lines_UNKNOWN_partitions",
    "#_lines_DEPTH_LIMITED_partitions",
    ---
    "#_lines_per_partition",
    "#_lines_per_EQ_partition",
    "#_lines_per_NEQ_partition",
    "#_lines_per_UNDECIDED_partition",
    "#_lines_per_MAYBE_EQ_partition",
    "#_lines_per_MAYBE_NEQ_partition",
    "#_lines_per_UNKNOWN_partition",
    "#_lines_per_DEPTH_LIMITED_partition",
    ---
    "%_line_coverage_per_partition",
    "%_line_coverage_per_EQ_partition",
    "%_line_coverage_per_NEQ_partition",
    "%_line_coverage_per_UNDECIDED_partition",
    "%_line_coverage_per_MAYBE_EQ_partition",
    "%_line_coverage_per_MAYBE_NEQ_partition",
    "%_line_coverage_per_UNKNOWN_partition",
    "%_line_coverage_per_DEPTH_LIMITED_partition",
    ---
    "#_lines",
    ---
    "#_lines_EQ",
    "#_lines_NEQ",
    "#_lines_UNDECIDED",
    "#_lines_non_mixed",
    "#_lines_only_EQ",
    "#_lines_only_NEQ",
    "#_lines_only_UNDECIDED",
    "#_lines_mixed",
    "#_lines_mixed_EQ_NEQ",
    "#_lines_mixed_EQ_UNDECIDED",
    "#_lines_mixed_NEQ_UNDECIDED",
    "#_lines_mixed_EQ_NEQ_UNDECIDED",
    ---
    "%_lines_EQ",
    "%_lines_NEQ",
    "%_lines_UNDECIDED",
    "%_lines_non_mixed",
    "%_lines_only_EQ",
    "%_lines_only_NEQ",
    "%_lines_only_UNDECIDED",
    "%_lines_mixed",
    "%_lines_mixed_EQ_NEQ",
    "%_lines_mixed_EQ_UNDECIDED",
    "%_lines_mixed_NEQ_UNDECIDED",
    "%_lines_mixed_EQ_NEQ_UNDECIDED"
)
SELECT
    r.benchmark,
    r.tool,
    ---
    r.has_timed_out,
    r.is_depth_limited,
    r.has_uif,
    r.runtime,
    r.errors,
    ---
    r.tool LIKE '%-base' AS is_base,
    r.tool LIKE '%-diff' AS is_diff,
    ---
    r.iteration_count,
    r.result_iteration,
    ---
    b.expected,
    r.result,
    ---
    i.is_correct,
    i.is_incorrect,
    i.is_undecided,
    ---
    i.is_fully_analyzed,
    ---
    i.is_reducible,
    i.are_partitions_reducible,
    i.are_lines_reducible,
    ---
    i.has_EQ,
    i.has_NEQ,
    i.has_UNDECIDED,
    ---
    i.is_non_mixed,
    i.has_only_EQ,
    i.has_only_NEQ,
    i.has_only_UNDECIDED,
    ---
    i.is_mixed,
    i.is_mixed_EQ_NEQ,
    i.is_mixed_EQ_UNDECIDED,
    i.is_mixed_NEQ_UNDECIDED,
    i.is_mixed_EQ_NEQ_UNDECIDED,
    ---
    i."#_partitions",
    ---
    i."#_partitions_EQ",
    i."#_partitions_NEQ",
    i."#_partitions_UNDECIDED",
    i."#_partitions_MAYBE_EQ",
    i."#_partitions_MAYBE_NEQ",
    i."#_partitions_UNKNOWN",
    i."#_partitions_DEPTH_LIMITED",
    ---
    i."%_partitions_EQ",
    i."%_partitions_NEQ",
    i."%_partitions_UNDECIDED",
    i."%_partitions_MAYBE_EQ",
    i."%_partitions_MAYBE_NEQ",
    i."%_partitions_UNKNOWN",
    i."%_partitions_DEPTH_LIMITED",
    ---
    i."#_lines_all_partitions",
    ---
    i."#_lines_EQ_partitions",
    i."#_lines_NEQ_partitions",
    i."#_lines_UNDECIDED_partitions",
    i."#_lines_MAYBE_EQ_partitions",
    i."#_lines_MAYBE_NEQ_partitions",
    i."#_lines_UNKNOWN_partitions",
    i."#_lines_DEPTH_LIMITED_partitions",
    ---
    i."#_lines_per_partition",
    i."#_lines_per_EQ_partition",
    i."#_lines_per_NEQ_partition",
    i."#_lines_per_UNDECIDED_partition",
    i."#_lines_per_MAYBE_EQ_partition",
    i."#_lines_per_MAYBE_NEQ_partition",
    i."#_lines_per_UNKNOWN_partition",
    i."#_lines_per_DEPTH_LIMITED_partition",
    ---
    i."%_line_coverage_per_partition",
    i."%_line_coverage_per_EQ_partition",
    i."%_line_coverage_per_NEQ_partition",
    i."%_line_coverage_per_UNDECIDED_partition",
    i."%_line_coverage_per_MAYBE_EQ_partition",
    i."%_line_coverage_per_MAYBE_NEQ_partition",
    i."%_line_coverage_per_UNKNOWN_partition",
    i."%_line_coverage_per_DEPTH_LIMITED_partition",
    ---
    i."#_lines",
    ---
    i."#_lines_EQ",
    i."#_lines_NEQ",
    i."#_lines_UNDECIDED",
    i."#_lines_non_mixed",
    i."#_lines_only_EQ",
    i."#_lines_only_NEQ",
    i."#_lines_only_UNDECIDED",
    i."#_lines_mixed",
    i."#_lines_mixed_EQ_NEQ",
    i."#_lines_mixed_EQ_UNDECIDED",
    i."#_lines_mixed_NEQ_UNDECIDED",
    i."#_lines_mixed_EQ_NEQ_UNDECIDED",
    ---
    i."%_lines_EQ",
    i."%_lines_NEQ",
    i."%_lines_UNDECIDED",
    i."%_lines_non_mixed",
    i."%_lines_only_EQ",
    i."%_lines_only_NEQ",
    i."%_lines_only_UNDECIDED",
    i."%_lines_mixed",
    i."%_lines_mixed_EQ_NEQ",
    i."%_lines_mixed_EQ_UNDECIDED",
    i."%_lines_mixed_NEQ_UNDECIDED",
    i."%_lines_mixed_EQ_NEQ_UNDECIDED"
FROM run AS r
INNER JOIN benchmark b on r.benchmark = b.benchmark
INNER JOIN mv_iteration_features AS i ON r.benchmark = i.benchmark and r.tool = i.tool AND r.result_iteration = i.iteration;
