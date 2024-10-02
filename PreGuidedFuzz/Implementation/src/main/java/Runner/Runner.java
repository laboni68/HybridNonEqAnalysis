//MIT-LICENSE
//Copyright (c) 2020-, Sahar Badihi, The University of British Columbia, and a number of other of contributors
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
//to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
//and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package Runner;

import DSE.DSE;
import GradDiff.GradDiff;
import IMPs.ImpactedS;
import Hybrid.Hybrid;
import SE.SE;
import com.microsoft.z3.Context;
import differencing.StopWatches;
import differencing.TimeFactory;
import differencing.classification.Classification;
import differencing.classification.RunClassifier;
import differencing.models.Benchmark;
import differencing.models.Iteration;
import differencing.models.Run;
import differencing.models.Settings;
import differencing.repositories.*;
import equiv.checking.OutputParser;
import equiv.checking.ProjectPaths;
import equiv.checking.SMTSummary;
import equiv.checking.Utils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static equiv.checking.Utils.*;

public class Runner{
    /** This is the main class to run each of the three tools **/
    protected String path;
    protected String MethodPath1,MethodPath2;
    protected Context context;

    public void setup(String path1, String path2) throws Exception {
        if(SystemUtils.IS_OS_WINDOWS && path1.contains("\\") || path2.contains("\\")){
            path1 = path1.replace("\\","/");
            path2 = path2.replace("\\","/");
        }
        int index = path1.lastIndexOf("/");
        if (index == -1)
            path = "";
        else {
            path = path1.substring(0,index + 1);
            MethodPath1 = path1;
            MethodPath2 = path2;
        }
        if(SystemUtils.IS_OS_LINUX) {
            File file = new File(ProjectPaths.z3);
            if(!file.canExecute()){
                boolean success = file.setExecutable(true);
                if(!success){
                    System.out.println(ANSI_RED + "Please make the z3 file in the current folder executable before proceeding" + ANSI_RESET);
                    System.exit(1);
                }
            }
            ProjectPaths.z3 = "./z3";
        }
    }

    public void setup(String path) throws Exception {
        this.path = path;
        if(!path.endsWith("/"))
            this.path+="/";
            String path1 = this.path + "oldV.java";
            String path2 = this.path + "newV.java";
        MethodPath1 = path1;
        MethodPath2 = path2;
        if(SystemUtils.IS_OS_LINUX) {
            File file = new File(ProjectPaths.z3);
            if(!file.canExecute()){
                boolean success = file.setExecutable(true);
                if(!success){
                    System.out.println(ANSI_RED + "Please make the z3 file in the current folder executable before proceeding" + ANSI_RESET);
                    System.exit(1);
                }
            }
            ProjectPaths.z3 = "./z3";
        }
    }

    private static void checkBound(Runner runner,int bound) throws FileNotFoundException {
        int minRange = 5,maxRange = 6;
        long time = System.currentTimeMillis();
        int numLoops = Math.max(Utils.extractLoops(runner.MethodPath1),Utils.extractLoops(runner.MethodPath2));
        long end = System.currentTimeMillis();
        int factor = 2, shift = 1;
        minRange = factor*numLoops + shift;
        maxRange = factor*numLoops + shift;
        if(bound < maxRange)
            System.out.println("[WARNING] If you want to have a complete summary (exercise all behaviors), make sure your bound is big enough.");
    }

