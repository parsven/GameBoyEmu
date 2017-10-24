package gameboyemu.system;


/**
 User: par
 Date: 2012-02-04
 */
public class WRAMimpl implements WRAM {


    private int[] mem = new int[4096*2];

    public void writeByte(int address, int b) {
        mem[address - 0xc000] = b;
    }

    public int readByte(int address) {
        return mem[address - 0xc000];
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