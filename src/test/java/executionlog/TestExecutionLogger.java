package executionlog;

import cpu.*;
import gameboyemu.cpu.CpuState;
import gameboyemu.cpu.LR35902StateBased;
import gameboyemu.executionlog.ExecutionLogger;
import gameboyemu.executionlog.ExecutionUnit;
import gameboyemu.executionlog.Loop;
import gameboyemu.executionlog.SequentialExecutionUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Created with IntelliJ IDEA.
 * User: par
 * Date: 7/6/12
 * Time: 8:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestExecutionLogger {


    private static int SEQUENTIAL_INSTRUCTION_ONE_BYTE_LONG = 0x00;
    private static int BRANCHING_INSTRUCTION_THREE_BYTES_LONG = 0xCD;

    private int pc;
    SimpleMemory vm;
    CpuState cpu;
    ExecutionLogger executionLogger;

    @Before public void setup() {
        pc = 0;
        vm = new SimpleMemory();
        cpu = new LR35902StateBased(vm) {
            @Override
            public int getPC() {
                return pc;
            }
        };
        executionLogger = new ExecutionLogger(vm);

    }

    @Test public void testingThatYouCanLogAStatementExecution() {
        executionLogger.beforeExecutingInstruction(cpu);
        pc = 1;
        executionLogger.afterExecutingInstruction(cpu);

        assertEquals(1, executionLogger.noOfRecordedInstructions());
        ExecutionUnit executionUnit = executionLogger.getExectionUnitNo(0);
        assertFalse(executionUnit.isComposite());
    }

    @Test public void testThatLinearRunDetectionWorks() {

        vm.init(SEQUENTIAL_INSTRUCTION_ONE_BYTE_LONG, SEQUENTIAL_INSTRUCTION_ONE_BYTE_LONG, BRANCHING_INSTRUCTION_THREE_BYTES_LONG, 0 , 0);
        executionLogger.beforeExecutingInstruction(cpu);
        pc = 1;
        executionLogger.afterExecutingInstruction(cpu);

        executionLogger.beforeExecutingInstruction(cpu);
        pc = 2;
        executionLogger.afterExecutingInstruction(cpu);

        executionLogger.beforeExecutingInstruction(cpu);
        pc = 0;
        executionLogger.afterExecutingInstruction(cpu);

        executionLogger.beforeExecutingInstruction(cpu);
        pc = 1;
        executionLogger.afterExecutingInstruction(cpu);

        executionLogger.analyze();

        ExecutionUnit executionUnit = executionLogger.getExectionUnitNo(0);
        assertTrue(executionUnit.isComposite());
        assertEquals(4, executionLogger.noOfRecordedInstructions());
        assertEquals(2, executionLogger.noOfExecutionUnits());
        assertEquals(3, executionUnit.childCount());
        assertFalse(executionLogger.getExectionUnitNo(1).isComposite());
    }

    @Test public void testThatLinearRunDetectionWorks2() {

        vm.init(SEQUENTIAL_INSTRUCTION_ONE_BYTE_LONG,
                BRANCHING_INSTRUCTION_THREE_BYTES_LONG, 0, 0,
                SEQUENTIAL_INSTRUCTION_ONE_BYTE_LONG,
                SEQUENTIAL_INSTRUCTION_ONE_BYTE_LONG,
                SEQUENTIAL_INSTRUCTION_ONE_BYTE_LONG,
                SEQUENTIAL_INSTRUCTION_ONE_BYTE_LONG);
        executionLogger.beforeExecutingInstruction(cpu);
        pc = 1;
        executionLogger.afterExecutingInstruction(cpu);

        executionLogger.beforeExecutingInstruction(cpu);
        pc = 5;
        executionLogger.afterExecutingInstruction(cpu);

        executionLogger.beforeExecutingInstruction(cpu);
        pc = 6;
        executionLogger.afterExecutingInstruction(cpu);

        executionLogger.beforeExecutingInstruction(cpu);
        pc = 7;
        executionLogger.afterExecutingInstruction(cpu);

        executionLogger.beforeExecutingInstruction(cpu);
        pc = 8;
        executionLogger.afterExecutingInstruction(cpu);

        executionLogger.analyze();

        ExecutionUnit executionUnit = executionLogger.getExectionUnitNo(1);
        assertTrue(executionUnit.isComposite());
        assertEquals(5, executionLogger.noOfRecordedInstructions());
        assertEquals(2, executionLogger.noOfExecutionUnits());

        assertEquals(3, executionUnit.childCount());

        assertTrue(executionLogger.getExectionUnitNo(0).isComposite());
        assertTrue(executionLogger.getExectionUnitNo(1).isComposite());

    }


    @Test public void testAtLeastTwoConsecutiveSequentials() {

        ExecutionLogger el = new ExecutionLogger(null);
        el.executionUnits.add(new SequentialExecutionUnit(0, 1));
        el.executionUnits.add(new SequentialExecutionUnit(1, 1));
        el.executionUnits.add(new SequentialExecutionUnit(2, 1));

        assertTrue(el.atLeastTwoConsecutiveSequentials(0));
        assertTrue(el.atLeastTwoConsecutiveSequentials(1));
        assertFalse(el.atLeastTwoConsecutiveSequentials(2));

        el = new ExecutionLogger(null);
        el.executionUnits.add(new SequentialExecutionUnit(0,2));
        el.executionUnits.add(new SequentialExecutionUnit(1,2));
        assertFalse(el.atLeastTwoConsecutiveSequentials(0));

        el = new ExecutionLogger(null);
        el.executionUnits.add(new SequentialExecutionUnit(0,1));
        el.executionUnits.add(new SequentialExecutionUnit(1,1));
        el.executionUnits.add(new ExecutionUnit(2,1) {
            @Override
            public boolean isComposite() {
                return false;
            }

            @Override
            public int childCount() {
                return 0;
            }
        });
        el.executionUnits.add(new SequentialExecutionUnit(3,1));

        assertTrue(el.atLeastTwoConsecutiveSequentials(0));
        assertTrue(el.atLeastTwoConsecutiveSequentials(1));
        assertTrue(el.atLeastTwoConsecutiveSequentials(2));
        assertFalse(el.atLeastTwoConsecutiveSequentials(3));

        el.executionUnits.add(new ExecutionUnit(4 , 1) {
            @Override
            public boolean isComposite() {
                return false;
            }

            @Override
            public int childCount() {
                return 0;
            }
        });
        el.executionUnits.add(new SequentialExecutionUnit(13, 1));
        assertFalse(el.atLeastTwoConsecutiveSequentials(4));

    }

    @Test public void testThatLastInstructionInARunCanBeBranch() {
        vm.init(SEQUENTIAL_INSTRUCTION_ONE_BYTE_LONG,
                BRANCHING_INSTRUCTION_THREE_BYTES_LONG, 0, 0);
        ExecutionLogger el = new ExecutionLogger(vm);

        el.beforeExecutingInstruction(cpu);
        pc = 1;
        el.afterExecutingInstruction(cpu);

        el.beforeExecutingInstruction(cpu);
        pc = 17;
        el.afterExecutingInstruction(cpu);

        el.analyze();

        assertEquals(1, el.noOfExecutionUnits());
        assertTrue(el.getExectionUnitNo(0).isComposite());
        assertEquals(2, el.getExectionUnitNo(0).childCount());

    }

    @Test public void testSequenceSplitter() {

    }

    @Test public void testSimpleLoopDetection() {
        vm.init(SEQUENTIAL_INSTRUCTION_ONE_BYTE_LONG,
                SEQUENTIAL_INSTRUCTION_ONE_BYTE_LONG,
                BRANCHING_INSTRUCTION_THREE_BYTES_LONG, 0, 0,
                SEQUENTIAL_INSTRUCTION_ONE_BYTE_LONG);

        ExecutionLogger el = new ExecutionLogger(vm);
        el.beforeExecutingInstruction(cpu);
        pc = 1;
        el.afterExecutingInstruction(cpu);

        el.beforeExecutingInstruction(cpu);
        pc = 2;
        el.afterExecutingInstruction(cpu);
        el.beforeExecutingInstruction(cpu);
        pc = 1;
        el.afterExecutingInstruction(cpu);

        el.beforeExecutingInstruction(cpu);
        pc = 2;
        el.afterExecutingInstruction(cpu);
        el.beforeExecutingInstruction(cpu);
        pc = 1;
        el.afterExecutingInstruction(cpu);

        el.beforeExecutingInstruction(cpu);
        pc = 2;
        el.afterExecutingInstruction(cpu);
        el.beforeExecutingInstruction(cpu);
        pc = 1;
        el.afterExecutingInstruction(cpu);

        el.beforeExecutingInstruction(cpu);
        pc = 2;
        el.afterExecutingInstruction(cpu);
        el.beforeExecutingInstruction(cpu);
        pc = 5;
        el.afterExecutingInstruction(cpu);

        el.analyze();

        boolean atleastOneLoop = false;
        for (ExecutionUnit executionUnit : el.executionUnits) {
            if(executionUnit instanceof Loop) {
                 atleastOneLoop = true;
            }
        }
        assertTrue(atleastOneLoop);

    }
}
