package gameboyemu.system;


import gameboyemu.cpu.BreakPointException;
import gameboyemu.cpu.Cpu;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: par
 * Date: 2012-02-23
 * Time: 21:21
 */
public class GBSystem {

    private static final int NOOFSCANLINES = 154;

    private final MemoryController memoryController;
    private final Cpu cpu;
    private final LCDController lcdController;

    public GBSystem(MemoryController memoryController, Cpu cpu, LCDController lcdController) {
        this.memoryController = memoryController;
        this.cpu = cpu;
        this.lcdController = lcdController;
    }

    public int executeFrame(int offset, int[] backbuffer, Set<Integer> breakPoints) throws BreakPointException {
        lcdController.resetScanLine();
        for(int i = 0 ; i < NOOFSCANLINES ; i++) {
            lcdController.advanceScanLine();
            offset = cpu.executeScanline(offset, breakPoints);
            if(i < 144) {
                lcdController.renderLine(backbuffer, i * 160 *3);
            }
        }
        return offset;
    }




}
