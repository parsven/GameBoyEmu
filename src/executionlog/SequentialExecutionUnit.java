package executionlog;

/**
 * Created with IntelliJ IDEA.
 * User: par
 * Date: 7/11/12
 * Time: 12:18 PM
 */

public class SequentialExecutionUnit extends ExecutionUnit {

    public SequentialExecutionUnit(int pc, int len) {
        super(pc, len);
    }

    @Override
    public boolean isComposite() {
        return false;
    }

    @Override
    public int childCount() {
        return 0;
    }

}
