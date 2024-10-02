name=RunningBenchmarks6000
benchmark=benchmarks

# benchmark=EqBench
bash $name.sh
python3 removeResult.py $benchmark $1 1
bash $name.sh
python3 removeResult.py $benchmark $1 2
bash $name.sh
python3 removeResult.py $benchmark $1 3
bash $name.sh
python3 removeResult.py $benchmark $1 4
bash $name.sh
python3 removeResult.py $benchmark $1 5