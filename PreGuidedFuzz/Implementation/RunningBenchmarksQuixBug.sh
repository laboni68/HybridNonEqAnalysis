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
# "97"
"120"
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
resultFile="ResultPrePostFuzzD10115Changed"$fuzztime".txt"
  # resultFile="ResultBFuzz"$fuzztime".txt"
  # resultFile="ResultBFuzz"$fuzztime".txt"
fi

runs=1

benchmarks=(
"NEq/BITCOUNT"
"NEq/GCD"
"NEq/SQRT"
"Eq/BITCOUNT"
"Eq/GCD"
"Eq/SQRT"
)

tools=(
  # "Hybrid-base"
  "Hybrid-diff"   # Hybrid
# "ARDiff-base"  # ARDiff
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

solver="z3"
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

  directory="../QuixBugs/${benchmark}"
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
          command="timeout --verbose --foreground ${timeout}s java -jar '${BASE_JAR_PATH}' --path1 ${oldV} --path2 ${newV} --tool H --s ${solver} --b ${depth_limit} --t ${timeout} > ${directory}/${baseFile}"
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
