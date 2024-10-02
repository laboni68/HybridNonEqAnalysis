package differencing.transformer;

import differencing.domain.ConstantInteger;
import differencing.domain.ConstantReal;
import differencing.domain.ConstantString;
import differencing.domain.Expression;

public class ValueToModelTransformer {
    public Expression transform(Object object) {
        if (object instanceof Integer) {
            return this.transform((Integer) object);
        } else if (object instanceof Double) {
            return this.transform((Double) object);
        } else if (object instanceof String) {
            return this.transform((String) object);
        }
        throw new RuntimeException("Unable to transform object '" + object + "' of class '" + object.getClass() + "' to model.");
    }

    public ConstantInteger transform(Integer value) {
        return new ConstantInteger(value);
    }

    public ConstantReal transform(Double value) {
        return new ConstantReal(value);
    }

    public ConstantString transform(String value) {
        return new ConstantString(value);
    }
}
