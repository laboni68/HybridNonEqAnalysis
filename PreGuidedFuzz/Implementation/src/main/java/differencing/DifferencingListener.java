package differencing;

import com.microsoft.z3.*;
import differencing.classification.Classification;
import differencing.classification.PartitionClassifier;
import differencing.domain.Model;
import differencing.models.Iteration;
import differencing.models.Partition;
import differencing.repositories.PartitionRepository;
import differencing.transformer.ModelToZ3Transformer;
import differencing.transformer.SpfToModelTransformer;
import differencing.transformer.ValueToModelTransformer;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.JVMReturnInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.util.MethodSpec;
import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.symbc.SymbolicListener;

import java.util.*;

public class DifferencingListener extends PropertyListenerAdapter implements AutoCloseable {
    private final Iteration iteration;
    private final MethodSpec areErrorsEquivalentSpec;
    private final MethodSpec areResultsEquivalentSpec;
    private final MethodSpec runSpec;
    private final SatisfiabilityChecker satChecker;

    private final ValueToModelTransformer valToModel = new ValueToModelTransformer();
    private final SpfToModelTransformer spfToModel = new SpfToModelTransformer();

    private final Set<Partition> partitions = new HashSet<>();

    private int partitionNr =  1;
    private Classification partitionClassification = null;
    private ReachabilityCheckResult partitionPcResult = null;
    private EquivalenceCheckResult partitionNeqResult = null;
    private EquivalenceCheckResult partitionEqResult = null;
    private boolean hasPartitionUifPc = false;
    private boolean hasPartitionUifV1 = false;
    private boolean hasPartitionUifV2 = false;
    private Integer partitionPcConstraintCount = null;
    private boolean hasPartitionTimedOut = false;
    private boolean isPartitionDepthLimited = false;
    private SymbolicListener symbolicListener = null;
    private List<String> programArgs=new ArrayList<>();
    private String combinedClass="";
    private Boolean fuzzingNeq=false;
    public int partitionCount=0;
    public boolean nullPartition=false;

    private final Context context = new Context();
    private final ModelToZ3Transformer modelToZ3 = new ModelToZ3Transformer(this.context);

    private BoolExpr v1Summary = null;
    private BoolExpr v2Summary = null;
    private Map<String, Expr<?>> variables = new HashMap<>();

    public DifferencingListener(Iteration iteration, DifferencingParameters parameters, int solverTimeout, SymbolicListener symbolicListener, String combinedClass) {
        StopWatches.start("iteration-" + iteration.iteration + ":partition-classification");

        this.iteration = iteration;
        this.areErrorsEquivalentSpec = MethodSpec.createMethodSpec("*.IDiff" + parameters.getToolName() + iteration.iteration + ".areErrorsEquivalent");
        this.areResultsEquivalentSpec = MethodSpec.createMethodSpec("*.IDiff" + parameters.getToolName() + iteration.iteration + ".areResultsEquivalent");
        this.runSpec = MethodSpec.createMethodSpec("*.IDiff" + parameters.getToolName() + iteration.iteration + ".run");
        this.satChecker = new SatisfiabilityChecker(solverTimeout);
        this.symbolicListener = symbolicListener;
        this.combinedClass=combinedClass;

        StopWatches.suspend("iteration-" + iteration.iteration + ":partition-classification");
    }

    @Override
    public void close() throws Exception {
        this.satChecker.close();
        this.context.close();

        StopWatches.stop("iteration-" + iteration.iteration + ":partition-classification");
    }

    public Context getContext() {
        return this.context;
    }

    public BoolExpr getV1Summary() {
        return this.v1Summary;
    }

    public BoolExpr getV2Summary() {
        return this.v2Summary;
    }

    public Map<String ,Expr<?>> getVariables() {
        return this.variables;
    }

    public Set<Partition> getPartitions() {
        return this.partitions;
    }

    public boolean isDepthLimited() {
        return this.partitions.stream().anyMatch(p -> p.result == Classification.DEPTH_LIMITED);
    }

