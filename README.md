# HybridNonEqAnalysis
## Baseline Symbolic Approaches
</br> https://github.com/resess/ARDiff.git is used for ARDiff, DSE and IMP-S
</br> https://zenodo.org/records/10033132 for PASDA (sqlite database saving are removed and clean version is saved in the PASDA folder).


## Boundary and Uniform fuzzing
</br> Both are implemented and codes will be found inside the PreGuidedFuzz/Implementation/src/java/differencing/DifferencingRunner.java file
</br> a <fuzzType> and <fuzztime> parameters will be taken to choose the uniform and boundary fuzzing with defined time.

## Pre-Guided-Fuzz Approach
</br> Pre-Guided-Fuzz approach is implemented inside the Pre-Guided-Fuzz appraoch folder 
</br> RunningBenchmarksCommonArDiff.sh will run Pre-Guided-fuzz approach on the programs: bash RunningBenchmarksCommonArDiff.sh 2000 boundary will run fuzzing for 2s with boundary fuzzing





