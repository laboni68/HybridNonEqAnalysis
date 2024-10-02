#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

DB_PATH="${SCRIPT_DIR}/analysis/results/sqlite.db"
DB_CREATE_TABLES_PATH="${SCRIPT_DIR}/analysis/create-tables.sql"
DB_CREATE_MATERIALIZED_VIEWS_PATH="${SCRIPT_DIR}/analysis/create-materialized-views.sql"
DB_CREATE_TABLES_PAPER_PATH="${SCRIPT_DIR}/analysis/create-tables-paper.sql"

BASE_JAR_PATH="${SCRIPT_DIR}/build/libs/ARDiff-base-1.0-SNAPSHOT-all.jar"
DIFF_JAR_PATH="${SCRIPT_DIR}/build/libs/ARDiff-diff-1.0-SNAPSHOT-all.jar"

dry_run=false

clean_db=false
force_build=false
print_commands=true

depth_limits=(
  "10"
)

timeouts=(
 # "10"
#  "30"
#  "90"
"97"
#  "300"
#  "900"
#  "3600"
)

fuzztime=$1 #500

baseFile="ResultBase.txt"
fuzzType=$2 #uniform or boundary
if [ "$fuzzType" == "uniform" ]; then
  resultFile="ResultFuzz"$fuzztime".txt"
else
  resultFile="ResultPrePostFuzzD1097Changed"$fuzztime".txt"
  # resultFile="ResultBFuzz"$fuzztime".txt"
fi

runs=1