    public boolean hasUif() {
        return this.partitions.stream().anyMatch(p -> p.hasUif);
    }

    @Override
    public void searchFinished(Search search) {
        this.variables.putAll(this.collectVariables(this.v1Summary));
        this.variables.putAll(this.collectVariables(this.v2Summary));
    }

    private Map<String, Expr<?>> collectVariables(Expr<?> expr) {
        if (expr == null) {
            return Collections.emptyMap();
        } else if (expr.isConst()) {
            String name = expr.getFuncDecl().getName().toString();
            if (name.startsWith("UF_") || name.startsWith("AF_")) {
                return Collections.emptyMap();
            }
            return Collections.singletonMap(name, expr);
        } else {
            Map<String, Expr<?>> vars = new HashMap<>();
            for (Expr<?> arg : expr.getArgs()) {
                vars.putAll(this.collectVariables(arg));
            }
            return vars;
        }
    }

    @Override
    public void searchConstraintHit(Search search) {
        if (search.getVM().getCurrentThread().isFirstStepInsn()) {
            return;
        }
        if (search.getDepth() >= search.getDepthLimit()) {
            this.isPartitionDepthLimited = true;
        }
        if (search.getSearchConstraint().contains("iteration time")) {
            this.hasPartitionTimedOut = true;
        }
        System.out.println("Call from searchConstraintHit");
        //this.partitionClassification = Classification.EQ;
        this.startNextPartition();
    }

