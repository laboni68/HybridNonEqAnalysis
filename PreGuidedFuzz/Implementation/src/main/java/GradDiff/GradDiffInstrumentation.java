//MIT-LICENSE
//Copyright (c) 2020-, Sahar Badihi, The University of British Columbia, and a number of other of contributors
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
//to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
//and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package GradDiff;

import DSE.DSEInstrumentation;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import equiv.checking.ProjectPaths;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class GradDiffInstrumentation extends DSEInstrumentation {
    private final boolean H1;
    private final boolean H2;
    private final boolean H31;
    private final boolean H32;
    private final String strategy;

    public GradDiffInstrumentation(
        String toolName,
        String path,
        String classPathOld,
        String classPathNew,
        String methodPathOld,
        String methodPathNew,
        int timeout,
        boolean H1,
        boolean H2,
        boolean H31,
        boolean H32,
        String strategy
    ) {
        super(toolName, path, classPathOld, classPathNew, methodPathOld, methodPathNew, timeout);
        this.H1 = H1;
        this.H2 = H2;
        this.H31 = H31;
        this.H32 = H32;
        this.strategy = strategy;
    }

    /**
     * This function is to determine the next uninterpreted function to refine
     * @return the next function to refine
     */
    public String getNextToRefine(Context context, BoolExpr summaryOld, BoolExpr summaryNew, Map<String, Expr<?>> variables) throws IOException {
        if (context == null || summaryOld == null || summaryNew == null || variables == null) {
            return "";
        }

        HashMap<String, Pair<FuncDecl<?>, HashSet<Expr<?>>>> functionsInstances = new HashMap<>();
        HashMap<Expr<?>, Integer> uFunctionsOld = new HashMap<>();
        HashMap<Expr<?>, Integer> uFunctionsNew = new HashMap<>();
        HashSet<Expr<?>> procCalls = new HashSet<>();

        //***To be added for trigo functions ******/
        this.createInstances(summaryOld, functionsInstances, uFunctionsOld, procCalls);
        this.createInstances(summaryNew, functionsInstances, uFunctionsNew, procCalls);

        //************************************************************Random*********************************************************************/

        if (this.strategy.equals("R")) {
            String pick = this.randomStrategy(
                uFunctionsOld,
                uFunctionsNew,
                functionsInstances
            );
            if (pick != null) {
                return pick;
            }
        }

        //************************************************************Heuristics*********************************************************************/

        ArrayList<String> functions = new ArrayList<>();

        if (this.H1) {
            this.heuristicH1(
                context,
                summaryOld,
                summaryNew,
                variables,
                uFunctionsOld,
                functionsInstances,
                procCalls,
                functions
            );
        }

        if (this.H2) {
            this.heuristicH2(
                uFunctionsOld,
                uFunctionsNew,
                functionsInstances,
                functions
            );
        }

        //****************************Intersection of H1 and H2********************************/

        List<String> functionsWithoutDuplicates = functions.stream().distinct().collect(Collectors.toList());
        //*add everyting left*****/
        if (functionsWithoutDuplicates.size() == 0) {
            Set<String> both = new HashSet<>();
            uFunctionsNew.keySet().forEach(expr -> both.add(expr.getFuncDecl().getName().toString()));
            uFunctionsOld.keySet().forEach(expr -> both.add(expr.getFuncDecl().getName().toString()));
            functionsWithoutDuplicates = getOrderedFunctionsList(getRestrictedInstances(functionsInstances, both), true);
        }

        if (functionsWithoutDuplicates.size() != 0) { //UNFunc left?
            //***************************************H Third********************************/
            if (this.H31 || this.H32) {
                return heuristicH3(functionsWithoutDuplicates);
            } else {
                //****************************without H3********************************/
                //****************************Randomly pick one********************************/
                Random rand = new Random();
                String radnomFunc = functionsWithoutDuplicates.get(rand.nextInt(functionsWithoutDuplicates.size()));
                Map<Integer, Pair<String, int[]>> statements = getStatementsFromFunction(radnomFunc);
                List<Integer> keysAsArray = new ArrayList<>(statements.keySet());
                Integer line = keysAsArray.get(rand.nextInt(keysAsArray.size()));
                return Integer.toString(line);
            }
        }

        return "";
    }


    /**
     * This function picks an uninterpreted function randomly
     * @return the chosen uninterpreted function
     */
    public String randomStrategy(
        HashMap<Expr<?>, Integer> uFunctionsOld,
        HashMap<Expr<?>, Integer> uFunctionsNew,
        HashMap<String, Pair<FuncDecl<?>, HashSet<Expr<?>>>> functionsInstances
    ) {
        Set<String> both = new HashSet<>();
        uFunctionsOld.keySet().forEach(expr -> both.add(expr.getFuncDecl().getName().toString()));
        uFunctionsNew.keySet().forEach(expr -> both.add(expr.getFuncDecl().getName().toString()));
        List<String> functionsWithoutDuplicates = getOrderedFunctionsList(getRestrictedInstances(functionsInstances, both), true);
        if (functionsWithoutDuplicates.size() != 0) {
            //**************************pick a random***************************************/
            Random rand = new Random();
            String radnomFunc = functionsWithoutDuplicates.get(rand.nextInt(functionsWithoutDuplicates.size()));
            Map<Integer, Pair<String, int[]>> statements = getStatementsFromFunction(radnomFunc);
            List<Integer> keysAsArray = new ArrayList<>(statements.keySet());
            Integer line = keysAsArray.get(rand.nextInt(keysAsArray.size()));
            return Integer.toString(line);
        }
        return null;
    }

    /**
     * This function applies the first heuristic
     * It checks if there exists an uninterpreted function for which a fixed value would make both programs equivalent no matter the values of the other variables
     * @param functions the list of uninterpreted functions
     */
    public void heuristicH1(
        Context context,
        BoolExpr summaryOld,
        BoolExpr summaryNew,
        Map<String, Expr<?>> variables,
        HashMap<Expr<?>, Integer> uFunctionsOld,
        HashMap<String, Pair<FuncDecl<?>, HashSet<Expr<?>>>> functionsInstances,
        HashSet<Expr<?>> procCalls,
        ArrayList<String> functions
    ) throws IOException {
        //********************make the solver***************************************/
        Set<String> uFunctionNames = new HashSet<>();
        uFunctionsOld.keySet().forEach(expr -> uFunctionNames.add(expr.getFuncDecl().getName().toString()));
        ArrayList<String> uFunctionNamesOrdered = getOrderedFunctionsList(getRestrictedInstances(functionsInstances, uFunctionNames), true);
        //**************************variables*********************************/
        List<Expr<?>> varList = new ArrayList<>();
        List<Expr<?>> substList = new ArrayList<>();
        for (String uFunctionName : uFunctionNamesOrdered) {
            HashSet<Expr<?>> uFunctionInstances = functionsInstances.get(uFunctionName).getValue(); //We need to get all the functions that will be substituted
            int j = 0;
            for (Expr<?> e : uFunctionInstances) {
                varList.add(e); // the original function symbol
                substList.add(context.mkConst(uFunctionName + "_" + j, e.getSort()));//for substitution
                j++;
            }
            j = 0;
            for (Expr<?> e : procCalls) {//we add all the uninterpreted functions related to interprocedural calls
                varList.add(e);
                substList.add(context.mkConst(uFunctionName + "_" + j, e.getSort()));
            }
        }
        //System.out.println("All UNFunc : " + substList);
        Expr<?>[] apps = varList.toArray(new Expr[0]);
        Expr<?>[] sub = substList.toArray(new Expr[0]);
        BoolExpr equiv = context.mkEq(summaryOld, summaryNew);
        Expr<?> quantifiableExp = equiv.substitute(apps, sub);
        //***********add variables***********/
        substList.addAll(variables.values());//add variables
        Expr<?>[] vars = substList.toArray(new Expr[0]);
        //System.out.println("All Vars : " + Arrays.toString(vars));
        //***********************************************************/
        for (String uFunctionName : uFunctionNamesOrdered) {
            List<Expr<?>> quantiferslist = new ArrayList<>();
            for (Expr<?> var : vars) {//we add the variables
                if (!var.toString().contains(uFunctionName)) {
                    quantiferslist.add(var);
                }
            }
            //System.out.println("All Vars except for the target: " + Arrays.toString(quantiferslist.toArray(new Expr[quantiferslist.size()])));
            //***********add variables***********/
            BoolExpr forAll = context.mkForall(quantiferslist.toArray(new Expr[0]), (BoolExpr) quantifiableExp, 1, null, null, null, null);
            //*********************************************/
            //run from command
            List<Expr<?>> targetList = new ArrayList<>();
            HashSet<Expr<?>> instances2 = functionsInstances.get(uFunctionName).getValue(); //We need to get all the functions that will be substituted
            int j = 0;
            for (Expr<?> e : instances2) {
                targetList.add(context.mkConst(uFunctionName + "_" + j, e.getSort()));//for substitution
                j++;
            }
            //System.out.println("after:" +forAll.toString());
            FileWriter fw = new FileWriter(this.path + "/H1Checking.smt2");
            BufferedWriter bw = new BufferedWriter(fw);
            //(declare-const UF_bess_1_0 Real) (assert + forall + ) (check-sat)
            //***************Get Type************/
            FuncDecl<?> uFunctionDeclaration = functionsInstances.get(uFunctionName).getKey();//usage?
            String uFunctionType = uFunctionDeclaration.getRange().getName().toString();
            //*************************************/
            for (Expr<?> expr : targetList) {
                bw.write("(declare-const " + expr + " " + uFunctionType + " )" + '\n');//TODO change with the type.
            }
            bw.write("(assert " + "\n");
            bw.write(forAll.toString());
            bw.write(") " + "\n");//end of assert
            bw.write("(check-sat)");
            bw.close();
            fw.close();
            String mainCommand = ProjectPaths.z3 + " -smt2 " + this.path + "/H1Checking.smt2 -T:" + this.timeout / 1000;
            //if (debug) System.out.println(mainCommand);
            Process p = Runtime.getRuntime().exec(mainCommand);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String answer = in.readLine();
            //*********************************************/
            if (answer != null && answer.equals("sat")) {
                functions.add(uFunctionName);
            }
        }
    }

    /**
     * This function applies the second heuristic
     * It collects all the functions that appear only in one of the summaries
     * @param functions a list of uninterpreted functions
     */
    public void heuristicH2(
        HashMap<Expr<?>, Integer> uFunctionsOld,
        HashMap<Expr<?>, Integer> uFunctionsNew,
        HashMap<String, Pair<FuncDecl<?>, HashSet<Expr<?>>>> functionsInstances,
        ArrayList<String> functions
    ) {
        MapDifference<Expr<?>, Integer> diff = Maps.difference(uFunctionsOld, uFunctionsNew);
        //******************************/
        Set<Expr<?>> left = diff.entriesOnlyOnLeft().keySet();
        Set<String> leftNames = new HashSet<>();
        left.forEach((expr) -> leftNames.add(expr.getFuncDecl().getName().toString()));
        ArrayList<String> tempLeft = getOrderedFunctionsList(getRestrictedInstances(functionsInstances, leftNames), true);
        functions.addAll(tempLeft);
        //******************************/
        Set<Expr<?>> right = diff.entriesOnlyOnRight().keySet();
        Set<String> rightNames = new HashSet<>();
        right.forEach((expr) -> rightNames.add(expr.getFuncDecl().getName().toString()));
        ArrayList<String> tempRight = getOrderedFunctionsList(getRestrictedInstances(functionsInstances, rightNames), false);
        functions.addAll(tempRight);
        //******************************/
        Map<Expr<?>, MapDifference.ValueDifference<Integer>> occurrences = diff.entriesDiffering();
        Set<String> differing = new HashSet<>();
        occurrences.keySet().forEach((expr) -> differing.add(expr.getFuncDecl().getName().toString()));
        ArrayList<String> tempDiff = getOrderedFunctionsList(getRestrictedInstances(functionsInstances, differing), true);
        functions.addAll(tempDiff);
        //******************************/
    }

    /**
     * This function applies the third heuristic, will select a UF based on simplicity
     * @return the selected UF as a statement number where it is defined
     */
    private String heuristicH3(List<String> functionsWithoutDuplicates) {
        HashMap<String, Integer> H3 = new HashMap<>();
        for (String functionsWithoutDuplicate : functionsWithoutDuplicates) {
            Map<Integer, Pair<String, int[]>> statements = getStatementsFromFunction(functionsWithoutDuplicate);
            for (Integer line : statements.keySet()) {
                Pair<String, int[]> statementInfo = statements.get(line);
                //System.out.println(line + " : "+statementInfo.getValue()[0]);
                if (this.H31 && !this.H32) {
                    H3.put(Integer.toString(line), statementInfo.getValue()[0]);
                } else if (!this.H31 && this.H32) {
                    H3.put(Integer.toString(line), statementInfo.getValue()[1]);
                } else if (this.H31 && this.H32) { //both scores
                    H3.put(Integer.toString(line), statementInfo.getValue()[0] + statementInfo.getValue()[1]);
                }
            }
        }
        Map<String, Integer> h3Sorted = sortByValue(H3);
        Map.Entry<String, Integer> entry = h3Sorted.entrySet().iterator().next();
        return entry.getKey();
    }

    //**************************Helper functions *********************************/

    /**
     * This function creates the map from every uninterpreted function in the set, to its declaration and to all its applications
     * For example UF_val_1 --> Pair (define-fun UF_val Int Int) ; List {(UF_val_1 x); (UF_val_1 y)}
     */
    public Map<String, Pair<FuncDecl<?>, HashSet<Expr<?>>>> getRestrictedInstances(Map<String, Pair<FuncDecl<?>, HashSet<Expr<?>>>> instances, Set<String> set) {
        //TODO must be changed for the new z3 parser parsing strings instead
        Map<String, Pair<FuncDecl<?>, HashSet<Expr<?>>>> results = new HashMap<>();
        for (String s : set) {
            results.put(s, instances.get(s));
        }
        return results;
    }

    /**
     * This function fills the map object that describes the unintepreted functions inside an expression expr
     * @param expr a Z3 expression
     * @param functionsInstances a map from a function name to the actual function declaration and all its occurrences
     * @param uFunctions the list of occurrences of each expression
     * @param procCalls a list of function calls
     */
    public void createInstances(
        Expr<?> expr,
        Map<String, Pair<FuncDecl<?>, HashSet<Expr<?>>>> functionsInstances,
        Map<Expr<?>, Integer> uFunctions,
        HashSet<Expr<?>> procCalls
    ) {
        if (expr == null || !expr.isApp()) {
            return;
        }

        FuncDecl<?> func = expr.getFuncDecl();
        String funcName = func.getName().toString();
        Expr<?>[] args = expr.getArgs();
        for (Expr<?> arg : args) {
            this.createInstances(arg, functionsInstances, uFunctions, procCalls);
        }

        if (funcName.startsWith("UF_")) {
            if (functionsInstances.containsKey(funcName)) {
                functionsInstances.get(funcName).getValue().add(expr);
                uFunctions.merge(expr, 1, Integer::sum);
            } else {
                HashSet<Expr<?>> set = new HashSet<>();
                set.add(expr);
                functionsInstances.put(funcName, new MutablePair<>(func, set));
                uFunctions.put(expr, 1);
            }
        } else if (funcName.startsWith("AF_")) {
            procCalls.add(expr);
        }
    }

    /**
     * This is a helper function to obtain every statement mapped to a given uninterpreted function
     * The output is in the form Map : Line number --> Pair (variable, [Number of control statements, Number of non linear arithmetic, Is mixed ?]
     * 1 if mixed, 0 otherwise
     */
    public Map<Integer, Pair<String, int[]>> getStatementsFromFunction(String func) {
        Map<Integer, Pair<String, int[]>> statements = new HashMap<>();

        String[] temp = getInfoFromUFunc(func); //Example : func = UF_val_1
        Integer block_id = Integer.parseInt(temp[1]);//Here we obtain 1
        String name = temp[0];   //Here we obtain val

        //Here we get all the outputs for the lines contained in block 1 for program1 and program2
        Map<Integer, Pair<String, int[]>> blockInfoV1 = this.statementInfoPerBlockOld.get(block_id);
        for (Integer line : blockInfoV1.keySet()) {
            //We have a pair (val, [number of control statements, number of non-linear arithmetic, is mixed ?]
            //Here you have access to the information you want with statementInfo.getValue();
            //We check that there is an output for this line (if the line is a declaration statement)
            Pair<String, int[]> statementInfo = blockInfoV1.get(line);
            if (statementInfo != null) {
                String val = statementInfo.getKey();
                if (val != null && val.equals(name)) {
                    statements.put(line, new MutablePair<>(val, statementInfo.getValue()));
                }
            }
        }
        Map<Integer, Pair<String, int[]>> blockInfoV2 = this.statementInfoPerBlockNew.get(block_id);
        for (Integer line : blockInfoV2.keySet()) {
            Pair<String, int[]> statementInfo = blockInfoV2.get(line);
            if (statementInfo != null) {
                String val = statementInfo.getKey();
                if (val != null && val.equals(name)) {
                    statements.put(line, new MutablePair<>(val, statementInfo.getValue()));
                }
            }
        }
        return statements;
    }

    /**
     * This is the function where you expand the lines related to a given uninterpreted function (add the lines to the change set)
     */
    public void expandFunction(String statement, ArrayList<Integer> changes) {
        changes.add(Integer.parseInt(statement));
        Collections.sort(changes);
    }

    /**
     * This is the function to obtain the block number and the variable name from the uninterpreted function
     */
    public String[] getInfoFromUFunc(String func) {
        int index = func.lastIndexOf("_");
        if (func.endsWith("*")) {
            func = func.substring(0, func.length() - 1);
        }
        String[] temp = {func.substring(3, index), func.substring(index + 1)};
        return temp;
    }

    /**
     * This function sorts the uninterpreted functions based on the order of appearance
     * The priority is given to the outermost functions, which are likely to not be the inputs of any other function
     */
    public ArrayList<String> getOrderedFunctionsList(Map<String, Pair<FuncDecl<?>, HashSet<Expr<?>>>> instances, boolean old) {
        ArrayList<String> functions = getInputsOfSomeFunctionsList(instances, old);
        ArrayList<String> inputsOfNoFunctions = getInputsOfNoFunctionsList(functions, instances.keySet(), old);
        inputsOfNoFunctions.addAll(functions);
        return inputsOfNoFunctions;
    }

    /**
     * This function retrieves every function which is the input of some other uninterpreted function
     */
    public ArrayList<String> getInputsOfSomeFunctionsList(Map<String, Pair<FuncDecl<?>, HashSet<Expr<?>>>> instances, boolean old) {
        HashSet<String> inputsOfSomeFunction = new HashSet<>();
        for (String var : instances.keySet()) {
            HashSet<Expr<?>> application = instances.get(var).getValue();
            for (Expr<?> expr : application) {
                Expr<?>[] args = expr.getArgs();
                for (Expr<?> arg : args) {
                    String name = arg.getFuncDecl().getName().toString();
                    if (name.startsWith("UF_")) {
                        inputsOfSomeFunction.add(name);
                    }
                }
            }
        }
        return sortFunctionsByOrder(inputsOfSomeFunction, old);
    }

    /**
     * This function retrieves every function which is the input of no other function
     * @param old whether we are considering the old or the new program
     */
    public ArrayList<String> getInputsOfNoFunctionsList(ArrayList<String> inputsOfSomeFunctions, Set<String> allFunctions, boolean old) {
        HashSet<String> inputsOfNoFunctions = new HashSet<>(allFunctions);
        inputsOfNoFunctions.removeAll(inputsOfSomeFunctions);
        return sortFunctionsByOrder(inputsOfNoFunctions, old);
    }

    /**
     * This function sorts the uninterpreted functions by reverse order of occurrence in the program
     * @param functions the functions to sort
     * @param old       if true it's the old program, otherwise the new program
     * @return the sorted list of the functions
     */
    public ArrayList<String> sortFunctionsByOrder(Set<String> functions, boolean old) {
        ArrayList<LinkedHashMap<String, Pair<Boolean, HashSet<String>>>> blocks = (old) ? this.blockResultsOld : this.blockResultsNew;
        ArrayList<String> orderedList = new ArrayList<>();
        for (int i = blocks.size(); i > 0; i--) {
            Map<String, Pair<Boolean, HashSet<String>>> info = blocks.get(i - 1);
            ArrayList<String> outputs = new ArrayList<>();
            for (String var : info.keySet()) {
                outputs.add("UF_" + var + "_" + i);
            }
            for (int j = outputs.size() - 1; j >= 0; j--) {
                String func = outputs.get(j);
                if (functions.contains(func)) {
                    orderedList.add(func);
                } else if (functions.contains(func + "*")) {
                    orderedList.add(func + "*");
                }
            }
        }
        return orderedList;
    }

    /**
     * This function sorts the uninterpreted functions by increasing complexity score
     * @param hm a map from a function to a complexity score
     * @return the sorted map
     */
    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) {
        List<Map.Entry<String, Integer>> list = new LinkedList<>(hm.entrySet());
        list.sort(Map.Entry.comparingByValue());
        HashMap<String, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
}
