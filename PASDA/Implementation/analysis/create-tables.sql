-- DROP TABLE IF EXISTS partition_instruction;
-- DROP TABLE IF EXISTS instruction;
-- DROP TABLE IF EXISTS partition;
-- DROP TABLE IF EXISTS iteration;
-- DROP TABLE IF EXISTS runtime;
-- DROP TABLE IF EXISTS run;
-- DROP TABLE IF EXISTS benchmark;

CREATE TABLE IF NOT EXISTS benchmark
(
    benchmark TEXT NOT NULL,

    expected TEXT,

    PRIMARY KEY (benchmark)
);

CREATE TABLE IF NOT EXISTS run
(
    id INTEGER NOT NULL,

    benchmark TEXT NOT NULL,

    result TEXT,
    has_timed_out BOOLEAN,
    is_depth_limited BOOLEAN,
    has_uif BOOLEAN,
    iteration_count INTEGER,
    result_iteration INTEGER,
    runtime REAL,
    errors TEXT,

    PRIMARY KEY (id),
    FOREIGN KEY (benchmark) REFERENCES benchmark(benchmark) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS run__benchmark ON run (benchmark);
CREATE INDEX IF NOT EXISTS run__result ON run (result);

CREATE TABLE IF NOT EXISTS settings
(
    run_id INTEGER NOT NULL,

    tool TEXT NOT NULL,

    run_timeout INT NOT NULL,
    iteration_timeout INT NOT NULL,
    solver_timeout INT NOT NULL,

    depth_limit INT NOT NULL,

    PRIMARY KEY (run_id),
    FOREIGN KEY (run_id) REFERENCES run(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS runtime
(
    id INTEGER NOT NULL,

    run_id INTEGER NOT NULL,

    topic TEXT NOT NULL,
    task TEXT NOT NULL,
    runtime REAL NOT NULL,
    step INTEGER NOT NULL,
    is_missing BOOLEAN NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (run_id) REFERENCES run(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS runtime__run_id ON runtime (run_id);

CREATE TABLE IF NOT EXISTS iteration
(
    id INTEGER NOT NULL,

    run_id INTEGER NOT NULL,

    iteration INTEGER NOT NULL,
    result TEXT,
    has_timed_out BOOLEAN,
    is_depth_limited BOOLEAN,
    has_uif BOOLEAN,
    partition_count INTEGER,
    runtime REAL,
    errors TEXT,

    PRIMARY KEY (id),
    FOREIGN KEY (run_id) REFERENCES run(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS iteration__run_id ON iteration (run_id);
CREATE INDEX IF NOT EXISTS iteration__result ON iteration (result);

CREATE TABLE IF NOT EXISTS partition
(
    id INTEGER NOT NULL,

    iteration_id INTEGER NOT NULL,

    partition INTEGER NOT NULL,
    result TEXT,
    pc_status INTEGER,
    pc_model TEXT,
    pc_reason_unknown TEXT,
    pc_statistics TEXT,
    neq_status INTEGER,
    neq_model TEXT,
    neq_result_v1 TEXT,
    neq_result_v2 TEXT,
    neq_reason_unknown TEXT,
    neq_statistics TEXT,
    eq_status INTEGER,
    eq_model TEXT,
    eq_result_v1 TEXT,
    eq_result_v2 TEXT,
    eq_reason_unknown TEXT,
    eq_statistics TEXT,
    has_uif BOOLEAN,
    has_uif_pc BOOLEAN,
    has_uif_v1 BOOLEAN,
    has_uif_v2 BOOLEAN,
    constraint_count INTEGER,
    runtime REAL,
    errors TEXT,

    PRIMARY KEY (id),
    FOREIGN KEY (iteration_id) REFERENCES iteration(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS partition__iteration_id ON partition (iteration_id);
CREATE INDEX IF NOT EXISTS partition__result ON partition (result);

CREATE TABLE IF NOT EXISTS instruction
(
    id INTEGER NOT NULL,

    iteration_id INTEGER NOT NULL,

    method TEXT NOT NULL,
    instruction_index INTEGER NOT NULL,
    instruction TEXT,
    position INTEGER,
    source_file TEXT,
    source_line INTEGER,

    PRIMARY KEY (id),
    FOREIGN KEY (iteration_id) REFERENCES iteration(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS instruction__iteration_id ON instruction (iteration_id);

CREATE TABLE IF NOT EXISTS partition_instruction
(
    id INTEGER NOT NULL,

    partition_id INTEGER NOT NULL,
    instruction_id INTEGER NOT NULL,

    version INTEGER NOT NULL,
    execution_index INTEGER NOT NULL,

    state INTEGER,
    choice INTEGER,

    PRIMARY KEY (id),
    FOREIGN KEY (partition_id) REFERENCES partition(id) ON DELETE CASCADE,
    FOREIGN KEY (instruction_id) REFERENCES instruction(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS partition_instruction__partition_id ON partition_instruction (partition_id);
CREATE INDEX IF NOT EXISTS partition_instruction__instruction_id ON partition_instruction (instruction_id);
