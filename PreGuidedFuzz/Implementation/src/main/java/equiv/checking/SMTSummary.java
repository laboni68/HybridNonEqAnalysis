//MIT-LICENSE
//Copyright (c) 2020-, Sahar Badihi, The University of British Columbia, and a number of other of contributors
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
//to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
//and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package equiv.checking;

import com.microsoft.z3.*;
import equiv.checking.symparser.SymParserSMTLib;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;

/**
 * This class collects all the information from constraint solving
 **/
public class SMTSummary {
    public BoolExpr summaryOld;
    public BoolExpr summaryNew;
    public Status status;

    public Map<String, Expr<?>> variables;

    public Context context;
    public Solver solver;

    public SymParserSMTLib parser;

    public long z3time;
    public boolean noUFunctions;

    public String toWrite;
    public String reasonUnknown;
    public String declarations;
    public String firstSummary = "";
    public String secondSummary = "";

    protected final String path;
    protected final String oldFileName;
    protected final String newFileName;
    protected final int timeout;

    protected final File terminalFileOld;
    protected final File terminalFileNew;
    protected final File jpfOutputFileOld;
    protected final File jpfOutputFileNew;
    protected final File jpfErrorFileOld;
    protected final File jpfErrorFileNew;
    protected final File toSolveFile;

    protected String terminalInput = "";

    protected boolean hasError = true;
    protected boolean hasReachedEnd = false;
    protected boolean isBoundEnough = true;

    public SMTSummary(String path, String oldFileName, String newFileName, int timeout) {
        this.path = path;
        this.oldFileName = oldFileName;
        this.newFileName = newFileName;
        this.timeout = timeout;

        this.terminalFileOld = new File(this.path + "/" + this.oldFileName + "Terminal.txt");
        this.terminalFileNew = new File(this.path + "/" + this.newFileName + "Terminal.txt");
        this.jpfOutputFileOld = new File(this.path + "/" + this.oldFileName + "JPFOutput.txt");
        this.jpfOutputFileNew = new File(this.path + "/" + this.newFileName + "JPFOutput.txt");
        this.jpfErrorFileOld = new File(this.path + "/" + this.oldFileName + "JPFError.txt");
        this.jpfErrorFileNew = new File(this.path + "/" + this.newFileName + "JPFError.txt");
        this.toSolveFile = new File(this.path + "/" + this.newFileName + "ToSolve.txt");

        this.toWrite = "";
    }

    /**
     * This function checks the equivalence of two programs using the program summaries fields
     */
    public void checkEquivalence() throws IOException {
        this.context = new Context();
        Tactic qfnra = this.context.mkTactic("qfnra-nlsat");
        //Tactic simplifyTactic = context.mkTactic("ctx-solver-simplify");
        Tactic simplifyTactic = this.context.mkTactic("simplify");
        Tactic smtTactic = this.context.mkTactic("smt");
        Tactic aig = this.context.mkTactic("aig");
        Tactic solveeqs = this.context.mkTactic("solve-eqs");
        Tactic or = this.context.andThen(smtTactic, this.context.parOr(simplifyTactic, solveeqs, aig, qfnra));

        this.parser = new SymParserSMTLib(this.context);

        this.terminalInput += "The summary for the old method (in z3 stmt2 format)\n";
        this.summaryOld = this.createSMTSummaryProgram(this.jpfOutputFileOld, this.parser);

        if (!this.hasError) { //there was no error while running JPF symbc
            Files.write(this.terminalFileOld.toPath(), this.terminalInput.getBytes());

            this.terminalInput = "The summary for the new method\n";
            this.summaryNew = this.createSMTSummaryProgram(this.jpfOutputFileNew, this.parser);
            Files.write(this.terminalFileNew.toPath(), this.terminalInput.getBytes());
        } else {
            this.summaryNew = null;
        }

        this.noUFunctions = this.parser.noUFunctions();

        if ((this.summaryNew == null || this.summaryOld == null) && this.hasError) {
            this.status = Status.UNKNOWN;
            if (!this.hasReachedEnd) {
                String error = "";
                if (this.summaryOld == null) {
                    error = this.parseErrorFile(this.jpfErrorFileOld);
                } else {
                    error = this.parseErrorFile(this.jpfErrorFileNew);
                }
                this.toWrite += "INFO: There was an error while running JPF";
                this.reasonUnknown = "Error while running JPF-symbc";
                if (!error.isEmpty()) {
                    this.reasonUnknown += " : " + error;
                    this.reasonUnknown += " (refer to JPFError.txt)";
                    if (error.contains("OutOfMemoryError")) {
                        this.reasonUnknown += "\n Try increasing your heap memory";
                    } else if (error.contains("Z3")) {
                        this.reasonUnknown += "\n Maybe try with a different solver";
                    }
                }
            } else {
                if (!this.isBoundEnough) {
                    this.toWrite += "INFO: There was an error while running JPF";
                    this.reasonUnknown = "[WARNING] Your bound is either too low to execute the program or you have an infinite loop";
                } else {
                    this.toWrite += "INFO: There was an error while parsing the JPF output to Z3 formulas ...";
                    this.reasonUnknown = "Unsupported operator by Z3 parser";
                }
            }
            return;
        } else if ((this.summaryNew == this.summaryOld && this.summaryNew == null) && this.hasError == false) {
            this.toWrite += "INFO: The return statements are not impacted or the summaries are empty, thus the programs are equivalent";
            this.status = Status.UNSATISFIABLE;
            return;
        }

        this.toWrite += "-------------------The Z3 formula for the old method (z3 smt format) -------------------------\n";
        this.toWrite += this.summaryOld.toString();
        this.toWrite += "\n-----------------------------------------------------------------------------------------------\n";
        this.toWrite += "\n-------------------The Z3 formula for the new method ------------------------------------------\n";
        this.toWrite += this.summaryNew.toString();
        this.toWrite += "\n-----------------------------------------------------------------------------------------------\n";
        //////////////
        this.variables = this.parser.varNames();
        //here maybe add everything to the solver, not just the final thing
        final BoolExpr t = (this.context.mkNot(this.context.mkEq(this.summaryOld, this.summaryNew)));
        this.toWrite += "\n-------------------The final Z3 formula for constraint solving -------------------------\n";
        this.toWrite += this.parser.declarations();
        this.toWrite += t.toString();
        this.toWrite += "\n-----------------------------------------------------------------------------------------------\n";
        this.runZ3FromTerminal(this.parser);
    }

