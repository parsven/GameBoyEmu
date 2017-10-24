package gameboyemu.executionlog;

import gameboyemu.cpu.CpuState;
import gameboyemu.cpu.VirtualMemory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: par
 * Date: 7/6/12
 * Time: 8:53 PM
 */
public class ExecutionLogger {

    private VirtualMemory vm;
    private int pc;
    int opCode;
    private int noOfRecordedInstructions;

    public ArrayList<ExecutionUnit> executionUnits = new ArrayList<>(1024);

    public ExecutionLogger(VirtualMemory vm) {
        this.vm = vm;
    }

    public int noOfRecordedInstructions() {
        return noOfRecordedInstructions;
    }

    public void beforeExecutingInstruction(CpuState cpu) {
        pc = cpu.getPC();
        opCode = vm.readByte(pc);
    }

    public void afterExecutingInstruction(CpuState cpu) {
        Instructions.Instruction instruction = Instructions.opcodes[opCode];
        if(instruction.sequential) { // && cpu.getPC() == pc + instruction.length) {
            ExecutionUnit eu = new SequentialExecutionUnit(pc, instruction.length);
            executionUnits.add(eu);
        } else {
            ExecutionUnit eu = new ExecutionUnit(pc, instruction.length) {
                @Override
                public boolean isComposite() {
                    return false;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public int childCount() {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }
            };
            executionUnits.add(eu);
        }
        noOfRecordedInstructions++;
    }

    public ExecutionUnit getExectionUnitNo(int i) {
        return executionUnits.get(i);
    }

    public int noOfExecutionUnits() {
        return executionUnits.size();
    }

    public void analyze() {
        replaceRunOfSequentialsWithComposite(0);
        detectLoops(0);
    }


    interface F<A,B> { B f(A a); }
    public static <A, B> A fold(F<A, F<B, A>> f, A z, Iterable<B> xs)
    { A p = z;
        for (B x : xs)
        { p = f.f(p).f(x); }
        return p;
    }


    private void detectLoops(int index) {
/*        fold(new F<ArrayList<ExecutionUnit>, F<? extends Object, ArrayList<ExecutionUnit>>>() {
            @Override
            public F<? extends Object, ArrayList<ExecutionUnit>> f(ArrayList<ExecutionUnit> executionUnits) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        }, executionUnits);
  */  }


    public List<ExecutionUnit> fold(ExecutionUnit left, Iterable<ExecutionUnit> rest, F<ExecutionUnit, ExecutionUnit> fn) {
        return null;
    }

    void replaceRunOfSequentialsWithComposite(int index) {

        while(index < executionUnits.size() - 1) {
            if(atLeastTwoConsecutiveSequentials(index)) {
                LinkedList<ExecutionUnit> childs = new LinkedList<ExecutionUnit>();
                ExecutionUnit firstExecutonUnit = executionUnits.remove(index);
                childs.add(firstExecutonUnit);
                while(atLeastTwoConsecutiveSequentials(index)) {
                    ExecutionUnit executionUnit = executionUnits.remove(index);
                    childs.add(executionUnit);
                }
                ExecutionUnit lastExecutionUnit = executionUnits.remove(index);
                childs.add(lastExecutionUnit);
                LinearSequentialRun newComposite = new LinearSequentialRun(firstExecutonUnit.getPc(), lastExecutionUnit, childs);
                executionUnits.add(index, newComposite);
            }
            index++;
        }
    }

    public boolean atLeastTwoConsecutiveSequentials(int beginIndex) {

        if(beginIndex + 1 > executionUnits.size() - 1) {
            return false;
        }

        ExecutionUnit first = executionUnits.get(beginIndex);
        ExecutionUnit second = executionUnits.get(beginIndex + 1);

        int firstLength = first.getLength();
        return first.getPc() + firstLength == second.getPc();
    }
}
