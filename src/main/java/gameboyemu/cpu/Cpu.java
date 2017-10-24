package gameboyemu.cpu;

import gameboyemu.cpu.BreakPointException;

import java.util.Set;

public interface Cpu {

	int step();

	int executeScanline(int rest, Set<Integer> breakPoints) throws BreakPointException;
}
