package system;

import cpu.LR35902StateBased;
import game.MBC;
import game.Rom;
import game.Tetris;

/**
 * Created by IntelliJ IDEA.
 * User: par
 * Date: 2012-02-04
 * Time: 14:25
 */
public class SystemTester {

    public static class LoggingIoPorts implements IoPorts {

        private final LCDController lcdController;

        public LoggingIoPorts(LCDController lcdController) {

            this.lcdController = lcdController;
        }

        public void writeByte(int adress, int b) {
            System.err.println("writeByte(" + Utils.wordIntToHexString(adress) + " , " + Utils.byteIntToHexString(b) + ")");
            lcdController.writeByte(adress, b);
        }

        public int readByte(int adress) {
            System.err.println("readByte(" + Utils.wordIntToHexString(adress) + ")");
            return lcdController.readByte(adress);
        }

    }
    
    public static void main(String[] args) {

        Rom rom = new Rom(Tetris.getRomContent());
        MBC mbc = rom.createMBC();

        WRAMimpl workRam = new WRAMimpl();
        HRAMimpl hram = new HRAMimpl();
        MemoryController memoryController = new MemoryController(mbc, workRam, hram, null, new LoggingIoPorts(null));
        LR35902StateBased cpu = new LR35902StateBased(memoryController);

        System.err.println("Starting");
        cpu.startup();
        for(int i = 0 ; i < 1000 ; i++) {
            System.err.println("pc=" + Utils.wordIntToHexString(cpu.getPc()));
            cpu.step();
        }

    }

}
