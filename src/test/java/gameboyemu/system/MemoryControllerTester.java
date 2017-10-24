package gameboyemu.system;

import gameboyemu.game.MBC;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class MemoryControllerTester {

	@Test public void testThatMemoryControllerMapsToMBC() {
		MBC mbcMock = mock(MBC.class);
		WRAM ramMock = mock(WRAM.class);
		HRAM hramMock = mock(HRAM.class);
		IoPorts ioMock = mock(IoPorts.class);
        VRAM videoRam = mock(VRAM.class);

		MemoryController mc = new MemoryController(mbcMock, ramMock, hramMock, videoRam, ioMock);

		mc.readByte(0x7fff);
		mc.readByte(0x8000);

		mc.readWord(0x0000);
		mc.readWord(0x8000);

		mc.writeByte(0x0100, 42);
		mc.writeByte(0xf100, 42);

		mc.writeWord(0x0100, 42);
		mc.writeWord(0xf100, 42);

		verify(mbcMock).readByte(0x7fff);
		verify(mbcMock, never()).readByte(0x8000);

		verify(mbcMock).readByte(0x0000);
		verify(mbcMock).readByte(0x0001);
		verify(mbcMock, never()).readByte(0x8000);

		verify(mbcMock, times(2)).writeByte(0x0100, 42);
		verify(mbcMock).writeByte(0x0101, 0);
		verify(mbcMock, never()).writeByte(0xf100, 42);

	}

	@Test public void testThatMemoryControllerMapsToWorkRam() {
		MBC mbcMock = mock(MBC.class);
		WRAM ramMock = mock(WRAM.class);
		HRAM hramMock = mock(HRAM.class);
		IoPorts ioMock = mock(IoPorts.class);
        VRAM videoRam = mock(VRAM.class);
        MemoryController mc = new MemoryController(mbcMock, ramMock, hramMock, videoRam, ioMock);

		mc.readByte(0xdfff);
		mc.readByte(0xb000);

		mc.readWord(0xc000);
		mc.readWord(0x8000);

		mc.writeByte(0xc100, 42);
		mc.writeByte(0xfff0, 42);

		mc.writeWord(0xc100, 42);
		mc.writeWord(0xfff0, 42);

		verify(ramMock).readByte(0xdfff);
		verify(ramMock, never()).readByte(0xb000);

		verify(ramMock).readByte(0xc000);
		verify(ramMock).readByte(0xc001);
		verify(ramMock, never()).readByte(0x8000);

		verify(ramMock, never()).writeByte(0xfff0, 42);

		verify(ramMock, times(2)).writeByte(0xc100, 42);
		verify(ramMock).writeByte(0xc101, 0);
		verify(ramMock, never()).writeByte(0xfff0, 42);
	}

	@Test public void testMemoryControllerWorkRamMirroring() {
		MBC mbcMock = mock(MBC.class);
		WRAM ramMock = mock(WRAM.class);
		HRAM hramMock = mock(HRAM.class);
		IoPorts ioMock = mock(IoPorts.class);
        VRAM videoRam = mock(VRAM.class);
        MemoryController mc = new MemoryController(mbcMock, ramMock, hramMock, videoRam,  ioMock);

		mc.readByte(0xffff);
		mc.readByte(0xb000);

		mc.readWord(0xe000);
		mc.readWord(0x8000);

		mc.writeByte(0xe100, 42);
		mc.writeByte(0xfff0, 42);

		mc.writeWord(0xe100, 42);
		mc.writeWord(0xfff0, 42);

		verify(ramMock, never()).readByte(0xdfff);
		verify(ramMock, never()).readByte(0xb000);

		verify(ramMock).readByte(0xc000);
		verify(ramMock).readByte(0xc001);
		verify(ramMock, never()).readByte(0x8000);

		verify(ramMock, times(2)).writeByte(0xc100, 42);
		verify(ramMock, never()).writeByte(0xfff0, 42);

		verify(ramMock).writeByte(0xc101, 0);
		verify(ramMock, never()).writeByte(0xfff0, 42);
	}

	@Test public void testMemoryControllerMapHRAM() {
		MBC mbcMock = mock(MBC.class);
		WRAM ramMock = mock(WRAM.class);
		HRAM hramMock = mock(HRAM.class);
		IoPorts ioMock = mock(IoPorts.class);
		MemoryController mc = new MemoryController(mbcMock, ramMock, hramMock,null,  ioMock);

		mc.readByte(0xff80);
		mc.readByte(0xff00);

		mc.readWord(0xff80);
		mc.readWord(0xff00);

		mc.writeByte(0xfffe, 42);
		mc.writeByte(0xffff, 42);

		mc.writeWord(0xff80, 42);
		mc.writeWord(0xfff0, 42);

		verify(hramMock, times(2)).readByte(0xff80);
		verify(hramMock, never()).readByte(0xff00);

		verify(hramMock).readByte(0xff81);
		verify(hramMock, never()).readByte(0xff00);

		verify(hramMock).writeByte(0xfffe, 42);
		verify(hramMock, never()).writeByte(0xffff, 42);

		verify(hramMock).writeByte(0xff80, 42);
		verify(hramMock).writeByte(0xff81, 0);
		verify(hramMock).writeByte(0xfff0, 42);
		verify(hramMock).writeByte(0xfff1, 0);
	}


	@Test public void testMemoryControllerMapsIoPorts() {
		IoPorts ioMock = mock(IoPorts.class);
		MemoryController mc = new MemoryController(null, null, null, null, ioMock);

        mc.readByte(0xff00);
        mc.readByte(0xff7f);
        mc.readWord(0xff02);

        mc.writeByte(0xff00, 0x42);
        mc.writeByte(0xff7f, 0x43);
	    mc.writeWord(0xff02, 0x1234);
        
        verify(ioMock).readByte(0xff00);
        verify(ioMock).readByte(0xff7f);
        verify(ioMock).readByte(0xff02);
		verify(ioMock).readByte(0xff03);

        verify(ioMock).writeByte(0xff00, 0x42);
        verify(ioMock).writeByte(0xff7f, 0x43);
        verify(ioMock).writeByte(0xff02, 0x34);
		verify(ioMock).writeByte(0xff03, 0x12);

    }
}
