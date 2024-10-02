//MIT-LICENSE
//Copyright (c) 2020-, Sahar Badihi, The University of British Columbia, and a number of other of contributors
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
//to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
//and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package IMPs;

import br.usp.each.saeg.asm.defuse.*;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithBody;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import equiv.checking.CommonBlockExtractor;
import equiv.checking.DefUseExtractor;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ProgramSlicer {
    /** This class implements program slicing **/
    public ArrayList<Integer> impactedStatements; //the initial list of changes

    public ProgramSlicer(ArrayList<Integer> changes){
        this.impactedStatements = new ArrayList<>(changes);
    }

    public ArrayList<Integer> getImpactedStatements() {
        return impactedStatements;
    }

    /**
     * This function extract the impacted statements inside a program by forward and backward slicing
     * @param prog the program
     * @param method the method containing the changes
     */
    public void impactedStatements(String prog, MethodNode method) throws FileNotFoundException, AnalyzerException {
        ClassOrInterfaceDeclaration c = (ClassOrInterfaceDeclaration) StaticJavaParser.parse(new File(prog)).getType(0);
        MethodDeclaration root = c.getMethods().get(0);
        DefUseInterpreter interpreter = new DefUseInterpreter();
        FlowAnalyzer<Value> flowAnalyzer = new FlowAnalyzer<>(interpreter);
        DefUseAnalyzer analyzer = new DefUseAnalyzer(flowAnalyzer, interpreter);
        analyzer.analyze("package/ClassName", method);
        Variable[] variables = analyzer.getVariables();
        HashMap<Integer, Integer> lineInst = DefUseExtractor.instructionToLine(method);
        DefUseChain[] chains = new DepthFirstDefUseChainSearch().search(
            analyzer.getDefUseFrames(),
            analyzer.getVariables(),
            flowAnalyzer.getSuccessors(),
            flowAnalyzer.getPredecessors()
        );
        int location = root.getBegin().get().line;
        ArrayList<Integer> previousImpacted = new ArrayList<>();
        this.forwardSlicing(chains, lineInst, variables, root, location, previousImpacted);
        this.backwardControlDependenceTransitive(root.getBody().get(), location);
        this.backwardDataDependence(chains, lineInst, variables);
        this.impactedStatements.remove(new Integer(location));
        Collections.sort(this.impactedStatements);
    }

    /**
     * This function performs forward slicing (control and data dependence)
     * @param chains an array of def-use relationships
     * @param lineInst a mapping from bytecode instruction to line number
     * @param variables the list of variables used in the method
     * @param root the method to analyze
     * @param location the current line number
     * @param previousImpacted the list of impacted statements
     */
    public void forwardSlicing(DefUseChain[] chains, HashMap<Integer, Integer> lineInst, Variable[] variables, MethodDeclaration root, int location, ArrayList<Integer> previousImpacted) {
        this.forwardControlDependence(root.getBody().get(), location);
        this.forwardDataDependence(chains, lineInst, variables);
        if (previousImpacted.size() != this.impactedStatements.size()) {
            previousImpacted = new ArrayList<>(this.impactedStatements);
            this.forwardSlicing(chains, lineInst, variables, root, location, previousImpacted);
        }
    }

    /**
     * This function extracts forward data dependencies (usages) in a method given the current list of impactedStatements
     * @param chains an array of def-use relations
     * @param lineInst a mapping from bytecode ins. to line number
     * @param variables the variables used in the current method
     */
    public void forwardDataDependence(DefUseChain[] chains, HashMap<Integer, Integer> lineInst, Variable[] variables) {
        for (DefUseChain chain : chains) {
            for (Integer key : lineInst.keySet()) {
                Integer value = lineInst.get(key);
                if (this.impactedStatements.contains(value)) {
                    if (key.equals(chain.def)) {
                        Integer use = lineInst.get(chain.use);
                        if (!this.impactedStatements.contains(use))
                            this.impactedStatements.add(use);
                    }
                }
            }
        }
    }

    /**
     * This function extracts backward data dependencies (definitions) in a method given the current list of impactedStatements
     * @param chains an array of def-use relations
     * @param lineInst a mapping from bytecode ins. to line number
     * @param variables the variables used in the current method
     */
    public void backwardDataDependence(DefUseChain[] chains, HashMap<Integer, Integer> lineInst, Variable[] variables) {
        for (DefUseChain chain : chains) {
            for (Integer key : lineInst.keySet()) {
                Integer value = lineInst.get(key);
                if (this.impactedStatements.contains(value)) {
                    if (key.equals(chain.use)) {
                        Integer def = lineInst.get(chain.def);
                        if (def != null && !this.impactedStatements.contains(def))
                            this.impactedStatements.add(def);
                    }
                }
            }
        }
    }

    /**
     * This method extract forward control dependences recursively
     * @param controlled the current statement
     * @param location the current line number
     */
    public void forwardControlDependence(Statement controlled, int location) {
        boolean impacted = this.impactedStatements.contains(location);
        if (controlled != null) {
            if (!(controlled instanceof BlockStmt))
                controlled = new BlockStmt(new NodeList<>(controlled));
            //We add the controlled statements
            for (Statement st : controlled.asBlockStmt().getStatements()) {
                int i = st.getBegin().get().line;
                if (!this.impactedStatements.contains(i)) {
                    if (impacted) {
                        this.impactedStatements.add(i);
                    }
                }
                //Check if it should be only true controlled or not
                //Recursively
                if (CommonBlockExtractor.isControlSmt(st)) {
                    Statement trueControlled = (st instanceof IfStmt) ? (st.asIfStmt()).getThenStmt() : ((NodeWithBody<?>) st).getBody();
                    this.forwardControlDependence(trueControlled, st.getBegin().get().line);
                    if (st instanceof IfStmt) {
                        IfStmt ist = st.asIfStmt();
                        if (ist.hasElseBranch())
                            this.forwardControlDependence(ist.getElseStmt().get(), st.getBegin().get().line);
                    }
                }
            }
        }
    }

    /**
     * This method extract backward control dependencies by going backward only once (not transitive)
     * @param st the current statement
     * @param location the current line number
     */
    private void backwardControlDependence(Statement st, int location) {
        boolean impacted = this.impactedStatements.contains(location);
        if (st != null) {
            ArrayList<Integer> additions = new ArrayList<>();
            if (!(st instanceof BlockStmt)) {
                st = new BlockStmt(new NodeList<>(st));
            }
            BlockStmt root = st.asBlockStmt();
            for (Statement statement : root.getStatements()) {
                int i = statement.getBegin().get().line;
                if (this.impactedStatements.contains(i) && !impacted) {
                    if (!additions.contains(location)) {
                        additions.add(location);
                    }
                }
                if (CommonBlockExtractor.isControlSmt(statement)) {
                    Statement trueControlled = (statement instanceof IfStmt) ? (statement.asIfStmt()).getThenStmt() : ((NodeWithBody<?>) statement).getBody();
                    this.backwardControlDependence(trueControlled, statement.getBegin().get().line);
                    if (statement instanceof IfStmt) {
                        IfStmt ist = statement.asIfStmt();
                        if (ist.hasElseBranch()) {
                            this.backwardControlDependence(ist.getElseStmt().get(), statement.getBegin().get().line);
                        }
                    }
                }
            }
            this.impactedStatements.addAll(additions);
        }
    }

    /**
     * This method extract backward control dependencies transitively
     */
    private void backwardControlDependenceTransitive(Statement st, int location) {
        boolean impacted = this.impactedStatements.contains(location);
        if (st != null) {
            ArrayList<Integer> additions = new ArrayList<>();
            if (!(st instanceof BlockStmt)) {
                st = new BlockStmt(new NodeList<>(st));
            }
            BlockStmt root = st.asBlockStmt();
            for (Statement statement : root.getStatements()) {
                int i = statement.getBegin().get().line;
                //To add recursively
                if (CommonBlockExtractor.isControlSmt(statement)) {
                    Statement trueControlled = (statement instanceof IfStmt) ? (statement.asIfStmt()).getThenStmt() : ((NodeWithBody<?>) statement).getBody();
                    this.backwardControlDependenceTransitive(trueControlled, statement.getBegin().get().line);
                    if (statement instanceof IfStmt) {
                        IfStmt ist = statement.asIfStmt();
                        if (ist.hasElseBranch())
                            this.backwardControlDependenceTransitive(ist.getElseStmt().get(), statement.getBegin().get().line);
                    }
                }
                if (this.impactedStatements.contains(i) && !impacted) {
                    if (!additions.contains(location)) {
                        additions.add(location);
                    }
                }
            }
            this.impactedStatements.addAll(additions);
        }
    }
}
