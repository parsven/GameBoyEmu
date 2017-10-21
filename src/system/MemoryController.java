package system;

import cpu.VirtualMemory;
import cpu.VirtualMemory16;
import game.MBC;

public class MemoryController implements VirtualMemory16 {
	private final MBC mbc;
	private final WRAM workRam;
	private final HRAM hiRam;
    private final VRAM videoRam;
    private final IoPorts ioPorts;
    private int interruptReg;

	public MemoryController(MBC mbc, WRAM workRam, HRAM hram, VRAM videoRam, IoPorts ioPorts) {
		this.mbc = mbc;
		this.workRam = workRam;
		hiRam = hram;
        this.videoRam = videoRam;
        this.ioPorts = ioPorts;
    }

	public void writeByte(int address, int value) {
		if((address & 0x8000) == 0) {
			mbc.writeByte(address, value);
		} else if(address == 0xffff) {
			interruptReg = value;
		} else if(address >= 0xff80) {
			hiRam.writeByte(address, value);
        }else if(address >= 0xff00) {
            ioPorts.writeByte(address, value);
		} else if((address & 0x4000) != 0){
			workRam.writeByte(address & 0xdfff, value);
	 	} else {
            videoRam.writeByte(address, value);
		}
	}

	public int readByte(int address) {
		if((address & 0x8000) == 0) {
			return mbc.readByte(address);
		} else if(address == 0xffff) {
			return interruptReg;
		} else if(address >= 0xff80) {
			return hiRam.readByte(address);
        }else if(address >= 0xff00) {
            return ioPorts.readByte(address);
		} else if((address & 0x4000) != 0){
			return workRam.readByte(address & 0xdfff);
		} else {
			return videoRam.readByte(address);
		}
	}

	static void writeWord(VirtualMemory vm, int address, int value) {
		int loByte = value & 0xff;
		int hiByte = (value & 0xff00) >> 8;
		vm.writeByte(address, loByte);
		vm.writeByte(address + 1, hiByte);
	}

	static int readWord(VirtualMemory vm, int address) {
		int loByte = vm.readByte(address) & 0xff;
		int hiByte = vm.readByte(address + 1) & 0xff;
		return (hiByte<<8) | loByte;

	}

	public void writeWord(int address, int value) {
		if((address & 0x8000) == 0) {
			writeWord(mbc, address, value);
		} else if(address >= 0xff80) {
			writeWord(hiRam, address, value);
        }else if(address >= 0xff00) {
            writeWord(ioPorts, address, value);
		} else if((address & 0x4000) != 0){
			writeWord(workRam, address & 0xdfff, value);
		} else {
		}
	}

	public int readWord(int address) {
		if((address & 0x8000) == 0) {
			return readWord(mbc, address);
		} else if(address >= 0xff80) {
			return readWord(hiRam, address);
        }else if(address >= 0xff00) {
            return readWord(ioPorts, address);
		} else if((address & 0x4000) != 0){
			return readWord(workRam, address & 0xdfff);
		} else {
			return 0;
		}
	}
}
