package equiv.checking;

import differencing.StopWatches;
import differencing.classification.Classification;
import differencing.classification.IterationClassifier;
import differencing.models.Iteration;
import differencing.models.Run;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OutputParser {
    public static Map<Integer, Iteration> readIterations(Run run, String tool, String errors, boolean isTimeout) throws IOException {
        Path benchmarkPath = Paths.get("..", "benchmarks", run.benchmark);
        Path outputFilePath = benchmarkPath.resolve("outputs").resolve(tool + ".txt");

        if (!outputFilePath.toFile().exists()) {
            boolean isError = !errors.isEmpty();
            assert isError || isTimeout;
            return Collections.singletonMap(1, new Iteration(
                run.id, 1,
                isTimeout ? Classification.TIMEOUT : Classification.ERROR,
                isTimeout, null, null, null,
                StopWatches.getTimeOrDefault("iteration-" + 1, null),
                errors
            ));
        }

        String outputString = new String(Files.readAllBytes(outputFilePath));
        String[] outputs = outputString.split("--Results--");

        Map<Integer, Iteration> iterations = new HashMap<>();

        for (int i = 1; i < outputs.length; i++) {
            String output = outputs[i];

            Boolean isDepthLimited = isDepthLimited(benchmarkPath, tool, i);
            Boolean hasUif = hasUif(benchmarkPath, tool, i);

            // The output file only contains completed iterations.
            // Thus, none of the read iterations should be classified as timed out.
            boolean hasTimedOut = false;

            Classification result = new IterationClassifier(
                false, false, !errors.isEmpty(), hasTimedOut,
                Collections.singletonList(classify(output, isDepthLimited))
            ).getClassification();

            boolean isLastIteration = i == outputs.length - 1;

            iterations.put(i, new Iteration(
                run.id, i, result,
                hasTimedOut, isDepthLimited, hasUif, null,
                StopWatches.getTime("iteration-" + i),
                isLastIteration ? errors : ""
            ));
        }

        // The output file only contains completed iterations.
        // Thus, we should always add another iteration that represent the
        // currently-in-progress iteration that hasn't been written to the
        // output file yet.
        if (isTimeout) {
            int iteration = iterations.size() + 1;
            iterations.put(iteration, new Iteration(
                run.id, iteration, Classification.TIMEOUT,
                true, null, null, null,
                StopWatches.getTimeOrDefault("iteration-" + iteration, null),
                ""
            ));
        }

        return iterations;
    }

    private static Classification classify(String output, Boolean isDepthLimited) {
        int index = output.indexOf("Output : ");
        if (index == -1) {
            throw new RuntimeException("Unable to classify output '" + output + "'.");
        }

        String relevantOutput = output.substring(index);
        if (relevantOutput.startsWith("Output : EQUIVALENT")) {
            if (isDepthLimited == null || isDepthLimited) {
                // An EQ classification can only be made if:
                // (1) both programs were fully analyzed
                // (2) no NEQ indicators were found.
                //
                // Depth-limited runs fail requirement (1). Thus, they can only
                // ever be classified as MAYBE_EQ, even if requirement (2) is
                // met, since there might still be NEQ indicators in the part
                // of the code that was not analyzed due to the depth-limit.
                return Classification.MAYBE_EQ;
            } else {
                return Classification.EQ;
            }
        } else if (relevantOutput.startsWith("Output : NOT EQUIVALENT")) {
            return Classification.NEQ;
        } else if (relevantOutput.startsWith("Output : UNKNOWN")) {
            if (relevantOutput.contains("too much abstraction")) {
                return Classification.MAYBE_NEQ;
            } else {
                return Classification.UNKNOWN;
            }
        }

        throw new RuntimeException("Unable to classify output '" + output + "'.");
    }

    private static Boolean isDepthLimited(Path benchmarkPath, String tool, Integer iteration) throws IOException {
        if (iteration == null) {
            return true;
        }

        Path instrumentedPath = benchmarkPath.resolve("instrumented");
        String depthLimitReached = "depth limit reached";

        String oldOutputFileName = "IoldV" + tool + iteration + "JPFOutput.txt";
        Path oldOutputFilePath = instrumentedPath.resolve(oldOutputFileName);
        String newOutputFileName = "InewV" + tool + iteration + "JPFOutput.txt";
        Path newOutputFilePath = instrumentedPath.resolve(newOutputFileName);

        if (!oldOutputFilePath.toFile().exists() || !newOutputFilePath.toFile().exists()) {
            return null;
        }

        String oldOutput = new String(Files.readAllBytes(oldOutputFilePath));
        boolean isOldVDepthLimited = oldOutput.contains(depthLimitReached);
        String newOutput = new String(Files.readAllBytes(newOutputFilePath));
        boolean isNewVDepthLimited = newOutput.contains(depthLimitReached);

        return isOldVDepthLimited || isNewVDepthLimited;
    }

    private static Boolean hasUif(Path benchmarkPath, String tool, int iteration) throws IOException {
        Path toSolvePath = benchmarkPath.resolve("instrumented").resolve("InewV" + tool + iteration + "ToSolve.txt");
        if (!toSolvePath.toFile().exists()) {
            return null;
        }
        String toSolve = new String(Files.readAllBytes(toSolvePath));
        return toSolve.contains("(declare-fun UF_");
    }
}