    public static void runTool(String tool, String p1, String p2, String solver,int b,int t, int minInt,int maxInt,double minDouble,double maxDouble,String strategy) throws IOException {
        StopWatches.start("run");
        StopWatches.start("run:initialization");

        String toolName = getToolName(tool, strategy);

        Benchmark benchmark = new Benchmark(getBenchmarkName(p1), getBenchmarkExpected(p1));
        System.out.println("benchmark info"+ benchmark.benchmark + " "+ benchmark.expected);

        // BenchmarkRepository.insertOrUpdate(benchmark);

        Run run = new Run(benchmark.benchmark);
        // RunRepository.insertOrUpdate(run);
        run.id=0;

        int solverTimeout = t / 3;
        Settings settings = new Settings(run.id, getToolVariant(tool, strategy), t, t, solverTimeout, b);
        // SettingsRepository.insertOrUpdate(settings);

        PrintStream systemError = System.err;
        Thread shutdownHook = new Thread(() -> {
            try {
                Map<Integer, Iteration> iterations = OutputParser.readIterations(run, toolName, "", true);
                IterationRepository.insertOrUpdate(iterations.values());

                Iteration resultIteration = new RunClassifier(iterations).getClassificationIteration();
                Iteration lastIteration = iterations.get(iterations.size());

                Run finishedRun = new Run(
                    benchmark.benchmark,
                    resultIteration.result,
                    lastIteration.hasTimedOut,
                    resultIteration.isDepthLimited,
                    resultIteration.hasUif,
                    lastIteration.iteration,
                    resultIteration.iteration,
                    StopWatches.getTime("run"),
                    lastIteration.errors
                );

                finishedRun.id = run.id;
                // RunRepository.insertOrUpdate(finishedRun);

                TimeRepository.insertOrUpdate(TimeFactory.create(finishedRun, StopWatches.getTimes()));
            } catch (Throwable e) {
                e.printStackTrace(systemError);
            }
        });

        Map<Integer, Iteration> iterations;
        String errors = "";

        try {
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            runToolInternal(tool, p1, p2, solver, b, solverTimeout, minInt, maxInt, minDouble, maxDouble, strategy);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            errors = ExceptionUtils.getStackTrace(e);
        } finally {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        }

        iterations = OutputParser.readIterations(run, toolName, errors, false);
        // IterationRepository.insertOrUpdate(iterations.values());

        Iteration resultIteration = new RunClassifier(iterations).getClassificationIteration();
        Iteration lastIteration = iterations.get(iterations.size());

        Run finishedRun = new Run(
            run.benchmark,
            resultIteration.result,
            lastIteration.hasTimedOut,
            resultIteration.isDepthLimited,
            resultIteration.hasUif,
            lastIteration.iteration,
            resultIteration.iteration,
            StopWatches.getTime("run"),
            lastIteration.errors
        );

        finishedRun.id = run.id;
        // RunRepository.insertOrUpdate(finishedRun);

        // Only stop the stopwatches on successful runs.
        // On unsuccessful runs, we cannot be sure they were started.
        if (finishedRun.errors.isEmpty()) {
            StopWatches.stop("run:finalization");
            StopWatches.stop("run");
        }

        // TimeRepository.insertOrUpdate(TimeFactory.create(finishedRun, StopWatches.getTimes()));
    }

    private static String getToolVariant(String tool, String strategy) {
        return getToolName(tool, strategy) + "-base";
    }

    private static String getToolName(String tool, String strategy) {
        if (tool.contains("S")) {
            return "SE";
        } else if (tool.contains("D")) {
            return "DSE";
        } else if (tool.contains("A")) {
            if (strategy.equals("R")) {
                return "ARDiff-R";
            } else if (strategy.equals("H3")) {
                return "ARDiff-H3";
            } else {
                return "ARDiff";
            }
        } else if (tool.contains("I")) {
            return "Imp";
        } else if (tool.contains("H")) {
            return "Hybrid";
        }
        throw new RuntimeException("Unknown tool '" + tool + "'.");
    }

    private static String getBenchmarkName(String path1) {
        // Path path = Paths.get("../benchmarks/").relativize(Paths.get(path1));
        Path path = Paths.get(path1);
        // Path path = Paths.get("../EqBench/").relativize(Paths.get(path1));
        return path.subpath(0, 3).toString();
    }

