package gameboyemu.system;

/**
 * Created by IntelliJ IDEA.
 * User: par
 * Date: 2012-02-05
 * Time: 12:24
 * To change this template use File | Settings | File Templates.
 */
public class HRAMimpl implements HRAM {


    private int[] mem = new int[0x7f];

    public void writeByte(int address, int b) {
        mem[address - 0xff80] = b;
    }

    public int readByte(int address) {
        return mem[address - 0xff80];
    }

    public void writeWord(int address, int w) {
        int lowByte = w & 0xff;
        int hiByte = (w & 0xff00) >> 8;
        writeByte(address, lowByte);
        writeByte(address + 1, hiByte);
    }

    public int readWord(int address) {
        int lowByte = readByte(address);
        int hiByte = readByte(address + 1);
        return (hiByte << 8)  | lowByte;
    }
}
