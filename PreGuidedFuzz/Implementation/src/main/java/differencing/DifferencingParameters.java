package differencing;

import differencing.classification.Classification;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

public class DifferencingParameters implements Serializable {
    private static final Path BENCHMARKS_PATH = Paths.get("..", "benchmarks");

    private final String directory;
    private final String toolName;
    private final MethodDescription oldMethodDescription;
    private final MethodDescription newMethodDescription;
    public final MethodDescription diffMethodDescription;

    private int iteration = 1;

    public DifferencingParameters(
        String directory,
        String toolName,
        MethodDescription oldMethodDescription,
        MethodDescription newMethodDescription,
        MethodDescription diffMethodDescription
    ) {
        this.directory = directory;
        this.toolName = toolName;
        this.oldMethodDescription = oldMethodDescription;
        this.newMethodDescription = newMethodDescription;
        this.diffMethodDescription = diffMethodDescription;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public String getToolName() {
        return this.toolName;
    }

    public String getToolVariant() {
        return this.toolName + "-diff";
    }

    public String getTargetDirectory() {
        return this.directory;
    }

    public String getBenchmarkName() {
        Path path = BENCHMARKS_PATH.relativize(Paths.get(this.directory));
        return path.subpath(0, 3).toString();
    }

    public String getExpectedResult() {
        // Path path = BENCHMARKS_PATH.relativize(Paths.get(this.directory));

        // While some benchmarks have the Eq/NEq folder at the second level
        // of the hierarchy (e.g., ModDiff/Eq/Add), others have it at the
        // third level (e.g., power/test/Eq), so we have to check both.

        // String name1 = path.getName(1).toString();
        // String name2 = path.getName(2).toString();
        String path1=this.directory;
        // String name1 = path1.split("/")[path1.split("/").length-2];
        // String name2 = path1.split("/")[path1.split("/").length-1];
        // System.out.println("getExpectedResult: " + path1);
        // System.out.println("getExpectedResult: " + name1 + " " + name2);

        if (path1.contains("Eq") || path1.contains("Eq"))  {
            return Classification.EQ.toString();
        } else if (path1.contains("NEq") || path1.contains("NEq") || path1.contains("Neq") || path1.contains("Neq")) {
            return Classification.NEQ.toString();
        }

        throw new RuntimeException("Cannot determine expected result for " + this.directory + ".");
    }

    public String getParameterFile() {
        return Paths.get(this.directory, "IDiff" + this.toolName + "-Parameters.txt").toString();
    }

    public String getOutputFile() {
        return Paths.get(this.directory, "IDiff" + this.toolName + "-Output.txt").toString();
    }

    public String getErrorFile() {
        return Paths.get(this.directory, "IDiff" + this.toolName + "-Error.txt").toString();
    }

    public String getBaseToolOutputFile() {
        return Paths.get(this.directory, "..", "outputs", this.toolName + ".txt").toString();
    }

    public String getOldVJavaFile() {
        return Paths.get(this.directory).getParent().resolve("oldV.java").toString(); //need to be changed to oldV
    }

    public String getNewVJavaFile() {
        return Paths.get(this.directory).getParent().resolve("newV.java").toString(); //need to be changed to newV
    }

    public String[] getJavaFiles() throws IOException {
        return this.getFiles("glob:**/IDiff" + this.toolName + "*.java");
    }

    public String[] getJpfFiles() throws IOException {
        return this.getFiles("glob:**/IDiff" + this.toolName + "*.jpf");
    }

    public String[] getJsonFiles() throws IOException {
        return this.getFiles("glob:**/IDiff" + this.toolName + "*-JSON*.json");
    }

    private String[] getFiles(String glob) throws IOException {
        List<String> answerFiles = new ArrayList<>();

        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);
        Files.walkFileTree(Paths.get(this.getTargetDirectory()), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                if (pathMatcher.matches(path)) {
                    answerFiles.add(path.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return answerFiles.toArray(new String[0]);
    }

    public String[] getGeneratedFiles() throws IOException {
        List<String> generatedFiles = new ArrayList<>();

        generatedFiles.addAll(Arrays.asList(this.getJavaFiles()));
        generatedFiles.addAll(Arrays.asList(this.getJpfFiles()));
        generatedFiles.addAll(Arrays.asList(this.getJsonFiles()));
        generatedFiles.add(this.getOutputFile());
        generatedFiles.add(this.getErrorFile());

        return generatedFiles.toArray(new String[0]);
    }

    public String getTargetNamespace() {
        return this.diffMethodDescription.getNamespace();
    }

    public String getTargetClassName() {
       // System.out.println("getTargetClassName: " + this.diffMethodDescription.getClassName() + this.iteration);
        return this.diffMethodDescription.getClassName() + this.iteration;
    }

    public String getSymbolicParameters() {
        int parameterCount = this.diffMethodDescription.getParameters().size();
        return String.join("#", Collections.nCopies(parameterCount, "sym"));
    }

    public String getInputParameters() {
        return this.diffMethodDescription.getParameters().stream()
            .map(parameter -> parameter.getDataType() + " " + parameter.getName())
            .collect(Collectors.joining(", "));
    }

    public String getInputVariables() {
        return this.diffMethodDescription.getParameters().stream()
            .map(MethodParameterDescription::getName)
            .collect(Collectors.joining(", "));
    }

    public String getInputValues() {
        return this.diffMethodDescription.getParameters().stream()
            .map(MethodParameterDescription::getPlaceholderValue)
            .collect(Collectors.joining(", "));
    }
    
    public String getInputValues2() {
        String values="";
        System.out.println("getInputValues: " + this.diffMethodDescription.getParameters().size());
        int i=0;
        int getParamLength=this.diffMethodDescription.getParameters().size();
        if (getParamLength==0){
            return "";
        }
        while(i<getParamLength-1){
            values=values+this.diffMethodDescription.getParameters().get(i).getPlaceholderArgs(i)+",";
            i++;
        }
        // for (i=0;i<this.diffMethodDescription.getParameters().size()-1;i++) {
        //     values=values+this.diffMethodDescription.getParameters().get(i).getPlaceholderArgs(i)+",";
        // }
        values=values+this.diffMethodDescription.getParameters().get(i).getPlaceholderArgs(i);
        //System.out.println("getInputValues: " + values);
        return values;
        // System.out.println("getInputValues: " + values);
        // return this.diffMethodDescription.getParameters().stream()
        //     .map(MethodParameterDescription::getPlaceholderValue)
        //     .collect(Collectors.joining(", "));
    }

    public String getOldNamespace() {
        return this.oldMethodDescription.getNamespace();
    }

    public String getOldClassName() {
        return this.oldMethodDescription.getClassName() + this.iteration;
    }

    public String getOldReturnType() {
        return this.oldMethodDescription.getResult().getDataType();
    }

    public String getOldResultDefaultValue() {
        return this.oldMethodDescription.getResult().getPlaceholderValue();
    }

    public String getNewNamespace() {
        return this.newMethodDescription.getNamespace();
    }

    public String getNewClassName() {
        return this.newMethodDescription.getClassName() + this.iteration;
    }
    public String getMethodName() {
       //System.out.println("getMethodName: " + this.newMethodDescription.getMethodName());
        //return this.getMethodName();
        return this.newMethodDescription.getMethodName();
    }

    public String getNewReturnType() {
        return this.newMethodDescription.getResult().getDataType();
    }

    public String getNewResultDefaultValue() {
        return this.newMethodDescription.getResult().getPlaceholderValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DifferencingParameters that = (DifferencingParameters) o;
        return Objects.equals(directory, that.directory)
            && Objects.equals(oldMethodDescription, that.oldMethodDescription)
            && Objects.equals(newMethodDescription, that.newMethodDescription)
            && Objects.equals(diffMethodDescription, that.diffMethodDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            directory,
            oldMethodDescription,
            newMethodDescription,
            diffMethodDescription
        );
    }
}


