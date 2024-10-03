#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )


BASE_JAR_PATH="/usr/lib/jvm/jdk1.8.0_202/bin/java -Djava.library.path=../Implementation/jpf-git/jpf-symbc/lib -javaagent:~/Downloads/ideaIU-2023.2.5/idea-IU-232.10227.8/lib/idea_rt.jar=39615:~/Downloads/ideaIU-2023.2.5/idea-IU-232.10227.8/bin -Dfile.encoding=UTF-8 -classpath /usr/lib/jvm/jdk1.8.0_202/lib/ant-javafx.jar:/usr/lib/jvm/jdk1.8.0_202/lib/dt.jar:/usr/lib/jvm/jdk1.8.0_202/lib/javafx-mx.jar:/usr/lib/jvm/jdk1.8.0_202/lib/jconsole.jar:/usr/lib/jvm/jdk1.8.0_202/lib/packager.jar:/usr/lib/jvm/jdk1.8.0_202/lib/sa-jdi.jar:/usr/lib/jvm/jdk1.8.0_202/lib/tools.jar:../Implementation/target/classes:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.runtime-3.18.0.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.jobs-3.10.800.jar:~/.m2/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar:~/.m2/repository/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.equinox.common-3.12.0.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.equinox.app-1.4.500.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.equinox.preferences-3.8.0.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.equinox.registry-3.8.800.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.commands-3.9.700.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.contenttype-3.7.700.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.resources-3.13.700.jar:~/.m2/repository/com/google/guava/guava/28.2-jre/guava-28.2-jre.jar:~/.m2/repository/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:~/.m2/repository/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:~/.m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:~/.m2/repository/org/checkerframework/checker-qual/2.10.0/checker-qual-2.10.0.jar:~/.m2/repository/com/google/errorprone/error_prone_annotations/2.3.4/error_prone_annotations-2.3.4.jar:~/.m2/repository/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3.jar:~/.m2/repository/com/github/gumtreediff/core/2.1.2/core-2.1.2.jar:~/.m2/repository/org/atteo/classindex/classindex/3.4/classindex-3.4.jar:~/.m2/repository/com/github/mpkorstanje/simmetrics-core/3.2.3/simmetrics-core-3.2.3.jar:~/.m2/repository/commons-codec/commons-codec/1.10/commons-codec-1.10.jar:~/.m2/repository/net/sf/trove4j/trove4j/3.0.3/trove4j-3.0.3.jar:~/.m2/repository/com/google/code/gson/gson/2.8.2/gson-2.8.2.jar:~/.m2/repository/org/jgrapht/jgrapht-core/1.0.1/jgrapht-core-1.0.1.jar:~/.m2/repository/com/github/gumtreediff/client/2.1.2/client-2.1.2.jar:~/.m2/repository/com/github/gumtreediff/gen.jdt/2.1.2/gen.jdt-2.1.2.jar:~/.m2/repository/br/usp/each/saeg/asm-defuse/0.0.6/asm-defuse-0.0.6.jar:~/.m2/repository/org/ow2/asm/asm-analysis/6.0/asm-analysis-6.0.jar:~/.m2/repository/org/ow2/asm/asm-tree/6.0/asm-tree-6.0.jar:~/.m2/repository/br/usp/each/saeg/saeg-commons/0.0.5/saeg-commons-0.0.5.jar:../Implementation/target/artifacts/Implementation_jar/com.microsoft.z3.jar:~/.m2/repository/org/ow2/asm/asm/6.0/asm-6.0.jar:~/.m2/repository/com/github/javaparser/javaparser-core/3.15.2/javaparser-core-3.15.2.jar:~/.m2/repository/org/eclipse/jdt/org.eclipse.jdt.core/3.26.0/org.eclipse.jdt.core-3.26.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.resources/3.21.0/org.eclipse.core.resources-3.21.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.expressions/3.9.400/org.eclipse.core.expressions-3.9.400.jar:~/.m2/repository/net/java/dev/jna/jna/5.14.0/jna-5.14.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.osgi/3.21.0/org.eclipse.osgi-3.21.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.runtime/3.31.100/org.eclipse.core.runtime-3.31.100.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.jobs/3.15.300/org.eclipse.core.jobs-3.15.300.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.contenttype/3.9.400/org.eclipse.core.contenttype-3.9.400.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.filesystem/1.11.0/org.eclipse.core.filesystem-1.11.0.jar:~/.m2/repository/net/java/dev/jna/jna-platform/5.14.0/jna-platform-5.14.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.text/3.14.100/org.eclipse.text-3.14.100.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.commands/3.12.100/org.eclipse.core.commands-3.12.100.jar:../Implementation/target/artifacts/Implementation_jar/jfxrt.jar Runner.Runner"
DIFF_JAR_PATH="${SCRIPT_DIR}/build/libs/ARDiff-diff-1.0-SNAPSHOT-all.jar"

