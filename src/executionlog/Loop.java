package executionlog;

/**
 * Created with IntelliJ IDEA.
 * User: par
 * Date: 7/24/12
 * Time: 6:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class Loop extends ExecutionUnit{
    protected Loop(int pc, int length) {
        super(pc, length);
    }

    @Override
    public boolean isComposite() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int childCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
