package gameboyemu.cpu;

/**
 * Created by IntelliJ IDEA.
 * User: par
 * Date: 2011-08-10
 * Time: 22:20
 * To change this template use File | Settings | File Templates.
 */
public interface VirtualMemory {
    void writeByte(int adress, int b);
    int readByte(int adress);
}
