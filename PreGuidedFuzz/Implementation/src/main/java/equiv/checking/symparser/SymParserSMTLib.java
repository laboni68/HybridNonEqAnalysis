//MIT-LICENSE
//Copyright (c) 2020-, Sahar Badihi, The University of British Columbia, and a number of other of contributors
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
//to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
//and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package equiv.checking.symparser;

import com.microsoft.z3.*;
import equiv.checking.Utils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;


public class SymParserSMTLib {
    protected final HashSet<String> mathFunctions;
    protected final Context context;

    protected String declarations;
    protected String functionsDefinitions;
    protected HashMap<String,FuncDecl<?>> variablesDeclaration;
    protected SymLexer reader;
    protected String spfOutput;
    protected final Map<String, Expr<?>> variables;

    protected boolean noUFunctions;

    /**
     * Add parsing arrays in parseElem
     * Consider <- in the parse formula as well ? it's basically , array[i] becomes i, check how to do mutation ??
     * We have that val[i] is mapped to (select val i)
     * We have that val[i] <- a is mapped to (store val i a) which returns a new array with a at the pos i
     * If it is an array than [i] could be an index instead of just the addtional data added to every variable
     */
    public SymParserSMTLib(Context context) {
        this.context = context;
        this.declarations = "";
        this.variables = new HashMap<>();
        this.mathFunctions = Utils.mathFunctions;
        this.noUFunctions = true;
        this.variablesDeclaration = new HashMap<>();
        initFunctionDefinitions();
    }

    public void initFunctionDefinitions(){
        functionsDefinitions = "(define-fun exp ((x Real)) Real (^ 2.718281828459045 x)) \n" +
                "(define-fun sqrt ((x Real)) Real (^ x 0.5)) \n" +
                "(declare-fun log (Real) Real) \n";
        //"(assert (forall ((x Real)) (= (log (exp x)) x))) \n "+
        //"(assert (forall ((x Real)) (= (exp (log x)) x))) \n";
        /*functionsDefinitions = "(define-fun exp ((x Real)) Real (^ 2.718281828459045 x)) \n" +
                "(define-fun sqrt ((x Real)) Real (^ 0.5 x)) \n";*/
    }

    public boolean noUFunctions() {
        return this.noUFunctions;
    }

    public String functionsDefinitions(){
        return functionsDefinitions;
    }
    public String declarations(){
        return this.declarations;
    }

    public Map<String, Expr<?>> varNames() {
        return this.variables;
    }

    //Grammar
    //F :=  E (=|<=|>=|<|>) E
    //var := [a-z][A_Z]*
    //E := E + T | E - T | T
    //T := T * F | T / F | F
    //F := var | U | (E) | -F
    //U /:= var(args)
    //args := empty | F(,F)*

    public Context context() {
        return this.context;
    }

    public HashMap<String,FuncDecl<?>> varDecl(){
        return this.variablesDeclaration;
    }

    public String parseConstraint(String spf) throws Exception {
        this.spfOutput = spf;
        if (this.spfOutput == null) {
            return null;
        }
        this.reader = new SymLexer(spf);
        this.reader.nextChar();
        //each line is a formula
        return parseFormula();
    }

    public String parseFormula() throws Exception {
        Pair<String, String> left = parseArithm();
        if (this.reader.eat('=')) {
            return "= " + left.getValue() + " " + parseArithm().getValue() + "";
        }
        if (this.reader.eat('!')) {
            if (this.reader.eat('=')) {
                return "not ( = " + left.getValue() + " " + parseArithm().getValue() + ")";
            }
            throw new Exception("Syntax error");
        }
        if (this.reader.eat('<')) {
            if (this.reader.eat('=')) {
                return "<= " + left.getValue() + " " + parseArithm().getValue() + "";
            }
            return "< " + left.getValue() + " " + parseArithm().getValue() + "";
        }
        if (this.reader.eat('>')) {
            if (this.reader.eat('=')) {
                return ">= " + left.getValue() + " " + parseArithm().getValue() + "";
            }
            return "> " + left.getValue() + " " + parseArithm().getValue() + "";
        }
        throw new Exception("Syntax error");
    }

