package game;

import system.Utils;

public class MBC0 implements MBC {
	private final byte[] romContent;

	public MBC0(byte[] romContent) {
		this.romContent = romContent;
        System.err.println("MBC0: Settings romcontent, size=" + Utils.wordIntToHexString(romContent.length));
	}

	public int readByte(int address) {
        int i = ((int) romContent[address]) & 0xff;
    //    System.err.println("MBC0: readByte(" + Utils.wordIntToHexString(address) + ") -> " + Utils.byteIntToHexString(i));

        return i;
	}


	public void writeByte(int address, int value) {
	}

	public void writeWord(int address, int value) {
	}


	public int readWord(int address) {
		int loByte = ((int)romContent[address]) & 0xff;
		int hiByte = ((int)romContent[address + 1]) & 0xff;
		return (hiByte<<8) | loByte;
	}
}
