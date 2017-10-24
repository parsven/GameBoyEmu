package gameboyemu.game;


public class Rom {
    private byte[] romContent;

    public Rom(byte[] romContent) {
        this.romContent = romContent;
    }

    public boolean verifyLogoCorrect() {
        byte[] logo = new byte[]{
                (byte)0xCE, (byte)0xED, (byte)0x66, (byte)0x66, (byte)0xCC, (byte)0x0D, (byte)0x00, (byte)0x0B,
                (byte)0x03, (byte)0x73, (byte)0x00, (byte)0x83, (byte)0x00, (byte)0x0C, (byte)0x00, (byte)0x0D,
                (byte)0x00, (byte)0x08, (byte)0x11, (byte)0x1F, (byte)0x88, (byte)0x89, (byte)0x00, (byte)0x0E,
                (byte)0xDC, (byte)0xCC, (byte)0x6E, (byte)0xE6, (byte)0xDD, (byte)0xDD, (byte)0xD9, (byte)0x99,
                (byte)0xBB, (byte)0xBB, (byte)0x67, (byte)0x63, (byte)0x6E, (byte)0x0E, (byte)0xEC, (byte)0xCC,
                (byte)0xDD, (byte)0xDC, (byte)0x99, (byte)0x9F, (byte)0xBB, (byte)0xB9, (byte)0x33, (byte)0x3E};

        for(int i = 0 ;  i < 0x18 ; i++) {
            if(romContent[0x104 + i] != logo[i]) {
                return false;
            }
        }
        return true;
    }

	public byte getCartridgeType() {
		return romContent[0x147];
	}

	public int getRomSizekB() {
		return 32 << romContent[0x148];
	}

	public int getRomBanks() {
		return 2 << romContent[0x148];

	}

	public int getRamSizekB() {
		return 0;
	}

	public MBC createMBC() {
		return new MBC0(romContent);
	}
}
