package executionlog;

import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: par
 * Date: 7/19/12
 * Time: 1:33 PM
 */

public class LinearSequentialRun extends ExecutionUnit {

    LinkedList<ExecutionUnit> children;
    private int pcOfLastExecutionUnit;

    public LinearSequentialRun(int pcOfFirstSequential, ExecutionUnit lastExecutionUnit, LinkedList<ExecutionUnit> children) {
        super(pcOfFirstSequential, lastExecutionUnit.getPc() + lastExecutionUnit.getLength());
        this.pcOfLastExecutionUnit = lastExecutionUnit.getPc();
        this.children = children;
    }

    @Override
    public boolean isComposite() {
        return true;
    }

    @Override
    public int childCount() {
        return children.size();
    }
}
