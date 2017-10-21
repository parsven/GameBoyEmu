package cpu;

/**
 * Created by IntelliJ IDEA.
 * User: noy
 * Date: 2/20/12
 * Time: 9:20 PM
 * To change this template use File | Settings | File Templates.
 */
public interface VirtualMemory16 extends VirtualMemory {
		void writeWord(int adress, int b);
		int readWord(int adress);
}
