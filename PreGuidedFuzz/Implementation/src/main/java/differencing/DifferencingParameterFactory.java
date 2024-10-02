package differencing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DifferencingParameterFactory {
    public void persist(File file, DifferencingParameters parameters) throws IOException {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        String json = gson.toJson(parameters);

        Files.write(file.toPath(), json.getBytes());
    }

    public DifferencingParameters load(File file) throws IOException {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        String json = new String(Files.readAllBytes(file.toPath()));

        return gson.fromJson(json, DifferencingParameters.class);
    }

    public DifferencingParameters create(String toolName, String directory) {
        return new DifferencingParameters(directory, toolName, null, null, null);
    }

    public DifferencingParameters create(
        String toolName,
        String directory,
        String namespace,
        String oldClassName,
        String newClassName,
        String methodSignature,
        String methodName,
        String[] methodParameters,
        Map<String, String> parameterTypes
    ) {
        List<MethodParameterDescription> parameterDescriptions = this.createParameterDescriptions(methodParameters, parameterTypes);
        MethodResultDescription resultDescription = this.createMethodResult(methodSignature);
        // System.out.println("methodSignature: " + methodSignature);

        MethodDescription oldMethodDescription = new MethodDescription(
            namespace,
            oldClassName + toolName,
            methodName,
            parameterDescriptions,
            resultDescription
        );

        MethodDescription newMethodDescription = new MethodDescription(
            namespace,
            newClassName + toolName,
            methodName,
            parameterDescriptions,
            resultDescription
        );

        MethodDescription diffMethodDescription = new MethodDescription(
            namespace,
            "IDiff" + toolName,
            "run",
            parameterDescriptions,
            resultDescription
        );

        return new DifferencingParameters(
            directory,
            toolName,
            oldMethodDescription,
            newMethodDescription,
            diffMethodDescription
        );
    }

    protected List<MethodParameterDescription> createParameterDescriptions(
        String[] methodParameters, Map<String, String> parameterTypes
    ) {
        return Arrays.stream(methodParameters)
            .map(parameterName -> {
                String dataType = Type.getType(parameterTypes.get(parameterName)).getClassName();
                return new MethodParameterDescription(parameterName, dataType);
            }).collect(Collectors.toList());
    }

    protected MethodResultDescription createMethodResult(String methodSignature) {
        return new MethodResultDescription(Type.getMethodType(methodSignature).getReturnType().getClassName());
    }
}
