//MIT-LICENSE
//Copyright (c) 2020-, Sahar Badihi, The University of British Columbia, and a number of other of contributors
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
//to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
//and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package GradDiff;

import DSE.DSE;
import com.microsoft.z3.Status;
import differencing.StopWatches;
import equiv.checking.ChangeExtractor;
import equiv.checking.OutputParser;
import equiv.checking.SMTSummary;
import equiv.checking.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class GradDiff extends DSE {
    /** This class runs ARDiff **/
    private final boolean H1;
    private final boolean H2;
    private final boolean H31;
    private final boolean H32;
    private final String strategy;

    private boolean onGoing = true;

    public GradDiff(
        String path,
        String path1,
        String path2,
        int bound,
        int timeout,
        String toolName,
        String SMTSolver,
        int minInt,
        int maxInt,
        double minDouble,
        double maxDouble,
        long minLong,
        long maxLong,
        boolean H1,
        boolean H2,
        boolean H31,
        boolean H32,
        String strategy
    ) {
        super(path, path1, path2, bound, timeout, toolName, SMTSolver, minInt, maxInt, minDouble, maxDouble, minLong, maxLong);
        this.H1 = H1;
        this.H2 = H2;
        this.H31 = H31;
        this.H32 = H32;
        this.strategy = strategy;
    }

    /**
     * This is the main function to run ARDiff
     */
    public SMTSummary runTool() throws Exception {
        boolean gumTreePassed = false;
        try {
            ChangeExtractor changeExtractor = new ChangeExtractor();
            ArrayList<Integer> changes = changeExtractor.obtainChanges(this.methodPathOld, this.methodPathNew, this.path + "instrumented");
            this.setPathToDummy(changeExtractor.getClasspath());
            gumTreePassed = true;
            SMTSummary summary = null;
            String result = "";
            int iteration = 0;
            String finalRes = "";

            GradDiffInstrumentation instrumentation = new GradDiffInstrumentation(
                this.toolName, this.path,
                this.classPathOld, this.classPathNew,
                this.methodPathOld, this.methodPathNew,
                this.timeout,
                this.H1, this.H2, this.H31, this.H32, this.strategy
            );

            StopWatches.stop("run:initialization");

            while (this.onGoing) {
                iteration++;
                StopWatches.start("iteration-" + iteration);

                result += "-----------------------Iteration : " + iteration + " -------------------------------------------\n";

                StopWatches.start("iteration-" + iteration + ":instrumentation");

                instrumentation.runInstrumentation(iteration, changes);
                this.times[0] = instrumentation.getInitializationRuntime();
                this.totalTimes[0] = instrumentation.getTotalInitializationRuntime();
                this.times[1] = instrumentation.getDefUseAndUifRuntime();
                this.totalTimes[1] = instrumentation.getTotalDefUseAndUifRuntime();

                StopWatches.stop("iteration-" + iteration + ":instrumentation");
                StopWatches.start("iteration-" + iteration + ":symbolic-execution");

                summary = this.runEquivalenceChecking(instrumentation);

                StopWatches.stop("iteration-" + iteration + ":symbolic-execution");

                finalRes = this.equivalenceResult(summary, changes, instrumentation, iteration);
                result += finalRes;

                StopWatches.start("iteration-" + iteration + ":finalization");

                Path outputPath = Paths.get(this.path, "..", "outputs", this.toolName + ".txt");
                outputPath.toFile().getParentFile().mkdirs();
                Files.write(outputPath, result.getBytes());

                Path modelPath = Paths.get(this.path, "..", "z3models", this.toolName + ".txt");
                modelPath.toFile().getParentFile().mkdirs();
                Files.write(modelPath, summary.toWrite.getBytes());

                StopWatches.stop("iteration-" + iteration + ":finalization");
                StopWatches.stop("iteration-" + iteration);
            }

            StopWatches.start("run:finalization");

            System.out.println(updateUserOutput(finalRes));

            assert summary != null;

            return summary;
        } catch (Exception e) {
            if (!gumTreePassed)
                System.out.println("An error/exception occurred when identifying the changes between the two methods.\n" +
                    "The GumTree module is still under development. Please check your examples or report this issue to us.\n\n");
            else
                System.out.println("An error/exception occurred when instrumenting the files or running the equivalence checking. Please report this issue to us.\n\n");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * This function outputs the result for the equivalence of two programs
     * @param smtSummary the summaries for both programs to be compared
     * @return the equivalence result as a string
     */
    public String equivalenceResult(
        SMTSummary smtSummary,
        ArrayList<Integer> changes,
        GradDiffInstrumentation instrumentation,
        int iteration
    ) throws IOException {
        StopWatches.start("iteration-" + iteration + ":program-classification");
        if (Utils.DEBUG)
            System.out.println("-----------------------The current status-------------------------------------------\n");
        if (Utils.DEBUG) System.out.println(smtSummary.status);
        String result = "-----------------------Results-------------------------------------------\n";
        if (smtSummary.status == Status.UNSATISFIABLE) {
            this.onGoing = false;
            result += "  -Initialization : " + this.times[0] / (Math.pow(10, 6)) + " ms\n";
            result += "  -Def-use and uninterpreted functions : " + this.times[1] / (Math.pow(10, 6)) + " ms\n";
            result += "  -Symbolic execution  : " + this.times[2] / (Math.pow(10, 6)) + " ms\n";
            result += "  -Creating Z3 expressions  : " + this.times[3] / (Math.pow(10, 6)) + " ms\n";
            result += "   -Constraint solving : " + this.times[4] / (Math.pow(10, 6)) + " ms\n";
            result += "Output : EQUIVALENT";
            result += "\n------------------------------END----------------------------------------\n";
            smtSummary.context.close();
            StopWatches.stop("iteration-" + iteration + ":program-classification");
            return result;
        } else if (smtSummary.status == Status.UNKNOWN) {
            result += "  -Initialization : " + this.times[0] / (Math.pow(10, 6)) + " ms\n";
            result += "  -Def-use and uninterpreted functions : " + this.times[1] / (Math.pow(10, 6)) + " ms\n";
            result += "  -Symbolic execution  : " + this.times[2] / (Math.pow(10, 6)) + " ms\n";
            result += "  -Creating Z3 expressions  : " + this.times[3] / (Math.pow(10, 6)) + " ms\n";
            result += "  -Constraint solving : " + this.times[4] / (Math.pow(10, 6)) + " ms\n";
            result += "Output : UNKNOWN \n";
            result += "Reason: " + smtSummary.reasonUnknown;
            if (smtSummary.reasonUnknown.contains("JPF-symbc") || smtSummary.noUFunctions) {
                this.onGoing = false;
                result += "\n------------------------------NOTHING To REFINE---------------------------------------\n";
                result += "\n------------------------------END Of REFINEMENT----------------------------------------\n";
                result += "\n------------------------------END----------------------------------------\n";
                smtSummary.context.close();
                StopWatches.stop("iteration-" + iteration + ":program-classification");
                return result;
            }
            result += "\n------------------------------To be continued (REFINING)----------------------------------------\n";
            //Here we should not run the first heuristic thus ?
            //H1 = false;
        } else { // if (smtSummary.status == Status.SATISFIABLE)
            result += "  -Initialization : " + this.times[0] / (Math.pow(10, 6)) + " ms\n";
            result += "  -Def-use and uninterpreted functions : " + this.times[1] / (Math.pow(10, 6)) + " ms\n";
            result += "  -Symbolic execution  : " + this.times[2] / (Math.pow(10, 6)) + " ms\n";
            result += "  -Creating Z3 expressions  : " + this.times[3] / (Math.pow(10, 6)) + " ms\n";
            result += "  -Constraint solving : " + this.times[4] / (Math.pow(10, 6)) + " ms\n";
            if (smtSummary.noUFunctions) {
                this.onGoing = false;
                result += "Output : NOT EQUIVALENT\n";
                result += "\n------------------------------NOTHING To REFINE---------------------------------------\n";
                result += "\n------------------------------END Of REFINEMENT----------------------------------------\n";
                result += "\n------------------------------END----------------------------------------\n";
                smtSummary.context.close();
                StopWatches.stop("iteration-" + iteration + ":program-classification");
                return result;
            }
            ////////////////////////////////refinement might help//////////////////////////////////
            long beg = System.nanoTime();
            //check for SATISFAIABLITY: the possiblity to be equivalent
            Status status = this.checkForSatisfiability(smtSummary);
            long endF = System.nanoTime() - beg;
            if (Utils.DEBUG) System.out.println("checking for satisfiability");
            if (Utils.DEBUG) System.out.println(status.toString());
            if (status == Status.UNSATISFIABLE) {// no chance to be EQUIVALENT
                this.onGoing = false;
                if (Utils.DEBUG) {
                    result += "Output : NOT EQUIVALENT\n";
                    result += "After checking for satisfiability : \n";
                    result += "-----------------------FINAL RESULTS-------------------------------------------\n";
                    result += "   -Constraint solving : " + endF / (Math.pow(10, 6)) + " ms\n";
                    result += "Output : NOT EQUIVALENT";
                    result += "\n------------------------------END----------------------------------------\n";
                } else {
                    result += "Output : NOT EQUIVALENT\n";
                    result += "\n------------------------------NOTHING To REFINE---------------------------------------\n";
                    result += "\n------------------------------END Of REFINEMENT----------------------------------------\n";
                    result += "\n------------------------------END----------------------------------------\n";
                }
                smtSummary.context.close();
                StopWatches.stop("iteration-" + iteration + ":program-classification");
                return result;
            } else {
                result += "Output : UNKNOWN \n";
                result += "Reason : solver found a counterexample, but it could be due to too much abstraction";
            }
        }

        StopWatches.stop("iteration-" + iteration + ":program-classification");
        StopWatches.start("iteration-" + iteration + ":refinement");

        // if ((neqResult == UNKNOWN && hasUif) || (neqResult == SAT && eqResult != SAT && hasUif))
        result += "\n------------------------------To be continued (REFINING)----------------------------------------\n";

        long start = System.nanoTime();
        String s = instrumentation.getNextToRefine(
            smtSummary.context,
            smtSummary.summaryOld,
            smtSummary.summaryNew,
            smtSummary.variables
        );
        long end = System.nanoTime();

        if (s.isEmpty()) {
            if (Utils.DEBUG) System.out.println("Nothing to refine");
            this.onGoing = false;
            this.times[3] += end - start;
            this.totalTimes[3] += times[3];
            result += "\n------------------------------NOTHING To REFINE---------------------------------------\n";
            result += "\n------------------------------END Of REFINEMENT----------------------------------------\n";
            result += "\n------------------------------END----------------------------------------\n";
            smtSummary.context.close();
        } else {
            if (Utils.DEBUG) System.out.println("----------------------------refining----------------------------");
            instrumentation.expandFunction(s, changes);
            end = System.nanoTime();
            this.times[3] += end - start;
            this.totalTimes[3] += times[3];
        }

        StopWatches.stop("iteration-" + iteration + ":refinement");

        return result;
    }

    public String updateUserOutput(String finalResult) {
        String newResult = "";
        String[] lines = finalResult.split("\n");
        newResult += lines[0] + "\n";
        for (int i = 1; i < 6; i++) {
            newResult += lines[i].replaceAll(":\\s.+\\sms", ": " + (this.totalTimes[i - 1] / Math.pow(10, 6)) + " ms") + "\n";
        }
        for (int i = 6; i < lines.length; i++)
            newResult += lines[i] + "\n";
        return newResult;

    }
}