benchmarks=(
# int and double cases
# "Eq/EVEN_ODD_PALINDROME"
"Eq/SUM_TO_N" 
"Eq/ROUNDED_AVG"
"Eq/LARGEST_DIVISOR"
"Eq/IS_SIMPLE_POWER"
# "Eq/TRI"
# "Eq/FACTORIAL"
# "Eq/DECIMAL_TO_BINARY"
# "Eq/DIGITS"
# "Eq/SKJKASDKD"
"Eq/CHOOSE_NUM"
# "Eq/EAT"
"Eq/IS_MULTIPLY_PRIME"
# "Eq/IS_EQUAL_TO_SUM_EVEN"
# "Eq/COUNT_UP_TO"
"Eq/TRUNCATE_NUMBER"
"Eq/MULTIPLY"
"Eq/X_OR_Y"
"Eq/GET_MAX_TRIPLES"
"Eq/MODP"
"Eq/ISCUBE"
# "Eq/GET_ODD_COLLATZ"
# "Eq/SOLVE"
"Eq/ADD"
# "Eq/PRIME_FIB"
# "Eq/FIBFIB"
# "Eq/STARTS_ONE_ENDS"
# "Eq/LARGEST_PRIME_FACTOR"
# "Eq/LARGEST_PRIME_FACTOR"
# "Eq/FIB4"
# "Eq/GREATEST_COMMON_DIVISOR"
# "Eq/CIRCULAR_SHIFT"
# "Eq/TRIANGLE_AREA_2"
# "Eq/STRING_SEQUENCE"
# "Eq/CHANGE_BASE"
# "Eq/INT_TO_MINI_ROMAN"
# "Eq/CAR_RACE_COLLISION"
# "Eq/FACTORIZE"
# "Eq/TRIANGLE_AREA"
# "Eq/GENERATE_INTEGERS"
# "Eq/MAKE_A_PILE"
# "Eq/IS_PRIME"
# "Eq/SORT_ARRAY_BINARY"
# "Eq/FIZZ_BUZZ"
"Eq/RIGHT_ANGLE_TRIANGLE"
"Eq/ANY_INT"
# "Eq/FIB"
# "Eq/EVEN_ODD_COUNT"
# "Eq/INTERSECTION"

 ##All cases 
# "Eq/MEDIAN"
# "Eq/WORDS_IN_SENTENCE"
# "Eq/REMOVE_DUPLICATES"
# "Eq/WORDS_STRINGS"
# "Eq/COMMON"
# "Eq/CORRECT_BRACKETING"
# "Eq/ROUNDED_AVG"
# "Eq/SIMPLIFY"
# "Eq/CYCPATTERN_CHECK"
# "Eq/IS_BORED"
# "Eq/SEARCH"
#"Eq/BY_LENGTH"
# "Eq/DECODE_CYCLIC"
# "Eq/SUM_PRODUCT"
# "Eq/CONCATENATE"
# "Eq/REMOVE_VOWELS"
# "Eq/CAN_ARRANGE"
# "Eq/VALID_DATE"
# "Eq/MATCH_PARENS"
# "Eq/COUNT_NUMS"
# "Eq/MOVE_ONE_BALL"
# "Eq/PRIME_FIB"
# "Eq/MAX_FILL"
# "Eq/SOLVE"
# "Eq/PAIRS_SUM_TO_ZERO"
# "Eq/DECIMAL_TO_BINARY"
# "Eq/COMPARE_ONE"
# "Eq/MIN_SUBARRAY_SUM"
# "Eq/SORT_ARRAY_BINARY"
# "Eq/FILE_NAME_CHECK"
# "Eq/ADD"
# "Eq/FACTORIZE"
# "Eq/INTERSPERSE"
# "Eq/CHOOSE_NUM"
# "Eq/TRI"
# "Eq/IS_SIMPLE_POWER"
# "Eq/ENCRYPT"
# "Eq/TOTAL_MATCH"
# "Eq/STRING_TO_MD5"
# "Eq/GET_CLOSET_VOWEL"
# "Eq/FIND_MAX"
# "Eq/LARGEST_DIVISOR"
# "Eq/SORTED_LIST_SUM"
# "Eq/FILTER_INTEGERS"
# "Eq/PARSE_NESTED_PARENS"
# "Eq/BELOW_ZERO"
# "Eq/GET_ODD_COLLATZ"
# "Eq/ISCUBE"
# "Eq/SOLVE_STRING"
# "Eq/INCR_LIST"
# "Eq/RESCALE_TO_UNIT"
# "Eq/DIGIT_SUM"
#  "Eq/X_OR_Y"
# "Eq/CLOSEST_INTEGER"
# "Eq/IS_PRIME"
# "Eq/ROLLING_MAX"
# "Eq/HOW_MANY_TIMES"
# "Eq/DIGITS"
# "Eq/SORT_NUMBERS"
# "Eq/ODD_COUNT"
# "Eq/TRIPLES_SUM_TO_ZERO"
# "Eq/EVEN_ODD_PALINDROME"
# "Eq/TRIANGLE_AREA"
# "Eq/DOUBLE_THE_DIFFERENCE"
# "Eq/IS_NESTED"
# "Eq/STRLEN"
# "Eq/FILTER_BY_PREFIX"
# "Eq/SPECIAL_FACTORIAL"
# "Eq/ANTI_SHUFFLE"
# "Eq/FIZZ_BUZZ"
# "Eq/CAR_RACE_COLLISION"
# "Eq/LONGEST"
# "Eq/IS_SORTED"
# "Eq/INTERSECTION"
# "Eq/COUNT_UP_TO"
# "Eq/FACTORIAL"
# "Eq/SELECT_WORDS"
# "Eq/EVEN_ODD_COUNT"
# "Eq/MIN_PATH"
# "Eq/IS_PALINDROME"
# "Eq/GET_MAX_TRIPLES"
# "Eq/STRANGE_SORT_LIST"
# "Eq/GENERATE_INTEGERS"
# "Eq/PLUCK"
# "Eq/PARSE_MUSIC"
# "Eq/FIND_ZERO"
# "Eq/FRUIT_DISTRIBUTION"
# "Eq/FIBFIB"
# "Eq/GREATEST_COMMON_DIVISOR"
# "Eq/FIX_SPACES"
# "Eq/SPLIT_WORDS"
# "Eq/MAX_ELEMENT"
# "Eq/EXCHANGE"
# "Eq/COUNT_DISTINCT_CHARACTERS"
# "Eq/HISTOGRAM"
# "Eq/MAKE_PALINDROME"
# "Eq/IS_MULTIPLY_PRIME"
# "Eq/DECODE_SHIFT"
# "Eq/ADD_EVEN_AT_ODD"
# "Eq/SOLUTION"
# "Eq/ENCODE"
# "Eq/IS_HAPPY"
# "Eq/ORDER_BY_POINTS"
# "Eq/NUMERICAL_LETTER_GRADE"
# "Eq/BELOW_THRESHOLD"
# "Eq/RIGHT_ANGLE_TRIANGLE"
# "Eq/MEAN_ABSOLUTE_DEVIATION"
# "Eq/HAS_CLOSE_ELEMENTS"
# "Eq/PROD_SIGNS"
# "Eq/MODP"
# "Eq/CHECK_IF_LAST_CHAR_IS_A_LETTER"
# "Eq/MULTIPLY"
# "Eq/MONOTONIC"
# "Eq/SORT_THIRD"
# "Eq/CHECK_DICT_CASE"
# "Eq/FIND_CLOSEST_ELEMENTS"
# "Eq/COUNT_UPPER"
# "Eq/HEX_KEY"
# "Eq/ADD_ELEMENTS"
# "Eq/SPECIAL_FILTER"
# "Eq/EAT"
# "Eq/WILL_IT_FLY"
# "Eq/CIRCULAR_SHIFT"
# "Eq/GET_ROW"
# "Eq/LARGEST_SMALLEST_INTEGERS"
# "Eq/CHANGE_BASE"
# "Eq/ANY_INT"
# "Eq/LARGEST_PRIME_FACTOR"
# "Eq/SKJKASDKD"
# "Eq/UNIQUE_DIGITS"
# "Eq/GET_POSITIVE"
# "Eq/SAME_CHARS"
# "Eq/FILTER_BY_SUBSTRING"
# "Eq/IS_EQUAL_TO_SUM_EVEN"
# "Eq/SUM_TO_N"
# "Eq/STRONGEST_EXTENSION"
# "Eq/DO_ALGEBRA"
# "Eq/REVERSE_DELETE"
# "Eq/SORT_EVEN"
# "Eq/STRING_XOR"
# "Eq/SMALLEST_CHANGE"
# "Eq/STARTS_ONE_ENDS"
# "Eq/BF"
# "Eq/FIB"
#"Eq/TRUNCATE_NUMBER"
# "Eq/VOWELS_COUNT"
# "Eq/SUM_SQUARED_NUMS"
# "Eq/STRING_SEQUENCE"
# "Eq/SUM_SQUARES"
# "Eq/MAKE_A_PILE"
# "Eq/PRIME_LENGTH"
# "Eq/INT_TO_MINI_ROMAN"
# "Eq/FIB4"
# "Eq/DERIVATIVE"
# "Eq/UNIQUE"
# "Eq/SEPARATE_PAREN_GROUPS"
# "Eq/SORT_ARRAY"
# "Eq/TRIANGLE_AREA_2"
# "Eq/COMPARE"
# "Eq/NEXT_SMALLEST"
#  "Eq/MAXIMUM_K"
#  "Eq/FLIP_CASE"
# "Eq/ALL_PREFIXES"
)