    public Pair<String,String> parseArithm() throws Exception {
        Pair<String,String> left = parseTerm();
        if (reader.eat('+')) {
            left = new MutablePair<>(left.getKey(),"+ "+left.getValue()+" "+parseArithm().getValue()+"");
        } else if (reader.eat('-')) {
            left = new MutablePair<>(left.getKey(),"- "+left.getValue()+" "+parseArithm().getValue()+"");
        }
        return left;
    }

    public Pair<String,String> parseTerm() throws Exception {
        Pair<String,String> left = parseFactor();
        String integer = "Int";//to check
        if (reader.eat('*')) {
            return new MutablePair<>(left.getKey(),"* "+left.getValue()+" "+parseTerm().getValue()+"");
        }
        if (reader.eat('/')) {
            Pair<String,String> right = parseTerm();
            String div = "/";
            if(left.getKey().equals(integer) && right.getKey().equals(left.getKey())) { //int division
                div = "div";
                return new MutablePair<>(integer,div+" "+left.getValue()+" "+right.getValue()+"");
            }
            return new MutablePair<>("Real",div+" "+left.getValue()+" "+right.getValue()+"");
        }
        if (reader.eat('%')) {
            //does not accept arithmetic expressions, only integer expressions
            return new MutablePair<>("Int","mod "+left.getValue()+" "+parseTerm().getValue()+"");
        }
        if(reader.eat('^')){
            return new MutablePair<>(left.getKey(),"^ "+left.getValue()+" "+parseTerm().getValue()+"");

        }
        return left;
        //else throw new Exception("Syntax error");
    }

    public Pair<String, String> parseFactor() throws Exception {
        if (reader.eat('-'))
            return parseFactor();
        String e = "";
        int start = reader.index;
        if (reader.eat('(')) {
            Pair<String, String> info = parseArithm();
            e = "( " + info.getValue() + " )";
            reader.eat(')');
            return new MutablePair<>(info.getKey(), e);
        }
        //check for the closing brace maybe not the best way to do it
        while (reader.current != ' ' && reader.current != -1 && reader.current != '(' && reader.current != ',' && reader.current != ')')
            reader.nextChar();
        String var = spfOutput.substring(start, reader.index);
        //System.out.println("Variable : "+var);
        return parseElem(var);
    }

    public Pair<String, String> parseElem(String var) throws Exception {
        if (var.startsWith("CONST")) {
            //maybe make real or make double instead !!

            String num = var.split("CONST_")[1];
            if (num.contains("E")) {// num 2.49958057E13
                String base = num.split("E")[0]; //base = 2.49958057
                String num2 = num.split("E")[1]; // num2 = 13
                num = "(* " + base + " (^ 10 " + num2 + "))";
            }
            if (var.contains("REAL")) {
                return new MutablePair<>("Real", num);
            } else {
                return new MutablePair<>("Int", num);
            }
        } else if (var.startsWith("UF_") || var.startsWith("AF_")) {
            //function
            return parseFunc(var);
        } else if (this.mathFunctions.contains(var)) {
            return parseMathFunc(var);
        } else {
            //  (currentTime_1_SYMINT[-100]
            return parseVar(var);
        }
    }

    protected Pair<String, String> parseMathFunc(String funcName) throws Exception {
        //TODO
        this.reader.eat('(');
        ArrayList<Pair<String, String>> arguments = parseArgs();
        this.reader.eat(')');
        String formula = " ( ";
        if (funcName.equals("pow")) {
            formula += "^ ";
        } else {
            formula += funcName + " ";
        }
        if (arguments != null && arguments.size() > 0) {
            for (Pair<String, String> s : arguments) { //here I might need to add (to_real ?), to check
                if (s.getKey().equals("Int")) {
                    formula += "(to_real " + s.getValue() + ")";
                } else {
                    formula += s.getValue() + " ";
                }
            }
        }
        formula += ")";
        return new MutablePair<>("Real", formula);
    }