dry_run=false

clean_db=false
force_build=false
print_commands=true

depth_limits=(
  "3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"10"
"10"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"13"
"13"
"3"
"3"
"3"
"3"
"5"
"5"
"10"
"10"
"3"
"3"
"3"
"3"
"3"
"3"
"10"
"5"
"10"
"5"
"10"
"5"
"3"
"3"
"3"
"3"
"10"
"10"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"10"
"5"
"3"
"3"
"3"
"15"
"15"
"20"
"20"
"3"
"3"
"25"
"25"
"10"
"10"
"3"
"3"
"15"
"15"
"20"
"20"
"3"
"3"
"25"
"25"
"10"
"10"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"10"
"10"
"3"
"3"
"10"
"5"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
"3"
)

timeouts=(
  #"10"
#"30"
  # "90"
#  "300"
"115"
#  "900"
#  "3600"
)

runs=1

benchmarks=(
"Airy/MAX/Eq"
"Airy/MAX/NEq"
"Airy/Sign/Eq"
"Airy/Sign/NEq"
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
"Bess/SIGN/Eq"
"Bess/SIGN/NEq"
"Bess/SQR/Eq"
"Bess/SQR/NEq"
"caldat/badluk/Eq"
"caldat/badluk/NEq"
"caldat/julday/Eq"
"caldat/julday/NEq"
"dart/test/Eq"
"dart/test/NEq"
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
"ModDiff/Eq/Add"
"ModDiff/Eq/Comp"
"ModDiff/Eq/Const"
"ModDiff/Eq/LoopMult10"
"ModDiff/NEq/LoopMult10"
"ModDiff/Eq/LoopMult15"
"ModDiff/NEq/LoopMult15"
"ModDiff/Eq/LoopMult2"
"ModDiff/NEq/LoopMult2"
"ModDiff/Eq/LoopMult20"
"ModDiff/NEq/LoopMult20"
"ModDiff/Eq/LoopMult5"
"ModDiff/NEq/LoopMult5"
"ModDiff/Eq/LoopSub"
"ModDiff/NEq/LoopSub"
"ModDiff/Eq/LoopUnreach10"
"ModDiff/NEq/LoopUnreach10"
"ModDiff/Eq/LoopUnreach15"
"ModDiff/NEq/LoopUnreach15"
"ModDiff/Eq/LoopUnreach2"
"ModDiff/NEq/LoopUnreach2"
"ModDiff/Eq/LoopUnreach20"
"ModDiff/NEq/LoopUnreach20"
"ModDiff/Eq/LoopUnreach5"
"ModDiff/NEq/LoopUnreach5"
"ModDiff/Eq/Sub"
"ModDiff/Eq/UnchLoop"
"ModDiff/NEq/UnchLoop"
"power/test/Eq"
"power/test/NEq"
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
"sine/mysin/Eq"
"sine/mysin/NEq"
"tcas/altseptest/Eq"
"tcas/altseptest/NEq"
"tcas/NonCrossingBiasedClimb/Eq"
"tcas/NonCrossingBiasedClimb/NEq"
"tcas/NonCrossingBiasedDescend/Eq"
"tcas/NonCrossingBiasedDescend/NEq"
"tsafe/conflict/Eq"
"tsafe/conflict/NEq"
"tsafe/normAngle/Eq"
"tsafe/normAngle/NEq"
"tsafe/snippet/Eq"
"tsafe/snippet/NEq"
)

