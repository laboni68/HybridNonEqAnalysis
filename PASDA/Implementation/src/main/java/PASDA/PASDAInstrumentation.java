package PASDA;

import br.usp.each.saeg.asm.defuse.Variable;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import differencing.DifferencingParameterFactory;
import differencing.DifferencingParameters;
import equiv.checking.DefUseExtractor;
import equiv.checking.Instrumentation;
import equiv.checking.SourceInstrumentation;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PASDAInstrumentation implements SourceInstrumentation {
    protected final String toolName;
    protected final String path;
    protected final String classPathOld;
    protected final String classPathNew;
    protected final String methodPathOld;
    protected final String methodPathNew;

    protected String packageName;
    protected String classNameOld;
    protected String classNameNew;
    protected String methodName;
    protected int methodParameterCount;

    protected long initializationRuntime;
    protected long defUseAndUifRuntime;

    public PASDAInstrumentation(
        String toolName,
        String path,
        String classPathOld,
        String classPathNew,
        String methodPathOld,
        String methodPathNew
    ) {
        this.toolName = toolName;
        this.path = path;
        this.classPathOld = classPathOld;
        this.classPathNew = classPathNew;
        this.methodPathOld = methodPathOld;
        this.methodPathNew = methodPathNew;
    }

    public String getPackageName() {
        assert this.packageName != null;
        return this.packageName;
    }

    public String getOldClassName() {
        assert this.classNameOld != null;
        return this.classNameOld;
    }

    public String getNewClassName() {
        assert this.classNameNew != null;
        return this.classNameNew;
    }

    public String getMethodName() {
        assert this.methodName != null;
        return this.methodName;
    }

    public int getMethodParameterCount() {
        return this.methodParameterCount;
    }

    public long getInitializationRuntime() {
        return this.initializationRuntime;
    }

    public long getDefUseAndUifRuntime() {
        return this.defUseAndUifRuntime;
    }

    public void runInstrumentation(int iteration, ArrayList<Integer> changes) throws Exception {
        long start = System.nanoTime();

        ClassNode classNodeOld = new ClassNode();
        ClassReader classReaderOld = new ClassReader(new FileInputStream(this.classPathOld));
        classReaderOld.accept(classNodeOld, 0);
        List<MethodNode> methodNodesOld = classNodeOld.methods;
        MethodNode targetMethodNodeOld = methodNodesOld.get(1); // method 0 is by default the "init" method
        String classNameOld = "I" + classNodeOld.name.substring(classNodeOld.name.lastIndexOf("/") + 1);

        ClassNode classNodeNew = new ClassNode();
        ClassReader classReaderNew = new ClassReader(new FileInputStream(this.classPathNew));
        classReaderNew.accept(classNodeNew, 0);
        List<MethodNode> methodNodesNew = classNodeNew.methods;
        MethodNode targetMethodNodeNew = methodNodesNew.get(1); // method 0 is by default the "init" method
        String classNameNew = "I" + classNodeNew.name.substring(classNodeNew.name.lastIndexOf("/") + 1);

        long end = System.nanoTime();
        this.initializationRuntime = end - start;

        start = System.nanoTime();

        //****Generating the main methods of each class ******/
        Variable[] variables = DefUseExtractor.getVariables(targetMethodNodeOld);
        String[] methodParams = DefUseExtractor.extractParams(targetMethodNodeOld);
        String[] constructorParams = DefUseExtractor.extractParamsConstructor(methodNodesOld.get(0));
        Map<String, String> variablesNamesTypesMapping = DefUseExtractor.getVariableTypesMapping();

        Instrumentation instrument = new Instrumentation(this.path, this.toolName, iteration);

        String mainMethod1 = instrument.getMainProcedure(classNameOld, targetMethodNodeOld.name, methodParams, constructorParams, variablesNamesTypesMapping);
        String mainMethod2 = instrument.getMainProcedure(classNameNew, targetMethodNodeNew.name, methodParams, constructorParams, variablesNamesTypesMapping);

        //*************Creating the new class files and the modified procedures ***************/
        instrument.setMethods(methodNodesOld);
        instrument.saveNewProcedure(this.methodPathOld, classNameOld, new ArrayList<>(), new HashMap<>(), mainMethod1);

        instrument.setMethods(methodNodesNew);
        instrument.saveNewProcedure(this.methodPathNew, classNameNew, new ArrayList<>(), new HashMap<>(), mainMethod2);

        end = System.nanoTime();
        this.defUseAndUifRuntime = end - start;

        this.packageName = instrument.packageName();
        this.classNameOld = classNameOld + this.toolName + iteration;
        this.classNameNew = classNameNew + this.toolName + iteration;
        this.methodName = targetMethodNodeNew.name;
        this.methodParameterCount = methodParams.length;

        //*********************Creating the differencing parameters ******************/
        System.out.println("Creating differencing parameters... "+ this.methodName);

        DifferencingParameterFactory factory = new DifferencingParameterFactory();

        DifferencingParameters parameters = factory.create(
            this.toolName,
            this.path,
            instrument.packageName(),
            classNameOld,
            classNameNew,
            targetMethodNodeOld.desc,
            this.methodName,
            methodParams,
            variablesNamesTypesMapping
        );

        Path filepath = Paths.get(parameters.getParameterFile());
        factory.persist(filepath.toFile(), parameters);
    }

    @Override
    public String getNextToRefine(Context context, BoolExpr summaryOld, BoolExpr summaryNew, Map<String, Expr<?>> variables) throws IOException {
        // PASDA-base only generates the parameters file -> return nothing.
        return "";
    }

    @Override
    public void expandFunction(String statement, ArrayList<Integer> changes) {
        // PASDA-base only generates the parameters file -> do nothing.
    }
}
