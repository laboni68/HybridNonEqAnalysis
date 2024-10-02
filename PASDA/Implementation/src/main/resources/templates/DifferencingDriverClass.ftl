<#-- @ftlvariable name="parameters" type="differencing.DifferencingParameters" -->

package ${parameters.targetNamespace};

import ${parameters.newNamespace}.${parameters.newClassName};
import ${parameters.oldNamespace}.${parameters.oldClassName};
import gov.nasa.jpf.symbc.Debug;

import java.util.Objects;

class DifferentOutputsException extends RuntimeException {
    public DifferentOutputsException() {
    }

    public DifferentOutputsException(String message) {
        super(message);
    }

    public DifferentOutputsException(String message, Throwable cause) {
        super(message, cause);
    }

    public DifferentOutputsException(Throwable cause) {
        super(cause);
    }

    public DifferentOutputsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

public class ${parameters.targetClassName} {

    public static boolean areErrorsEquivalent(Object a, Object b) { return false; }

    public static boolean areResultsEquivalent(int a, int b) { return false; }
    public static boolean areResultsEquivalent(long a, long b) { return false; }
    public static boolean areResultsEquivalent(short a, short b) { return false; }
    public static boolean areResultsEquivalent(byte a, byte b) { return false; }
    public static boolean areResultsEquivalent(float a, float b) { return false; }
    public static boolean areResultsEquivalent(double a, double b) { return false; }
    public static boolean areResultsEquivalent(boolean a, boolean b) { return false; }
    public static boolean areResultsEquivalent(Object a, Object b) { return false; }

    public static ${parameters.oldReturnType} run(${parameters.inputParameters}) {
        ${parameters.oldReturnType} result_old = ${parameters.oldResultDefaultValue};
        ${parameters.newReturnType} result_new = ${parameters.newResultDefaultValue};

        Throwable error_old = null;
        Throwable error_new = null;

        try {
            result_old = ${parameters.oldClassName}.${parameters.methodName}(${parameters.inputVariables});
        } catch (Throwable e) {
            error_old = e;
        }

        try {
            result_new = ${parameters.newClassName}.${parameters.methodName}(${parameters.inputVariables});
        } catch (Throwable e) {
            error_new = e;
        }

        boolean areErrorsEquivalent = areErrorsEquivalent(error_old, error_new);

        System.out.println("Differencing Driver Output:");

        System.out.println("  Errors:");
        System.out.println("  - Old: " + error_old);
        System.out.println("  - New: " + error_new);
        System.out.println("  - Equivalent: " + areErrorsEquivalent);

        if (!areErrorsEquivalent) {
            if (error_old != null && error_new != null) {
                String msg = "result_old (" + error_old + ") != result_new (" + error_new + ")";
                throw new DifferentOutputsException(msg);
            } else if (error_old != null) { // && error_new == null
                String msg = "result_old (" + error_old + ") != result_new";
                throw new DifferentOutputsException(msg);
            } else { // error_old == null & error_new != null
                String msg = "result_old != result_new (" + error_new + ")";
                throw new DifferentOutputsException(msg);
            }
        }

        boolean areResultsEquivalent = areResultsEquivalent(result_old, result_new);

        System.out.println("  Results:");
        System.out.println("  - Equivalent: " + areResultsEquivalent);

        if (!areResultsEquivalent) {
            String msg = "result_old != result_new";
            throw new DifferentOutputsException(msg);
        }

        return result_new;
    }

    public static void main(String[] args) {
        ${parameters.targetClassName}.run(${parameters.inputValues});
    }
}
