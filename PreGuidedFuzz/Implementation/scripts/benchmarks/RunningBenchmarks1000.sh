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

fuzztime=1000

fuzzType="uniform"
fuzzType="boundary"

timeouts=(
  "120"
  # "10"
 # "20"
#"30"
#"80"
#"90"
# "120"
#"240"
#  "300"
#  "900"
#  "3600"
)

runs=1

benchmarks=(
  "Airy/MAX/Eq"
 "Airy/MAX/NEq"
   "Airy/Sign/Eq"
   "Airy/Sign/NEq"
  "Bess/SIGN/Eq"
   "Bess/SIGN/NEq"
  "Bess/SQR/Eq"
  "Bess/SQR/NEq"
  "Bess/bessi/Eq"
  "Bess/bessi/NEq"
  "Bess/bessi0/Eq"
  "Bess/bessi0/NEq"
  "Bess/bessi1/Eq"
  "Bess/bessi1/NEq"
  "Bess/bessj/Eq"
   "Bess/bessj/NEq"
  "Bess/bessj0/Eq"
  "Bess/bessj0/NEq"
  "Bess/bessj1/Eq"
  "Bess/bessj1/NEq"
  "Bess/bessk/Eq"
  "Bess/bessk/NEq"
  "Bess/bessk0/Eq"
  "Bess/bessk0/NEq"
  "Bess/bessk1/Eq"
  "Bess/bessk1/NEq"
  "Bess/bessy/Eq"
  "Bess/bessy/NEq"
  "Bess/bessy0/Eq"
  "Bess/bessy0/NEq"
  "Bess/bessy1/Eq"
  "Bess/bessy1/NEq"
 "Bess/dawson/Eq"
   "Bess/dawson/NEq"
  "Bess/probks/Eq"
  "Bess/probks/NEq"
  "Bess/pythag/Eq"
  "Bess/pythag/NEq"
 "Ell/brent/Eq"
   "Ell/brent/NEq"
   "Ell/dbrent/Eq"
  "Ell/dbrent/NEq"
   "Ell/ell/Eq"
  "Ell/ell/NEq"
   "Ell/ellpi/Eq"
  "Ell/ellpi/NEq"
  "Ell/plgndr/Eq"
  "Ell/plgndr/NEq"
  "Ell/rc/Eq"
  "Ell/rc/NEq"
   "Ell/rd/Eq"
   "Ell/rd/NEq"
   "Ell/rf/Eq"
   "Ell/rf/NEq"
   "Ell/rj/Eq"
   "Ell/rj/NEq"
   "Ell/zbrent/Eq"
  "Ell/zbrent/NEq"
  "ModDiff/Eq/Add"
 "ModDiff/Eq/Comp"
  "ModDiff/Eq/Const"
  "ModDiff/Eq/LoopMult10"
  "ModDiff/Eq/LoopMult15"
  "ModDiff/Eq/LoopMult2"
   "ModDiff/Eq/LoopMult20"
  "ModDiff/Eq/LoopMult5"
  "ModDiff/Eq/LoopSub"
  "ModDiff/Eq/LoopUnreach10"
  "ModDiff/Eq/LoopUnreach15"
  "ModDiff/Eq/LoopUnreach2"
  "ModDiff/Eq/LoopUnreach20"
  "ModDiff/Eq/LoopUnreach5"
  "ModDiff/Eq/Sub"
  "ModDiff/Eq/UnchLoop"
 "ModDiff/NEq/LoopMult10"
 "ModDiff/NEq/LoopMult15"
  "ModDiff/NEq/LoopMult2"
 "ModDiff/NEq/LoopMult20"
   "ModDiff/NEq/LoopMult5"
  "ModDiff/NEq/LoopSub"
  "ModDiff/NEq/LoopUnreach10"
  "ModDiff/NEq/LoopUnreach15"
  "ModDiff/NEq/LoopUnreach2"
  "ModDiff/NEq/LoopUnreach20"
   "ModDiff/NEq/LoopUnreach5"
  "ModDiff/NEq/UnchLoop"
  "Ran/bnldev/Eq"
   "Ran/bnldev/NEq"
   "Ran/expdev/Eq"
   "Ran/expdev/NEq"
   "Ran/gamdev/Eq"
  "Ran/gamdev/NEq"
  "Ran/gammln/Eq"
  "Ran/gammln/NEq"
  "Ran/gasdev/Eq"
  "Ran/gasdev/NEq"
   "Ran/poidev/Eq"
  "Ran/poidev/NEq"
   "Ran/ran/Eq"
  "Ran/ran/NEq"
  "Ran/ranzero/Eq"
  "Ran/ranzero/NEq"
  "caldat/badluk/Eq"
  "caldat/badluk/NEq"
  "caldat/julday/Eq"
  "caldat/julday/NEq"
  "dart/test/Eq"
  "dart/test/NEq"
  "gam/betacf/Eq"
  "gam/betacf/NEq"
  "gam/betai/Eq"
  "gam/betai/NEq"
  "gam/ei/Eq"
  "gam/ei/NEq"
  "gam/erfcc/Eq"
  "gam/erfcc/NEq"
  "gam/expint/Eq"
  "gam/expint/NEq"
  "gam/gammp/Eq"
  "gam/gammp/NEq"
  "gam/gammq/Eq"
   "gam/gammq/NEq"
   "gam/gcf/Eq"
    "gam/gcf/NEq"
   "gam/gser/Eq"
  "gam/gser/NEq"
  "power/test/Eq"
  "power/test/NEq"
  "sine/mysin/Eq"
  "sine/mysin/NEq"
  "tcas/NonCrossingBiasedClimb/Eq"
  "tcas/NonCrossingBiasedClimb/NEq"
  "tcas/NonCrossingBiasedDescend/Eq"
  "tcas/NonCrossingBiasedDescend/NEq"
  "tcas/altseptest/Eq"
  "tcas/altseptest/NEq"
  "tsafe/conflict/Eq"
  "tsafe/conflict/NEq"
  "tsafe/normAngle/Eq"
  "tsafe/normAngle/NEq"
  "tsafe/snippet/Eq"
  "tsafe/snippet/NEq"
)

