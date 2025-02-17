/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * Symbolic Pathfinder (jpf-symbc) is licensed under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package gov.nasa.jpf.symbc.bytecode;

import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

// we should factor out some of the code and put it in a parent class for all "if statements"

/**
 * YN: fixed choice selection in symcrete support (Yannic Noller <nolleryc@gmail.com>)
 */
public class IFEQ extends gov.nasa.jpf.jvm.bytecode.IFEQ {
    public IFEQ(int targetPosition) {
        super(targetPosition);
    }

    @Override
    public Instruction execute(ThreadInfo ti) {

        StackFrame sf = ti.getModifiableTopFrame();
        IntegerExpression sym_v = (IntegerExpression) sf.getOperandAttr();
        if (sym_v == null) { // the condition is concrete
            return super.execute(ti);
        } else { // the condition is symbolic

            String[] dp = SymbolicInstructionFactory.dp;
            ChoiceGenerator<?> cg;

            if (!ti.isFirstStepInsn()) { // first time around
                if (SymbolicInstructionFactory.collect_constraints)
                    cg = new PCChoiceGenerator(1);
                else {
                    if (dp[0].equalsIgnoreCase("omega")) // hack because omega does not handle not or or correctly
                        cg = new PCChoiceGenerator(3);
                    else
                        cg = new PCChoiceGenerator(2);
                }
                ((PCChoiceGenerator) cg).setOffset(this.position);
                ((PCChoiceGenerator) cg).setMethodName(this.getMethodInfo().getFullName());
                ti.getVM().getSystemState().setNextChoiceGenerator(cg);
                return this;
            } else { // this is what really returns results
                cg = ti.getVM().getSystemState().getChoiceGenerator();

                conditionValue = popConditionValue(sf);
                assert (cg instanceof PCChoiceGenerator) : "expected PCChoiceGenerator, got: " + cg;
                if (SymbolicInstructionFactory.collect_constraints) {
                    // YN: reuse conditionValue written from concrete exec + set choice correctly
                    ((PCChoiceGenerator) cg).select(conditionValue ? 1 : 0);
                } else {
                    conditionValue = (Integer) cg.getNextChoice() == 0 ? false : true;
                }
            }

            PathCondition pc;

            // pc is updated with the pc stored in the choice generator above
            // get the path condition from the
            // previous choice generator of the same type

            ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);

            if (prev_cg == null)
                pc = new PathCondition();
            else
                pc = ((PCChoiceGenerator) prev_cg).getCurrentPC();

            assert pc != null;
            pc.setLineNumber(this.getLineNumber());
            if (SymbolicInstructionFactory.collect_constraints) {
                if (conditionValue) {
                    pc._addDet(Comparator.EQ, sym_v, 0);
                    ((PCChoiceGenerator) cg).setCurrentPC(pc);
                    return getTarget();
                } else {
                    pc._addDet(Comparator.NE, sym_v, 0);
                    ((PCChoiceGenerator) cg).setCurrentPC(pc);
                    return getNext(ti);
                }
            }

            if (conditionValue) {
                pc._addDet(Comparator.EQ, sym_v, 0);
                if (!pc.simplify()) {// not satisfiable
                    ti.getVM().getSystemState().setIgnored(true);
                } else {
                    ((PCChoiceGenerator) cg).setCurrentPC(pc);
                }
                return getTarget();
            } else {
                if (dp[0].equalsIgnoreCase("omega")) {// hack
                    if ((Integer) cg.getNextChoice() == 0)
                        pc._addDet(Comparator.GT, sym_v, 0);
                    else {// ==2
                        assert ((Integer) cg.getNextChoice() == 2);
                        pc._addDet(Comparator.LT, sym_v, 0);
                    }
                } else
                    pc._addDet(Comparator.NE, sym_v, 0);
                if (!pc.simplify()) {// not satisfiable
                    ti.getVM().getSystemState().setIgnored(true);
                } else {
                    ((PCChoiceGenerator) cg).setCurrentPC(pc);
                }
                return getNext(ti);
            }
        }
    }
}
