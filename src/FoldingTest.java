
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: par
 * Date: 2012-07-30
 * Time: 18:47
 * To change this template use File | Settings | File Templates.
 */
public class FoldingTest {
    
    
    /*
    public Object collapseLoops(Object first, PeekableIterator rest) {
        if(first instanceof Loop) {
            Object peek = rest.peek();
            if(((Loop)first).isIterationOf(peek)) {
                ((Loop)first).addIteration(rest.next());
                
                return null;
            } else {
                return rest.next();
            }
        }
    }

*/
    
    
    private Collection fold(Iterator<Composable> list) {
        Collection<Composable> res = new LinkedList<>();

        while(list.hasNext()) {

        }


        return res;
    }
    
    
    
    private boolean isCombineable(Composable one, Composable two) {

        return false;
    }
    
    private Composite compose(Composable one, Composable two) {

        return null;
    }
    
    private boolean collapsable(Composite composite, Composable element) {

        return false;
    }

    private Composite compose(Composite composite, Composable element) {

        return composite;
    }
    
    
    /*
    private void isIterationOf(Object peek) {
    }

    private class Loop {
        public boolean isIterationOf(Object peek) {
            return false;
        }

        public void addIteration(Object peek) {
            //To change body of created methods use File | Settings | File Templates.
        }
    }

    private class PeekableIterator {
        public Object peek() {
            //To change body of created methods use File | Settings | File Templates.
        }

        public Object next() {
            return null;  //To change body of created methods use File | Settings | File Templates.
        }
    }
*/
    private class Composable {
    }

    private class Composite {
        
    }
}
