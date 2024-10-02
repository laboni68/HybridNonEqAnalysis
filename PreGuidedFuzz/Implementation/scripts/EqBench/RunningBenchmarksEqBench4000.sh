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
fuzztime=4000


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
  # "Airy/MAX/Eq"
#  "Airy/MAX/NEq"
"ran/gammln/Eq"
"ran/gammln/Neq"
"ran/ran/Eq"
"ran/ran/Neq"
"ran/poidev/Eq"
"ran/poidev/Neq"
"ran/bnldev/Eq"
"ran/bnldev/Neq"
"ran/ranzero/Eq"
"ran/ranzero/Neq"
"ran/gasdev/Eq"
"ran/gasdev/Neq"
"ran/gamdev/Eq"
"ran/gamdev/Neq"
"ran/ranthree/Eq"
"ran/ranthree/Neq"
"ran/expdev/Eq"
"ran/expdev/Neq"
"ran/ranwo/Eq"
"ran/ranwo/Neq"
"ran/ranone/Eq"
"ran/ranone/Neq"
"ell/ellpi/Eq"
"ell/ellpi/Neq"
"ell/ell/Eq"
"ell/ell/Neq"
"ell/sncndn/Eq"
"ell/sncndn/Neq"
"ell/dbrent/Eq"
"ell/dbrent/Neq"
"ell/rd/Eq"
"ell/rd/Neq"
"ell/rf/Eq"
"ell/rf/Neq"
"ell/plgndr/Eq"
"ell/plgndr/Neq"
"ell/brent/Eq"
"ell/brent/Neq"
"ell/rc/Eq"
"ell/rc/Neq"
"ell/zbrent/Eq"
"ell/zbrent/Neq"
"ell/elle/Eq"
"ell/elle/Neq"
"ell/ellProgram/Eq"
"ell/ellProgram/Neq"
"ell/rj/Eq"
"ell/rj/Neq"
"tcas/NonCrossingBiasedDescend/Eq"
"tcas/NonCrossingBiasedDescend/Neq"
"tcas/tcas/Eq"
"tcas/tcas/Neq"
"tcas/altseptest/Eq"
"tcas/altseptest/Neq"
"tcas/NonCrossingBiasedClimb/Eq"
"tcas/NonCrossingBiasedClimb/Neq"
"optimization/optimization/Eq"
"optimization/optimization/Neq"
"optimization/wood/Eq"
"optimization/wood/Neq"
"optimization/theta/Eq"
"optimization/theta/Neq"
"frenel/frenelProgram/Eq"
"frenel/frenelProgram/Neq"
"frenel/cisi/Eq"
"frenel/cisi/Neq"
"frenel/frenel/Eq"
"frenel/frenel/Neq"
"tsafe/conflict/Eq"
"tsafe/conflict/Neq"
"tsafe/tsafe/Eq"
"tsafe/tsafe/Neq"
"tsafe/normAngle/Eq"
"tsafe/normAngle/Neq"
"tsafe/snippet/Eq"
"tsafe/snippet/Neq"
"ej_hash/testCollision2/Eq"
"ej_hash/testCollision2/Neq"
"ej_hash/testCollision4/Eq"
"ej_hash/testCollision4/Neq"
"ej_hash/ej_hash/Eq"
"ej_hash/ej_hash/Neq"
"ej_hash/hashCode/Eq"
"ej_hash/hashCode/Neq"
"ej_hash/testCollision1/Eq"
"ej_hash/testCollision1/Neq"
"ej_hash/testCollision3/Eq"
"ej_hash/testCollision3/Neq"
"gam/factln/Eq"
"gam/factln/Neq"
"gam/erfcc/Eq"
"gam/erfcc/Neq"
"gam/betacf/Eq"
"gam/betacf/Neq"
"gam/gam/Eq"
"gam/gam/Neq"
"gam/gammq/Eq"
"gam/gammq/Neq"
"gam/gcf/Eq"
"gam/gcf/Neq"
"gam/gser/Eq"
"gam/gser/Neq"
"gam/betai/Eq"
"gam/betai/Neq"
"gam/expint/Eq"
"gam/expint/Neq"
"gam/factrl/Eq"
"gam/factrl/Neq"
"gam/gammp/Eq"
"gam/gammp/Neq"
"gam/ei/Eq"
"gam/ei/Neq"
"statcalc/addValue/Eq"
"statcalc/addValue/Neq"
"caldat/caldat/Eq"
"caldat/caldat/Neq"
"caldat/flmoon/Eq"
"caldat/flmoon/Neq"
"caldat/julday/Eq"
"caldat/julday/Neq"
"caldat/caldatProgram/Eq"
"caldat/caldatProgram/Neq"
"caldat/badluk/Eq"
"caldat/badluk/Neq"
"dart/test/Eq"
"dart/test/Neq"
"airy/MAX/Eq"
"airy/MAX/Neq"
"airy/sphbes/Eq"
"airy/sphbes/Neq"
"airy/beschb/Eq"
"airy/beschb/Neq"
"airy/airy/Eq"
"airy/airy/Neq"
"airy/chebev_c1d/Eq"
"airy/chebev_c1d/Neq"
"airy/Sign/Eq"
"airy/Sign/Neq"
"REVE/addhorn/Eq"
"REVE/addhorn/Neq"
"REVE/barthe/Eq"
"REVE/barthe/Neq"
"REVE/limit1/Eq"
"REVE/limit1/Neq"
"REVE/loop5/Eq"
"REVE/loop5/Neq"
"REVE/barthe2/Eq"
"REVE/barthe2/Neq"
"REVE/simpleloop/Eq"
"REVE/simpleloop/Neq"
"REVE/barthe2big2/Eq"
"REVE/barthe2big2/Neq"
"REVE/loop2/Eq"
"REVE/loop2/Neq"
"REVE/limit2/Eq"
"REVE/limit2/Neq"
"REVE/whileif/Eq"
"REVE/whileif/Neq"
"REVE/inlining/Eq"
"REVE/inlining/Neq"
"REVE/limit3/Eq"
"REVE/limit3/Neq"
"REVE/loop3/Eq"
"REVE/loop3/Neq"
"REVE/bug15/Eq"
"REVE/bug15/Neq"
"REVE/nestedwhile/Eq"
"REVE/nestedwhile/Neq"
"REVE/loop/Eq"
"REVE/loop/Neq"
"REVE/ackermann/Eq"
"REVE/ackermann/Neq"
"REVE/digits10/Eq"
"REVE/digits10/Neq"
"REVE/triangular/Eq"
"REVE/triangular/Neq"
"REVE/mccarthy91/Eq"
"REVE/mccarthy91/Neq"
"REVE/average/Eq"
"REVE/average/Neq"
"REVE/barthe2big/Eq"
"REVE/barthe2big/Neq"
"CLEVER/Add/Eq"
"CLEVER/Add/Neq"
"CLEVER/factorial/Eq"
"CLEVER/factorial/Neq"
"CLEVER/LoopUnreach2/Eq"
"CLEVER/LoopUnreach2/Neq"
"CLEVER/oneBound/Eq"
"CLEVER/oneBound/Neq"
"CLEVER/Comp/Eq"
"CLEVER/Comp/Neq"
"CLEVER/LoopMult15/Eq"
"CLEVER/LoopMult15/Neq"
"CLEVER/LoopMult2/Eq"
"CLEVER/LoopMult2/Neq"
"CLEVER/divide/Eq"
"CLEVER/divide/Neq"
"CLEVER/LoopUnreach20/Eq"
"CLEVER/LoopUnreach20/Neq"
"CLEVER/pos/Eq"
"CLEVER/pos/Neq"
"CLEVER/LoopSub/Eq"
"CLEVER/LoopSub/Neq"
"CLEVER/is_prime1/Eq"
"CLEVER/is_prime1/Neq"
"CLEVER/Const/Eq"
"CLEVER/Const/Neq"
"CLEVER/LoopUnreach15/Eq"
"CLEVER/LoopUnreach15/Neq"
"CLEVER/fib/Eq"
"CLEVER/fib/Neq"
"CLEVER/oneN2/Eq"
"CLEVER/oneN2/Neq"
"CLEVER/LoopUnreach5/Eq"
"CLEVER/LoopUnreach5/Neq"
"CLEVER/ltfive/Eq"
"CLEVER/ltfive/Neq"
"CLEVER/LoopMult20/Eq"
"CLEVER/LoopMult20/Neq"
"CLEVER/LoopUnreach10/Eq"
"CLEVER/LoopUnreach10/Neq"
"CLEVER/multiple/Eq"
"CLEVER/multiple/Neq"
"CLEVER/Sub/Eq"
"CLEVER/Sub/Neq"
"CLEVER/getSign2/Eq"
"CLEVER/getSign2/Neq"
"CLEVER/UnchLoop/Eq"
"CLEVER/UnchLoop/Neq"
"CLEVER/LoopMult10/Eq"
"CLEVER/LoopMult10/Neq"
"CLEVER/odd/Eq"
"CLEVER/odd/Neq"
"CLEVER/LoopMult5/Eq"
"CLEVER/LoopMult5/Neq"
"bess/pythag/Eq"
"bess/pythag/Neq"
"bess/dawson/Eq"
"bess/dawson/Neq"
"bess/bessi1/Eq"
"bess/bessi1/Neq"
"bess/bessk0/Eq"
"bess/bessk0/Neq"
"bess/bessy0/Eq"
"bess/bessy0/Neq"
"bess/bessi/Eq"
"bess/bessi/Neq"
"bess/bessj1/Eq"
"bess/bessj1/Neq"
"bess/bessy/Eq"
"bess/bessy/Neq"
"bess/probks/Eq"
"bess/probks/Neq"
"bess/SIGN/Eq"
"bess/SIGN/Neq"
"bess/bessj0/Eq"
"bess/bessj0/Neq"
"bess/bessk1/Eq"
"bess/bessk1/Neq"
"bess/bess/Eq"
"bess/bess/Neq"
"bess/SQR/Eq"
"bess/SQR/Neq"
"bess/bessj/Eq"
"bess/bessj/Neq"
"bess/bessi0/Eq"
"bess/bessi0/Neq"
"bess/bessy1/Eq"
"bess/bessy1/Neq"
"bess/bessk/Eq"
"bess/bessk/Neq"
"sine/mysin/Eq"
"sine/mysin/Neq"
"raytrace/sphere/Eq"
"raytrace/sphere/Neq"
"raytrace/surface/Eq"
"raytrace/surface/Neq"
"raytrace/normalize/Eq"
"raytrace/normalize/Neq"
"raytrace/light/Eq"
"raytrace/light/Neq"
"raytrace/raytrace/Eq"
"raytrace/raytrace/Neq"
"raytrace/intersect/Eq"
"raytrace/intersect/Neq"
"pow/test/Eq"
"pow/test/Neq"
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

  directory="../EqBench/${benchmark}"
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
          command="timeout --verbose --foreground ${timeout}s java -jar '${DIFF_JAR_PATH}' ${directory} PASDA ${timeout} ${depth_limit} ${fuzztime} ${fuzzType} > ${directory}/ResultBFuzz4000.txt"
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
