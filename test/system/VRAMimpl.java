package system;

/**
 * Created by IntelliJ IDEA.
 * User: par
 * Date: 2012-03-11
 * Time: 18:12
 * To change this template use File | Settings | File Templates.
 */
public class VRAMimpl implements VRAM {
    private int[] mem = new int[4096*2];

    public void writeByte(int address, int b) {
     //   System.err.println("write VRAM(" + Utils.wordIntToHexString(address) + ", " + Utils.byteIntToHexString(b)+")");
        mem[address - 0x8000] = b;
    }

    public int readByte(int address) {
   //     System.err.println("read VRAM(" + Utils.wordIntToHexString(address) + ") -> " + Utils.byteIntToHexString(mem[address - 0x8000]));
        return mem[address - 0x8000];
    }
}