    protected Pair<String, String> parseFunc(String funcName) throws Exception {
        //I could store in the list all the inputs from here using arguments and just take the uf_ thing
        String[] name = funcName.split("_SYM");
        String func = name[0];
        String formula = "";
        //here I need to check if the number of arguments of the list matches
        String sort = "(";
        if (name.length == 2) {
            if (func.startsWith("UF")) {
                noUFunctions = false;
            }
            ArrayList<Pair<String, String>> arguments;
            reader.eat('(');
            if (reader.eat(')')) {
                arguments = new ArrayList<>();
            } else {
                arguments = parseArgs();
                for (Pair<String, String> s : arguments) {
                    formula += s.getValue() + " ";
                    sort += s.getKey() + " ";
                }
                //formula = formula.substring(0,formula.length() - 1);
                reader.eat(')');
            }
            sort += ")";
            String ret = "";
            Sort retS = null;
            if (name[1].equals("INT")) {
                ret = "Int";
                retS = this.context.mkIntSort();
            } else {
                ret = "Real";
                retS = this.context.mkRealSort();
            }
            FuncDecl<?> f = null, funcDecl = this.variablesDeclaration.get(func);
            if (funcDecl != null) {
                int size = funcDecl.getDomainSize();
                if (size == arguments.size()) {
                    f = funcDecl;
                } else {
                    func += "*";
                }
            }
            if (arguments.size() > 0) {
                formula = " ( " + func + " " + formula + ")";
            } else {
                formula = func + " " + formula;
            }

            if (f == null) {
                Sort[] typeArgs = new Sort[arguments.size()];
                for (int i = 0; i < arguments.size(); i++) {
                    String type = arguments.get(i).getKey();
                    typeArgs[i] = (type.equals("Int"))
                        ? this.context.mkIntSort()
                        : this.context.mkRealSort();
                }
                f = this.context.mkFuncDecl(func, typeArgs, retS);
                this.declarations += "(declare-fun " + func + " " + sort + " " + ret + ")\n";
                this.variablesDeclaration.put(func, f);

            }
            return new MutablePair<>(ret, formula);
        }
        return null;
    }

    public ArrayList<Pair<String, String>> parseArgs() throws Exception {
        ArrayList<Pair<String, String>> args = new ArrayList<>();
        args.add(parseFactor());
        if (reader.eat(',')) {
            args.addAll(parseArgs());
        }
        return args;
    }

    public Pair<String, String> parseVar(String var) {
        String regex = "_[0-9]+_SYM";
        if (!var.contains("SYM")) {
            return parseCast(var);
        }
        String[] name = var.split(regex);
        return parseVariable(name);
    }

    public Pair<String, String> parseCast(String string) {
        String var = string.split("\\[")[0];
        if (var.contains("REAL")) {
            //To be checked
            if (!this.variables.containsKey(var)) {
                this.declarations += "(declare-fun " + var + " () Real)\n";
                Expr<RealSort> e = context.mkRealConst(var);
                this.variables.put(var, e);
                this.variablesDeclaration.put(var, e.getFuncDecl());
            }
            return new MutablePair<>("Real", var);
        }
        if (!this.variables.containsKey(var)) {
            this.declarations += "(declare-fun " + var + " () Int)\n";
            Expr<IntSort> e = this.context.mkIntConst(var);
            this.variables.put(var, e);
            this.variablesDeclaration.put(var, e.getFuncDecl());
        }
        return new MutablePair<>("Int", var);
    }

    public Pair<String, String> parseVariable(String[] name) {
        String ret = "Int";
        if (name[1].contains("REAL")) {
            if (!this.variables.containsKey(name[0])) {
                this.declarations += "(declare-fun " + name[0] + " () Real)\n";
                Expr<RealSort> e = this.context.mkRealConst(name[0]);
                this.variables.put(name[0], e);
                this.variablesDeclaration.put(e.toString(), e.getFuncDecl());
            }
            ret = "Real";
        } else {
            if (!this.variables.containsKey(name[0])) {
                this.declarations += "(declare-fun " + name[0] + " () Int)\n";
                Expr<IntSort> e = context.mkIntConst(name[0]);
                this.variables.put(name[0], e);
                this.variablesDeclaration.put(e.toString(), e.getFuncDecl());
            }
        }
        return new MutablePair<>(ret, name[0]);
    }

    public void createDecl(String name, String type) {
        if (!this.variables.containsKey(name)) {
            if (type.equals("int")) {
                Expr<IntSort> e = this.context.mkIntConst(name);
                this.variables.put(name, e);
                this.variablesDeclaration.put(e.toString(), e.getFuncDecl());
            } else {
                Expr<RealSort> e = this.context.mkRealConst(name);
                this.variables.put(name, e);
                this.variablesDeclaration.put(e.toString(), e.getFuncDecl());
            }
        }
    }
}
