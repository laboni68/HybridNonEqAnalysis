package differencing;

import java.io.Serializable;
import java.util.Objects;

public class MethodResultDescription implements Serializable {
    private final String dataType;

    public MethodResultDescription(String dataType) {
        this.dataType = dataType;
    }

    public String getDataType() {
        return this.dataType;
    }

    public String getPlaceholderValue() {
        switch (this.dataType) {
            case "int":
            case "long":
            case "short":
            case "byte":
            case "float":
            case "double":
            case "char":
                return "0";
            case "boolean":
                return "true";
            case "String":
                return "\"test\"";
            default:
                return "null";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodResultDescription that = (MethodResultDescription) o;
        return Objects.equals(dataType, that.dataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataType);
    }
}
