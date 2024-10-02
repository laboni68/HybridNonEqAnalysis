package differencing;

import java.io.Serializable;
import java.util.Objects;

public class MethodParameterDescription implements Serializable {
    private final String name;
    private final String dataType;

    public MethodParameterDescription(String name, String dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    public String getName() {
        return this.name;
    }

    public String getDataType() {
        return this.dataType;
    }

    public String getPlaceholderValue() {
        switch (this.dataType) {
            case "boolean":
                return "true";
            case "char":
                return "'a'";
            case "String":
                return "\"test\"";
            case "double[]":
                return "new double[]{5,6,7,8,9}";
            case "int[]":
                return "new int[]{5,6,7,8,9}";
            case "String[]":
                return "new String[]{\"yes\",\"no\"}";
            case "int":
                return "1";
            default:
                return "1";
        }
    }

    public String getPlaceholderArgs(int index) {
        switch (this.dataType) {
            case "boolean":
                return "true";
            case "char":
                return "args[" + index + "].charAt(0)";
            case "String":
                return "args[" + index + "]";
            case "double[]":
                return "new double[]{5,6,7,8,9}";
            case "int[]":
                return "new int[]{5,6,7,8,9}";
            case "String[]":
                return "new String[]{\"yes\",\"no\"}";
            case "int":
                return "Integer.parseInt(args[" + index + "])";
            case "double":
                return "Double.parseDouble(args[" + index + "])";
            default:
                return "";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodParameterDescription that = (MethodParameterDescription) o;
        return Objects.equals(name, that.name) && Objects.equals(dataType, that.dataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dataType);
    }
}
