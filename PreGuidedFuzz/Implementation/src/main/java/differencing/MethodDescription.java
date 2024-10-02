package differencing;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class MethodDescription implements Serializable {
    private final String namespace;
    private final String className;
    private final String methodName;
    private final List<MethodParameterDescription> parameters;
    private final MethodResultDescription result;

    public MethodDescription(
        String namespace,
        String className,
        String methodName,
        List<MethodParameterDescription> parameters,
        MethodResultDescription result
    ) {
        this.namespace = namespace;
        this.className = className;
        this.methodName = methodName;
        this.parameters = parameters;
        this.result = result;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<MethodParameterDescription> getParameters() {
        return parameters;
    }

    public MethodResultDescription getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodDescription that = (MethodDescription) o;
        return Objects.equals(namespace, that.namespace)
            && Objects.equals(className, that.className)
            && Objects.equals(methodName, that.methodName)
            && Objects.equals(parameters, that.parameters)
            && Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, className, methodName, parameters, result);
    }
}
