package cpu;

/**
 * Created by IntelliJ IDEA.
 * User: par
 * Date: 2011-08-10
 * Time: 22:22
 * To change this template use File | Settings | File Templates.
 */
public class SimpleMemory implements VirtualMemory16 {

    int[] mem = new int[65536];

    public SimpleMemory(int ... initmem) {
        init(initmem);
    }

    public void init(int ... initmem) {
        int cnt = 0;
        for (int i : initmem) {
            mem[cnt++] = i;
        }
    }

    public void writeByte(int adress, int b) {
        mem[adress] = b;
    }

    public int readByte(int adress) {
        return mem[adress];
    }

    public void writeWord(int adress, int w) {
        int lowByte = w & 0xff;
        int hiByte = (w & 0xff00) >> 8;
        writeByte(adress, lowByte);
        writeByte(adress + 1, hiByte);
    }

    public int readWord(int adress) {
        int lowByte = readByte(adress);
        int hiByte = readByte(adress + 1);
        return (hiByte << 8)  | lowByte;
    }
}