    private static String getBenchmarkExpected(String path1) {
        // Path path = Paths.get("../benchmarks/").relativize(Paths.get(path1));;
        // // System.out.println("path: "+path);
        //  Path path2 = Paths.get("../EqBench/").relativize(Paths.get(path1));
        // System.out.println("path2: "+path2);
        // String name1 = path2.getName(1).toString();
        // String name2 = path2.getName(2).toString();
        // String name1 = path1.split("/")[path1.split("/").length-2];
        // String name2 = path1.split("/")[path1.split("/").length-1];
        String name1=path1;
        String name2=path1;
        // System.out.println("name1: "+name1);
        // System.out.println("name2: "+name2);

        if (name1.contains("Eq") || name2.contains("Eq")) {
            return Classification.EQ.toString();
        } else if (name1.contains("NEq") || name2.contains("NEq") || name1.contains("Neq") || name2.contains("Neq")) {
            return Classification.NEQ.toString();
        }

        throw new RuntimeException("Cannot determine expected result for " + path1 + ".");
    }

    private static SMTSummary runToolInternal(String tool, String p1, String p2, String solver, int b, int t, int minInt, int maxInt, double minDouble, double maxDouble, String strategy) {
        try {
            //the path to two target versions
            ////************************************************************************+////
            //the depth limit
            int bound = b;
            ////************************************************************************+////
            //the range of values
            long minLong = (long)minInt;
            long maxLong = (long)maxInt;
            ////************************************************************************+////
            //the time out for the constraint solving
            int timeout = t * 1000;// (100 seconds * 1000 ms) for z3. and jpf which both are in MS
            ////************************************************************************+////
            //the choice of the constraint solvers
            String SMTSolver = solver;
            //String SMTSolver = "coral";
            //String SMTSolver = "z3inc";
            //String SMTSolver = "choco";
            //String SMTSolver = "cvc3";
            ////************************************************************************+////
            Runner runner = new Runner();
            runner.setup(p1,p2);
            if(System.getProperty("os.name").contains("Windows"))
                System.out.println("\n[NOTE] If you want to have a complete summary (exercise all behaviors), make sure your bound is big enough.");

            else System.out.println(ANSI_GREEN + "[NOTE] If you want to have a complete summary (exercise all behaviors), make sure your bound is big enough."+ANSI_RESET);
            ////*******************************************************************************************************************************************+////
            if(tool.contains("S")) {
                System.out.println("*****************************************************************************");
                System.out.println("------------------------------------SE-----------------------------------");
                System.out.println("*****************************************************************************");
                deleteGeneratedFiles("SE", runner.path);
                SE se = new SE(runner.path, runner.MethodPath1, runner.MethodPath2, bound, timeout, "SE", SMTSolver, minInt, maxInt, minDouble, maxDouble, minLong, maxLong);
                return se.runTool();
            }
            ////*******************************************************************************************************************************************+////
            ////*******************************************************************************************************************************************+////
            ////*******************************************************************************************************************************************+////
            if(tool.contains("D")) {
                System.out.println("*****************************************************************************");
                System.out.println("------------------------------------DSE-----------------------------------");
                System.out.println("*****************************************************************************");
                deleteGeneratedFiles("DSE", runner.path);
                DSE dse = new DSE(runner.path, runner.MethodPath1, runner.MethodPath2, bound, timeout, "DSE", SMTSolver, minInt, maxInt, minDouble, maxDouble, minLong, maxLong);
                return dse.runTool();
            }
            ////*******************************************************************************************************************************************+////
            ////*******************************************************************************************************************************************+////
            ////*******************************************************************************************************************************************+////
            if(tool.contains("A")) {
                System.out.println("*****************************************************************************");
                boolean H1 = true;
                boolean H2 = true;
                boolean H31 = true;
                boolean H32 = true;
                String toolName = "ARDiff";
                if(strategy.equals("R")){
                    H1 = H2 = H32 = H31 = false;
                    toolName += "R";
                    System.out.println("------------------------------------ARDIFF-R-----------------------------------");
                }
                else if(strategy.equals("H3")){
                    H1 = H2 = false;
                    toolName += "H3";
                    System.out.println("------------------------------------ARDIFF-H3-----------------------------------");
                }
                else
                    System.out.println("------------------------------------ARDIFF-----------------------------------");
                System.out.println("*****************************************************************************");
                deleteGeneratedFiles(toolName, runner.path);
                GradDiff gradDiff = new GradDiff(runner.path, runner.MethodPath1, runner.MethodPath2, bound, timeout, toolName, SMTSolver, minInt, maxInt, minDouble, maxDouble, minLong, maxLong, H1, H2, H31, H32, strategy);
                return gradDiff.runTool();
            }
            ////*******************************************************************************************************************************************+////
            ////*******************************************************************************************************************************************+////
            ////*******************************************************************************************************************************************+////
            if(tool.contains("I")) {
                System.out.println("*****************************************************************************");
                System.out.println("------------------------------------IMP-S-----------------------------------");
                System.out.println("*****************************************************************************");
                deleteGeneratedFiles("Imp", runner.path);
                ImpactedS impactedS = new ImpactedS(runner.path, runner.MethodPath1, runner.MethodPath2, bound, timeout, "Imp", SMTSolver, minInt, maxInt, minDouble, maxDouble, minLong, maxLong);
                return impactedS.runTool();
            }
            ////*******************************************************************************************************************************************+////
            ////*******************************************************************************************************************************************+////
            ////*******************************************************************************************************************************************+////
            if(tool.contains("H")) {
                System.out.println("*****************************************************************************");
                System.out.println("------------------------------------Hybrid-----------------------------------");
                System.out.println("*****************************************************************************");
                deleteGeneratedFiles("Hybrid", runner.path);
                Hybrid Hybrid = new Hybrid(runner.path, runner.MethodPath1, runner.MethodPath2, "Hybrid");
                return Hybrid.runTool();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Tool '" + tool + "' is not supported.");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Arguments:");
        System.out.println("--path1 path/to/the/first/method --path2 path/to/the/second/method --tool ToolName --s SMTSolverName --t timeout --b bound --minint minInt --maxint maxInt --mindouble minDouble --maxdouble maxDouble");
        System.out.println("*****************");
        System.out.println("--tool ToolName: to choose the tool to run (Default is ARDiff):");
        System.out.println("--tool D: Run DSE");
        System.out.println("--tool I: Run IMP-S");
        System.out.println("--tool A: Run ARDiff");
        if(DEBUG) {
            System.out.println("*****************");
            System.out.println("--s SMTSolverName: to choose the SMTSolver (Default is coral):");
            System.out.println("--s z3");
            System.out.println("--s coral");
            System.out.println("--s choco");
            System.out.println("--s cvc3");
            System.out.println("*****************");
        }
        System.out.println("--t timeout: to choose the timeout for constraint solving in millisecond (Default is 300000 MS):");
        System.out.println("*****************");
        System.out.println("--b bound: to choose the loop bound (Default is 5)");
        System.out.println("*****************");
        System.out.println("--minint: the minimum value of integers in the program (Default is -100):");
        System.out.println("*****************");
        System.out.println("--maxint: the maximum value of integers in the program (Default is 100):");
        System.out.println("*****************");
        System.out.println("--mindouble: the minimum value of doubles in the program (Default is -100.0):");
        System.out.println("*****************");
        System.out.println("--maxdouble: the maximum value of doubles in the program (Default is 100.0):");
        System.out.println("*****************");
        System.out.println("--H: the heuristics for ARDiff (R,H3 or H123)");
        System.out.println("*****************");
        if(args.length<4){
            System.out.println("Arguments are missing, you should AT LEAST specify the paths to both methods!");
            System.exit(1);
        }
        String path1 = "";
        String path2 = "";
        /**************/
        String tool = "A";
        String solver = "coral";
        String strategy = "H123";
        int bound = 5;//loop bound
        int timeout = 3600;
        int minint = -100;
        int maxint = 100;
        double mindouble = -100.0;
        double maxdouble = 100.0;
        /**************/

        for(int i = 0; i < 23; i+=2) {
            if (args.length > i) {
                if(args[i].equals("--path1")){
                    path1 = args[i+1];
                }
                if(args[i].equals("--path2")){
                    path2 = args[i+1];
                }
                if (args[i].equals("--tool")) {
                    if (args.length < i + 2) {
                        System.out.println("You need to specify the tool you want to use. If not remove the argument --tool");
                        System.exit(1);
                    }
                    tool = args[i + 1];
                }
                if (args[i].equals("--s")) {
                        if (args.length < i + 2) {
                            System.out.println("You need to specify the solver you want to use. If not remove the argument --s");
                            System.exit(1);
                        }
                        solver = args[i + 1];
                }
                if(args[i].equals("--t")){
                    if (args.length < i + 2) {
                        System.out.println("You need to specify the timeout. If not, remove the argument --t");
                        System.exit(1);
                    }
                    timeout = Integer.parseInt(args[i + 1]);
                }
                if (args[i].equals("--b")) {

                    if (args.length < i + 2) {
                        System.out.println("You need to specify the loop bounds. If not, remove the argument --b");
                        System.exit(1);
                    }
                    bound = Integer.parseInt(args[i + 1]);
                }
                if(args[i].equals("--minint")){
                    if(args.length < i + 2){
                        System.out.println("You need to specify the min int value.If not, remove the argument --minint");
                        System.exit(1);
                    }
                    minint = Integer.parseInt(args[i+1]);
                }
                if(args[i].equals("--maxint")){
                    if(args.length < i + 2){
                        System.out.println("You need to specify the max int value.If not, remove the argument --maxint");
                        System.exit(1);
                    }
                    maxint = Integer.parseInt(args[i+1]);
                }
                if(args[i].equals("--mindouble")){
                    if(args.length < i + 2){
                        System.out.println("You need to specify the min double value.If not, remove the argument --mindouble");
                        System.exit(1);
                    }
                    mindouble = Double.parseDouble(args[i+1]);
                }
                 if(args[i].equals("--maxdouble")) {
                    if (args.length < i + 2) {
                        System.out.println("You need to specify the max double value.If not, remove the argument --maxdouble");
                        System.exit(1);
                    }
                    maxdouble = Double.parseDouble(args[i + 1]);
                }
                 if(args[i].equals("--H")){
                    if(args.length < i+2){
                        System.out.println("You need to specify a heuristic. If not, remove the argument --H");
                        System.exit(1);
                    }
                    strategy = args[i+1];
                }
            }
        }
        if(path1.isEmpty() || path2.isEmpty()){
            System.out.println("\nPlease provide proper paths");
            System.exit(1);
        }

        runTool(tool,path1,path2,solver,bound,timeout,minint,maxint,mindouble,maxdouble,strategy);
    }

    public static void deleteGeneratedFiles(String tool, String directory) throws IOException {
        getGeneratedFiles(tool, directory).forEach(path -> path.toFile().delete());
    }

    public static List<Path> getGeneratedFiles(String tool, String directory) throws IOException {
        List<Path> generatedFiles = new ArrayList<>();

        Path benchmarkPath = Paths.get(directory);
        Path instrumentedPath = benchmarkPath.resolve("instrumented");
        Path outputsPath = benchmarkPath.resolve("outputs");
        Path modelsPath = benchmarkPath.resolve("z3models");

        generatedFiles.add(instrumentedPath.resolve("blocknewV.txt"));
        generatedFiles.add(instrumentedPath.resolve("gumtree.txt"));
        generatedFiles.add(instrumentedPath.resolve("H1Checking.smt2"));

        generatedFiles.addAll(getOldVFiles(tool, instrumentedPath));
        generatedFiles.addAll(getNewVFiles(tool, instrumentedPath));

        generatedFiles.add(outputsPath.resolve(tool + ".txt"));
        generatedFiles.add(modelsPath.resolve(tool + ".txt"));

        return generatedFiles;
    }

    public static List<Path> getOldVFiles(String tool, Path directory) throws IOException {
        return getFiles("glob:**/IoldV" + tool + "*", directory);
    }

    public static List<Path> getNewVFiles(String tool, Path directory) throws IOException {
        return getFiles("glob:**/InewV" + tool + "*", directory);
    }

    private static List<Path> getFiles(String glob, Path directory) throws IOException {
        List<Path> answerFiles = new ArrayList<>();

        if (!directory.toFile().exists()) {
            return answerFiles;
        }

        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                if (pathMatcher.matches(path)) {
                    answerFiles.add(path);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return answerFiles;
    }
}
