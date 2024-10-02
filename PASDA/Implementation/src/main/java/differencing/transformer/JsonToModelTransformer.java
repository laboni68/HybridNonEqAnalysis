package differencing.transformer;

import com.google.gson.*;
import differencing.domain.Error;
import differencing.domain.*;

import java.lang.reflect.Type;

public class JsonToModelTransformer {
    private final Gson gson;

    public JsonToModelTransformer() {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        builder.setPrettyPrinting();

        builder.registerTypeAdapter(Operation.class, new OperationDeserializer());
        builder.registerTypeAdapter(Operator.class, new OperatorDeserializer());
        builder.registerTypeAdapter(ConstantInteger.class, new ConstantIntegerDeserializer());
        builder.registerTypeAdapter(ConstantReal.class, new ConstantRealDeserializer());
        builder.registerTypeAdapter(ConstantString.class, new ConstantStringDeserializer());
        builder.registerTypeAdapter(VariableInteger.class, new VariableIntegerDeserializer());
        builder.registerTypeAdapter(VariableReal.class, new VariableRealDeserializer());
        builder.registerTypeAdapter(VariableString.class, new VariableStringDeserializer());
        builder.registerTypeAdapter(SymbolicIntegerFunction.class, new SymbolicIntegerFunctionDeserializer());
        builder.registerTypeAdapter(SymbolicRealFunction.class, new SymbolicRealFunctionDeserializer());
        builder.registerTypeAdapter(SymbolicStringFunction.class, new SymbolicStringFunctionDeserializer());
        builder.registerTypeAdapter(Error.class, new ErrorDeserializer());

        builder.registerTypeHierarchyAdapter(Model.class, new ModelDeserializer());

        this.gson = builder.create();
    }

    public Model transform(String json) {
        return this.gson.fromJson(json, Model.class);
    }

    private static class ModelDeserializer implements JsonDeserializer<Model> {
        @Override
        public Model deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String packageName = Model.class.getPackage().getName();
            String className = packageName + "." + jsonObject.get("_type").getAsString();

            try {
                return context.deserialize(jsonElement, Class.forName(className));
            } catch (ClassNotFoundException exception) {
                throw new JsonParseException("Unknown class: " + className, exception);
            }
        }
    }

    private static class OperationDeserializer implements JsonDeserializer<Operation> {
        @Override
        public Operation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            Expression left = context.deserialize(jsonObject.get("left"), Expression.class);
            Operator op = context.deserialize(jsonObject.get("op"), Operator.class);
            Expression right = context.deserialize(jsonObject.get("right"), Expression.class);

            return new Operation(left, op, right);
        }
    }

    private static class OperatorDeserializer implements JsonDeserializer<Operator> {
        @Override
        public Operator deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            return Operator.get(jsonElement.getAsJsonObject().get("symbol").getAsString());
        }
    }

    private static class ConstantIntegerDeserializer implements JsonDeserializer<ConstantInteger> {
        @Override
        public ConstantInteger deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            return new ConstantInteger(jsonElement.getAsJsonObject().get("value").getAsLong());
        }
    }

    private static class ConstantRealDeserializer implements JsonDeserializer<ConstantReal> {
        @Override
        public ConstantReal deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            return new ConstantReal(jsonElement.getAsJsonObject().get("value").getAsDouble());
        }
    }

    private static class ConstantStringDeserializer implements JsonDeserializer<ConstantString> {
        @Override
        public ConstantString deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            return new ConstantString(jsonElement.getAsJsonObject().get("value").getAsString());
        }
    }

    private static class VariableIntegerDeserializer implements JsonDeserializer<VariableInteger> {
        @Override
        public VariableInteger deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            return new VariableInteger(jsonElement.getAsJsonObject().get("name").getAsString());
        }
    }

    private static class VariableRealDeserializer implements JsonDeserializer<VariableReal> {
        @Override
        public VariableReal deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            return new VariableReal(jsonElement.getAsJsonObject().get("name").getAsString());
        }
    }

    private static class VariableStringDeserializer implements JsonDeserializer<VariableString> {
        @Override
        public VariableString deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            return new VariableString(jsonElement.getAsJsonObject().get("name").getAsString());
        }
    }

    private static class SymbolicIntegerFunctionDeserializer implements JsonDeserializer<SymbolicIntegerFunction> {
        @Override
        public SymbolicIntegerFunction deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            String name = jsonObject.get("name").getAsString();
            Expression[] args = context.deserialize(jsonObject.get("args"), Expression[].class);

            return new SymbolicIntegerFunction(name, args);
        }
    }

    private static class SymbolicRealFunctionDeserializer implements JsonDeserializer<SymbolicRealFunction> {
        @Override
        public SymbolicRealFunction deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            String name = jsonObject.get("name").getAsString();
            Expression[] args = context.deserialize(jsonObject.get("args"), Expression[].class);

            return new SymbolicRealFunction(name, args);
        }
    }

    private static class SymbolicStringFunctionDeserializer implements JsonDeserializer<SymbolicStringFunction> {
        @Override
        public SymbolicStringFunction deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            String name = jsonObject.get("name").getAsString();
            Expression[] args = context.deserialize(jsonObject.get("args"), Expression[].class);

            return new SymbolicStringFunction(name, args);
        }
    }

    private static class ErrorDeserializer implements JsonDeserializer<Error> {
        @Override
        public Error deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            String errorType = jsonObject.get("type").getAsString();
            String message = jsonObject.get("message").getAsString();

            return new Error(errorType, message);
        }
    }
}
