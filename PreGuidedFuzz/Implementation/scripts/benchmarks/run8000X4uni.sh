name=RunningBenchmarksCommonArDiff
benchmark=benchmarks
# name=RunningBenchmarksEqBench1000
# benchmark=EqBench

bash $name.sh 8000 uniform
python3 removeResult.py $benchmark $1 1
bash $name.sh 8000 uniform
python3 removeResult.py $benchmark $1 2
bash $name.sh 8000 uniform
python3 removeResult.py $benchmark $1 3
bash $name.sh 8000 uniform
python3 removeResult.py $benchmark $1 4
bash $name.sh 8000 uniform
python3 removeResult.py $benchmark $1 5