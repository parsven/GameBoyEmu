package gameboyemu.cpu;

/**
 * Created by IntelliJ IDEA.
 * User: par
 * Date: 2011-08-10
 * Time: 22:08
 * To change this template use File | Settings | File Templates.
 */
public interface CpuState {
    boolean getZ();
    void setZ(boolean z);

    boolean getN();
    void setN(boolean n);

    boolean getH(); 
    void setH(boolean h);

    boolean getC();
    void setC(boolean c);

    int getAF();
    int getBC();
    int getDE();
    int getHL();
    int getSP();
    int getPC();
}
