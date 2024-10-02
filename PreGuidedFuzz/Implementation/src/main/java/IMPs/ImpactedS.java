//MIT-LICENSE
//Copyright (c) 2020-, Sahar Badihi, The University of British Columbia, and a number of other of contributors
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
//to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
//and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package IMPs;

import br.usp.each.saeg.asm.defuse.Variable;
import com.microsoft.z3.Status;
import differencing.StopWatches;
import equiv.checking.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ImpactedS {
    protected String path;
    protected String classPathOld;
    protected String classPathNew;
    protected String methodPathOld;
    protected String methodPathNew;
    protected long[] times;
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
    protected  ArrayList<Integer> changes;
    //Variable to put true to include dummy statements
    protected boolean dummy = false;

    public ImpactedS(
        String path,
        String methodPathOld,
        String methodPathNew,
        int bound,
        int timeout,
        String toolName,
        String SMTSolver,
        int minInt,
        int maxInt,
        double minDouble,
        double maxDouble,
        long minLong,
        long maxLong
    ){
        this.path = path;
        this.methodPathOld = methodPathOld;
        this.methodPathNew = methodPathNew;
        this.bound = bound;
        this.timeout = timeout;
        this.times = new long[5];
        this.toolName = toolName;
        this.SMTSolver = SMTSolver;
        this.minInt = minInt;
        this.maxInt = maxInt;
        this.minDouble = minDouble;
        this.maxDouble = maxDouble;
        this.minLong = minLong;
        this.maxLong = maxLong;
    }

    /**
     * This functions sets up all the file paths
     */
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

    /**
     * This is the main function to run the tool
     */
    public SMTSummary runTool() throws Exception {
        boolean gumTreePassed = false;
        try {
            int iteration = 1;

            ChangeExtractor changeExtractor = new ChangeExtractor();
            this.changes =  changeExtractor.obtainChanges(this.methodPathOld, this.methodPathNew, this.path + "instrumented");
            this.setPathToDummy(changeExtractor.getClasspath());
            gumTreePassed = true;

            StopWatches.stop("run:initialization");
            StopWatches.start("iteration-" + iteration);
            StopWatches.start("iteration-" + iteration + ":symbolic-execution");

            SMTSummary summary = runEquivalenceChecking(iteration);

            StopWatches.stop("iteration-" + iteration + ":symbolic-execution");
            StopWatches.start("iteration-" + iteration + ":program-classification");

            String result = equivalenceResult(summary);

            StopWatches.stop("iteration-" + iteration + ":program-classification");
            StopWatches.start("iteration-" + iteration + ":finalization");

            System.out.println(result);

            Path outputPath = Paths.get(this.path, "..", "outputs", this.toolName + ".txt");
            outputPath.toFile().getParentFile().mkdirs();
            Files.write(outputPath, result.getBytes());

            StopWatches.stop("iteration-" + iteration + ":finalization");
            StopWatches.stop("iteration-" + iteration);
            StopWatches.start("run:finalization");

            return summary;
        } catch (Exception e) {
            if (!gumTreePassed) {
                System.out.println("An error/exception occurred when identifying the changes between the two methods.\n" +
                    "The GumTree module is still under development. Please check your examples or report this issue to us.\n\n");
            } else {
                System.out.println("An error/exception occurred when instrumenting the files or running the equivalence checking. Please report this issue to us.\n\n");
            }
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * This method runs all steps of the equivalence checking
     * Change extraction
     * Program slicing for impacted statements extraction
     * JPF inputs creation and symbolic execution on those
     * Z3 constraint solving on JPF output returning a summary of the execution
     * @return A SMT summary object corresponding to the information and results obtained while running JPF + Z3
     */
    public SMTSummary runEquivalenceChecking(int iteration) throws Exception {
        long start, end;
        start = System.nanoTime();

        ClassNode classNodeOld = new ClassNode();
        ClassReader classReaderOld = new ClassReader(new FileInputStream(this.classPathOld));
        classReaderOld.accept(classNodeOld, 0);
        List<MethodNode> methodNodesOld = classNodeOld.methods;
        MethodNode targetMethodNodeOld = methodNodesOld.get(1);//method 0 is by default the "init" method

        ClassNode classNodeNew = new ClassNode();
        ClassReader classReaderNew = new ClassReader(new FileInputStream(this.classPathNew));
        classReaderNew.accept(classNodeNew, 0);
        List<MethodNode> methodNodesNew = classNodeNew.methods;
        MethodNode targetMethodNodeNew = methodNodesNew.get(1);//method 0 is by default the "init" method

        String classNameOld = "I" + classNodeOld.name.substring(classNodeOld.name.lastIndexOf("/") + 1);
        String classNameNew = "I" + classNodeNew.name.substring(classNodeNew.name.lastIndexOf("/") + 1);

        end = System.nanoTime();
        this.times[0] = end - start;

        //************* To obtain the methods parameters ***********/
        start = System.nanoTime();
        Variable[] variablesNew = DefUseExtractor.getVariables(targetMethodNodeNew);
        Map<String, String> variablesNamesTypesMapping = DefUseExtractor.getVariableTypesMapping(); ////(x, I)
        String[] methodParams = DefUseExtractor.extractParams(targetMethodNodeNew);
        String[] constructorParams = DefUseExtractor.extractParamsConstructor(methodNodesNew.get(0));

        //*****To be replaced , after running more tests******/
        //Overwrite the files instead of creating new files or adjust the paths given to commonBlockExtractor
        if (Utils.DEBUG) System.out.println("changes are " + this.changes);
        ///////////////////////////////////////////////////
        ProgramSlicer programSlicer = new ProgramSlicer(this.changes);
        programSlicer.impactedStatements(this.methodPathOld, targetMethodNodeOld);
        ArrayList<Integer> listOne = new ArrayList<>(programSlicer.getImpactedStatements());
        //////////////////////////////////////////////////////
        ProgramSlicer programSlicerNew = new ProgramSlicer(this.changes);
        programSlicer.impactedStatements(this.methodPathNew, targetMethodNodeNew);
        ArrayList<Integer> listTwo = new ArrayList<>(programSlicerNew.getImpactedStatements());
        //////////////////////////////////////////////////////
        Set<Integer> set = new LinkedHashSet<>(listOne);
        set.addAll(listTwo);
        programSlicer.impactedStatements = new ArrayList<>(set);
        Collections.sort(programSlicer.impactedStatements);
        if (Utils.DEBUG) {
            System.out.println("Impacted statements combined " + programSlicer.getImpactedStatements());
        }

        //*****End ******/

        Instrumentation instrument = new Instrumentation(this.path, this.toolName, iteration);

        //****Generating the main methods of each class ******/
        String mainMethodOld = instrument.getMainProcedure(classNameOld, targetMethodNodeOld.name, methodParams, constructorParams, variablesNamesTypesMapping);
        String mainMethodNew = instrument.getMainProcedure(classNameNew, targetMethodNodeNew.name, methodParams, constructorParams, variablesNamesTypesMapping);

        //*************Creating the new class files with the added main method ***************/
        instrument.setMethods(methodNodesOld);
        instrument.saveNewProcedure(this.methodPathOld, classNameOld, null, null, mainMethodOld);
        instrument.setMethods(methodNodesNew);
        instrument.saveNewProcedure(this.methodPathNew, classNameNew, null, null, mainMethodNew);

        end = System.nanoTime();
        this.times[1] = end - start;

        //*********************Running the symbolic execution ******************/
        start = System.nanoTime();

        SymbolicExecutionRunner symbEx = new SymbolicExecutionRunner(
            this.path,
            instrument.packageName(),
            classNameOld + this.toolName + iteration,
            classNameNew + this.toolName + iteration,
            targetMethodNodeNew.name,
            methodParams.length,
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

        end = System.nanoTime();
        this.times[2] = end - start;

        start = System.nanoTime();

        SMTSummary summary = new ImpactedSSummary(
            this.path,
            classNameOld + this.toolName + iteration,
            classNameNew + this.toolName + iteration,
            this.timeout,
            programSlicer.getImpactedStatements()
        );
        summary.checkEquivalence();

        end = System.nanoTime();
        this.times[3] = end - start;
        this.times[4] = summary.z3time;

        Path modelPath = Paths.get(this.path, "..", "z3models", this.toolName + ".txt");
        modelPath.toFile().getParentFile().mkdirs();
        Files.write(modelPath, summary.toWrite.getBytes());

        return summary;
    }

    /**
     * This function outputs the result of equivalence checking based on the input
     * @param smtSummary the summary of the runs
     * @return the final output
     */
    public String equivalenceResult(SMTSummary smtSummary) {
        //check the status here
        String result = "-----------------------Results-------------------------------------------\n";
        result += "  -Initialization : " + times[0] / (Math.pow(10, 6)) + " ms\n";
        result += "  -Program slicing : " + times[1] / (Math.pow(10, 6)) + " ms\n";
        result += "  -Symbolic execution  : " + times[2] / (Math.pow(10, 6)) + " ms\n";
        result += "  -Creating Z3 expressions  : " + times[3] / (Math.pow(10, 6)) + " ms\n";
        result += "   -Constraint solving : " + times[4] / (Math.pow(10, 6)) + " ms\n";
        if (smtSummary.status == Status.UNSATISFIABLE) {
            result += "Output : EQUIVALENT";
        } else if (smtSummary.status == Status.UNKNOWN) {
            result += "Output : UNKNOWN \n";
            result += "Reason: " + smtSummary.reasonUnknown;
        } else {
            //smtSummary.functionalDelta();
            result += "Output : NOT EQUIVALENT";
        }
        smtSummary.context.close();
        result += "\n-----------------------END-------------------------------------------\n";
        return result;
    }
}
