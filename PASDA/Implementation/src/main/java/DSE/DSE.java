//MIT-LICENSE
//Copyright (c) 2020-, Sahar Badihi, The University of British Columbia, and a number of other of contributors
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
//to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
//and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package DSE;

import com.microsoft.z3.Status;
import differencing.StopWatches;
import equiv.checking.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class DSE {
    /** The class to run DSE **/
    protected String path;
    protected String classPathOld;
    protected String classPathNew;
    protected String methodPathOld;
    protected String methodPathNew;
    protected long[] times = new long[5];
    protected long[] totalTimes = new long[5];
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

    public DSE(
        String path,
        String methodPathOld,
        String methodPathNew,
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
        this.path = path;
        this.methodPathOld = methodPathOld;
        this.methodPathNew = methodPathNew;
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

    /**
     * This is a setup method to set the correct file paths
     */
    public void setPathToDummy(String classpath) {
        this.path = this.path + "instrumented";
        int indexOld = this.methodPathOld.lastIndexOf("/");
        int indexNew = this.methodPathNew.lastIndexOf("/");
        String packageOld = this.methodPathOld.substring(indexOld + 1);
        String packageNew = this.methodPathNew.substring(indexNew + 1);
        this.methodPathOld = this.path + "/" + packageOld;
        this.methodPathNew = this.path + "/" + packageNew;
        this.classPathOld = "target/classes/" + classpath + "/" + packageOld.split("\\.")[0] + ".class";
        this.classPathNew = "target/classes/" + classpath + "/" + packageNew.split("\\.")[0] + ".class";
    }

    /**
     * The main method to run DSE
     */
    public SMTSummary runTool() throws Exception {
        try {
            int iteration = 1;

            ChangeExtractor changeExtractor = new ChangeExtractor();
            ArrayList<Integer> changes = changeExtractor.obtainChanges(this.methodPathOld, this.methodPathNew, this.path + "instrumented");
            this.setPathToDummy(changeExtractor.getClasspath());

            DSEInstrumentation instrumentation = new DSEInstrumentation(
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
            this.totalTimes[0] = instrumentation.getTotalInitializationRuntime();
            this.times[1] = instrumentation.getDefUseAndUifRuntime();
            this.totalTimes[1] = instrumentation.getTotalDefUseAndUifRuntime();

            StopWatches.stop("iteration-" + iteration + ":instrumentation");
            StopWatches.start("iteration-" + iteration + ":symbolic-execution");

            SMTSummary summary = this.runEquivalenceChecking(instrumentation);

            StopWatches.stop("iteration-" + iteration + ":symbolic-execution");
            StopWatches.start("iteration-" + iteration + ":program-classification");

            String result = this.equivalenceResult(summary);

            StopWatches.stop("iteration-" + iteration + ":program-classification");
            StopWatches.start("iteration-" + iteration + ":finalization");

            System.out.println(result);

            Path outputPath = Paths.get(this.path, "..", "outputs", this.toolName + ".txt");
            outputPath.toFile().getParentFile().mkdirs();
            Files.write(outputPath, result.getBytes());

            Path modelPath = Paths.get(this.path, "..", "z3models", this.toolName + ".txt");
            modelPath.toFile().getParentFile().mkdirs();
            Files.write(modelPath, summary.toWrite.getBytes());

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

    /**
     * This method runs all steps of the equivalence checking
     * Change extraction
     * Common-block extraction
     * Def-use extraction to create the uninterpreted functions
     * JPF inputs creation and symbolic execution on those
     * Z3 constraint solving on JPF output returning a summary of the execution
     *
     * @return A SMT summary object corresponding to the information and results obtained while running JPF + Z3
     */
    public SMTSummary runEquivalenceChecking(DSEInstrumentation instrumentation) throws Exception {
        //*********************Running the symbolic execution ******************/
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
        this.totalTimes[2] += this.times[2];

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
        this.totalTimes[3] += this.times[3];
        this.times[4] = summary.z3time;
        this.totalTimes[4] += this.times[4];

        return summary;
    }

    /**
     * This function outputs the result of equivalence checking based on the input
     *
     * @param smtSummary the summary of the runs
     * @return the final output
     */
    public String equivalenceResult(SMTSummary smtSummary) throws IOException {
        //check the status here
        String result = "-----------------------Results-------------------------------------------\n";
        result += "  -Def-use and uninterpreted functions : " + times[1] / (Math.pow(10, 6)) + "\n";
        result += "  -Symbolic execution  : " + times[2] / (Math.pow(10, 6)) + " ms\n";
        result += "  -Creating Z3 expressions  : " + times[3] / (Math.pow(10, 6)) + " ms\n";
        result += "  -Constraint solving : " + times[4] / (Math.pow(10, 6)) + " ms\n";
        if (smtSummary.status == Status.UNSATISFIABLE)
            result += "Output : EQUIVALENT";
        else if (smtSummary.status == Status.UNKNOWN) {
            result += "Output : UNKNOWN \n";
            result += "Reason: " + smtSummary.reasonUnknown;
        } else {
            if (Utils.DEBUG) System.out.println(smtSummary.solver.getModel());
            //Here we are going to check for satisfiability
            Status status = this.checkForSatisfiability(smtSummary);
            if (status == Status.UNSATISFIABLE) {//No chance to be EQUIVALENT
                result += "Output : NOT EQUIVALENT";
            } else {
                result += "Output : UNKNOWN \n";
                result += "Reason : solver found a counterexample, but it could be due to too much abstraction";
            }
        }

        smtSummary.context.close();
        result += "\n-----------------------END-------------------------------------------\n";
        return result;
    }

    /**
     * This is a helper function to check for the satisfiability of the formulas contained in the input
     */
    public Status checkForSatisfiability(SMTSummary smtSummary) throws IOException {
        if (smtSummary.noUFunctions) {
            return Status.UNSATISFIABLE;
        }
        return this.runZ3FromTerminal(smtSummary);
    }

    public Status runZ3FromTerminal(SMTSummary smtSummary) throws IOException {
        Status status = null;
        String oldSummary = smtSummary.firstSummary.replace(" Ret ", " Ret_1 ");
        String newSummary = smtSummary.secondSummary.replace(" Ret ", " Ret_2 ");
        String toSolve = smtSummary.declarations
            + "(assert (" + oldSummary + "))\n"
            + "(assert (" + newSummary + "))\n"
            + "(assert (= Ret_1 Ret_2))\n"
            + "(check-sat-using (then smt (par-or simplify aig solve-eqs qfnra-nlsat)))";
        String tmp2 = methodPathOld.replace("/", "").replace(".", "");
        File tmp = File.createTempFile(toolName + tmp2 + "SATCHECK", null);
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(tmp));
        bw2.write(toSolve);
        bw2.close();
        String mainCommand = ProjectPaths.z3 + " -smt2 " + tmp.getAbsolutePath() + " -t:" + timeout;
        if (Utils.DEBUG) System.out.println(mainCommand);
        Process p1 = Runtime.getRuntime().exec(mainCommand);
        BufferedReader in = new BufferedReader(new InputStreamReader(p1.getInputStream()));
        String answer = in.readLine();
        switch (answer) {
            case "sat":
                status = Status.SATISFIABLE;
                break;
            case "unsat":
                status = Status.UNSATISFIABLE;
                break;
            default:
                status = Status.UNKNOWN;
                break;
        }
        tmp.deleteOnExit();
        return status;
    }
}
