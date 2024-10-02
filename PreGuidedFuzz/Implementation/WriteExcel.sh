# benchmark=benchmarks
# benchmark=EqBench
benchmark=$1
if [ $benchmark = "EqBench" ]; then
    readFile="readHybridResultEqBench.py"
else
    readFile="readHybridResult.py"
fi
for j in {1..5}; do
        python3 $readFile $benchmark "1Fuzz500"$j 1
    done
for j in {1..5}; do
        python3 $readFile $benchmark "1BFuzz500"$j 1
    done

for i in {1..10}; do
    for j in {1..5}; do
        python3 $readFile $benchmark "1Fuzz"$i"000"$j 1
    done
done

for i in {1..10}; do
    for j in {1..5}; do
        python3 $readFile $benchmark "1BFuzz"$i"000"$j 1
    done
done