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

    public static boolean areErrorsEquivalent(Throwable e, Throwable e1) {
        if (e == e1) {
            System.out.println("e == e1");
            return true; // Both are the same instance
        }

        if (e == null || e1 == null) {
            System.out.println("e == null || e1 == null");
            return false; // One is null and the other is not
        }

        if (!e.getClass().equals(e1.getClass())) {
            System.out.println("!e.getClass().equals(e1.getClass())");
            return false; // Different types
        }

        if (!Objects.equals(e.getMessage(), e1.getMessage())) {
            System.out.println("!Objects.equals(e.getMessage(), e1.getMessage())");
            return false; // Different messages
        }

        StackTraceElement[] eStackTrace = e.getStackTrace();
        StackTraceElement[] e1StackTrace = e1.getStackTrace();

        if (eStackTrace.length != e1StackTrace.length) {
            System.out.println("eStackTrace.length != e1StackTrace.length");
            return false; // Different stack trace lengths
        }

        // for (int i = 0; i < eStackTrace.length; i++) {
        //     if (!eStackTrace[i].equals(e1StackTrace[i])) {
        //         System.out.println("!eStackTrace[i].equals(e1StackTrace[i])"+eStackTrace[i]+" "+e1StackTrace[i]);
        //         return false; // Different stack trace elements
        //     }
        // }

        return areErrorsEquivalent(e.getCause(), e1.getCause()); // Compare causes recursively
    }

    public static boolean areResultsEquivalent(int a, int b) { return a==b; }
    public static boolean areResultsEquivalent(long a, long b) { return a==b; }
    public static boolean areResultsEquivalent(short a, short b) { return a==b; }
    public static boolean areResultsEquivalent(byte a, byte b) { return a==b; }
    public static boolean areResultsEquivalent(float a, float b) { return a==b; }
    public static boolean areResultsEquivalent(double a, double b) { return a==b; }
    public static boolean areResultsEquivalent(boolean a, boolean b) { return a==b; }
    public static boolean areResultsEquivalent(Object a, Object b) { return a==b; }

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
        System.out.println("  - Error Equivalent: " + areErrorsEquivalent);

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
        System.out.println("  - Equivalent: " + areResultsEquivalent+" "+result_new+" "+result_old);

        if (!areResultsEquivalent) {
            //String msg = "result_old != result_new";
            String msg = "result_old (" + result_old + ") != result_new (" + result_new + ")";
            throw new DifferentOutputsException(msg);
        }

        return result_new;
    }

    public static void main(String[] args) {
        ${parameters.targetClassName}.run(${parameters.inputValues});
    }
}
