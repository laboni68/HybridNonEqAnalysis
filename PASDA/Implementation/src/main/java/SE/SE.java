//MIT-LICENSE
//Copyright (c) 2020-, Sahar Badihi, The University of British Columbia, and a number of other of contributors
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
//to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
//and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package SE;

import com.microsoft.z3.Status;
import differencing.StopWatches;
import equiv.checking.ChangeExtractor;
import equiv.checking.OutputParser;
import equiv.checking.SMTSummary;
import equiv.checking.SymbolicExecutionRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class SE {
    protected String path;
    protected String methodPathOld;
    protected String methodPathNew;
    protected String classPathOld;
    protected String classPathNew;
    protected long[] times = new long[5];
    protected int bound;
    protected int timeout;
    protected String toolName;
    protected String SMTSolver;
    protected int minInt;
    protected int maxInt;
    protected double minDouble;
    protected double maxDouble;
    protected long minLong;
    protected long maxLong;

    public SE(
        String path,
        String path1,
        String path2,
        int bound,
        int timeout,
        String tool,
        String SMTSolver,
        int minInt,
        int maxInt,
        double minDouble,
        double maxDouble,
        long minLong,
        long maxLong
    ) {
        this.methodPathOld = path1;
        this.methodPathNew = path2;
        this.path = path;
        this.bound = bound;
        this.timeout = timeout;
        this.toolName = tool;
        this.SMTSolver = SMTSolver;
        this.minInt = minInt;
        this.maxInt = maxInt;
        this.minDouble = minDouble;
        this.maxDouble = maxDouble;
        this.minLong = minLong;
        this.maxLong = maxLong;
    }

    public void setPathToDummy(String classpath) {
        this.path = this.path + "instrumented";
        int index = this.methodPathOld.lastIndexOf("/");
        int index2 = this.methodPathNew.lastIndexOf("/");
        String package1 = this.methodPathOld.substring(index + 1);
        String package2 = this.methodPathNew.substring(index2 + 1);
        this.methodPathOld = this.path + "/" + package1;
        this.methodPathNew = this.path + "/" + package2;
        this.classPathOld = "target/classes/" + classpath + "/" + package1.split("\\.")[0] + ".class";
        this.classPathNew = "target/classes/" + classpath + "/" + package2.split("\\.")[0] + ".class";
    }

    public SMTSummary runTool() throws Exception {
        try {
            int iteration = 1;

            ChangeExtractor changeExtractor = new ChangeExtractor();
            ArrayList<Integer> changes = changeExtractor.obtainChanges(this.methodPathOld, this.methodPathNew, this.path + "instrumented");
            this.setPathToDummy(changeExtractor.getClasspath());

            SEInstrumentation instrumentation = new SEInstrumentation(
                this.toolName, this.path,
                this.classPathOld, this.classPathNew,
                this.methodPathOld, this.methodPathNew,
                this.timeout
            );

            StopWatches.stop("run:initialization");
            StopWatches.start("iteration-" + iteration);
            StopWatches.start("iteration-" + iteration + ":instrumentation");

            instrumentation.runInstrumentation(iteration, changes);
            this.times[0] = instrumentation.getInitializationRuntime();
            this.times[1] = instrumentation.getDefUseAndUifRuntime();

            StopWatches.stop("iteration-" + iteration + ":instrumentation");
            StopWatches.start("iteration-" + iteration + ":symbolic-execution");

            SMTSummary summary = this.runEquivalenceChecking(instrumentation);

            StopWatches.stop("iteration-" + iteration + ":symbolic-execution");
            StopWatches.start("iteration-" + iteration + ":program-classification");

            String result = this.equivalenceResult(summary);

            StopWatches.stop("iteration-" + iteration + ":program-classification");
            StopWatches.start("iteration-" + iteration + ":finalization");

            System.out.println(result);

            Path resultPath = Paths.get(this.path, "..", "outputs", this.toolName + ".txt");
            resultPath.getParent().toFile().mkdirs();
            Files.write(resultPath, result.getBytes());

            Path modelsPath = Paths.get(this.path, "..", "z3models", this.toolName + ".txt");
            modelsPath.getParent().toFile().mkdirs();
            Files.write(modelsPath, summary.toWrite.getBytes());

            StopWatches.stop("iteration-" + iteration + ":finalization");
            StopWatches.stop("iteration-" + iteration);
            StopWatches.start("run:finalization");

            return summary;
        } catch (Exception e) {
            System.out.println("An error/exception occurred when instrumenting the files or running the equivalence checking. Please report this issue to us.\n\n");
            e.printStackTrace();
            throw e;
        }
    }

    public SMTSummary runEquivalenceChecking(SEInstrumentation instrumentation) throws Exception {
        /**********************Running the symbolic execution ******************/

        long start = System.nanoTime();

        SymbolicExecutionRunner symbEx = new SymbolicExecutionRunner(
            this.path,
            instrumentation.getPackageName(),
            instrumentation.getOldClassName(),
            instrumentation.getNewClassName(),
            instrumentation.getMethodName(),
            instrumentation.getMethodParameterCount(),
            this.bound,
            this.timeout,
            this.SMTSolver,
            this.minInt,
            this.maxInt,
            this.minDouble,
            this.maxDouble,
            this.minLong,
            this.maxLong
        );

        symbEx.creatingJpfFiles();
        symbEx.runningJavaPathFinder();

        long end = System.nanoTime();
        this.times[2] = end - start;
        start = System.nanoTime();

        SMTSummary summary = new SMTSummary(
            this.path,
            instrumentation.getOldClassName(),
            instrumentation.getNewClassName(),
            this.timeout
        );
        summary.checkEquivalence();

        end = System.nanoTime();
        this.times[3] = end - start;
        this.times[4] = summary.z3time;

        return summary;
    }

    /**
     * This function outputs the result of equivalence checking based on the input
     *
     * @param smtSummary the summary of the runs
     * @return the final output
     */
    public String equivalenceResult(SMTSummary smtSummary) {
        //check the status here
        String result = "-----------------------Results-------------------------------------------\n";
        result += "  -Def-use and uninterpreted functions : " + this.times[1] / (Math.pow(10, 6)) + "\n";
        result += "  -Symbolic execution  : " + this.times[2] / (Math.pow(10, 6)) + " ms\n";
        result += "  -Creating Z3 expressions  : " + this.times[3] / (Math.pow(10, 6)) + " ms\n";
        result += "  -Constraint solving : " + this.times[4] / (Math.pow(10, 6)) + " ms\n";

        if (smtSummary.status == Status.UNSATISFIABLE) {
            result += "Output : EQUIVALENT";
        } else if (smtSummary.status == Status.SATISFIABLE) {
            result += "Output : NOT EQUIVALENT";
        } else if (smtSummary.status == Status.UNKNOWN) {
            result += "Output : UNKNOWN \n";
            result += "Reason: " + smtSummary.reasonUnknown;
        } else {
            throw new RuntimeException("Unknown solver status '" + smtSummary.status + "'.");
        }

        smtSummary.context.close();
        result += "\n-----------------------END-------------------------------------------\n";
        return result;
    }
}
