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
import differencing.RunJava;

import javax.tools.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Random;

public class DifferencingRunner {
    private final Configuration freeMarkerConfiguration;

    public static void main(String[] args) throws Exception {
        // Arguments: [benchmark] [tool] [timeout]
        // - [benchmark]: Path to the benchmark directory, e.g., ../benchmarks/.
        // - [tool]: SE, DSEs, Imp, ARDiffs, ARDiffR, or ARDiffH3.
        // - [timeout]: Maximum time to use across all iterations of the run.
        // - [depth_limit]: Maximum number of loop iterations to analyze.

        
        // System.out.println(args[1]);
        // System.out.println(args[2]);
        long startTime = System.currentTimeMillis();
        new DifferencingRunner().run(args[0], args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), args[5]);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime; // duration in milliseconds
        System.out.println("Execution time in milliseconds: " + duration+" ms, fuzztime "+Integer.parseInt(args[4])+" ms");
    }
    

    // public List<String> BoundaryFuzzing(List<String> programArgs, string dataType){
    //     // List<String> programArgs=new ArrayList<>();
    //     if (dataType.equals("int")){

    //     }
    //     else if (dataType.equals("double")){

    //     }
       
    //     return programArgs;
    // }

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

    // public String runJavaClass(String className, String classpath, List<String> programArgs) throws IOException, InterruptedException {
    //     List<String> command = new ArrayList<>();
    //     command.add("java");
    //     command.add("-cp");
    //     command.add(classpath);
    //     command.add(className);
    //     command.addAll(programArgs);
    //     ProcessBuilder processBuilder = new ProcessBuilder(command);
    //     processBuilder.redirectErrorStream(true);
    //     Process process = processBuilder.start();

    //     StringBuilder output = new StringBuilder();
    //     try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
    //         String line;
    //         while ((line = reader.readLine()) != null) {
    //             output.append(line).append("\n");
    //         }
    //     }
    //     process.waitFor();
    //     return output.toString();
    // }

    public void run(String benchmarkDir, String toolName, int runTimeout, int depthLimit, int fuzzTime, String fuzzingType) throws Exception {
        int iterationTimeout = runTimeout;
        int solverTimeout = (int) Math.round(runTimeout / Math.cbrt(runTimeout));
        //System.out.println("Tool Name "+ toolName);
        // Read the differencing configuration:
        Path parameterFilePath = Paths.get(benchmarkDir, "instrumented", "IDiff" + toolName + "-Parameters.txt");
        // System.out.println(args[0]);
        String fileLocation=benchmarkDir;
        // Split the string by '/'
        String[] parts = fileLocation.split("/");
        String combinedClass="demo.";
        int length=parts.length;
        // Print the result
        for (int i = 1; i < length; i++) {
            //System.out.println(parts[i]);
            combinedClass=combinedClass+parts[i]+".";
        }
        if (combinedClass.contains("ModDiff")){
            combinedClass=combinedClass.replace("ModDiff.Eq","ModDiffEq");
            combinedClass=combinedClass.replace("ModDiff.NEq","ModDiffNeq");
           combinedClass=combinedClass+"instrumented.IDiffPASDA1fuzz"; 
        }
        else{
            combinedClass=combinedClass+"instrumented.IDiffPASDA1fuzz";
        }
        
        // System.out.println(combinedClass);

        DifferencingParameterFactory parameterFactory = new DifferencingParameterFactory();

        //------------------------------------------------------------------------------------------
        // Check whether the necessary "configuration" file exists.
        // If not, terminate the program with a BASE_TOOL_MISSING result.

        if (!parameterFilePath.toFile().exists()) {
            String error = "Error: '" + parameterFilePath + "' does not exist.";

            DifferencingParameters parameters = parameterFactory.create(toolName, benchmarkDir);
            Arrays.stream(parameters.getGeneratedFiles()).forEach(file -> new File(file).delete());

            System.out.println(parameters.getBenchmarkName());
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
            System.out.println(run.id+" "+parameters.getToolVariant()+" "+runTimeout+" "+iterationTimeout+" "+solverTimeout);
            Settings settings = new Settings(run.id, parameters.getToolVariant(), runTimeout, iterationTimeout, solverTimeout, 10);
            //SettingsRepository.insertOrUpdate(settings);

            System.out.println(error);
            return;
        }
        
        //------------------------------------------------------------------------------------------

        DifferencingParameters parameters = parameterFactory.load(parameterFilePath.toFile());

        Arrays.stream(parameters.getGeneratedFiles()).forEach(file -> new File(file).delete());
        // System.out.println("Test 1 2 3 4 ...");
        // int fuzzingTime=5; //time limit for fuzzing time
        // System.out.println("Fuzzing started"+runTimeout);
        int fuzzingTime=fuzzTime/1000;
        StopWatches.start("run");
        // //Added by Laboni for testing
        File javaFile = this.createDifferencingDriverClassFuzzing(parameters);
        // //System.out.println(ProjectPaths.classpath);
        this.compile(ProjectPaths.classpath, javaFile);
        // System.out.println(Double.MAX_VALUE);
        // System.out.println(Long.MAX_VALUE);
        // System.out.println(Integer.MAX_VALUE);

        // System.out.println(Double.MIN_VALUE);
        // System.out.println(Long.MIN_VALUE);
        // System.out.println(Integer.MIN_VALUE);
        //====================FUZZING STARTED=========================
        int corpusCount=0;
        // long seed = 12345L;
        // Random random = new Random(seed);
        Random random = new Random();
        int uniformFuzz=0, boundaryFuzz=0;
        if(fuzzingType.trim().equals("uniform")) uniformFuzz=1;
        else if(fuzzingType.trim().equals("boundary")) boundaryFuzz=1;
        // int uniformFuzz=1;
        // int boundaryFuzz=0;
        // System.out.println("Fuzzing started "+fuzzingType+" "+fuzzingTime+" "+uniformFuzz+" "+boundaryFuzz);

        if (uniformFuzz==1){
           StopWatches.start("fuzz");
            while(true){
                if(StopWatches.getTime("fuzz")>fuzzingTime){
                    System.out.println("Fuzzing time limit exceeded");
                    break;
                }
                corpusCount++;
                // Random random = new Random();
                List<String> programArgs=new ArrayList<>();

                for(int i=0;i<parameters.diffMethodDescription.getParameters().size();i++){
                    // int countInt=0, countLong=0, countDouble=0, countFloat=0;
                    // System.out.println(parameters.diffMethodDescription.getParameters().get(i).getDataType());
                    String dataType=parameters.diffMethodDescription.getParameters().get(i).getDataType();
                    // if(uniformFuzz==1){
                            if(dataType.equals("int")){
                            // int min=-2147483648;
                            // int max=2147483647;
                            // int val=min + (int)(random.nextDouble() * (max - min));
                            int val=random.nextInt();
                            // programArgs.add(String.valueOf(random.nextInt(max-min+1)+min));
                            System.out.println(val);
                            programArgs.add(String.valueOf(val));
                        }
                        else if(dataType.equals("long")){
                            long min=-9223372036854775808L;
                            long max=9223372036854775807L;
                            long val=min + (long)(random.nextDouble() * (max - min));
                            System.out.println(val+" "+random.nextDouble());
                            programArgs.add(String.valueOf(val));
                            //  programArgs.add(String.valueOf(random.nextLong()));

                        }
                        else if(dataType.equals("double")){
                            // programArgs.add(String.valueOf(random.nextDouble()));
                            // double min = -Double.MAX_VALUE;
                            // double max = Double.MAX_VALUE;

                            // // Generate a random double between -Double.MAX_VALUE and Double.MAX_VALUE
                            // double randomValue = min + (random.nextDouble() * (max - min));

                            // double randomValue = random.nextDouble();
                            // int power=random.nextInt(308);
                            // int sign=random.nextInt(2);
                            // if(sign==0)
                            //     randomValue=randomValue*Math.pow(10,power);
                            // else
                            //     randomValue=(-1)*randomValue*Math.pow(10,power);

                            // generate randome double using 64-bit bitstring randomization
                            StringBuilder bitstring = new StringBuilder(64);
                            int z;
                            for (z = 0; z < 64; z++){
                                bitstring.append(random.nextBoolean() ? '1' : '0');
                            }
                            // long bitlong = Long.parseUnsignedLong(bitstring.toString(), 2); 
                            String bits = bitstring.toString();
                            int sign = bits.charAt(0) == '0' ? 1 : -1;
                            int exponent = Integer.parseInt(bits.substring(1,12), 2) - 1023;
                            long mantissaBits = Long.parseLong(bits.substring(12), 2);
                            double mantissa = 1.0;
                            for (z = 0; z < 52; z++){
                                if ((mantissaBits & (1L << (51 - z))) != 0){
                                    mantissa += Math.pow(2, -(z + 1));
                                }
                            }
                            double randomValue = sign * Math.pow(2, exponent) * mantissa;
                            // System.out.println(randomValue+" "+random.nextDouble());
                            System.out.println(randomValue);
                            programArgs.add(String.valueOf(randomValue));
                        }
                        else if(dataType.equals("String")){
                            programArgs.add("test");
                        }
                    }

                // if (boundaryFuzz==1){
                //     break;
                // }
                RunJava runJava=new RunJava();
                String output=runJava.runJavaClass(combinedClass,"target/classes/", programArgs);
            // System.out.println(output);
                if (output.contains("false") && output.contains("NaN")!=true)
                {
                    // System.out.println("The two versions are not equivalent");
                    System.out.println(parameters.getTargetDirectory() +" "+programArgs+" "+corpusCount+" -> " +"NEQ");
                    return;
                }
            }
            System.out.println("Fuzzing time limit exceeded : "+corpusCount);
            //==========================FUZZING ENDED========================= 
        }
        else if(boundaryFuzz==1){
            List<Integer> interestingIntWindowR1 = Arrays.asList(new Integer[]{-128, 1073741823, -2147483648});
            List<Integer> interestingIntWindowR2 = Arrays.asList(new Integer[]{128, 2147483647, -1073741824});
            List<Long> interestingLongWindowR1 = Arrays.asList(new Long[]{-128L, 4611686018427387903L, -9223372036854775808L});
            List<Long> interestingLongWindowR2 = Arrays.asList(new Long[]{128L, 9223372036854775807L, -4611686018427387904L});
            // List<Double> interestingDoubleWindowR1 = Arrays.asList(new Double[]{-1024.0, 1.7976931348623158e+305, -1.7976931348623158e+308});
            // List<Double> interestingDoubleWindowR2 = Arrays.asList(new Double[]{1024.0, 1.7976931348623158e+308, -1.7976931348623158e+305});
            List<Double> interestingDoubleWindowR1 = Arrays.asList(new Double[]{0.0, 1.7976931348623158e+305, -1.7976931348623158e+308});
            List<Double> interestingDoubleWindowR2 = Arrays.asList(new Double[]{10.0, 1.7976931348623158e+308, -1.7976931348623158e+305});
            int rangeBound=3;
            if(fuzzingTime!=0)
                rangeBound=fuzzingTime*6;
            // int rangeBound=20;
            int rangeIndex=0;
            // int countInt=0, countLong=0, countDouble=0;
            List<Integer> paramCount=new ArrayList<>();
            StopWatches.start("fuzz");
            int countRangeIndex=0;
            while(true){
                if(StopWatches.getTime("fuzz")>fuzzingTime){
                    System.out.println("Fuzzing time limit exceeded");
                    break;
                }
                corpusCount++;
                
                // Random random = new Random();
                List<String> programArgs=new ArrayList<>();

                for(int i=0;i<parameters.diffMethodDescription.getParameters().size();i++){
                    countRangeIndex++;
                    // System.out.println("range index "+rangeIndex);
                    String dataType=parameters.diffMethodDescription.getParameters().get(i).getDataType();
                            if(dataType.equals("int")){
                                if (rangeIndex<=interestingDoubleWindowR1.size()-1){
                                    int min=interestingIntWindowR1.get(rangeIndex);
                                    int max=interestingIntWindowR2.get(rangeIndex);
                                    int val=min + (int)(random.nextDouble() * (max - min));
                                    System.out.println(val);
                                    programArgs.add(String.valueOf(val));
                                }
                                else{
                                    int val=random.nextInt();
                                    System.out.println(val);
                                    programArgs.add(String.valueOf(val));
                                }
                        }
                        else if(dataType.equals("long")){
                            if (rangeIndex<=interestingDoubleWindowR1.size()-1){
                                long min=interestingLongWindowR1.get(rangeIndex);
                                long max=interestingLongWindowR2.get(rangeIndex);
                                long val=min + (long)(random.nextDouble() * (max - min));
                                // System.out.println(val+" "+random.nextDouble());
                                System.out.println(val);
                                programArgs.add(String.valueOf(val));
                            }
                            else{
                                long min=-9223372036854775808L;
                                long max=9223372036854775807L;
                                long val=min + (long)(random.nextDouble() * (max - min));
                                // System.out.println(val+" "+random.nextDouble());
                                System.out.println(val);
                                programArgs.add(String.valueOf(val));
                            }
                            // long min=interestingLongWindowR2.get(rangeIndex);
                            // long max=interestingLongWindowR1.get(rangeIndex);
                            // long val=min + (long)(random.nextDouble() * (max - min));
                            // System.out.println(val+" "+random.nextDouble());
                            // programArgs.add(String.valueOf(val));
                        }
                        else if(dataType.equals("double")){
                            // programArgs.add(String.valueOf(random.nextDouble()));
                            if (rangeIndex<=interestingDoubleWindowR1.size()-1){
                                if (rangeIndex == 0){
                                    double randomValue = random.nextDouble();
                                    // int index=Integer.parseInt(interestingDoubleWindowR2.get(rangeIndex).toString());
                                    int index=10;
                                    int power=random.nextInt(index+1);
                                    // int power=random.nextInt((int)interestingDoubleWindowR2.get(rangeIndex)+1);
                                    int sign=random.nextInt(2);
                                    if(sign==0)
                                        randomValue=randomValue*Math.pow(2,power);
                                    else
                                        randomValue=(-1)*randomValue*Math.pow(2,power);
                                    System.out.println(randomValue);
                                    programArgs.add(String.valueOf(randomValue));
                                }
                                else {
                                    double min=interestingDoubleWindowR1.get(rangeIndex);
                                    double max=interestingDoubleWindowR2.get(rangeIndex);
                                    double val=min + (double)(random.nextDouble() * (max - min));
                                    System.out.println(val);
                                    programArgs.add(String.valueOf(val));
                                }
                            }
                            else{
                                double randomValue = random.nextDouble();
                                int power=random.nextInt(308);
                                int sign=random.nextInt(2);
                                if(sign==0)
                                    randomValue=randomValue*Math.pow(10,power);
                                else
                                    randomValue=(-1)*randomValue*Math.pow(10,power);
                                System.out.println(randomValue);
                                programArgs.add(String.valueOf(randomValue));
                            }
                        }
                        else if(dataType.equals("String")){
                            programArgs.add("test");
                        }
                    }
                // System.out.println("----------------------");
                // System.out.println(parameters.diffMethodDescription.getParameters().size()+" bound "+rangeBound+" count "+countRangeIndex);
                if (parameters.diffMethodDescription.getParameters().size()==0) rangeIndex=0;
                else
                rangeIndex=countRangeIndex/(parameters.diffMethodDescription.getParameters().size()*rangeBound);
                // System.out.println("Range Index "+rangeIndex+" "+corpusCount);
                RunJava runJava=new RunJava();
                String output=runJava.runJavaClass(combinedClass,"target/classes/", programArgs);
                if (output.contains("false") && output.contains("NaN")!=true)
                {
                    System.out.println(parameters.getTargetDirectory() +" "+programArgs+" "+corpusCount+" -> " +"NEQ");
                    return;
                }
            }
            System.out.println("Fuzzing time limit exceeded : "+corpusCount);
            //==========================FUZZING ENDED=========================
        }
        
        // List<String> programArgs=new ArrayList<>();
        // Random random = new Random();
        // for(int i=0;i<parameters.diffMethodDescription.getParameters().size();i++){
        //     //System.out.println(parameters.diffMethodDescription.getParameters().get(i).getDataType());
        //     String dataType=parameters.diffMethodDescription.getParameters().get(i).getDataType();
        //     if(dataType.equals("int"))
        //         programArgs.add(String.valueOf(random.nextInt()));
        //     else if(dataType.equals("double"))

        //         programArgs.add(String.valueOf(random.nextDouble()));
        //     programArgs.add(parameters.diffMethodDescription.getParameters().get(i).getPlaceholderValue());
        // }
        // String output=runJavaClass(combinedClass,"target/classes/", programArgs);
        // // System.out.println(output);
        // if(output.contains("false"))
        // {
        //     System.out.println("The two versions are not equivalent");
        //     return;
        // }
        // else{
        //     System.out.println("The two versions are equivalent");
        // }

        //Uncomment when need to run new algorithm
        File configFile = this.createDifferencingJpfConfiguration(parameters, solverTimeout, depthLimit);
        //================================================================================================
        StopWatches.start("run");
        StopWatches.start("run:initialization");

        Benchmark benchmark = new Benchmark(parameters.getBenchmarkName(), parameters.getExpectedResult());
        //BenchmarkRepository.insertOrUpdate(benchmark);

        Run run = new Run(parameters.getBenchmarkName());
       //System.out.println(run.id+" "+parameters.getToolVariant()+" "+runTimeout+" "+iterationTimeout+" "+solverTimeout);
        run.id=0;
        //RunRepository.insertOrUpdate(run);

        Settings settings = new Settings(run.id, parameters.getToolVariant(), runTimeout, iterationTimeout, solverTimeout, 10);
        //SettingsRepository.insertOrUpdate(settings);
        //System.out.println("Settings initialization done");

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
                //System.out.println("Iterations "+iterations.size()+" "+iterations);
                Run finishedRun = this.finalizeRun(run, iterations, true, false, "");

               // TimeRepository.insertOrUpdate(TimeFactory.create(finishedRun, StopWatches.getTimes()));

                systemError.println("TIMEOUT: " +diffListener.partitionCount+" "+parameters.getTargetDirectory() + " -> " + finishedRun.result);
                System.out.println("TIMEOUT: " +diffListener.partitionCount+" "+ parameters.getTargetDirectory() + " -> " + finishedRun.result);
            } catch (Throwable e) {
                e.printStackTrace(systemError);
                e.printStackTrace(errorStream);
                throw e;
            }
        });

        Runtime.getRuntime().addShutdownHook(shutdownHook);
        boolean fuzzAgain=false;
        //System.out.println("Shutdown hook added.");
        //----------------------------------------------------------------------
         TimeoutChecker timeoutChecker;
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
            int maybeCount=0;
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
                configFile = this.createDifferencingJpfConfiguration(parameters, solverTimeout, depthLimit);

                Config config = JPF.createConfig(new String[]{configFile.getAbsolutePath()});
                JPF jpf = new JPF(config);
                SymbolicListener symbolicListener = new SymbolicListener(config, jpf);
                DifferencingListener diffListener = new DifferencingListener(iteration, parameters, solverTimeout, symbolicListener, combinedClass);

                // TimeoutChecker timeoutChecker = new TimeoutChecker(diffListener, iteration, iterationTimeout);
                 timeoutChecker = new TimeoutChecker(diffListener, iteration, iterationTimeout);

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

                    javaFile = this.createDifferencingDriverClass(parameters);
                    this.compile(ProjectPaths.classpath, javaFile);

                    StopWatches.stop("iteration-" + iteration.iteration + ":instrumentation");
                    StopWatches.start("iteration-" + iteration.iteration + ":symbolic-execution");
                    // File configFile = this.createDifferencingJpfConfiguration(parameters, solverTimeout, depthLimit);
                    // configFile = this.createDifferencingJpfConfiguration(parameters, solverTimeout, depthLimit);
                   
                    // Config config = JPF.createConfig(new String[]{configFile.getAbsolutePath()});
                    // JPF jpf = new JPF(config);

                    if(iteration.iteration==1){
                        shouldKeepIterating = true;
                        iteration=new Iteration(run.id,1);
                        iterations.put(iteration.iteration, iteration);
                        instrumentation.expandFunction("non-empty", changes);
                        continue;
                    }

                    jpf.addListener(unreachableListener);
                    jpf.addListener(timeoutChecker);
                    // jpf.addListener(new SymbolicListener(config, jpf));
                    jpf.addListener(symbolicListener);
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
                    if(diffListener.nullPartition==true){
                        if(symbolicListener.allSummaries.get(symbolicListener.currentMethodName).getPathConditions().size()<=0){
                            // System.out.println("Partition limit reached; so result should be MAYBE_EQ");
                            // systemError.println(parameters.getTargetDirectory() + " -> MAYBE_EQ");
                            iteration.result=Classification.MAYBE_EQ;
                         }
                     }
                    // systemOutput.print("Iteration " + iteration.iteration + " - SUCCESS: ");
                    systemOutput.println("Iteration " + iteration.iteration+" partitionCount "+diffListener.partitionCount +" " +parameters.getTargetDirectory() + " -> " + iteration.result);

                } else {
                    System.out.println("Iteration " + iteration.iteration +" partitionCount "+diffListener.partitionCount +" - ERROR: ");
                    System.out.println(parameters.getTargetDirectory() + " -> " + iteration.result);
                    systemError.println("Iteration " + iteration.iteration +" partitionCount "+diffListener.partitionCount +" - ERROR: ");
                    systemError.println(parameters.getTargetDirectory() + " -> " + iteration.result);
                }
                // if(diffListener.nullPartition==true){
                //    if(symbolicListener.allSummaries.get(symbolicListener.currentMethodName).getPathConditions().size()<=0){
                //        System.out.println("Partition limit reached; so result should be MAYBE_EQ");
                //        systemError.println(parameters.getTargetDirectory() + " -> MAYBE_EQ");
                //     }
                // }
                diffListener.partitionCount=0;

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
                    maybeCount++;
                    if (!nextToRefine.isEmpty()) {
                        instrumentation.expandFunction(nextToRefine, changes);
                        shouldKeepIterating = true;
                    } else {
                        // System.out.println("");
                        // systemOutput.println("Nothing left to refine.");
                        // System.out.println("Nothing left to refine. "+maybeCount);
                    }
                }
                // if(iteration.iteration==1)
                //     shouldKeepIterating=false;
                if(iteration.iteration>=10){
                    fuzzAgain=true;
                    shouldKeepIterating=false;
                }

                StopWatches.stop("iteration-" + iteration.iteration + ":refinement");
                StopWatches.stop("iteration-" + iteration.iteration);
            } while (shouldKeepIterating);
            if (timeoutChecker.partionReached==true){
                System.out.println("Partition limit reached; so result should be MAYBE_EQ");
                System.out.println("Run fuzzer again");
                fuzzAgain=true;
            }   
            System.out.println("Maybe count "+maybeCount);

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
        //System.out.println("hastimeout "+hasTimedOut);
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
        // System.out.println(parameters.getTargetClassName());
        file.getParentFile().mkdirs();

        try (PrintWriter writer = new PrintWriter(file)) {
            template.process(root, writer);
        }

        return file;
    }

    public File createDifferencingDriverClassFuzzing(DifferencingParameters parameters) throws IOException, TemplateException {
        /* Create a data-model */
        Map<String, Object> root = new HashMap<>();
        root.put("parameters", parameters);

        /* Get the template (uses cache internally) */
        Template template = this.freeMarkerConfiguration.getTemplate("DifferencingDriverClass2.ftl");

        /* Merge data-model with template */
        File file = new File(parameters.getTargetDirectory() + "/" + parameters.getTargetClassName() + "fuzz.java");
        // System.out.println(parameters.getTargetClassName());
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
