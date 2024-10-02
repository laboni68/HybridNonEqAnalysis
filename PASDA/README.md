## Contents

This (https://zenodo.org/doi/10.5281/zenodo.7595851) is a replication package for the paper:  
PASDA: A Partition-Based Semantic Differencing Approach with Best Effort Classification of Undecided Cases

The replication package contains:

1. our implementations of the four evaluated tools (PASDA, ARDiff, DSE, and PRV),
2. the 141 benchmark cases (73 equivalent, 68 non-equivalent) from the ARDiff benchmark,
3. the scripts that we used to collect and analyze the evaluation data for the four tools,
4. all data (raw and processed) collected during the evaluation.

The corresponding GitHub repository (which contains 1-3, but does not contain 4) can be found at:  
https://github.com/glockyco/PASDA

---

## Usage Instructions

The following instructions assume that you have a working Docker installation.
If you don't have Docker installed, you can get it at https://docs.docker.com/engine/install/.

### Viewing the Collected Data

1. Extract the `PASDA.zip` file.

2. Start Adminer (https://www.adminer.org/) by running the following command in the root directory of PASDA (the directory that contains the `compose.yml` file):

    ```
    docker-compose up adminer
    ```

3. Access Adminer in your web browser at: http://localhost:18080/?sqlite=&username=pasda

4. Enter "pasda" in the "Password" field.

5. Enter "/pasda/results.db" in the "Database" field.

6. Click "Login".

7. Wait until the page has fully loaded. This might take a minute or two. Loading is finished when a sidebar appears on the left side of the website.

8. Start exploring the data by clicking on one of the "select"s in the sidebar.

Database tables with a prefix of `__paper__table__` are included in the paper as tables. For example, `__paper__table__runtime_per_step` holds the data of Table 10. 

Database tables with a prefix of `__paper__stats__` contain evaluation results that are referenced in the main text of the paper but don't have a dedicated table in the paper. For example `__paper__stats__tool_result_comparison` supports the claim that "at the 300 s timeout, 44 of the 73 EQ cases are correctly classified by both tools, whereas 3 are only correctly classified by PASDA and 4 by ARDiff."

The `results.db` file is a standard SQLite database file, so other tools / viewers than Adminer that support such files can also be used to explore the data.


### Running the Benchmark / Data Collection

1. Extract the `PASDA.zip` file.

2. Start Adminer (https://www.adminer.org/) and the benchmark by running the following command in the root directory of PASDA (the directory that contains the `compose.yml` file):

    ```
    docker-compose up
    ```

3. Access Adminer in your web browser at: http://localhost:18080/?sqlite=&username=pasda

4. Enter "pasda" in the "Password" field.

5. Enter "/pasda/sqlite.db" in the "Database" field.

6. Click "Login".

7. Start exploring the data by clicking on one of the "select"s in the sidebar.

The collected data will be incomplete until the benchmark has been fully executed. For example, the `__paper__` tables are only created once all tools have finished their analysis.

By default, only PASDA and ARDiff are executed once for every benchmark case with a timeout of 10 s. With these settings, execution should finish within ca. 30 min to a few hours.  Actual execution times highly depend on your hardware and how many resources you have allocated to Docker. 

To include additional tools and timeouts in the analysis, or to execute multiple runs for each configuration, modify the the corresponding settings in `Implementation/RunningBenchmarks.sh` before (re-)starting the Docker containers. The tools that can be selected are (PASDA-base is not fully implemented): 

| Internal Name | Name in Paper | Type            | UIFs | UIF Refinement | Initial Non-Abstracted Iteration |
|---------------|---------------|-----------------|------|----------------|----------------------------------|
| PASDA-diff    | PASDA         | partition-based | yes  | yes            | yes                              |
| PASDA-base    | -             | summary-based   | yes  | yes            | yes                              |
| ARDiff-diff   | -             | partition-based | yes  | yes            | no                               |
| ARDiff-base   | ARDiff        | summary-based   | yes  | yes            | no                               |
| DSE-diff      | -             | partition-based | yes  | no             | no                               |
| DSE-base      | DSE           | summary-based   | yes  | no             | no                               |
| SE-diff       | PRV           | partition-based | no   | no             | no                               |
| SE-base       | -             | summary-based   | no   | no             | no                               |

Running Java in AMD64 Docker containers on Apple Silicon (i.e., M1/M2/... Macs) can be quite finnicky at times (see, e.g. https://github.com/docker/for-mac/issues/7006). To run the benchmark without Docker, you need to have the following installed: a Java 8 JDK (e.g., https://openjdk.org/install/) that has to be set as the system-wide default JDK, and the sqlean shell (https://github.com/nalgeon/sqlean/blob/main/docs/shell.md), which needs to be aliased as `sqlite3` on the machine. With those two installed, execute the `Implementation/RunningBenchmarks.sh` file to start the benchmark. Collected data can then be viewed as usual by accessing the "/pasda/sqlite.db" database through Adminer.

### Exploring the Implementation

PASDAs implementation can be found in `Implementation/src/main/java/differencing`.

The entrypoint for PASDAs execution is the `DifferencingRunner` class. Other interesting classes / methods include:

- `DifferencingRunner.createDifferencingDriverClass`: creates the product program,
- `ExecutionListener`: collects coverage information,
- `DifferencingListener`: collects partition-effects pairs and triggers equivalence checking,
- `Partition-/Iteration-/RunClassifier`: calculates equivalence classifications from equivalence checking results.

Code outside the `differencing` namespace belongs to the modified / fixed ARDiff implementation.

---

## ARDiff Bugs and Fixes

While working with ARDiff, we identified two implementation bugs that cause ARDiff to report false positive NEQ results for a small subset of programs. In the following, we describe these bugs and how we fixed them. The example that we use to demonstrate the bugs is Listing 1 from the paper:

```
double eq_v1(int x, double y) {
  for (int i = 0; i < 1; i++) { x += 0; }
  return x + y + 1;
}

double eq_v2(int x, double y) {
  for (int i = 0; i < x; i++) { x *= 1; }
  return 1 + x + y;
}
```

The original ARDiff implementations by Badihi et al. without our modifications can be found at:  
https://github.com/resess/ARDiff

### Bug 1: Handling of Type Casts

Whenever a symbolic integer value is explicitly or implicitly cast to a double value during the symbolic execution, SPF introduces a new variable REAL_X to hold the result of the type cast. X, in this case, is a positive integer value that starts at 1 and is incremented after each type cast. Therefore, REAL_1, REAL_2, etc., uniquely identify the results of different type casts that were performed during the symbolic execution of a program (for type casts from double to int, the introduced variables are called INT_1, INT_2, etc.). While the introduction of these variables is not problematic on its own, the way in which ARDiff constructs solver queries does not correctly account for the presence of these variables. For example, if the loops in line 2 of eq_v1 and line 7 of eq_v2 were removed, ARDiff would construct the following Z3 query to check whether the values returned from lines 3 and 8 are non-equivalent:

```
...

(define-fun eq_v1 () Bool (and
  (= REAL_1 (to_real x))       // => REAL_1 = x
  (= Ret (+ REAL_1 y 1.0))))

(define-fun eq_v2 () Bool (and
  (= REAL_1 (to_real (+ x 1))) // => REAL_1 = x + 1
  (= Ret (+ REAL_1 y))))

(assert eq_v1)                 // Assert v1, but not v2.
(assert (not (= eq_v1 eq_v2))) // Check non-equivalence.
(check-sat)
(get-model)
```

When sending this query to Z3, it identifies the query to be satisfiable, which indicates that the two programs are non-equivalent. Z3 returns the following model to demonstrate satisfiability:

```
(model                            
  (define-fun x () Int 0)
  (define-fun y () Real 0.0)
  (define-fun REAL_1 () Real 0.0) // REAL_1  = x, but REAL_1 != x + 1
  (define-fun Ret () Real 1.0))
```

Closer inspection of the model and query reveals that the model does satisfy the query but does not satisfy eq_v2 (REAL_1 should be 1 in eq_v2’s case). This is because the used query only asserts that eq_v1 must hold, but does not assert that eq_v2 must hold. More generally speaking, ARDiff produces false positive NEQ results for equivalent target programs if (i) the same REAL_X variable is used to represent different values in the two target programs or (ii) the second program produces more REAL_X variables than the first one. In both of these cases, the query that is used by ARDiff to identify non-equivalence allows models that do not match the method summary of the second program.

### Bug 2: Handling of Incomplete Summaries

The summaries that are collected via symbolic execution are incomplete if any of the program paths are not fully explored due to the configured depth limit. Equivalence of two incomplete summaries should be checked based on the partitions that are present in both summaries, whereas partitions that are only present in one of the summaries should be excluded from the equivalence check. ARDiff, however, always checks equivalence based on all partitions that are present in the two incomplete summaries. This causes spurious NEQ results for programs for which different partitions are depth-limited. For example, ARDiff would report eq_v1 and eq_v2 as NEQ even if their return statements were syntactically equivalent because eq_v1 is never depth-limited whereas eq_v2 is depth-limited if 𝑥 is greater than the configured depth limit.

### Implemented Fixes

To fix the described bugs, we modified the Z3 queries that are used by ARDiff to check non-/equivalence of the two target programs. For example, the code listing below shows the new query that is used to check non-equivalence of eq_v1 and eq_v2 for a depth limit of 10. With this new query, ARDiff correctly identifies the two programs as MAYBE_EQ (i.e., EQ up to the depth limit) rather than NEQ. The changes that we applied to ARDiff’s queries are as follows. (1) REAL_X variables of the two summaries now use a shared index variable X. This prevents name conflicts across the two programs. (2) The summaries of both target programs are asserted. This ensures that only models that satisfy both summaries are valid, and that only partitions covered by both summaries are considered by the check. (3) The two summaries use different Ret variables Ret_1 and Ret_2. This is necessary because asserting both summaries would otherwise force the overall query to be UNSAT.

```
...

(define-fun eq_v1 () Bool (and
  (= REAL_1 (to_real x))
  (= Ret_1 (+ y 1.0 REAL_1))))

(define-fun eq_v2 () Bool (and
  (< x 11)
  (= REAL_2 (to_real (+ x 1)))  // Change (1): Different REAL_X indices.
  (= Ret_2 (+ REAL_2 y))))

(assert eq_v1)
(assert eq_v2)                  // Change (2): Both summaries asserted.
(assert (not (= Ret_1 Ret_2)))  // Change (3): Different Ret variables.
(check-sat)
(get-model)
```
