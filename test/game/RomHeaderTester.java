package game;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class RomHeaderTester {




    @Test
    public void parseHeader() {


    }

    @Test public void testThatNintendoLogoIsVerified() throws IOException {

	    byte[] romContent = Tetris.getRomContent();
	    Rom rom = new Rom(romContent);
        assertTrue(rom.verifyLogoCorrect());
        romContent[0x109] = (byte) 0xaa;
        assertFalse(rom.verifyLogoCorrect());
	}
	
	@Test public void testThatTetrisIsCartridgeType0x00() {
		Rom rom = new Rom(Tetris.getRomContent());
		assertEquals((byte)0x00, rom.getCartridgeType());
	}

	@Test public void testThatTetrisHasRomSize32kB() {
		Rom rom = new Rom(Tetris.getRomContent());
		assertEquals(32, rom.getRomSizekB());
	}

	@Test public void testThatTetrisHas2RomBanks() {
		Rom rom = new Rom(Tetris.getRomContent());
		assertEquals(2, rom.getRomBanks());
	}

	@Test public void testThatTetrisHas0Ram() {
		Rom rom = new Rom(Tetris.getRomContent());
		assertEquals(0, rom.getRamSizekB());
	}

	@Test public void testThatTetrisHasMBC0() {
		Rom rom = new Rom(Tetris.getRomContent());
		assertTrue(rom.createMBC() instanceof MBC0);
	}
}

/*
Opening /home/noy/IdeaProjects/GameBoyEmu/TETRIS.GB...reading header...OK
  Name:		TETRIS (ROM ONLY)
  ROM Size:	2x16kB
  RAM Size:	0 bytes
  Manufacturer:	01h (Nintendo)
  Checksum:	CRC=16BFh/CMP=0Ah
  Start Addr:	0150h
  Version:	01h
  Japanese:	YES
  SuperGameBoy:	NO
  ColorGameBoy:	NO
  Battery:	NO
  Clock/Timer:	NO
  RumblePack:	NO
  TiltSensor:	NO
Loading /home/noy/IdeaProjects/GameBoyEmu/TETRIS.GB...OK
Initializing hardware and CPU:
  456 CPU cycles per HBlank
  70224 CPU cycles per VBlank
  154 scanlines
RUNNING ROM CODE...
XIO:  fatal IO error 11 (Resource temporarily unavailable) on X server ":0"
      after 1423 requests (1422 known processed) with 0 events remaining.
noy@noy-901-lubuntu:/tmp$





*/