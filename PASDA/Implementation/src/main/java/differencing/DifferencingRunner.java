package differencing;

import differencing.classification.Classification;
import differencing.classification.IterationClassifier;
import differencing.classification.RunClassifier;
import differencing.models.Benchmark;
import differencing.models.Iteration;
import differencing.models.Run;
import differencing.models.Settings;
import differencing.repositories.*;
import equiv.checking.ChangeExtractor;
import equiv.checking.ProjectPaths;
import equiv.checking.SourceInstrumentation;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.symbc.SymbolicListener;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.tools.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DifferencingRunner {
    private final Configuration freeMarkerConfiguration;

    public static void main(String[] args) throws Exception {
        // Arguments: [benchmark] [tool] [timeout]
        // - [benchmark]: Path to the benchmark directory, e.g., ../benchmarks/.
        // - [tool]: SE, DSEs, Imp, ARDiffs, ARDiffR, or ARDiffH3.
        // - [timeout]: Maximum time to use across all iterations of the run.
        // - [depth_limit]: Maximum number of loop iterations to analyze.

        // System.out.println(args[0]);
        // System.out.println(args[1]);
        // System.out.println(args[2]);
        long startTime = System.currentTimeMillis();
        new DifferencingRunner().run(args[0], args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime; // duration in milliseconds
        System.out.println("Execution time in milliseconds: " + duration+" ms");
    }

    public DifferencingRunner() throws IOException {
        /* Create and adjust the FreeMarker configuration singleton */
        this.freeMarkerConfiguration = new Configuration(Configuration.VERSION_2_3_31);
        this.freeMarkerConfiguration.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));
        // Recommended settings for new projects:
        this.freeMarkerConfiguration.setDefaultEncoding("UTF-8");
        this.freeMarkerConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        this.freeMarkerConfiguration.setLogTemplateExceptions(false);
        this.freeMarkerConfiguration.setWrapUncheckedExceptions(true);
        this.freeMarkerConfiguration.setFallbackOnNullLoopVariable(false);
    }

    public void run(String benchmarkDir, String toolName, int runTimeout, int depthLimit) throws Exception {
        int iterationTimeout = runTimeout;
        int solverTimeout = (int) Math.round(runTimeout / Math.cbrt(runTimeout));
        System.out.println("Run timeout: " + runTimeout+" Solver timeout: "+solverTimeout+" Iteration timeout: "+iterationTimeout);
        // System.out.println("Tool Name "+ toolName);
        // Read the differencing configuration:
        Path parameterFilePath = Paths.get(benchmarkDir, "instrumented", "IDiff" + toolName + "-Parameters.txt");

        DifferencingParameterFactory parameterFactory = new DifferencingParameterFactory();

        //------------------------------------------------------------------------------------------
        // Check whether the necessary "configuration" file exists.
        // If not, terminate the program with a BASE_TOOL_MISSING result.

        if (!parameterFilePath.toFile().exists()) {
            String error = "Error: '" + parameterFilePath + "' does not exist.";

            DifferencingParameters parameters = parameterFactory.create(toolName, benchmarkDir);
            Arrays.stream(parameters.getGeneratedFiles()).forEach(file -> new File(file).delete());

            // System.out.println(parameters.getBenchmarkName());
            Benchmark benchmark = new Benchmark(parameters.getBenchmarkName(), parameters.getExpectedResult());

            Run run = new Run(
                parameters.getBenchmarkName(),
                Classification.BASE_TOOL_MISSING,
                null, null, null, null, null, null,
                error
            );

            //BenchmarkRepository.insertOrUpdate(benchmark);
            //RunRepository.insertOrUpdate(run);
            run.id=0;
            //System.out.println(run.id+" "+parameters.getToolVariant()+" "+runTimeout+" "+iterationTimeout+" "+solverTimeout);
            Settings settings = new Settings(run.id, parameters.getToolVariant(), runTimeout, iterationTimeout, solverTimeout, 10);
            //SettingsRepository.insertOrUpdate(settings);

            //System.out.println(error);
            return;
        }
        
        //------------------------------------------------------------------------------------------

        DifferencingParameters parameters = parameterFactory.load(parameterFilePath.toFile());

        Arrays.stream(parameters.getGeneratedFiles()).forEach(file -> new File(file).delete());
       
         File configFile = this.createDifferencingJpfConfiguration(parameters, solverTimeout, depthLimit);
        //================================================================================================
        StopWatches.start("run");
        StopWatches.start("run:initialization");

        Benchmark benchmark = new Benchmark(parameters.getBenchmarkName(), parameters.getExpectedResult());
        //BenchmarkRepository.insertOrUpdate(benchmark);

        Run run = new Run(parameters.getBenchmarkName());
        // System.out.println(run.id+" "+parameters.getToolVariant()+" "+runTimeout+" "+iterationTimeout+" "+solverTimeout);
        run.id=0;
        //RunRepository.insertOrUpdate(run);

        Settings settings = new Settings(run.id, parameters.getToolVariant(), runTimeout, iterationTimeout, solverTimeout, 10);
        //SettingsRepository.insertOrUpdate(settings);
        // System.out.println("Settings initialization done");

        Map<Integer, Iteration> iterations = new HashMap<>();
        Map<Integer, DifferencingListener> diffListeners = new HashMap<>();

        //----------------------------------------------------------------------
        // "Redirect" output to log files.

        PrintStream systemOutput = System.out;
        PrintStream systemError = System.err;

        PrintStream outputStream = new PrintStream(new BufferedOutputStream(Files.newOutputStream(Paths.get(parameters.getOutputFile()))));
        PrintStream errorStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(parameters.getErrorFile())));

        // System.out.println("After print stream");
        // // System.setOut(outputStream);
        // System.out.println("After print output stream");
        // // System.setErr(errorStream);
        // System.out.println("Before thread");


        //------------------------------------------------------------------
        // Create a shutdown handler in case the run / iteration is
        // forcefully stopped because it exceeds the timeout.

        Thread shutdownHook = new Thread(() -> {
            // System.out.println("In the thread.");
            try {
                // System.out.println("Shutdown hook activated.");
                Iteration currentIteration = iterations.get(iterations.size());
                DifferencingListener diffListener = diffListeners.get(currentIteration.iteration);

                Iteration finishedIteration = this.finalizeIteration(currentIteration, diffListener, true, false, "");
                iterations.put(finishedIteration.iteration, finishedIteration);
                // System.out.println("Iterations "+iterations.size()+" "+iterations);
                Run finishedRun = this.finalizeRun(run, iterations, true, false, "");

               // TimeRepository.insertOrUpdate(TimeFactory.create(finishedRun, StopWatches.getTimes()));

                systemError.println("TIMEOUT: " + parameters.getTargetDirectory() + " -> " + finishedRun.result);
                System.out.println("TIMEOUT: " + parameters.getTargetDirectory() + " -> " + finishedRun.result);
            } catch (Throwable e) {
                e.printStackTrace(systemError);
                e.printStackTrace(errorStream);
                throw e;
            }
        });

        Runtime.getRuntime().addShutdownHook(shutdownHook);
        //System.out.println("Shutdown hook added.");
        //----------------------------------------------------------------------

        try {
            ChangeExtractor changeExtractor = new ChangeExtractor();
            ArrayList<Integer> changes = changeExtractor.obtainChanges(
                parameters.getOldVJavaFile(),
                parameters.getNewVJavaFile(),
                parameters.getTargetDirectory()
            );

            SourceInstrumentation instrumentation = InstrumentationFactory.create(
                toolName,
                solverTimeout,
                changeExtractor
            );

            StopWatches.stop("run:initialization");

            boolean shouldKeepIterating;

            do { // while (shouldKeepIterating)
                // System.out.println("Iteration TEST "+(iterations.size() + 1));
                StopWatches.start("iteration-" + (iterations.size() + 1));
                StopWatches.start("iteration-" + (iterations.size() + 1) + ":initialization");

                Iteration iteration = new Iteration(run.id, iterations.size() + 1);
                iterations.put(iteration.iteration, iteration);

               // IterationRepository.insertOrUpdate(iteration);

                parameters.setIteration(iteration.iteration);

                IgnoreUnreachablePathsListener unreachableListener = new IgnoreUnreachablePathsListener(solverTimeout);
                // System.out.println("Iteration "+iteration+" "+parameters);
                ExecutionListener execListener = new ExecutionListener(iteration, parameters);
                PathConditionListener pcListener = new PathConditionListener(iteration, parameters);
                DifferencingListener diffListener = new DifferencingListener(iteration, parameters, solverTimeout);

                TimeoutChecker timeoutChecker = new TimeoutChecker(diffListener, iteration, iterationTimeout);

                diffListeners.put(iteration.iteration, diffListener);
                if (diffListeners.containsKey(iteration.iteration - 1)) {
                    diffListeners.get(iteration.iteration - 1).close();
                    diffListeners.remove(iteration.iteration - 1);
                }

                //--------------------------------------------------------------
                // Execute the actual equivalence checking / differencing.
                // Each iteration consists of a source code instrumentation
                // step and a symbolic execution step.

                boolean hasSucceeded = false;
                String errors = "";

                try {
                    StopWatches.stop("iteration-" + iteration.iteration + ":initialization");
                    StopWatches.start("iteration-" + iteration.iteration + ":instrumentation");

                    instrumentation.runInstrumentation(iteration.iteration, changes);

                    File javaFile = this.createDifferencingDriverClass(parameters);
                    this.compile(ProjectPaths.classpath, javaFile);

                    StopWatches.stop("iteration-" + iteration.iteration + ":instrumentation");
                    StopWatches.start("iteration-" + iteration.iteration + ":symbolic-execution");

                    // File configFile = this.createDifferencingJpfConfiguration(parameters, solverTimeout, depthLimit);
                     configFile = this.createDifferencingJpfConfiguration(parameters, solverTimeout, depthLimit);

                    Config config = JPF.createConfig(new String[]{configFile.getAbsolutePath()});
                    JPF jpf = new JPF(config);
                    jpf.addListener(unreachableListener);
                    jpf.addListener(timeoutChecker);
                    jpf.addListener(new SymbolicListener(config, jpf));
                    jpf.addListener(execListener);
                    jpf.addListener(pcListener);
                    jpf.addListener(diffListener);
                    jpf.run();

                    hasSucceeded = true;
                } catch (Throwable e) {
                    errors = ExceptionUtils.getStackTrace(e);
                    e.printStackTrace(systemError);
                    e.printStackTrace(errorStream);
                } finally {
                    StopWatches.stop("iteration-" + iteration.iteration + ":symbolic-execution");
                }

                //------------------------------------------------------------------
                // Write the results of this iteration to the DB + console.

                StopWatches.start("iteration-" + iteration.iteration + ":program-classification");

                iteration = this.finalizeIteration(iteration, diffListener, false, !hasSucceeded, errors);
                // System.out.println("Iteration before "+iteration.iteration+" "+iteration.result);
                iterations.put(iteration.iteration, iteration);
                //  System.out.println("Iteration after "+iteration.iteration+" "+iteration.result);

                if (hasSucceeded) {
                    systemOutput.print("Iteration " + iteration.iteration + " - SUCCESS: ");
                    systemOutput.println(parameters.getTargetDirectory() + " -> " + iteration.result);
                } else {
                    systemError.println("Iteration " + iteration.iteration + " - ERROR: ");
                    systemError.println(parameters.getTargetDirectory() + " -> " + iteration.result);
                }

                StopWatches.stop("iteration-" + iteration.iteration + ":program-classification");

                //------------------------------------------------------------------
                // Check if we should do another iteration.
                // If yes, update the instrumentation object accordingly so a
                // more concretized version of the source code is generated by
                // the instrumentation step of the next iteration.

                StopWatches.start("iteration-" + iteration.iteration + ":refinement");

                boolean isFinalResult =
                    iteration.result == Classification.EQ
                        || iteration.result == Classification.NEQ
                        || iteration.result == Classification.ERROR;

                shouldKeepIterating = false;
                if (!isFinalResult && StopWatches.getTime("run") < runTimeout) {
                    String nextToRefine = instrumentation.getNextToRefine(
                        diffListener.getContext(),
                        diffListener.getV1Summary(),
                        diffListener.getV2Summary(),
                        diffListener.getVariables()
                    );

                    if (!nextToRefine.isEmpty()) {
                        instrumentation.expandFunction(nextToRefine, changes);
                        shouldKeepIterating = true;
                    } else {
                        systemOutput.println("Nothing left to refine.");
                    }
                }

                StopWatches.stop("iteration-" + iteration.iteration + ":refinement");
                StopWatches.stop("iteration-" + iteration.iteration);
            } while (shouldKeepIterating);

            //----------------------------------------------------------------------
            // Write the overall run results to the DB.
            // System.out.println("Iterations "+iterations.size()+" "+iterations);

            StopWatches.start("run:finalization");
            

            Run finishedRun = this.finalizeRun(run, iterations, false, false, "");

            StopWatches.stop("run:finalization");
            StopWatches.stop("run");

           // TimeRepository.insertOrUpdate(TimeFactory.create(finishedRun, StopWatches.getTimes()));

            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (Throwable e) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);

            e.printStackTrace(systemError);
            e.printStackTrace(errorStream);

            try {
                // System.out.println("Check 12 34");
                // System.out.println("Iterations "+iterations.size()+" "+iterations);
                Run finishedRun = this.finalizeRun(run, iterations, false, true, ExceptionUtils.getStackTrace(e));

                systemError.println("ERROR: " + parameters.getTargetDirectory() + " -> " + finishedRun.result);
            } catch (Throwable ex) {
                ex.printStackTrace(systemError);
                ex.printStackTrace(errorStream);
            }
        }
    }

    public Iteration finalizeIteration(
        Iteration iteration,
        DifferencingListener diffListener,
        boolean hasTimedOut,
        boolean isError,
        String errors
    ) {
        Classification result = new IterationClassifier(
            false, false, isError, hasTimedOut,
            diffListener.getPartitions()
        ).getClassification();

        Iteration finishedIteration = new Iteration(
            iteration.runId,
            iteration.iteration,
            result,
            hasTimedOut,
            diffListener.isDepthLimited(),
            diffListener.hasUif(),
            diffListener.getPartitions().size(),
            StopWatches.getTime("iteration-" + iteration.iteration),
            errors
        );

        finishedIteration.id = iteration.id;
        //IterationRepository.insertOrUpdate(finishedIteration);

        return finishedIteration;
    }

    public Run finalizeRun(
        Run run,
        Map<Integer, Iteration> iterations,
        boolean hasTimedOut,
        boolean isError,
        String errors
    ) {
       // System.out.println("In finalizeRun function "+iterations.size()+" "+iterations);
        RunClassifier runClassifier = new RunClassifier(iterations);
        Iteration resultIteration = runClassifier.getClassificationIteration();
        Iteration lastIteration = iterations.get(iterations.size());

        Run finishedRun = new Run(
            run.benchmark,
            isError ? Classification.ERROR : resultIteration.result,
            hasTimedOut || lastIteration.hasTimedOut,
            resultIteration.isDepthLimited,
            resultIteration.hasUif,
            lastIteration.iteration,
            resultIteration.iteration,
            StopWatches.getTime("run"),
            errors + lastIteration.errors
        );

        finishedRun.id = run.id;
        //RunRepository.insertOrUpdate(finishedRun);

        return finishedRun;
    }

    public File createDifferencingDriverClass(DifferencingParameters parameters) throws IOException, TemplateException {
        /* Create a data-model */
        Map<String, Object> root = new HashMap<>();
        root.put("parameters", parameters);

        /* Get the template (uses cache internally) */
        Template template = this.freeMarkerConfiguration.getTemplate("DifferencingDriverClass.ftl");

        /* Merge data-model with template */
        File file = new File(parameters.getTargetDirectory() + "/" + parameters.getTargetClassName() + ".java");
        //System.out.println(parameters.getTargetClassName());
        file.getParentFile().mkdirs();

        try (PrintWriter writer = new PrintWriter(file)) {
            template.process(root, writer);
        }

        return file;
    }

    public File createDifferencingJpfConfiguration(
        DifferencingParameters parameters,
        int timeout,
        int depthLimit
    ) throws IOException, TemplateException {
        /* Create a data-model */
        Map<String, Object> root = new HashMap<>();
        root.put("parameters", parameters);
        root.put("timeout", timeout * 1000);
        root.put("depthLimit", depthLimit);

        /* Get the template (uses cache internally) */
        Template template = this.freeMarkerConfiguration.getTemplate("DifferencingConfiguration.ftl");

        /* Merge data-model with template */
        File file = new File(parameters.getTargetDirectory() + "/" + parameters.getTargetClassName() + ".jpf");
        file.getParentFile().mkdirs();

        try (PrintWriter writer = new PrintWriter(file)) {
            template.process(root, writer);
        }

        return file;
    }

    // The compile method is copied from ARDiff's equiv.checking.Utils interface.
    private void compile(String classpath,File newFile) throws IOException {
        File path = new File(classpath);
        path.getParentFile().mkdirs();
        //Think about whether to do it for the classpaths in the tool as well (maybe folder instrumented not automatically created)
        if(!path.exists())
            path.mkdir();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticCollector, null, null);
        String dir = classpath;
        List<String> classpathParts = Arrays.asList(classpath, ProjectPaths.jpf_core_jar, ProjectPaths.jpf_symbc_jar);
        classpath = String.join(SystemUtils.IS_OS_WINDOWS ? ";" : ":", classpathParts);
        List<String> options = Arrays.asList("-g", "-cp", classpath, "-d", dir);
        Iterable<? extends JavaFileObject> cpu =
            fileManager.getJavaFileObjectsFromFiles(Arrays.asList(new File[]{newFile}));
        boolean success = compiler.getTask(null, fileManager, diagnosticCollector, options, null, cpu).call();
        if(!success){
            List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticCollector.getDiagnostics();
            String message = "Compilation error: ";
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
                // read error details from the diagnostic object
                message+=diagnostic.getMessage(null);
            }
            throw new IOException("Compilation error: "+message);
        }
    }
}