    /**
     * This function runs Z3 constraint solver from the terminal
     * @param parser a Z3 parser object
     */
    public void runZ3FromTerminal(SymParserSMTLib parser) throws IOException {
        this.declarations = parser.declarations() + parser.functionsDefinitions();
        this.declarations = this.declarations.replaceAll(
            "\\(declare-fun Ret \\(\\) (\\w+)\\)",
            "(declare-fun Ret () $1)\n(declare-fun Ret_1 () $1)\n(declare-fun Ret_2 () $1)"
        );
        String oldSummary = this.firstSummary.replace(" Ret ", " Ret_1 ");
        String newSummary = this.secondSummary.replace(" Ret ", " Ret_2 ");
        String toSolve = this.declarations
            + "(assert (" + oldSummary + "))\n"
            + "(assert (" + newSummary + "))\n"
            + "(assert (not (= Ret_1 Ret_2)))\n"
            + "(check-sat-using (then smt (par-or simplify aig solve-eqs qfnra-nlsat)))\n"
            + "(get-info:reason-unknown)\n"
            + "(get-model)";
        Files.write(this.toSolveFile.toPath(), toSolve.getBytes());

        String mainCommand = ProjectPaths.z3 + " -smt2 " + this.toSolveFile.getPath() + " -T:" +this.timeout / 1000;
        long start = System.nanoTime();
        Process z3Process = Runtime.getRuntime().exec(mainCommand);
        long end = System.nanoTime();
        this.z3time = end - start;

        BufferedReader in = new BufferedReader(new InputStreamReader(z3Process.getInputStream()));
        BufferedReader err = new BufferedReader(new InputStreamReader(z3Process.getErrorStream()));
        String line = null;
        String answer = in.readLine();
        if (answer == null) {
            this.status = Status.UNKNOWN;
            String error = err.readLine();
            this.reasonUnknown = "Error while running z3" + (error != null ? " : " + error : "");
        } else if (answer.equals("timeout")) {
            //maybe read err line ?
            this.status = Status.UNKNOWN;
            this.reasonUnknown = "timeout";
        } else {
            String reason = in.readLine();
            String model = "";
            while ((line = in.readLine()) != null) {
                model += line + "\n";
            }
            switch (answer) {
                case "sat":
                    this.status = Status.SATISFIABLE;
                    this.toWrite = "\n\n----------------------------------------------------Model (the counterexample in z3 smt2 format): ---------------------------------\n" + model
                        + "\n-----------------------------------------------------------------------------------------------\n" + this.toWrite;
                    break;
                case "unsat":
                    this.status = Status.UNSATISFIABLE;
                    break;
                case "unknown":
                    this.status = Status.UNKNOWN;
                    if (reason != null) {
                        reason = reason.replace(":reason-unknown ", "");
                    }
                    this.reasonUnknown = reason;
                    break;
            }
        }
        in.close();
        err.close();
    }

