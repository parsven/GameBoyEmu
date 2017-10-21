package executionlog;

/**
 * Created with IntelliJ IDEA.
 * User: par
 * Date: 7/10/12
 * Time: 8:38 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ExecutionUnit {

    private int pc;
    protected int length;

    protected ExecutionUnit(int pc, int length) {
        this.pc = pc;
        this.length = length;
    }


    public abstract boolean isComposite();

    public abstract int childCount();

    public int getPc() {
        return pc;
    }

    public int getLength() {
        return length;
    }
}
