for i in {1..10}; do
    for j in {1..5}; do
        rm "../"$1"/Fuzz"$i"000"$j".xlsx"
    done
done

for i in {1..10}; do
    for j in {1..5}; do
        rm "../"$1"/BFuzz"$i"000"$j".xlsx"
    done
done

for j in {1..5}; do
        rm "../"$1"/Fuzz500"$j".xlsx"
    done
for j in {1..5}; do
       rm "../"$1"/BFuzz500"$j".xlsx"
    done