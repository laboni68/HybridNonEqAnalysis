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

fuzztime=$1 #500

baseFile="ResultBaseFuzz.txt"

fuzzType=$2 #uniform or boundary
if [ "$fuzzType" == "uniform" ]; then
  resultFile="ResultFuzz"$fuzztime".txt"
else
  resultFile="ResultPrePostFuzzT115"$fuzztime".txt"
# elif [ "$fuzzType" == "boundary" ]; then
#   resultFile="ResultPreGuidedFuzz"$fuzztime".txt"
fi


timeouts=(
  "115"
)

runs=1

# benchmarks=(
# #  "airy/airy/Eq"
# # "airy/airy/Neq"
# # "airy/beschb/Eq"
# # "airy/beschb/Neq"
# # "airy/chebev_c1d/Eq"
# # "airy/chebev_c1d/Neq"
# # "airy/sphbes/Eq"
# # "airy/sphbes/Neq"
# # "bess/bess/Eq"
# "bess/bess/Neq" 
# # "caldat/caldat/Eq"
# # "caldat/caldat/Neq"
# # "caldat/caldatProgram/Eq"
# # "caldat/caldatProgram/Neq"
# # "caldat/flmoon/Eq"
# # "caldat/flmoon/Neq"
# "CLEVER/divide/Eq" 
# "CLEVER/divide/Neq" 
# # "CLEVER/factorial/Eq"
# # "CLEVER/factorial/Neq"
# # "CLEVER/fib/Eq"
# # "CLEVER/fib/Neq"
# "CLEVER/getSign2/Eq" 
# "CLEVER/getSign2/Neq" 
# "CLEVER/is_prime1/Eq" 
# "CLEVER/is_prime1/Neq"  
# "CLEVER/ltfive/Eq" 
# "CLEVER/multiple/Eq" 
# # "CLEVER/odd/Eq" 
# "CLEVER/odd/Neq" 
# "CLEVER/oneBound/Eq" 
# "CLEVER/oneN2/Eq" 
# "CLEVER/oneN2/Neq" 
# # "CLEVER/pos/Eq"
# "CLEVER/pos/Neq" 
# # "ej_hash/ej_hash/Eq"
# # "ej_hash/ej_hash/Neq"
# # "ej_hash/hashCode/Eq"
# # "ej_hash/hashCode/Neq"
# # "ej_hash/testCollision1/Eq"
# # "ej_hash/testCollision1/Neq"
# # "ej_hash/testCollision2/Eq"
# # "ej_hash/testCollision2/Neq"
# # "ej_hash/testCollision3/Eq"
# # "ej_hash/testCollision3/Neq"
# # "ej_hash/testCollision4/Eq"
# # "ej_hash/testCollision4/Neq"
# # "ell/elle/Eq"
# # "ell/elle/Neq"
# # "ell/ellProgram/Eq"
# # "ell/ellProgram/Neq"
# # "ell/sncndn/Eq"
# # "ell/sncndn/Neq"
# # "frenel/cisi/Eq"
# # "frenel/cisi/Neq"
# # "frenel/frenel/Eq"
# # "frenel/frenel/Neq"
# # "frenel/frenelProgram/Eq"
# # "frenel/frenelProgram/Neq"
# # "gam/factln/Eq"
# # "gam/factln/Neq"
# # "gam/factrl/Eq"
# # "gam/factrl/Neq"
# # "gam/gam/Eq"
# # "gam/gam/Neq" 
# # "optimization/optimization/Eq"
# "optimization/optimization/Neq" 
# "optimization/theta/Eq" 
# "optimization/theta/Neq" 
# # "optimization/wood/Eq"
# # "optimization/wood/Neq"
# "ran/ranone/Eq" 
# "ran/ranone/Neq" 
# # "ran/ranthree/Eq"
# "ran/ranthree/Neq" 
# "ran/ranwo/Eq" 
# "ran/ranwo/Neq" 
# # "raytrace/intersect/Eq"
# # "raytrace/intersect/Neq"
# # "raytrace/light/Eq"
# # "raytrace/light/Neq"
# # "raytrace/normalize/Eq"
# # "raytrace/normalize/Neq"
# # "raytrace/raytrace/Eq"
# # "raytrace/raytrace/Neq"
# # "raytrace/sphere/Eq"
# # "raytrace/sphere/Neq"
# # "raytrace/surface/Eq"
# # "raytrace/surface/Neq"
# # "REVE/ackermann/Eq"
# # "REVE/ackermann/Neq"
# # "REVE/addhorn/Eq"
# # "REVE/addhorn/Neq" 
# "REVE/average/Eq" 
# "REVE/barthe/Eq" 
# # "REVE/barthe/Neq" 
# "REVE/barthe2/Eq" 
# "REVE/barthe2big/Eq" 
# "REVE/barthe2big2/Eq" 
# "REVE/bug15/Eq" 
# "REVE/digits10/Eq"
# "REVE/inlining/Eq" 
# "REVE/inlining/Neq" 
# "REVE/limit1/Eq" 
# "REVE/limit1/Neq" 
# "REVE/limit2/Eq" 
# "REVE/limit2/Neq" 
# "REVE/limit3/Eq" 
# "REVE/loop/Eq" 
# "REVE/loop2/Eq" 
# "REVE/loop3/Eq"
# "REVE/loop5/Eq" 
# "REVE/loop5/Neq" 
# "REVE/mccarthy91/Eq" 
# "REVE/nestedwhile/Eq" 
# "REVE/nestedwhile/Neq" 
# "REVE/simpleloop/Eq" 
# # "REVE/triangular/Eq"
# # "REVE/whileif/Eq"
# # "statcalc/addValue/Eq"
# # "statcalc/addValue/Neq"
# # "tcas/tcas/Eq" 
# "tcas/tcas/Neq" 
# "tsafe/tsafe/Eq" 
# "tsafe/tsafe/Neq" 
# "optimization/optimization/Neq" #was there before
# )
benchmarks=(
"REVE/addhorn/Eq" #wok
"REVE/addhorn/Neq" #wok
"REVE/limit3/Eq" #wok
"REVE/limit2/Neq" #wok
"CLEVER/odd/Eq" #wok
"bess/bess/Neq" #wok
"CLEVER/divide/Eq" #wok
"CLEVER/divide/Neq" #wok
"CLEVER/getSign2/Eq" #wok
"CLEVER/getSign2/Neq" #wok
"CLEVER/is_prime1/Eq" #wok
"CLEVER/is_prime1/Neq" #wok
"CLEVER/ltfive/Eq" #wok
"CLEVER/multiple/Eq" #wok
"CLEVER/odd/Neq" #wok
"CLEVER/oneBound/Eq" #wok
"CLEVER/oneN2/Eq" #wok
"CLEVER/oneN2/Neq" #wok
"CLEVER/pos/Neq" #wok
"optimization/theta/Eq" #wok
"optimization/theta/Neq" #wok
"ran/ranone/Eq" #wok
"ran/ranone/Neq" #wok
"ran/ranthree/Neq" #wok
"ran/ranwo/Eq" #wok
"ran/ranwo/Neq" #wok
"REVE/average/Eq" #wok
"REVE/barthe/Eq" #wok
 "REVE/barthe/Neq" #wok
"REVE/barthe2/Eq" #wok
"REVE/barthe2big/Eq" #wok
"REVE/barthe2big2/Eq" #wok
"REVE/bug15/Eq" #wok
"REVE/inlining/Eq" #wok
"REVE/inlining/Neq" #wok
"REVE/limit1/Eq" #wok
"REVE/limit1/Neq" #wok
"REVE/limit2/Eq" #wok
"REVE/loop/Eq" #wok
"REVE/loop2/Eq" #wok
"REVE/loop3/Eq" #wok
"REVE/loop5/Eq" #wok
"REVE/loop5/Neq" #wok
"REVE/mccarthy91/Eq" #wok
"REVE/nestedwhile/Eq" #wok
"REVE/nestedwhile/Neq" #wok
"REVE/simpleloop/Eq" #wok
"tcas/tcas/Neq" #wok
"tsafe/tsafe/Eq" #wok
"tsafe/tsafe/Neq" #wok
)




tools=(
# "Hybrid-base"
"Hybrid-diff"   # Hybrid
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
      "Hybrid-base")
          command="timeout --verbose --foreground ${timeout}s java -jar '${BASE_JAR_PATH}' --path1 ${oldV} --path2 ${newV} --tool P --s coral --b ${depth_limit} --t ${timeout} > ${directory}/${baseFile}"
          ;;
      "Hybrid-diff")
          command="timeout --verbose --foreground ${timeout}s java -jar '${DIFF_JAR_PATH}' ${directory} Hybrid ${timeout} ${depth_limit} ${fuzztime} ${fuzzType} > ${directory}/${resultFile}"
          ;;
  esac

  if [ "$print_commands" = true ]; then
    echo "${command}"
  fi

  if [ "$dry_run" = false ]; then
    echo ""
    mkdir -p "${directory}/instrumented"
    eval "${command}"
  fi
done

# Create "materialized views"

# printf "Creating materialized views ... "
# sqlite3 "${DB_PATH}" < "${DB_CREATE_MATERIALIZED_VIEWS_PATH}" > /dev/null
# sqlite3 "${DB_PATH}" < "${DB_CREATE_TABLES_PAPER_PATH}" > /dev/null

printf "done!\n"
