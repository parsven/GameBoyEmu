package cpu;

import org.junit.Test;

import java.util.Collections;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: noy
 * Date: 1/5/12
 * Time: 10:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestCpuExecutionTiming {

	@Test
	public void testThatTheRightNumberOfNOPsAreExecutedDuringAScanLine() throws BreakPointException {
		int expectedNoOfNopsExecuted = 456 / 4;
		SimpleMemory mem = new SimpleMemory();
		LR35902StateBased cpu = new LR35902StateBased(mem);
		cpu.setPC(0);
		int rest = cpu.executeScanline(0, Collections.emptySet());
		assertEquals(0, rest);
		assertEquals(expectedNoOfNopsExecuted, cpu.getPc());
	}

	@Test
	public void testThatRestCyclesAreReturnedDuringAScanLine() throws BreakPointException {
		int expectedNoOfNopsExecuted = 456 / 4;
		SimpleMemory mem = new SimpleMemory();
		LR35902StateBased cpu = new LR35902StateBased(mem);
		mem.writeByte(expectedNoOfNopsExecuted - 1,0xc5);
		cpu.setPC(0);
		int rest = cpu.executeScanline(0, Collections.emptySet());
		assertEquals(12, rest);
		assertEquals(expectedNoOfNopsExecuted, cpu.getPc());
	}

	@Test
	public void testThatCarryCyclesAreConsideredDuringAScanLine() throws BreakPointException {
		int expectedNoOfNopsExecuted = 456 / 4;
		SimpleMemory mem = new SimpleMemory();
		LR35902StateBased cpu = new LR35902StateBased(mem);
		mem.writeByte(expectedNoOfNopsExecuted - 2,0xc5);
		cpu.setPC(0);
		int rest = cpu.executeScanline(0, Collections.emptySet());
		assertEquals(8, rest);
		assertEquals(expectedNoOfNopsExecuted -1, cpu.getPc());
		cpu.setPC(0);

		int rest2 = cpu.executeScanline(4, Collections.emptySet());
		assertEquals(expectedNoOfNopsExecuted -1, cpu.getPc());
		assertEquals(12, rest2);
	}

}
