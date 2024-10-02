//MIT-LICENSE
//Copyright (c) 2020-, Sahar Badihi, The University of British Columbia, and a number of other of contributors
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
//to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
//and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package DSE;

import br.usp.each.saeg.asm.defuse.Variable;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import differencing.DifferencingParameterFactory;
import differencing.DifferencingParameters;
import equiv.checking.CommonBlockExtractor;
import equiv.checking.DefUseExtractor;
import equiv.checking.Instrumentation;
import equiv.checking.SourceInstrumentation;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DSEInstrumentation implements SourceInstrumentation {
    protected final String toolName;
    protected final String path;
    protected final String classPathOld;
    protected final String classPathNew;
    protected final String methodPathOld;
    protected final String methodPathNew;
    protected final int timeout;

    protected ArrayList<ArrayList<Integer>> blocks;
    protected Map<Integer, String[]> outputsPerBlockOld;
    protected Map<Integer, String[]> outputsPerBlockNew;
    protected Map<Integer, Map<Integer, Pair<String, int[]>>> statementInfoPerBlockOld;
    protected Map<Integer, Map<Integer, Pair<String, int[]>>> statementInfoPerBlockNew;
    protected ArrayList<LinkedHashMap<String, Pair<Boolean, HashSet<String>>>> blockResultsOld;
    protected ArrayList<LinkedHashMap<String, Pair<Boolean, HashSet<String>>>> blockResultsNew;

    protected String packageName;
    protected String classNameOld;
    protected String classNameNew;
    protected String methodName;
    protected int methodParameterCount;

    protected long initializationRuntime;
    protected long totalInitializationRuntime;

    protected long defUseAndUifRuntime;
    protected long totalDefUseAndUifRuntime;

    public DSEInstrumentation(
        String toolName,
        String path,
        String classPathOld,
        String classPathNew,
        String methodPathOld,
        String methodPathNew,
        int timeout
    ) {
        this.toolName = toolName;
        this.path = path;
        this.classPathOld = classPathOld;
        this.classPathNew = classPathNew;
        this.methodPathOld = methodPathOld;
        this.methodPathNew = methodPathNew;
        this.timeout = timeout;
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

    public long getTotalInitializationRuntime() {
        return this.totalInitializationRuntime;
    }

    public long getDefUseAndUifRuntime() {
        return this.defUseAndUifRuntime;
    }

    public long getTotalDefUseAndUifRuntime() {
        return this.totalDefUseAndUifRuntime;
    }

    public void runInstrumentation(int iteration, ArrayList<Integer> changes) throws Exception {
        long start = System.nanoTime();

        ClassNode classNodeOld = new ClassNode();
        ClassReader classReaderOld = new ClassReader(new FileInputStream(this.classPathOld));
        classReaderOld.accept(classNodeOld, 0);
        List<MethodNode> methodNodesOld = classNodeOld.methods;
        MethodNode targetMethodNodeOld = methodNodesOld.get(1);//method 0 is by default the "init" method
        String classNameOld = "I" + classNodeOld.name.substring(classNodeOld.name.lastIndexOf("/") + 1);

        ClassNode classNodeNew = new ClassNode();
        ClassReader classReaderNew = new ClassReader(new FileInputStream(this.classPathNew));
        classReaderNew.accept(classNodeNew, 0);
        List<MethodNode> methodNodesNew = classNodeNew.methods;
        MethodNode targetMethodNodeNew = methodNodesNew.get(1);//method 0 is by default the "init" method
        String classNameNew = "I" + classNodeNew.name.substring(classNodeNew.name.lastIndexOf("/") + 1);

        long end = System.nanoTime();
        this.initializationRuntime = end - start;
        this.totalInitializationRuntime += this.initializationRuntime;

        start = System.nanoTime();
        String commonBlocks = CommonBlockExtractor.saveCommonBlocks(this.path, this.methodPathOld, this.methodPathNew, changes);

        //************************For method 1**********************/
        Variable[] variablesOld = DefUseExtractor.getVariables(targetMethodNodeOld);
        String[] methodParams = DefUseExtractor.extractParams(targetMethodNodeOld);
        String[] constructorParams = DefUseExtractor.extractParamsConstructor(methodNodesOld.get(0));
        Map<String, String> variablesNamesTypesMappingOld = DefUseExtractor.getVariableTypesMapping();

        TreeMap<Integer, Pair<String, HashSet<String>>> defUsePerLineOld = DefUseExtractor.defUsePerLine(targetMethodNodeOld);
        TreeMap<Integer, Pair<String, HashSet<String>>> defUsePerLineNew = DefUseExtractor.defUsePerLine(targetMethodNodeNew);

        mergeDefUse(defUsePerLineOld, defUsePerLineNew, changes);

        this.blockResultsOld = DefUseExtractor.extractBlocksInputsOutputs(defUsePerLineOld, CommonBlockExtractor.rootOld, targetMethodNodeOld, CommonBlockExtractor.blocks);
        this.outputsPerBlockOld = DefUseExtractor.outputsPerBlock();
        this.statementInfoPerBlockOld = DefUseExtractor.getStatementInfoPerBlock();

        //************************For method 2**********************/
        Variable[] variablesNew = DefUseExtractor.getVariables(targetMethodNodeNew);
        String[] methodParamsNew = DefUseExtractor.extractParams(targetMethodNodeNew);
        String[] constructorParamsNew = DefUseExtractor.extractParamsConstructor(methodNodesNew.get(0));
        Map<String, String> variablesNamesTypesMappingNew = DefUseExtractor.getVariableTypesMapping();

        this.blockResultsNew = DefUseExtractor.extractBlocksInputsOutputs(defUsePerLineNew, CommonBlockExtractor.rootNew, targetMethodNodeNew, CommonBlockExtractor.blocks);
        this.outputsPerBlockNew = DefUseExtractor.outputsPerBlock();
        this.statementInfoPerBlockNew = DefUseExtractor.getStatementInfoPerBlock();

        //*********************************************************/
        Instrumentation instrument = new Instrumentation(this.path, this.toolName, iteration);
        instrument.setBlocks(CommonBlockExtractor.blocks);
        this.blocks = CommonBlockExtractor.blocks;

        //************Mapping each block index to a list of actual variable names ***********/
        Pair<ArrayList<String>, Map<Integer, ArrayList<String>>> uF = instrument.creatingUninterpretedFunction(this.blockResultsOld, variablesNamesTypesMappingOld, methodParams);
        Pair<ArrayList<String>, Map<Integer, ArrayList<String>>> uF2 = instrument.creatingUninterpretedFunction(this.blockResultsNew, variablesNamesTypesMappingNew, methodParams);

        //****Generating the main methods of each class ******/
        String mainMethod1 = instrument.getMainProcedure(classNameOld, targetMethodNodeNew.name, methodParams, constructorParams, variablesNamesTypesMappingOld);
        String mainMethod2 = instrument.getMainProcedure(classNameNew, targetMethodNodeNew.name, methodParams, constructorParams, variablesNamesTypesMappingNew);

        //*************Creating the new class files and the modified procedures ***************/
        instrument.setMethods(methodNodesOld);
        instrument.saveNewProcedure(this.methodPathOld, classNameOld, uF.getKey(), uF.getValue(), mainMethod1);
        instrument.setMethods(methodNodesNew);
        instrument.saveNewProcedure(this.methodPathNew, classNameNew, uF2.getKey(), uF2.getValue(), mainMethod2);

        end = System.nanoTime();
        this.defUseAndUifRuntime = end - start;
        this.totalDefUseAndUifRuntime += this.defUseAndUifRuntime;

        this.packageName = instrument.packageName();
        this.classNameOld = classNameOld + this.toolName + iteration;
        this.classNameNew = classNameNew + this.toolName + iteration;
        this.methodName = targetMethodNodeNew.name;
        this.methodParameterCount = methodParams.length;

        //*********************Creating the differencing parameters ******************/

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
            variablesNamesTypesMappingOld
        );

        Path filepath = Paths.get(parameters.getParameterFile());
        factory.persist(filepath.toFile(), parameters);
    }

    /**
     * This function serves to guarantee the def-uses match in case a variable is used in a program and not in the other
     */
    private static void mergeDefUse(
        TreeMap<Integer, Pair<String, HashSet<String>>> defUsePerLineOld,
        TreeMap<Integer, Pair<String, HashSet<String>>> defUsePerLineNew,
        ArrayList<Integer> changes
    ) {
        for (int i = 0; i < defUsePerLineOld.size(); i++) {
            if (!changes.contains(i)) {
                Pair<String, HashSet<String>> defUseOld = defUsePerLineOld.get(i), defUseNew = defUsePerLineNew.get(i);
                if (defUseOld == null) {
                    if (defUseNew != null)
                        defUsePerLineOld.put(i, defUseNew);

                } else if (defUseNew == null) {
                    defUsePerLineNew.put(i, defUseOld);
                } else {//both are non-null
                    String var1 = defUseOld.getKey(), var2 = defUseNew.getKey();
                    if (var1 == null) {
                        if (var2 != null) { //we update the variable name in the other program but keep the same inputs
                            HashSet<String> inputs1 = defUseOld.getValue();
                            defUsePerLineOld.remove(i);
                            defUsePerLineOld.put(i, new MutablePair<>(var2, inputs1));
                        }
                    } else if (var2 == null) {
                        HashSet<String> inputs2 = defUseNew.getValue();
                        defUsePerLineNew.remove(i);
                        defUsePerLineNew.put(i, new MutablePair<>(var1, inputs2));
                    }
                }
            }
        }
    }

    @Override
    public String getNextToRefine(Context context, BoolExpr summaryOld, BoolExpr summaryNew, Map<String, Expr<?>> variables) throws IOException {
        // DSE has no refinement strategy -> return nothing.
        return "";
    }

    @Override
    public void expandFunction(String statement, ArrayList<Integer> changes) {
        // DSE has no expansion strategy -> do nothing.
    }
}