tools=(
 "Hybrid-base"
 "Hybrid-diff"   # Hybrid
#  "ARDiff-base"  # ARDiff
#  "ARDiff-diff"
 # "DSE-base"     # DSE
 # "DSE-diff"
 # "SE-base"
 # "SE-diff"      # PRV
)

newline=$'\n'
runs_settings=()
for depth_limit in "${depth_limits[@]}"; do
  for timeout in "${timeouts[@]}"; do
    for ((count = 1; count <= runs; count++)); do
      for benchmark in "${benchmarks[@]}"; do
        for tool in "${tools[@]}"; do
          runs_settings+=("${benchmark},${tool},${timeout},${depth_limit}${newline}")
        done
      done
    done
  done
done

# To execute a specific set of benchmark:tool:timeout:depth-limit combinations,
# uncomment the following lines and add the corresponding configuration settings.
# This simply overwrites the 'run_settings' variable, so everything above can remain unchanged.
#IFS=$'\n' read -r -d '' -a runs_settings <<< "Ell/brent/Eq,ARDiff-base,30,10
#Ell/brent/Eq,ARDiff-base,90,10
#Ell/brent/Eq,ARDiff-base,90,10"

#(IFS=""; echo -e  "${runs_settings[*]}" > "runs_settings.txt")

calculate_time() {
    local start_time=$1
    local elapsed_seconds=$((SECONDS - start_time))
    local days=$((elapsed_seconds / (60 * 60 * 24)))
    local hours=$((elapsed_seconds / (60 * 60) % 24))
    local minutes=$((elapsed_seconds / 60 % 60))
    local seconds=$((elapsed_seconds % 60))

    local total_runs=$2
    local current_run=$3

    local total_seconds=$((elapsed_seconds * total_runs / current_run))
    local remaining_seconds=$((total_seconds - elapsed_seconds))

    local remaining_days=$((remaining_seconds / (60 * 60 * 24)))
    local remaining_hours=$((remaining_seconds / (60 * 60) % 24))
    local remaining_minutes=$((remaining_seconds / 60 % 60))
    local remaining_seconds=$((remaining_seconds % 60))

    printf "Elapsed time:             %02d days %02d hours %02d minutes %02d seconds\n" "$days" "$hours" "$minutes" "$seconds"
    printf "Estimated remaining time: %02d days %02d hours %02d minutes %02d seconds\n" "$remaining_days" "$remaining_hours" "$remaining_minutes" "$remaining_seconds"
}

