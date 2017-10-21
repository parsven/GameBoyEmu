package cpu;

import java.util.Set;

public interface Cpu {

	int step();

	int executeScanline(int rest, Set<Integer> breakPoints) throws BreakPointException;
}