    /**
     * This functions creates a Z3 input from JPF output
     * @param parser a Z3 parser that works on strings
     */
    public BoolExpr createSMTSummaryProgram(File jpfOutputFile, SymParserSMTLib parser) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(jpfOutputFile));
        String st;
        Context context = parser.context();
        this.hasReachedEnd = false;
        this.isBoundEnough = true;
        try {
            String previousSum = null, TotalSum = null;
            while ((st = br.readLine()) != null) {
                if (st.contains("Method Summaries")) {
                    if (this.hasError) {
                        this.isBoundEnough = false;
                    }
                    this.hasReachedEnd = true;
                    break;
                }
                if (st.isEmpty())
                    continue;
                if (st.contains("Summary")) {
                    this.hasError = false;
                    st = br.readLine();
                    String prevPCs = null, currentPC = null, pathSummary = null;
                    if (st != null) {
                        if (st.startsWith("PC is")) {
                            st = br.readLine();
                        }
                        if (st != null) {
                            while (st != null && !st.contains("Ret_0_SYM")) {
                                String constraint = obtainConstraint(st);
                                //parsedConstraints.add(constraint);
                                if (constraint != null) {
                                    currentPC = parser.parseConstraint(constraint);
                                    if (prevPCs != null) {
                                        currentPC = "and ( " + prevPCs + ") ( " + currentPC + " )";
                                    }
                                    prevPCs = currentPC;
                                }
                                st = br.readLine();
                            } // I am return statement
                            if (st != null) {
                                String returnConstraint = obtainConstraint(st);
                                if (returnConstraint != null) {
                                    String ret = parser.parseConstraint(returnConstraint);
                                    if (currentPC != null) {
                                        pathSummary = "and ( " + currentPC + " ) ( " + ret + " )";
                                    } else {
                                        pathSummary = ret;
                                    }
                                } else {
                                    pathSummary = null;
                                }
                            }
                        }
                    }// we are done with the path summary of this path => pathSystem.out.println(index + pathSummary.toString());
                    if (previousSum != null && pathSummary != null) {
                        TotalSum = "or ( " + previousSum + ") ( " + pathSummary + " )";
                        previousSum = TotalSum;
                    } else if (previousSum == null && pathSummary != null) {
                        previousSum = pathSummary;
                        TotalSum = previousSum;
                    }
                } //we update the total summary with the summary of this path
            }//we reach end of the file

            if (this.hasError || !this.hasReachedEnd) {
                this.hasError = true;
                return null;
            }
            if (this.firstSummary.isEmpty()) {
                this.firstSummary = TotalSum;
            } else {
                this.secondSummary = TotalSum;
            }
            if (TotalSum == null)
                return null;
            TotalSum = parser.functionsDefinitions() + "(assert ( " + TotalSum + " ))";
            Object[] func = parser.varDecl().values().toArray();
            Symbol[] symbols = new Symbol[func.length];
            FuncDecl<?>[] functions = new FuncDecl[func.length];
            for (int i = 0; i < functions.length; i++) {
                symbols[i] = ((FuncDecl<?>) func[i]).getName();
                functions[i] = (FuncDecl<?>) func[i];
            }
            this.terminalInput += TotalSum + "\n";

            BoolExpr summary = context.parseSMTLIB2String(TotalSum, null, null, symbols, functions)[0];
            return summary;
        } catch (Exception e) {
            this.hasError = true;
            throw new RuntimeException(e);
        }
    }

    /**
     * This functions returns eventual errors/exceptions from running JPF-symbc
     */
    public String parseErrorFile(File jpfErrorFile) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(jpfErrorFile));
            String st;
            while ((st = br.readLine()) != null) {
                if (st.startsWith("java.") && (st.contains("Error") || st.contains("Exception"))) ;
                return st;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    /**
     * This is a helper method to parse a JPF constraint
     * @param st a JPF constraint as a string
     */
    public String obtainConstraint(String st){
        String[] split=st.split(":")[1].split("&&");
        return split[0];
    }

    public String obtainReturn(String st) {
        return st.split("Return is: ")[1];
    }
}