tools=(
# "PASDA-base"
"PASDA-diff"   # PASDA
 # "ARDiff-base"  # ARDiff
#  "ARDiff-diff"
#  "DSE-base"     # DSE
#  "DSE-diff"
#  "SE-base"
#  "SE-diff"      # PRV
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

  directory="../benchmarks/${benchmark}"
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
      "ARDiff-base")
          command="timeout --verbose --foreground ${timeout}s java -jar '${BASE_JAR_PATH}' --path1 ${oldV} --path2 ${newV} --tool A --s coral --H H123 --b ${depth_limit} --t ${timeout}"
          ;;
      "ARDiff-diff")
          command="timeout --verbose --foreground ${timeout}s java -jar '${DIFF_JAR_PATH}' ${directory} ARDiff ${timeout} ${depth_limit}"
          ;;
      "DSE-base")
          command="timeout --verbose --foreground ${timeout}s java -jar '${BASE_JAR_PATH}' --path1 ${oldV} --path2 ${newV} --tool D --s coral --b ${depth_limit} --t ${timeout}"
          ;;
      "DSE-diff")
          command="timeout --verbose --foreground ${timeout}s java -jar '${DIFF_JAR_PATH}' ${directory} DSE ${timeout} ${depth_limit}"
          ;;
      "PASDA-base")
          command="timeout --verbose --foreground ${timeout}s java -jar '${BASE_JAR_PATH}' --path1 ${oldV} --path2 ${newV} --tool P --s coral --b ${depth_limit} --t ${timeout}"
          ;;
      "PASDA-diff")
          command="timeout --verbose --foreground ${timeout}s java -jar '${DIFF_JAR_PATH}' ${directory} PASDA ${timeout} ${depth_limit} ${fuzztime} ${fuzzType} > ${directory}/ResultFuzz1000.txt"
          ;;
      "SE-base")
          command="timeout --verbose --foreground ${timeout}s java -jar '${BASE_JAR_PATH}' --path1 ${oldV} --path2 ${newV} --tool S --s coral --b ${depth_limit} --t ${timeout}"
          ;;
      "SE-diff")
          command="timeout --verbose --foreground ${timeout}s java -jar '${DIFF_JAR_PATH}' ${directory} SE ${timeout} ${depth_limit}"
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
    #pkill z3
    #pkill -f RunJPF.jar
  fi

  calculate_time "$seconds_at_start" "$total_runs" "$current_run"
  printf "\n\n"
done

# Create "materialized views"

# printf "Creating materialized views ... "
# sqlite3 "${DB_PATH}" < "${DB_CREATE_MATERIALIZED_VIEWS_PATH}" > /dev/null
# sqlite3 "${DB_PATH}" < "${DB_CREATE_TABLES_PAPER_PATH}" > /dev/null

printf "done!\n"