    @Override
    public void propertyViolated(Search search) {
        if (search.getVM().getCurrentThread().isFirstStepInsn()) {
            return;
        }
        System.out.println("Call from propertyViolated");
        // System.out.println(symbolicListener.currentMethodName);
        // System.out.println(symbolicListener.allSummaries.get(symbolicListener.currentMethodName).getPathConditions().size());
        int size_of_path_conditions = symbolicListener.allSummaries.get(symbolicListener.currentMethodName).getPathConditions().size();
    //    for(int i=0;i<symbolicListener.allSummaries.get(symbolicListener.currentMethodName).getPathConditions().size();i++){
    //        System.out.println(i+" : "+symbolicListener.allSummaries.get(symbolicListener.currentMethodName).getPathConditions().get(i)._1+" : "+symbolicListener.allSummaries.get(symbolicListener.currentMethodName).getPathConditions().get(i)._2);
    //      }
        // System.out.println("Property violated const: "+symbolicListener.allSummaries.get(symbolicListener.currentMethodName).getPathConditions().get(size_of_path_conditions-1)._1);
       // MethodSummary methodSummary = symbolicListener.allSummaries.get(symbolicListener.currentMethodName);
        String pc = (String) symbolicListener.allSummaries.get(symbolicListener.currentMethodName).getPathConditions().get(size_of_path_conditions-1)._1;
        String argValues = symbolicListener.allSummaries.get(symbolicListener.currentMethodName).getArgValues();
        String symValues = symbolicListener.allSummaries.get(symbolicListener.currentMethodName).getSymValues();
        StringTokenizer st = new StringTokenizer(symValues, ",");
        StringTokenizer st2 = new StringTokenizer(argValues, ",");
        String token = "";
        // System.out.println("combined "+combinedClass);
        if(combinedClass.contains("ModDiff")){
            combinedClass=combinedClass.replace("ModDiff.NEq","ModDiffNeq");
            combinedClass=combinedClass.replace("ModDiff.Eq","ModDiffEq");
        }

        // System.out.println("Arg Value: " + st2.nextToken());
        Random random = new Random();
         String inputs="";
        while (st.hasMoreTokens())
        {
            token = st.nextToken();
           // System.out.println("Token: " + token);
            if (pc.contains(token)) {
                String temp = pc.substring(pc.indexOf(token));
            if (temp.indexOf(']') >= 0) {
                String val = temp.substring(temp.indexOf("[") + 1, temp.indexOf("]"));
                 inputs += val + " ";
                 //System.out.println("Val: " + val);
                 if(val.equals("-9223372036854775808"))
                 {
                        programArgs.add("-92233");
                 }
                 else if(val.equals("9223372036854775808"))
                 {
                        // programArgs.add(String.valueOf(random.nextDouble()));
                        programArgs.add("92233"); 
                 }
                //  else if(val.equals("4.9E-324")){
                       
                //         programArgs.add(String.valueOf(random.nextDouble()));
                //  }
                 else{
                        programArgs.add(val);
                 }
                
                // System.out.println("Val: " + val);
            }
            // else{
            //      programArgs.add("92233");
            // }
        }
        else{
            // programArgs.add(String.valueOf(random.nextDouble()));
            programArgs.add("92233");
        }
        }
        System.out.println("Program Args: " + programArgs.toString());
        RunJava runJava = new RunJava();
        try {
            boolean hasUif = this.hasPartitionUifPc || this.hasPartitionUifV1 || this.hasPartitionUifV2;
            System.out.println("hasUif: " + hasUif);
            String output = runJava.runJavaClass(combinedClass, "target/classes/", programArgs);
            System.out.println("Output is: "+output);
            if (output.contains("false") && output.contains("NaN")!=true) {
                System.out.println("The two versions are not equivalent");
                fuzzingNeq=true;
                this.partitionClassification = Classification.NEQ;
                search.terminate(); //
                // System.out.println("The two versions are not equivalent");
                // System.out.println(parameters.getTargetDirectory() + " -> " +"NEQ");
                // return;
            }
            else{
                this.partitionClassification = Classification.MAYBE_EQ;
                // if(hasUif){
                //     this.partitionClassification = Classification.MAYBE_EQ;
                // }
                // else{
                //     this.partitionClassification = Classification.NEQ;
                // }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        programArgs=new ArrayList<>();
        this.startNextPartition();
    }

    @Override
    public void executeInstruction(VM vm, ThreadInfo currentThread, Instruction instructionToExecute) {
        //System.out.println("-----executeInstruction---In DifferentingListener");
        if (!(instructionToExecute instanceof JVMReturnInstruction)) {
            return;
        }
        MethodInfo mi = instructionToExecute.getMethodInfo();
       //System.out.println("method info: " + mi);
        if (this.runSpec.matches(mi)) {
            System.out.println("Call from executeInstruction");
            System.out.println("Partition is eq...");
            this.partitionClassification = Classification.EQ;
            System.out.println("Partition classification: done ");
            this.startNextPartition();
            //System.out.println("Run spec--> After start next partition");
        } else if (this.areErrorsEquivalentSpec.matches(mi)) {
            System.out.println("Error equivalence spec");
            StopWatches.suspend("iteration-" + iteration.iteration + ":symbolic-execution");
            StopWatches.resume("iteration-" + iteration.iteration + ":partition-classification");

            ThreadInfo threadInfo = vm.getCurrentThread();
            StackFrame stackFrame = threadInfo.getModifiableTopFrame();
            LocalVarInfo[] localVars = stackFrame.getLocalVars();

            // Our areErrorsEquivalent methods all have two method parameters
            // (a and b) and no other local variables, so the total
            // number of local variables should always be two.
            assert localVars.length == 2;

            // Get the concrete values of the two parameters:
            Object[] argumentValues = stackFrame.getArgumentValues(threadInfo);
            Object v1Value = argumentValues[0];
            Object v2Value = argumentValues[1];
            // System.out.println("v1Value: " + v1Value+" v2Value: "+v2Value);

            String v1Error = v1Value == null ? null : ((DynamicElementInfo) v1Value).getClassInfo().getName();
            String v2Error = v2Value == null ? null : ((DynamicElementInfo) v2Value).getClassInfo().getName();

            boolean areEquivalent = Objects.equals(v1Error, v2Error);

            // System.out.println("v1Error: " + v1Error+" v2Error: "+v2Error);

            if (areEquivalent) {
                stackFrame.setOperand(0, 1, false);
            } else {
                this.partitionNeqResult = new EquivalenceCheckResult(Status.SATISFIABLE, null, null, null, null, null);
                this.partitionEqResult = new EquivalenceCheckResult(Status.UNSATISFIABLE, null, null, null, null, null);
                stackFrame.setOperand(0, 0, false);
            }

            StopWatches.suspend("iteration-" + iteration.iteration + ":partition-classification");
            StopWatches.resume("iteration-" + iteration.iteration + ":symbolic-execution");
            //System.out.println("========End Error Eq Spec=======");
        } else if (this.areResultsEquivalentSpec.matches(mi)) {
            System.out.println("Output equivalence spec");
            StopWatches.suspend("iteration-" + iteration.iteration + ":symbolic-execution");
            StopWatches.resume("iteration-" + iteration.iteration + ":partition-classification");
            

            ThreadInfo threadInfo = vm.getCurrentThread();
            StackFrame stackFrame = threadInfo.getModifiableTopFrame();
            LocalVarInfo[] localVars = stackFrame.getLocalVars();

            

            // Our areResultsEquivalent methods all have two method parameters
            // (a and b) and no other local variables, so the total
            // number of local variables should always be two.
            assert localVars.length == 2;

            // -------------------------------------------------------
            // Get the current symbolic state of the program.

            // Get the current path condition:
            PathCondition pathCondition = PathCondition.getPC(vm);
            Constraint pcConstraint = pathCondition.header;
            //System.out.println("After or before call from property violated?");
            //System.out.println("path condition : " + pathCondition);
            //System.out.println("pc Constraint in diff listener : " + pcConstraint);
            

            Object[] argumentValues = stackFrame.getArgumentValues(threadInfo);

            // Get the symbolic value of the first parameter:
            int v1SlotIndex = localVars[0].getSlotIndex();
            Expression v1Expression = (Expression) stackFrame.getSlotAttr(v1SlotIndex);
            // Get the concrete value of the first parameter:
            Object v1Value = argumentValues[0];

            // Get the symbolic value of the second parameter:
            int v2SlotIndex = localVars[1].getSlotIndex();
            Expression v2Expression = (Expression) stackFrame.getSlotAttr(v2SlotIndex);
            // Get the concrete value of the second parameter:
            Object v2Value = argumentValues[1];
            //System.out.println("v1Value: " + v1Value+" v2Value: "+v2Value);

            // -------------------------------------------------------
            // Check equivalence of the two parameters using an SMT solver.

            boolean v1IsConcrete = v1Expression == null;
            boolean v2IsConcrete = v2Expression == null;

            Model pcModel = this.spfToModel.transform(pcConstraint);
            Model v1Model = v1IsConcrete ? this.valToModel.transform(v1Value) : this.spfToModel.transform(v1Expression);
            Model v2Model = v2IsConcrete ? this.valToModel.transform(v2Value) : this.spfToModel.transform(v2Expression);
            

            this.v1Summary = this.addPartitionResultToSummary(this.v1Summary, pcModel, v1Model);
            this.v2Summary = this.addPartitionResultToSummary(this.v2Summary, pcModel, v2Model);
            // System.out.println("v1Summary: " + this.v1Summary);
            // System.out.println("v2Summary: " + this.v2Summary);

            this.partitionPcConstraintCount = this.getConstraintCount(pcConstraint);

            this.hasPartitionUifPc = HasUifVisitor.hasUif(pcModel);
            this.hasPartitionUifV1 = HasUifVisitor.hasUif(v1Model);
            this.hasPartitionUifV2 = HasUifVisitor.hasUif(v2Model);
            //System.out.println("hasPartitionUifPc: " + this.hasPartitionUifPc+" hasPartitionUifV1: "+this.hasPartitionUifV1+" hasPartitionUifV2: "+this.hasPartitionUifV2);

            // boolean hasUif = this.hasPartitionUifPc || this.hasPartitionUifV1 || this.hasPartitionUifV2;
            //System.out.println("hasUif: " + hasUif);
            // this.partitionPcResult = new ReachabilityCheckResult(Status.SATISFIABLE, null, null, null);
            // if(Search.currentError != null){
            //      this.partitionClassification = new PartitionClassifier(
            //     false, false, false, this.hasPartitionTimedOut, this.isPartitionDepthLimited,
            //     this.partitionPcResult.status ,Status.UNSATISFIABLE, Status.SATISFIABLE,
            //     this.hasPartitionUifPc, hasUif
            // ).getClassification();
            // }
            // else{
            //    this.partitionClassification = new PartitionClassifier(
            //     false, false, false, this.hasPartitionTimedOut, this.isPartitionDepthLimited,
            //     this.partitionPcResult.status ,Status.SATISFIABLE, Status.UNSATISFIABLE,
            //     this.hasPartitionUifPc, hasUif
            // ).getClassification();
            // }
            

            // this.partitionPcResult = this.satChecker.checkPc(pcModel); //rechability check
            this.partitionNeqResult = this.satChecker.checkNeq(pcModel, v1Model, v2Model); //equivalence check
            this.partitionEqResult = this.satChecker.checkEq(pcModel, v1Model, v2Model); //non-equivalence check
            
            
            // System.out.println("partitionPcResult: " + this.partitionPcResult.status);
            System.out.println("partitionNeqResult: " + this.partitionNeqResult.status);
            System.out.println("partitionEqResult: " + this.partitionEqResult.status);

            // this.partitionClassification = new PartitionClassifier(
            //     false, false, false, this.hasPartitionTimedOut, this.isPartitionDepthLimited,
            //     this.partitionPcResult.status, this.partitionNeqResult.status, this.partitionEqResult.status,
            //     this.hasPartitionUifPc, hasUif
            // ).getClassification();
            
            // //System.out.println("partitionClassification in executeInstruction: " + this.partitionClassification);
            // System.out.println("Timeout "+this.hasPartitionTimedOut+" depth limit "+this.isPartitionDepthLimited+" has uif? "+this.hasPartitionUifPc);
            // System.out.println("Path condition reachability "+this.partitionPcResult.status+" Neq "+this.partitionNeqResult.status+" Eq "+this.partitionEqResult.status);
            // if (this.partitionClassification == Classification.EQ ||
            //     this.partitionClassification == Classification.UNREACHABLE
            // ) {
            //    // System.out.println("EQ or UNREACHABLE");
            //     // If we are sure that the partition is
            //     // EQ or UNREACHABLE, mark it as EQ.
            //     stackFrame.setOperand(0, 1, false);
            // } else {
            //    // System.out.println("NOT EQ or UNREACHABLE");
            //     // If we aren't 100% sure that the partition is EQ,
            //     // mark it as NEQ, just to be safe.
            //     stackFrame.setOperand(0, 0, false);
            // }
            // System.out.println("========End Output Eq Spec=======");

            StopWatches.suspend("iteration-" + iteration.iteration + ":partition-classification");
            StopWatches.resume("iteration-" + iteration.iteration + ":symbolic-execution");
        }
    }

    private BoolExpr addPartitionResultToSummary(BoolExpr summary, Model pcModel, Model resultModel) {
        BoolExpr pcExpr = (BoolExpr) this.modelToZ3.transform(pcModel);
        Expr<?> v1Expr = this.modelToZ3.transform(resultModel);
        BoolExpr retExpr = this.context.mkEq(this.context.mkConst("Ret", v1Expr.getSort()), v1Expr);
        BoolExpr partitionExpr = pcModel == null ? retExpr : this.context.mkAnd(pcExpr, retExpr);
        return summary == null ? partitionExpr : this.context.mkOr(summary, partitionExpr);
    }

    private void startNextPartition() {
        partitionCount++;
        System.out.println("Start next partition function in DifferencingListener");
        StopWatches.suspend("iteration-" + iteration.iteration + ":symbolic-execution");
        StopWatches.resume("iteration-" + iteration.iteration + ":partition-classification");

        if (this.partitionClassification == null) {
            nullPartition=true;
            // int size_of_path_conditions = symbolicListener.allSummaries.get(symbolicListener.currentMethodName).getPathConditions().size();
            // System.out.println("Partition classification is null "+size_of_path_conditions);
            // if (size_of_path_conditions<=0){
            //     System.out.println("size of path condition "+ size_of_path_conditions);
            //     this.partitionClassification = Classification.MAYBE_EQ;
            // }
            // else{
                this.partitionClassification = Classification.EQ;
            // }
            // if (this.isPartitionDepthLimited) {
            //     this.partitionClassification = Classification.DEPTH_LIMITED;
            // } else {
            //     this.partitionClassification = Classification.UNKNOWN;
            // }
            
            // this.partitionClassification = Classification.MAYBE_EQ;
        }
        //     PathCondition pathCondition = PathCondition.getPC(VM.getVM());
        //     Constraint pcConstraint = pathCondition.header;
        //     Model pcModel = this.spfToModel.transform(pcConstraint);

        //     this.partitionPcConstraintCount = this.getConstraintCount(pcConstraint);
        //     this.hasPartitionUifPc = HasUifVisitor.hasUif(pcModel);

        //     this.partitionPcResult = this.satChecker.checkPc(pcModel);

        //     Status pcStatus = this.partitionPcResult == null ? null : this.partitionPcResult.status;;
        //     Status neqStatus = this.partitionNeqResult == null ? null : this.partitionNeqResult.status;
        //     Status eqStatus = this.partitionEqResult == null ? null : this.partitionEqResult.status;
        //     System.out.println("pcStatus: " + pcStatus+" neqStatus: "+neqStatus+" eqStatus: "+eqStatus);

        //     this.partitionClassification = new PartitionClassifier(
        //         false, false, false, this.hasPartitionTimedOut, this.isPartitionDepthLimited,
        //         pcStatus, neqStatus, eqStatus,
        //         this.hasPartitionUifPc, this.hasPartitionUifPc
        //     ).getClassification();
        // }
        // System.out.println("Search --> "+Search.currentError);
        //System.out.println("Outside of if "+ this.partitionClassification.toString()+" Neq Result "+this.partitionNeqResult+" Eq Result "+this.partitionEqResult);
        // if(fuzzingNeq==true){ // && iteration.iteration == 1){
        //     //System.out.println("Search Error: "+Search.currentError);
        //     // this.partitionClassification = Classification.MAYBE_NEQ;
        //     this.partitionClassification = Classification.NEQ;
        //     //TO-DO: Add the test to check whether MAYBE_NEQ or actual NEQ
        // }
        Partition partition = new Partition(
            this.iteration.id,
            this.partitionNr,
            this.partitionClassification,
            this.partitionPcResult,
            this.partitionNeqResult,
            this.partitionEqResult,
            this.hasPartitionUifPc,
            this.hasPartitionUifV1,
            this.hasPartitionUifV2,
            this.partitionPcConstraintCount,
            StopWatches.getTime("iteration-" + this.iteration.iteration),
            ""
        );

        // partition.id = PartitionRepository.getId(partition);
        //PartitionRepository.insertOrUpdate(partition);
        partition.id=this.partitionNr;

        this.partitions.add(partition);
        this.partitionNr++;
        this.partitionClassification = null;
        this.partitionPcResult = null;
        this.partitionNeqResult = null;
        this.partitionEqResult = null;
        this.hasPartitionUifPc = false;
        this.hasPartitionUifV1 = false;
        this.hasPartitionUifV2 = false;
        this.partitionPcConstraintCount = null;
        this.hasPartitionTimedOut = false;
        this.isPartitionDepthLimited = false;

        StopWatches.suspend("iteration-" + iteration.iteration + ":partition-classification");
        StopWatches.resume("iteration-" + iteration.iteration + ":symbolic-execution");
    }

    private int getConstraintCount(Constraint pcConstraint) {
        int constraintCount = 0;
        Constraint c = pcConstraint;
        while (c != null) {
            constraintCount++;
            c = c.and;
        }
        return constraintCount;
    }
}