# Set up the database

if [ "$clean_db" = true ] ; then
  rm "${DB_PATH}"
fi

if [ ! -f "${DB_PATH}" ]; then
  touch "${DB_PATH}"
  sqlite3 "${DB_PATH}" < "${DB_CREATE_TABLES_PATH}" > /dev/null
fi

# Build the application JAR files

if [ "$force_build" = true ]  || [ ! -f "$BASE_JAR_PATH" ] ; then
  printf "Building base JAR file ..."

  # Build base JAR
  command="./gradlew -PmainClass=Runner.Runner shadowJar"

  if [ "$print_commands" = true ] ; then
    printf "\n%s" "${command}"
  fi

  if [ "$dry_run" = false ] ; then
    eval "${command}"
  fi

  printf "\n"
fi

if [ "$force_build" = true ]  || [ ! -f "$DIFF_JAR_PATH" ] ; then
  printf "Building diff JAR file ..."

  # Build diff JAR
  command="./gradlew -PmainClass=differencing.DifferencingRunner shadowJar"

  if [ "$print_commands" = true ] ; then
    printf "\n%s" "${command}"
  fi

  if [ "$dry_run" = false ] ; then
    eval "${command}"
  fi

  printf "\n"
fi

# Process the benchmark programs

seconds_at_start=$SECONDS

current_run=1
total_runs=${#runs_settings[@]}

for run_settings in "${runs_settings[@]}"; do
  IFS=',' read -r benchmark tool timeout depth_limit <<< "$run_settings"
  echo "[$(date +"%Y-%m-%d %T")] Run $((current_run++)) of ${total_runs} - Benchmark: ${benchmark}, Tool: ${tool}, Timeout: ${timeout}, Depth-Limit: ${depth_limit}"

  directory="../benchmarksHumaneval/${benchmark}"
  if [ ! -d "${directory}" ]; then
    echo "ERROR: The directory '${directory}' does not exist."
    continue
  fi

  oldV="${directory}/oldV.java"
  if [ ! -f "${oldV}" ]; then
    echo "ERROR: The file '${oldV}' does not exist."
    continue
  fi

  newV="${directory}/newV.java"
  if [ ! -f "${newV}" ]; then
    echo "ERROR: The file '${newV}' does not exist."
    continue
  fi

  command=""
  case $tool in    
      "Hybrid-base")
          command="timeout --verbose --foreground ${timeout}s java -jar '${BASE_JAR_PATH}' --path1 ${oldV} --path2 ${newV} --tool H --s coral --b ${depth_limit} --t ${timeout} > ${directory}/${baseFile}"
          ;;
      "Hybrid-diff")
          command="timeout --verbose --foreground ${timeout}s java -jar '${DIFF_JAR_PATH}' ${directory} Hybrid ${timeout} ${depth_limit} ${fuzztime} ${fuzzType} > ${directory}/${resultFile}"
          ;;
      *)
          echo "ERROR: Unknown tool '$tool'."
          continue
          ;;
  esac

  if [ "$print_commands" = true ]; then
    echo "${command}"
  fi

  if [ "$dry_run" = false ]; then
    echo ""
    mkdir -p "${directory}/instrumented"
    eval "${command}"

    # Kill any leftover z3 / RunJPF.jar processes
    # that were started by the base tools.
    # This is necessary in case a base tool was
    # stopped by the timeout and the child processes
    # were, therefore, not correctly terminated.
    # pkill z3
    # pkill -f RunJPF.jar
  fi

  calculate_time "$seconds_at_start" "$total_runs" "$current_run"
  printf "\n\n"
done

# Create "materialized views"

# printf "Creating materialized views ... "
# sqlite3 "${DB_PATH}" < "${DB_CREATE_MATERIALIZED_VIEWS_PATH}" > /dev/null
# sqlite3 "${DB_PATH}" < "${DB_CREATE_TABLES_PAPER_PATH}" > /dev/null

printf "done!\n"