tools=(
 "ARDiff-base"  # ARDiff
#  "DSE-base"     # DSE
# "Impacted-base" #Impacted-SE
)

newline=$'\n'
runs_settings=()
  for timeout in "${timeouts[@]}"; do
    for ((count = 1; count <= runs; count++)); do
    index=0
      for benchmark in "${benchmarks[@]}"; do
        for tool in "${tools[@]}"; do
          runs_settings+=("${benchmark},${tool},${timeout},${depth_limits[$index]}${newline}")
          index=$((index+1))
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
          command="timeout ${timeout} /usr/lib/jvm/jdk1.8.0_202/bin/java -Djava.library.path=../Implementation/jpf-git/jpf-symbc/lib -javaagent:~/Downloads/ideaIU-2023.2.5/idea-IU-232.10227.8/lib/idea_rt.jar=39615:~/Downloads/ideaIU-2023.2.5/idea-IU-232.10227.8/bin -Dfile.encoding=UTF-8 -classpath /usr/lib/jvm/jdk1.8.0_202/lib/ant-javafx.jar:/usr/lib/jvm/jdk1.8.0_202/lib/dt.jar:/usr/lib/jvm/jdk1.8.0_202/lib/javafx-mx.jar:/usr/lib/jvm/jdk1.8.0_202/lib/jconsole.jar:/usr/lib/jvm/jdk1.8.0_202/lib/packager.jar:/usr/lib/jvm/jdk1.8.0_202/lib/sa-jdi.jar:/usr/lib/jvm/jdk1.8.0_202/lib/tools.jar:../Implementation/target/classes:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.runtime-3.18.0.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.jobs-3.10.800.jar:~/.m2/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar:~/.m2/repository/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.equinox.common-3.12.0.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.equinox.app-1.4.500.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.equinox.preferences-3.8.0.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.equinox.registry-3.8.800.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.commands-3.9.700.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.contenttype-3.7.700.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.resources-3.13.700.jar:~/.m2/repository/com/google/guava/guava/28.2-jre/guava-28.2-jre.jar:~/.m2/repository/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:~/.m2/repository/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:~/.m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:~/.m2/repository/org/checkerframework/checker-qual/2.10.0/checker-qual-2.10.0.jar:~/.m2/repository/com/google/errorprone/error_prone_annotations/2.3.4/error_prone_annotations-2.3.4.jar:~/.m2/repository/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3.jar:~/.m2/repository/com/github/gumtreediff/core/2.1.2/core-2.1.2.jar:~/.m2/repository/org/atteo/classindex/classindex/3.4/classindex-3.4.jar:~/.m2/repository/com/github/mpkorstanje/simmetrics-core/3.2.3/simmetrics-core-3.2.3.jar:~/.m2/repository/commons-codec/commons-codec/1.10/commons-codec-1.10.jar:~/.m2/repository/net/sf/trove4j/trove4j/3.0.3/trove4j-3.0.3.jar:~/.m2/repository/com/google/code/gson/gson/2.8.2/gson-2.8.2.jar:~/.m2/repository/org/jgrapht/jgrapht-core/1.0.1/jgrapht-core-1.0.1.jar:~/.m2/repository/com/github/gumtreediff/client/2.1.2/client-2.1.2.jar:~/.m2/repository/com/github/gumtreediff/gen.jdt/2.1.2/gen.jdt-2.1.2.jar:~/.m2/repository/br/usp/each/saeg/asm-defuse/0.0.6/asm-defuse-0.0.6.jar:~/.m2/repository/org/ow2/asm/asm-analysis/6.0/asm-analysis-6.0.jar:~/.m2/repository/org/ow2/asm/asm-tree/6.0/asm-tree-6.0.jar:~/.m2/repository/br/usp/each/saeg/saeg-commons/0.0.5/saeg-commons-0.0.5.jar:../Implementation/target/artifacts/Implementation_jar/com.microsoft.z3.jar:~/.m2/repository/org/ow2/asm/asm/6.0/asm-6.0.jar:~/.m2/repository/com/github/javaparser/javaparser-core/3.15.2/javaparser-core-3.15.2.jar:~/.m2/repository/org/eclipse/jdt/org.eclipse.jdt.core/3.26.0/org.eclipse.jdt.core-3.26.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.resources/3.21.0/org.eclipse.core.resources-3.21.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.expressions/3.9.400/org.eclipse.core.expressions-3.9.400.jar:~/.m2/repository/net/java/dev/jna/jna/5.14.0/jna-5.14.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.osgi/3.21.0/org.eclipse.osgi-3.21.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.runtime/3.31.100/org.eclipse.core.runtime-3.31.100.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.jobs/3.15.300/org.eclipse.core.jobs-3.15.300.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.contenttype/3.9.400/org.eclipse.core.contenttype-3.9.400.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.filesystem/1.11.0/org.eclipse.core.filesystem-1.11.0.jar:~/.m2/repository/net/java/dev/jna/jna-platform/5.14.0/jna-platform-5.14.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.text/3.14.100/org.eclipse.text-3.14.100.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.commands/3.12.100/org.eclipse.core.commands-3.12.100.jar:../Implementation/target/artifacts/Implementation_jar/jfxrt.jar Runner.Runner --path1 ${oldV} --path2 ${newV} --tool A --s coral --H H123 --b ${depth_limit} > ${directory}/ResultArDiffThres_Var_115.txt"
          ;;
      "Impacted-base")
          command="timeout ${timeout} /usr/lib/jvm/jdk1.8.0_202/bin/java -Djava.library.path=../Implementation/jpf-git/jpf-symbc/lib -javaagent:~/Downloads/ideaIU-2023.2.5/idea-IU-232.10227.8/lib/idea_rt.jar=39615:~/Downloads/ideaIU-2023.2.5/idea-IU-232.10227.8/bin -Dfile.encoding=UTF-8 -classpath /usr/lib/jvm/jdk1.8.0_202/lib/ant-javafx.jar:/usr/lib/jvm/jdk1.8.0_202/lib/dt.jar:/usr/lib/jvm/jdk1.8.0_202/lib/javafx-mx.jar:/usr/lib/jvm/jdk1.8.0_202/lib/jconsole.jar:/usr/lib/jvm/jdk1.8.0_202/lib/packager.jar:/usr/lib/jvm/jdk1.8.0_202/lib/sa-jdi.jar:/usr/lib/jvm/jdk1.8.0_202/lib/tools.jar:../Implementation/target/classes:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.runtime-3.18.0.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.jobs-3.10.800.jar:~/.m2/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar:~/.m2/repository/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.equinox.common-3.12.0.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.equinox.app-1.4.500.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.equinox.preferences-3.8.0.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.equinox.registry-3.8.800.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.commands-3.9.700.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.contenttype-3.7.700.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.resources-3.13.700.jar:~/.m2/repository/com/google/guava/guava/28.2-jre/guava-28.2-jre.jar:~/.m2/repository/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:~/.m2/repository/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:~/.m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:~/.m2/repository/org/checkerframework/checker-qual/2.10.0/checker-qual-2.10.0.jar:~/.m2/repository/com/google/errorprone/error_prone_annotations/2.3.4/error_prone_annotations-2.3.4.jar:~/.m2/repository/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3.jar:~/.m2/repository/com/github/gumtreediff/core/2.1.2/core-2.1.2.jar:~/.m2/repository/org/atteo/classindex/classindex/3.4/classindex-3.4.jar:~/.m2/repository/com/github/mpkorstanje/simmetrics-core/3.2.3/simmetrics-core-3.2.3.jar:~/.m2/repository/commons-codec/commons-codec/1.10/commons-codec-1.10.jar:~/.m2/repository/net/sf/trove4j/trove4j/3.0.3/trove4j-3.0.3.jar:~/.m2/repository/com/google/code/gson/gson/2.8.2/gson-2.8.2.jar:~/.m2/repository/org/jgrapht/jgrapht-core/1.0.1/jgrapht-core-1.0.1.jar:~/.m2/repository/com/github/gumtreediff/client/2.1.2/client-2.1.2.jar:~/.m2/repository/com/github/gumtreediff/gen.jdt/2.1.2/gen.jdt-2.1.2.jar:~/.m2/repository/br/usp/each/saeg/asm-defuse/0.0.6/asm-defuse-0.0.6.jar:~/.m2/repository/org/ow2/asm/asm-analysis/6.0/asm-analysis-6.0.jar:~/.m2/repository/org/ow2/asm/asm-tree/6.0/asm-tree-6.0.jar:~/.m2/repository/br/usp/each/saeg/saeg-commons/0.0.5/saeg-commons-0.0.5.jar:../Implementation/target/artifacts/Implementation_jar/com.microsoft.z3.jar:~/.m2/repository/org/ow2/asm/asm/6.0/asm-6.0.jar:~/.m2/repository/com/github/javaparser/javaparser-core/3.15.2/javaparser-core-3.15.2.jar:~/.m2/repository/org/eclipse/jdt/org.eclipse.jdt.core/3.26.0/org.eclipse.jdt.core-3.26.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.resources/3.21.0/org.eclipse.core.resources-3.21.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.expressions/3.9.400/org.eclipse.core.expressions-3.9.400.jar:~/.m2/repository/net/java/dev/jna/jna/5.14.0/jna-5.14.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.osgi/3.21.0/org.eclipse.osgi-3.21.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.runtime/3.31.100/org.eclipse.core.runtime-3.31.100.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.jobs/3.15.300/org.eclipse.core.jobs-3.15.300.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.contenttype/3.9.400/org.eclipse.core.contenttype-3.9.400.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.filesystem/1.11.0/org.eclipse.core.filesystem-1.11.0.jar:~/.m2/repository/net/java/dev/jna/jna-platform/5.14.0/jna-platform-5.14.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.text/3.14.100/org.eclipse.text-3.14.100.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.commands/3.12.100/org.eclipse.core.commands-3.12.100.jar:../Implementation/target/artifacts/Implementation_jar/jfxrt.jar Runner.Runner --path1 ${oldV} --path2 ${newV} --tool I --s coral --b ${depth_limit} > ${directory}/ResultImpacted120.txt"
          ;;
      "DSE-base")
          command="timeout ${timeout} /usr/lib/jvm/jdk1.8.0_202/bin/java -Djava.library.path=../Implementation/jpf-git/jpf-symbc/lib -javaagent:~/Downloads/ideaIU-2023.2.5/idea-IU-232.10227.8/lib/idea_rt.jar=39615:~/Downloads/ideaIU-2023.2.5/idea-IU-232.10227.8/bin -Dfile.encoding=UTF-8 -classpath /usr/lib/jvm/jdk1.8.0_202/lib/ant-javafx.jar:/usr/lib/jvm/jdk1.8.0_202/lib/dt.jar:/usr/lib/jvm/jdk1.8.0_202/lib/javafx-mx.jar:/usr/lib/jvm/jdk1.8.0_202/lib/jconsole.jar:/usr/lib/jvm/jdk1.8.0_202/lib/packager.jar:/usr/lib/jvm/jdk1.8.0_202/lib/sa-jdi.jar:/usr/lib/jvm/jdk1.8.0_202/lib/tools.jar:../Implementation/target/classes:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.runtime-3.18.0.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.jobs-3.10.800.jar:~/.m2/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar:~/.m2/repository/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.equinox.common-3.12.0.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.equinox.app-1.4.500.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.equinox.preferences-3.8.0.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.equinox.registry-3.8.800.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.commands-3.9.700.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.contenttype-3.7.700.jar:../Implementation/target/artifacts/Implementation_jar/org.eclipse.core.resources-3.13.700.jar:~/.m2/repository/com/google/guava/guava/28.2-jre/guava-28.2-jre.jar:~/.m2/repository/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:~/.m2/repository/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:~/.m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:~/.m2/repository/org/checkerframework/checker-qual/2.10.0/checker-qual-2.10.0.jar:~/.m2/repository/com/google/errorprone/error_prone_annotations/2.3.4/error_prone_annotations-2.3.4.jar:~/.m2/repository/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3.jar:~/.m2/repository/com/github/gumtreediff/core/2.1.2/core-2.1.2.jar:~/.m2/repository/org/atteo/classindex/classindex/3.4/classindex-3.4.jar:~/.m2/repository/com/github/mpkorstanje/simmetrics-core/3.2.3/simmetrics-core-3.2.3.jar:~/.m2/repository/commons-codec/commons-codec/1.10/commons-codec-1.10.jar:~/.m2/repository/net/sf/trove4j/trove4j/3.0.3/trove4j-3.0.3.jar:~/.m2/repository/com/google/code/gson/gson/2.8.2/gson-2.8.2.jar:~/.m2/repository/org/jgrapht/jgrapht-core/1.0.1/jgrapht-core-1.0.1.jar:~/.m2/repository/com/github/gumtreediff/client/2.1.2/client-2.1.2.jar:~/.m2/repository/com/github/gumtreediff/gen.jdt/2.1.2/gen.jdt-2.1.2.jar:~/.m2/repository/br/usp/each/saeg/asm-defuse/0.0.6/asm-defuse-0.0.6.jar:~/.m2/repository/org/ow2/asm/asm-analysis/6.0/asm-analysis-6.0.jar:~/.m2/repository/org/ow2/asm/asm-tree/6.0/asm-tree-6.0.jar:~/.m2/repository/br/usp/each/saeg/saeg-commons/0.0.5/saeg-commons-0.0.5.jar:../Implementation/target/artifacts/Implementation_jar/com.microsoft.z3.jar:~/.m2/repository/org/ow2/asm/asm/6.0/asm-6.0.jar:~/.m2/repository/com/github/javaparser/javaparser-core/3.15.2/javaparser-core-3.15.2.jar:~/.m2/repository/org/eclipse/jdt/org.eclipse.jdt.core/3.26.0/org.eclipse.jdt.core-3.26.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.resources/3.21.0/org.eclipse.core.resources-3.21.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.expressions/3.9.400/org.eclipse.core.expressions-3.9.400.jar:~/.m2/repository/net/java/dev/jna/jna/5.14.0/jna-5.14.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.osgi/3.21.0/org.eclipse.osgi-3.21.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.runtime/3.31.100/org.eclipse.core.runtime-3.31.100.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.jobs/3.15.300/org.eclipse.core.jobs-3.15.300.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.contenttype/3.9.400/org.eclipse.core.contenttype-3.9.400.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.filesystem/1.11.0/org.eclipse.core.filesystem-1.11.0.jar:~/.m2/repository/net/java/dev/jna/jna-platform/5.14.0/jna-platform-5.14.0.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.text/3.14.100/org.eclipse.text-3.14.100.jar:~/.m2/repository/org/eclipse/platform/org.eclipse.core.commands/3.12.100/org.eclipse.core.commands-3.12.100.jar:../Implementation/target/artifacts/Implementation_jar/jfxrt.jar Runner.Runner --path1 ${oldV} --path2 ${newV} --tool D --s coral --b ${depth_limit}  > ${directory}/ResultDSE120.txt"
          ;;
    
      *)
          echo "ERROR: Unknown tool '$tool'."
          continue
          ;;
  esac

  # if [ "$print_commands" = true ]; then
  #   echo "${command}"
  # fi

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

  # calculate_time "$seconds_at_start" "$total_runs" "$current_run"
  # printf "\n\n"
done

# Create "materialized views"

# printf "Creating materialized views ... "
# sqlite3 "${DB_PATH}" < "${DB_CREATE_MATERIALIZED_VIEWS_PATH}" > /dev/null
# sqlite3 "${DB_PATH}" < "${DB_CREATE_TABLES_PAPER_PATH}" > /dev/null

printf "done!\n"
