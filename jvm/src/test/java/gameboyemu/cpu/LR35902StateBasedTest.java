package gameboyemu.cpu;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: par
 * Date: 2011-08-10
 * Time: 21:55
 * To change this template use File | Settings | File Templates.
 */
public class LR35902StateBasedTest {

    private SimpleMemory mem;
    private LR35902StateBased cpu;

    @Before
    public void setUp() {
        mem = new SimpleMemory();
        cpu = new LR35902StateBased(mem);
        cpu.setPC(0);
    }

    @Test public void testThatCpuRemembersFlags() {
        cpu.setC(true);
        cpu.setN(true);
        cpu.setH(false);
        cpu.setZ(false);

        assertTrue(cpu.getC());
        assertTrue(cpu.getN());
        assertFalse(cpu.getH());
        assertFalse(cpu.getZ());

        cpu.setC(false);
        cpu.setN(false);
        cpu.setH(true);
        cpu.setZ(true);

        assertFalse(cpu.getC());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertTrue(cpu.getZ());

        cpu.setC(true);
        cpu.setN(false);
        cpu.setH(true);
        cpu.setZ(false);

        assertTrue(cpu.getC());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testThatNopIncreasePcWithOneAndTakesFourCycles() {
        mem.writeByte(0,0);
        cpu.setPC(0);
        int cycles = cpu.step();

        assertEquals(4, cycles);
        assertEquals(1, cpu.getPc());
    }

    @Test public void testThat8bitRegisterGettersAndSettersWork() {
        cpu.setRegB(42);
        cpu.setRegC(37);

        assertEquals(42, cpu.getRegB());
        assertEquals(37, cpu.getRegC());

        cpu.setRegD(15);
        cpu.setRegE(99);

        assertEquals(15, cpu.getRegD());
        assertEquals(99, cpu.getRegE());

        cpu.setRegH(73);
        cpu.setRegL(28);

        assertEquals(73, cpu.getRegH());
        assertEquals(28, cpu.getRegL());
     }

    private void StepCPU_AssertPcValueAndCycles(int expectedPc, int expectedCycles) {
        int cycles = cpu.step();
        assertEquals(expectedPc, cpu.getPc());
        assertEquals(expectedCycles, cycles);
    }

    @Test public void testOpCode0x01() {
        //LD BC, d16
        // 3     12
        // - - - -
        mem.init(0x01, 0x17, 0x01);
        StepCPU_AssertPcValueAndCycles(3, 12);
        assertEquals(0x0117, cpu.getRegBC());
    }

    @Test public void testOpCode0x02() {
        //LD (BC),A
        //1  8
        //- - - -
        mem.init(0x02);
        cpu.setRegBC(100);
        cpu.setRegA(42);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, mem.readByte(100));
    }

    @Test public void testOpCode0x03() {
        //INC BC
        //1  8
        //- - - -
        mem.init(0x03);
        cpu.setRegBC(100);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(101, cpu.getRegBC());
    }

    @Test public void testOpCode0x04_1() {
        //INC B
        //1  4
        //Z 0 H -
        mem.init(0x04);
        cpu.setRegB(0x0f);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x10, cpu.getRegB());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testOpCode0x04_2() {
        //INC B
        //1  4
        //Z 0 H -
        mem.init(0x04);
        cpu.setRegB(0xff);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x00, cpu.getRegB());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertTrue(cpu.getZ());
    }

    @Test public void testOpCode0x05_1() {
        //DEC B
        //1  4
        //Z 1 H -
        mem.init(0x05);
        cpu.setRegB(0x10);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x0f, cpu.getRegB());
        assertTrue(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testOpCode0x05_2() {
        //DEC B
        //1  4
        //Z 1 H -
        mem.init(0x05);
        cpu.setRegB(0x01);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x00, cpu.getRegB());
        assertTrue(cpu.getN());
        assertFalse(cpu.getH());
        assertTrue(cpu.getZ());
    }

    @Test public void testOpCode0x06() {
        //    LD B,d8
        // 2  8
        // - - - -
        mem.init(0x06, 0x17);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertEquals(0x17, cpu.getRegB());
    }

    @Test public void testOpCode0x07() {
        // RLCA
        // 1  4
        // 0 0 0 C
        mem.init(0x07, 0x07);
        cpu.setRegA(0x82);
        cpu.setC(false);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFalse(cpu.getZ());
        assertFalse(cpu.getN());
        assertFalse(cpu.getH());
        assertTrue(cpu.getC());
        assertEquals(0x04, cpu.getRegA());
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFalse(cpu.getC());
        assertEquals(0x09, cpu.getRegA());
    }

    @Test public void testOpCode0x08() {
        //LD (a16),SP
        //3  20
        //- - - -
        mem.init(0x08, 0x34, 0x12);
        cpu.setRegSP(0xbeef);
        StepCPU_AssertPcValueAndCycles(3, 20);
        assertEquals(0xbeef, mem.readWord(0x1234));
    }

    @Test public void testOpCode0x09() {
        //ADD HL,BC
        //1  8
        //- 0 H C
        mem.init(0x09, 0x09);
        cpu.setRegHL(0xff99);
        cpu.setRegBC(0x778a);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertFlagsAre_znHC();
        assertEquals(0x7723, cpu.getRegHL());
        cpu.setRegBC(0x000c);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertFlagsAre_znhc();
        assertEquals(0x772f, cpu.getRegHL());
    }

    @Test public void testOpCode0x0A() {
        //LD A,(BC)
        //1  8
        //- - - -
        mem.init(0x0A);
        cpu.setRegBC(100);
        mem.writeByte(100, 42);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, cpu.getRegA());
    }

    @Test public void testOpCode0x0B() {
        //DEC BC
        //1  8
        //- - - -
        mem.init(0x0B);
        cpu.setRegBC(100);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(99, cpu.getRegBC());
    }

    @Test public void testOpCode0x0C_1() {
        //INC C
        //1  4
        //Z 0 H -
        mem.init(0x0C);
        cpu.setRegC(0x0f);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x10, cpu.getRegC());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testOpCode0x0C_2() {
        //INC C
        //1  4
        //Z 0 H -
        mem.init(0x0C);
        cpu.setRegC(0xff);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x00, cpu.getRegC());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertTrue(cpu.getZ());
    }

    @Test public void testOpCode0x0D_1() {
        //DEC C
        //1  4
        //Z 1 H -
        mem.init(0x0D);
        cpu.setRegC(0x10);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x0f, cpu.getRegC());
        assertTrue(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testOpCode0x0D_2() {
        //DEC C
        //1  4
        //Z 1 H -
        mem.init(0x0D);
        cpu.setRegC(0x01);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x00, cpu.getRegC());
        assertTrue(cpu.getN());
        assertFalse(cpu.getH());
        assertTrue(cpu.getZ());
    }

    @Test public void testOpCode0x0E() {
        //    LD C,d8
        // 2  8
        // - - - -
        mem.init(0x0E, 0x17);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertEquals(0x17, cpu.getRegC());
    }

    @Test public void testOpCode0x0F() {
        // RRCA
        // 1  4
        // 0 0 0 C
        mem.init(0x0f, 0x0f);
        cpu.setRegA(0x81);
        cpu.setC(false);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFalse(cpu.getZ());
        assertFalse(cpu.getN());
        assertFalse(cpu.getH());
        assertTrue(cpu.getC());
        assertEquals(0x40, cpu.getRegA());
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFalse(cpu.getC());
        assertEquals(0xa0, cpu.getRegA());
    }

    @Test public void testOpCode0x10() {
        //STOP 0
        //2  4
        //- - - -
        mem.init(0x10);
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertTrue(cpu.isStopped());
    }


    @Test public void testOpCode0x11() {
        //LD DE, d16
        // 3     12
        // - - - -
        mem.init(0x11, 0x17, 0x01);
        StepCPU_AssertPcValueAndCycles(3, 12);
        assertEquals(0x0117, cpu.getRegDE());
    }

    @Test public void testOpCode0x12() {
        //LD (DE),A
        //1  8
        //- - - -
        mem.init(0x12);
        cpu.setRegDE(100);
        cpu.setRegA(42);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, mem.readByte(100));
    }

    @Test public void testOpCode0x13() {
        //INC DE
        //1  8
        //- - - -
        mem.init(0x13);
        cpu.setRegDE(100);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(101, cpu.getRegDE());
    }

    @Test public void testOpCode0x14_1() {
        //INC D
        //1  4
        //Z 0 H -
        mem.init(0x14);
        cpu.setRegD(0x0f);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x10, cpu.getRegD());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testOpCode0x14_2() {
        //INC D
        //1  4
        //Z 0 H -
        mem.init(0x14);
        cpu.setRegD(0xff);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x00, cpu.getRegD());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertTrue(cpu.getZ());
    }

    @Test public void testOpCode0x15_1() {
        //DEC D
        //1  4
        //Z 1 H -
        mem.init(0x15);
        cpu.setRegD(0x10);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x0f, cpu.getRegD());
        assertTrue(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testOpCode0x15_2() {
        //DEC D
        //1  4
        //Z 1 H -
        mem.init(0x15);
        cpu.setRegD(0x01);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x00, cpu.getRegD());
        assertTrue(cpu.getN());
        assertFalse(cpu.getH());
        assertTrue(cpu.getZ());
    }

    @Test public void testOpCode0x16() {
        // LD D,d8
        // 2  8
        // - - - -
        mem.init(0x16, 0x17);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertEquals(0x17, cpu.getRegD());
    }

    @Test public void testOpCode0x17() {
        // RLA
        // 1  4
        // 0 0 0 C
        mem.init(0x17, 0x17);
        cpu.setRegA(0x82);
        cpu.setC(false);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFalse(cpu.getZ());
        assertFalse(cpu.getN());
        assertFalse(cpu.getH());
        assertTrue(cpu.getC());
        assertEquals(0x04, cpu.getRegA());
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFalse(cpu.getC());
        assertEquals(0x08, cpu.getRegA());
    }

    @Test public void testOpCode0x18() {
        //JR r8
        //2  12
        //- - - -
        mem.init(0x18,0x00, 0x18, 0x02, 0x00, 0x00, 0x18, 0xf8);
        StepCPU_AssertPcValueAndCycles(2, 12);
        StepCPU_AssertPcValueAndCycles(6, 12);
        StepCPU_AssertPcValueAndCycles(0, 12);
    }


    @Test public void testOpCode0x19() {
        //ADD HL,DE
        //1  8
        //- 0 H C
        mem.init(0x19, 0x19);
        cpu.setRegHL(0xff99);
        cpu.setRegDE(0x778a);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertFlagsAre_znHC();
        assertEquals(0x7723, cpu.getRegHL());
        cpu.setRegDE(0x000c);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertFlagsAre_znhc();
        assertEquals(0x772f, cpu.getRegHL());
    }

    
    @Test public void testOpCode0x1A() {
        //LD A,(DE)
        //1  8
        //- - - -
        mem.init(0x1A);
        cpu.setRegDE(100);
        mem.writeByte(100, 42);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, cpu.getRegA());
    }

    @Test public void testOpCode0x1B() {
        //DEC DE
        //1  8
        //- - - -
        mem.init(0x1B);
        cpu.setRegDE(100);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(99, cpu.getRegDE());
    }

    @Test public void testOpCode0x1C_1() {
        //INC E
        //1  4
        //Z 0 H -
        mem.init(0x1C);
        cpu.setRegE(0x0f);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x10, cpu.getRegE());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testOpCode0x1C_2() {
        //INC E
        //1  4
        //Z 0 H -
        mem.init(0x1C);
        cpu.setRegE(0xff);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x00, cpu.getRegE());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertTrue(cpu.getZ());
    }

    @Test public void testOpCode0x1D_1() {
        //DEC E
        //1  4
        //Z 1 H -
        mem.init(0x1D);
        cpu.setRegE(0x10);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x0f, cpu.getRegE());
        assertTrue(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testOpCode0x1D_2() {
        //DEC E
        //1  4
        //Z 1 H -
        mem.init(0x1D);
        cpu.setRegE(0x01);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x00, cpu.getRegE());
        assertTrue(cpu.getN());
        assertFalse(cpu.getH());
        assertTrue(cpu.getZ());
    }

    @Test public void testOpCode0x1E() {
        //    LD E,d8
        // 2  8
        // - - - -
        mem.init(0x1E, 0x17);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertEquals(0x17, cpu.getRegE());
    }

    @Test public void testOpCode0x1F() {
        // RRA
        // 1  4
        // 0 0 0 C
        mem.init(0x1f, 0x1f);
        cpu.setRegA(0x81);
        cpu.setC(false);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFalse(cpu.getZ());
        assertFalse(cpu.getN());
        assertFalse(cpu.getH());
        assertTrue(cpu.getC());
        assertEquals(0x40, cpu.getRegA());
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFalse(cpu.getC());
        assertEquals(0x20, cpu.getRegA());
    }

    @Test public void testOpCode0x20() {
        //JR NZ,r8
        //2  12/8
        //- - - -
        testConditionalR8Jump(0x20, new SourceFlagSetter() {
            public void setSourceFlag(boolean v) {
                cpu.setZ(!v);
            }
        });
    }

    private void testConditionalR8Jump(int opCode, SourceFlagSetter sfs) {

        mem.init(opCode,0x00, opCode, 0x02, 0x00, 0x00, opCode, 0xf8);
        sfs.setSourceFlag(true);
        StepCPU_AssertPcValueAndCycles(2, 12);
        StepCPU_AssertPcValueAndCycles(6, 12);
        StepCPU_AssertPcValueAndCycles(0, 12);
        StepCPU_AssertPcValueAndCycles(2, 12);
        sfs.setSourceFlag(false);
        StepCPU_AssertPcValueAndCycles(4, 8);
    }

    @Test public void testOpCode0x21() {
        //LD HL, d16
        // 3     12
        // - - - -
        mem.init(0x21, 0x17, 0x01);
        StepCPU_AssertPcValueAndCycles(3, 12);
        assertEquals(0x0117, cpu.getRegHL());
    }

    @Test public void testOpCode0x22() {
        //LD (HL+),A
        //1  8
        //- - - -
        mem.init(0x22);
        cpu.setRegHL(100);
        cpu.setRegA(42);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, mem.readByte(100));
        assertEquals(101, cpu.getRegHL());
    }

    @Test public void testOpCode0x23() {
        //INC HL
        //1  8
        //- - - -
        mem.init(0x23);
        cpu.setRegHL(100);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(101, cpu.getRegHL());
    }

    @Test public void testOpCode0x24_1() {
        //INC H
        //1  4
        //Z 0 H -
        mem.init(0x24);
        cpu.setRegH(0x0f);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x10, cpu.getRegH());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testOpCode0x24_2() {
        //INC H
        //1  4
        //Z 0 H -
        mem.init(0x24);
        cpu.setRegH(0xff);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x00, cpu.getRegH());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertTrue(cpu.getZ());
    }

    @Test public void testOpCode0x25_1() {
        //DEC H
        //1  4
        //Z 1 H -
        mem.init(0x25);
        cpu.setRegH(0x10);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x0f, cpu.getRegH());
        assertTrue(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testOpCode0x25_2() {
        //DEC H
        //1  4
        //Z 1 H -
        mem.init(0x25);
        cpu.setRegH(0x01);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x00, cpu.getRegH());
        assertTrue(cpu.getN());
        assertFalse(cpu.getH());
        assertTrue(cpu.getZ());
    }

    @Test public void testOpCode0x26() {
        //    LD H,d8
        // 2  8
        // - - - -
        mem.init(0x26, 0x17);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertEquals(0x17, cpu.getRegH());
    }

    @Test public void testOpCode0x28() {
        //JR Z,r8
        //2  12/8
        //- - - -
        testConditionalR8Jump(0x28, new SourceFlagSetter() {
            public void setSourceFlag(boolean v) {
                cpu.setZ(v);
            }
        });
    }

    @Test public void testOpCode0x29() {
        //ADD HL,HL
        //1  8
        //- 0 H C
        mem.init(0x29, 0x29);
        cpu.setRegHL(0xff99);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertFlagsAre_znHC();
        assertEquals(0xff32, cpu.getRegHL());
        cpu.setRegHL(0x0007);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertFlagsAre_znhc();
        assertEquals(0x000e, cpu.getRegHL());
    }

    @Test public void testOpCode0x2A() {
        //LD A,(HL+)
        //1  8
        //- - - -
        mem.init(0x2A);
        cpu.setRegHL(100);
        mem.writeByte(100, 42);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, cpu.getRegA());
        assertEquals(101, cpu.getRegHL());
    }

    @Test public void testOpCode0x2B() {
        //DEC HL
        //1  8
        //- - - -
        mem.init(0x2B);
        cpu.setRegHL(100);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(99, cpu.getRegHL());
    }

    @Test public void testOpCode0x2C_1() {
        //INC L
        //1  4
        //Z 0 H -
        mem.init(0x2C);
        cpu.setRegL(0x0f);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x10, cpu.getRegL());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testOpCode0x2C_2() {
        //INC L
        //1  4
        //Z 0 H -
        mem.init(0x2C);
        cpu.setRegL(0xff);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x00, cpu.getRegL());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertTrue(cpu.getZ());
    }

    @Test public void testOpCode0x2D_1() {
        //DEC L
        //1  4
        //Z 1 H -
        mem.init(0x2D);
        cpu.setRegL(0x10);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x0f, cpu.getRegL());
        assertTrue(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testOpCode0x2D_2() {
        //DEC L
        //1  4
        //Z 1 H -
        mem.init(0x2D);
        cpu.setRegL(0x01);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x00, cpu.getRegL());
        assertTrue(cpu.getN());
        assertFalse(cpu.getH());
        assertTrue(cpu.getZ());
    }

    @Test public void testOpCode0x2E() {
        //    LD L,d8
        // 2  8
        // - - - -
        mem.init(0x2E, 0x17);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertEquals(0x17, cpu.getRegL());
    }

    @Test public void testOpCode0x2F() {
        //CPL
        //1  4
        //- 1 1 -
        mem.init(0x2f);
        cpu.setRegA(0x42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertTrue(cpu.getN());
        assertTrue(cpu.getH());
        assertEquals(0xbd, cpu.getRegA());
    }

    @Test public void testOpCode0x30() {
        //JR NC,r8
        //2  12/8
        //- - - -
        testConditionalR8Jump(0x30, new SourceFlagSetter() {
            public void setSourceFlag(boolean v) {
                cpu.setC(!v);
            }
        });
    }

    @Test public void testOpCode0x31() {
        //LD SP, d16
        // 3     12
        // - - - -
        mem.init(0x31, 0x17, 0x01);
        StepCPU_AssertPcValueAndCycles(3, 12);
        assertEquals(0x0117, cpu.getRegSP());
    }

    @Test public void testOpCode0x32() {
        //LD (HL-),A
        //1  8
        //- - - -
        mem.init(0x32);
        cpu.setRegHL(100);
        cpu.setRegA(42);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, mem.readByte(100));
        assertEquals(99, cpu.getRegHL());
    }

    @Test public void testOpCode0x33() {
        //INC SP
        //1  8
        //- - - -
        mem.init(0x33);
        cpu.setRegSP(100);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(101, cpu.getRegSP());
    }

    @Test public void testOpCode0x34_1() {
        //INC (HL)
        //1  12
        //Z 0 H -
        mem.init(0x34);
        cpu.setRegHL(100);
        mem.writeByte(100,0x0f);
        StepCPU_AssertPcValueAndCycles(1, 12);
        assertEquals(0x10, mem.readByte(100));
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testOpCode0x34_2() {
        //INC (HL)
        //1  12
        //Z 0 H -
        mem.init(0x34);
        cpu.setRegHL(100);
        mem.writeByte(100, 0xff);
        StepCPU_AssertPcValueAndCycles(1, 12);
        assertEquals(0x00, mem.readByte(100));
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertTrue(cpu.getZ());
    }

    @Test public void testOpCode0x35_1() {
        //DEC (HL)
        //1  12
        //Z 1 H -
        mem.init(0x35);
        cpu.setRegHL(100);
        mem.writeByte(100,0x10);
        StepCPU_AssertPcValueAndCycles(1, 12);
        assertEquals(0x0f, mem.readByte(100));
        assertTrue(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testOpCode0x35_2() {
        //DEC (HL)
        //1  12
        //Z 1 H -
        mem.init(0x35);
        cpu.setRegHL(100);
        mem.writeByte(100,0x01);
        StepCPU_AssertPcValueAndCycles(1, 12);
        assertEquals(0x00, mem.readByte(100));
        assertTrue(cpu.getN());
        assertFalse(cpu.getH());
        assertTrue(cpu.getZ());
    }

    @Test public void testOpCode0x36() {
        //    LD (HL),d8
        // 2  12
        // - - - -
        mem.init(0x36, 0x17);
        cpu.setRegHL(1234);
        StepCPU_AssertPcValueAndCycles(2, 12);
        assertEquals(0x17, mem.readByte(1234));
    }

    @Test public void testOpCode0x37() {
        //SCF
        //1  4
        //- 0 0 1
        mem.init(0x37);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFalse(cpu.getN());
        assertFalse(cpu.getH());
        assertTrue(cpu.getC());
    }

    @Test public void testOpCode0x38() {
        //JR C,r8
        //2  12/8
        //- - - -
        testConditionalR8Jump(0x38, new SourceFlagSetter() {
            public void setSourceFlag(boolean v) {
                cpu.setC(v);
            }
        });
    }

    @Test public void testOpCode0x39() {
        //ADD HL,SP
        //1  8
        //- 0 H C
        mem.init(0x39, 0x39);
        cpu.setRegHL(0xff99);
        cpu.setRegSP(0x778a);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertFlagsAre_znHC();
        assertEquals(0x7723, cpu.getRegHL());
        cpu.setRegSP(0x000c);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertFlagsAre_znhc();
        assertEquals(0x772f, cpu.getRegHL());
    }

    @Test public void testOpCode0x3A() {
        //LD A,(HL-)
        //1  8
        //- - - -
        mem.init(0x3A);
        cpu.setRegHL(100);
        mem.writeByte(100, 42);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, cpu.getRegA());
        assertEquals(99, cpu.getRegHL());
    }

    @Test public void testOpCode0x3B() {
        //DEC SP
        //1  8
        //- - - -
        mem.init(0x3B);
        cpu.setRegSP(100);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(99, cpu.getRegSP());
    }
    
    @Test public void testOpCode0x3C_1() {
        //INC A
        //1  4
        //Z 0 H -
        mem.init(0x3C);
        cpu.setRegA(0x0f);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x10, cpu.getRegA());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testOpCode0x3C_2() {
        //INC A
        //1  4
        //Z 0 H -
        mem.init(0x3C);
        cpu.setRegA(0xff);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x00, cpu.getRegA());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertTrue(cpu.getZ());
    }

    @Test public void testOpCode0x3D_1() {
        //DEC A
        //1  4
        //Z 1 H -
        mem.init(0x3D);
        cpu.setRegA(0x10);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x0f, cpu.getRegA());
        assertTrue(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getZ());
    }

    @Test public void testOpCode0x3D_2() {
        //DEC A
        //1  4
        //Z 1 H -
        mem.init(0x3D);
        cpu.setRegA(0x01);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x00, cpu.getRegA());
        assertTrue(cpu.getN());
        assertFalse(cpu.getH());
        assertTrue(cpu.getZ());
    }

    @Test public void testOpCode0x3E() {
        //    LD A,d8
        // 2  8
        // - - - -
        mem.init(0x3E, 0x17);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertEquals(0x17, cpu.getRegA());
    }

    @Test public void testOpCode0x3F() {
        //CCF
        //1  4
        //- 0 0 C
        mem.init(0x3f, 0x3f);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFalse(cpu.getN());
        assertFalse(cpu.getH());
        assertTrue(cpu.getC());
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFalse(cpu.getC());
    }

    @Test public void testOpCode0x40() {
        //LD B,B
        //1  4
        //- - - -
        mem.init(0x40);
        cpu.setRegB(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegB());
    }

    @Test public void testOpCode0x41() {
        //LD B,C
        //1  4
        //- - - -
        mem.init(0x41);
        cpu.setRegC(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegB());
    }

    @Test public void testOpCode0x42() {
        //LD B,D
        //1  4
        //- - - -
        mem.init(0x42);
        cpu.setRegD(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegB());
    }

    @Test public void testOpCode0x43() {
        //LD B,E
        //1  4
        //- - - -
        mem.init(0x43);
        cpu.setRegE(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegB());
    }

    @Test public void testOpCode0x44() {
        //LD B,H
        //1  4
        //- - - -
        mem.init(0x44);
        cpu.setRegH(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegB());
    }

    @Test public void testOpCode0x45() {
        //LD B,L
        //1  4
        //- - - -
        mem.init(0x45);
        cpu.setRegL(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegB());
    }

    @Test public void testOpCode0x46() {
        //LD B,(HL)
        //1  8
        //- - - -
        mem.init(0x46);
        cpu.setRegHL(100);
        mem.writeByte(100, 42);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, cpu.getRegB());
    }

    @Test public void testOpCode0x47() {
        //LD B,A
        //1  4
        //- - - -
        mem.init(0x47);
        cpu.setRegA(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegB());
    }

    @Test public void testOpCode0x48() {
        //LD C,B
        //1  4
        //- - - -
        mem.init(0x48);
        cpu.setRegB(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegC());
    }

    @Test public void testOpCode0x49() {
        //LD C,C
        //1  4
        //- - - -
        mem.init(0x49);
        cpu.setRegC(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegC());
    }

    @Test public void testOpCode0x4A() {
        //LD C,D
        //1  4
        //- - - -
        mem.init(0x4A);
        cpu.setRegD(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegC());
    }

    @Test public void testOpCode0x4B() {
        //LD C,E
        //1  4
        //- - - -
        mem.init(0x4B);
        cpu.setRegE(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegC());
    }

    @Test public void testOpCode0x4C() {
        //LD C,H
        //1  4
        //- - - -
        mem.init(0x4C);
        cpu.setRegH(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegC());
    }

    @Test public void testOpCode0x4D() {
        //LD C,L
        //1  4
        //- - - -
        mem.init(0x4D);
        cpu.setRegL(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegC());
    }

    @Test public void testOpCode0x4E() {
        //LD C,(HL)
        //1  8
        //- - - -
        mem.init(0x4E);
        cpu.setRegHL(100);
        mem.writeByte(100, 42);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, cpu.getRegC());
    }

    @Test public void testOpCode0x4F() {
        //LD C,A
        //1  4
        //- - - -
        mem.init(0x4F);
        cpu.setRegA(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegC());
    }

    @Test public void testOpCode0x50() {
        //LD D,B
        //1  4
        //- - - -
        mem.init(0x50);
        cpu.setRegB(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegD());
    }

    @Test public void testOpCode0x51() {
        //LD D,C
        //1  4
        //- - - -
        mem.init(0x51);
        cpu.setRegC(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegD());
    }

    @Test public void testOpCode0x52() {
        //LD D,D
        //1  4
        //- - - -
        mem.init(0x52);
        cpu.setRegD(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegD());
    }

    @Test public void testOpCode0x53() {
        //LD D,E
        //1  4
        //- - - -
        mem.init(0x53);
        cpu.setRegE(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegD());
    }

    @Test public void testOpCode0x54() {
        //LD D,H
        //1  4
        //- - - -
        mem.init(0x54);
        cpu.setRegH(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegD());
    }

    @Test public void testOpCode0x55() {
        //LD D,L
        //1  4
        //- - - -
        mem.init(0x55);
        cpu.setRegL(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegD());
    }

    @Test public void testOpCode0x56() {
        //LD D,(HL)
        //1  8
        //- - - -
        mem.init(0x56);
        cpu.setRegHL(100);
        mem.writeByte(100, 42);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, cpu.getRegD());
    }

    @Test public void testOpCode0x57() {
        //LD D,A
        //1  4
        //- - - -
        mem.init(0x57);
        cpu.setRegA(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegD());
    }

    @Test public void testOpCode0x58() {
        //LD E,B
        //1  4
        //- - - -
        mem.init(0x58);
        cpu.setRegB(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegE());
    }

    @Test public void testOpCode0x59() {
        //LD E,C
        //1  4
        //- - - -
        mem.init(0x59);
        cpu.setRegC(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegE());
    }

    @Test public void testOpCode0x5A() {
        //LD E,D
        //1  4
        //- - - -
        mem.init(0x5A);
        cpu.setRegD(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegE());
    }

    @Test public void testOpCode0x5B() {
        //LD E,E
        //1  4
        //- - - -
        mem.init(0x5B);
        cpu.setRegE(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegE());
    }

    @Test public void testOpCode0x5C() {
        //LD E,H
        //1  4
        //- - - -
        mem.init(0x5C);
        cpu.setRegH(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegE());
    }

    @Test public void testOpCode0x5D() {
        //LD E,L
        //1  4
        //- - - -
        mem.init(0x5D);
        cpu.setRegL(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegE());
    }

    @Test public void testOpCode0x5E() {
        //LD E,(HL)
        //1  8
        //- - - -
        mem.init(0x5E);
        cpu.setRegHL(100);
        mem.writeByte(100, 42);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, cpu.getRegE());
    }

    @Test public void testOpCode0x5F() {
        //LD E,A
        //1  4
        //- - - -
        mem.init(0x5F);
        cpu.setRegA(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegE());
    }

    @Test public void testOpCode0x60() {
        //LD H,B
        //1  4
        //- - - -
        mem.init(0x60);
        cpu.setRegB(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegH());
    }

    @Test public void testOpCode0x61() {
        //LD H,C
        //1  4
        //- - - -
        mem.init(0x61);
        cpu.setRegC(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegH());
    }

    @Test public void testOpCode0x62() {
        //LD H,D
        //1  4
        //- - - -
        mem.init(0x62);
        cpu.setRegD(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegH());
    }

    @Test public void testOpCode0x63() {
        //LD H,E
        //1  4
        //- - - -
        mem.init(0x63);
        cpu.setRegE(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegH());
    }

    @Test public void testOpCode0x64() {
        //LD H,H
        //1  4
        //- - - -
        mem.init(0x64);
        cpu.setRegH(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegH());
    }

    @Test public void testOpCode0x65() {
        //LD H,L
        //1  4
        //- - - -
        mem.init(0x65);
        cpu.setRegL(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegH());
    }

    @Test public void testOpCode0x66() {
        //LD H,(HL)
        //1  8
        //- - - -
        mem.init(0x66);
        cpu.setRegHL(100);
        mem.writeByte(100, 42);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, cpu.getRegH());
    }

    @Test public void testOpCode0x67() {
        //LD H,A
        //1  4
        //- - - -
        mem.init(0x67);
        cpu.setRegA(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegH());
    }

    @Test public void testOpCode0x68() {
        //LD L,B
        //1  4
        //- - - -
        mem.init(0x68);
        cpu.setRegB(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegL());
    }

    @Test public void testOpCode0x69() {
        //LD L,C
        //1  4
        //- - - -
        mem.init(0x69);
        cpu.setRegC(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegL());
    }

    @Test public void testOpCode0x6A() {
        //LD L,D
        //1  4
        //- - - -
        mem.init(0x6A);
        cpu.setRegD(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegL());
    }

    @Test public void testOpCode0x6B() {
        //LD L,E
        //1  4
        //- - - -
        mem.init(0x6B);
        cpu.setRegE(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegL());
    }

    @Test public void testOpCode0x6C() {
        //LD L,H
        //1  4
        //- - - -
        mem.init(0x6C);
        cpu.setRegH(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegL());
    }

    @Test public void testOpCode0x6D() {
        //LD L,L
        //1  4
        //- - - -
        mem.init(0x6D);
        cpu.setRegL(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegL());
    }

    @Test public void testOpCode0x6E() {
        //LD L,(HL)
        //1  8
        //- - - -
        mem.init(0x6E);
        cpu.setRegHL(100);
        mem.writeByte(100, 42);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, cpu.getRegL());
    }

    @Test public void testOpCode0x6F() {
        //LD L,A
        //1  4
        //- - - -
        mem.init(0x6F);
        cpu.setRegA(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegL());
    }

    @Test public void testOpCode0x70() {
        //LD (HL),B
        //1  8
        //- - - -
        mem.init(0x70);
        cpu.setRegB(42);
        cpu.setRegHL(100);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, mem.readByte(100));
    }

    @Test public void testOpCode0x71() {
        //LD (HL),C
        //1  8
        //- - - -
        mem.init(0x71);
        cpu.setRegC(42);
        cpu.setRegHL(100);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, mem.readByte(100));
    }

    @Test public void testOpCode0x72() {
        //LD (HL),D
        //1  8
        //- - - -
        mem.init(0x72);
        cpu.setRegD(42);
        cpu.setRegHL(100);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, mem.readByte(100));
    }

    @Test public void testOpCode0x73() {
        //LD (HL),E
        //1  8
        //- - - -
        mem.init(0x73);
        cpu.setRegE(42);
        cpu.setRegHL(100);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, mem.readByte(100));
    }

    @Test public void testOpCode0x74() {
        //LD (HL),H
        //1  8
        //- - - -
        mem.init(0x74);
        cpu.setRegHL(0x0142);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(0x01, mem.readByte(0x142));
    }

    @Test public void testOpCode0x75() {
        //LD (HL),L
        //1  8
        //- - - -
        mem.init(0x75);
        cpu.setRegHL(0x0142);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(0x42, mem.readByte(0x142));
    }

    @Test public void testOpCode0x76() {
        //HALT
        //1  4
        //- - - -
        mem.init(0x76);
        StepCPU_AssertPcValueAndCycles(0, 4);
        assertTrue(cpu.isHalted());
    }

    @Test public void testOpCode0x77() {
        //LD (HL),A
        //1  8
        //- - - -
        mem.init(0x77);
        cpu.setRegA(42);
        cpu.setRegHL(100);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, mem.readByte(100));
    }

    @Test public void testOpCode0x78() {
        //LD A,B
        //1  4
        //- - - -
        mem.init(0x78);
        cpu.setRegB(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegA());
    }

    @Test public void testOpCode0x79() {
        //LD A,C
        //1  4
        //- - - -
        mem.init(0x79);
        cpu.setRegC(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegA());
    }

    @Test public void testOpCode0x7A() {
        //LD A,D
        //1  4
        //- - - -
        mem.init(0x7A);
        cpu.setRegD(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegA());
    }

    @Test public void testOpCode0x7B() {
        //LD A,E
        //1  4
        //- - - -
        mem.init(0x7B);
        cpu.setRegE(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegA());
    }

    @Test public void testOpCode0x7C() {
        //LD A,H
        //1  4
        //- - - -
        mem.init(0x7C);
        cpu.setRegH(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegA());
    }

    @Test public void testOpCode0x7D() {
        //LD A,L
        //1  4
        //- - - -
        mem.init(0x7D);
        cpu.setRegL(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegA());
    }

    @Test public void testOpCode0x7E() {
        //LD A,(HL)
        //1  8
        //- - - -
        mem.init(0x7E);
        cpu.setRegHL(100);
        mem.writeByte(100, 42);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(42, cpu.getRegA());
    }

    @Test public void testOpCode0x7F() {
        //LD A,A
        //1  4
        //- - - -
        mem.init(0x7F);
        cpu.setRegA(42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(42, cpu.getRegA());
    }

    @Test public void testOpCode0x80() {
        //ADD A,B
        // 1  4
        // Z 0 H C
        mem.init(0x80, 0x80);
        cpu.setRegA(0x99);
        cpu.setRegB(0x83);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_znhC();
        assertEquals(0x1c, cpu.getRegA());
        cpu.setRegB(0xe4);
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFlagsAre_ZnHC();
        assertEquals(0, cpu.getRegA());
    }

    @Test public void testOpCode0x81() {
        //ADD A,C
        // 1  4
        // Z 0 H C
        mem.init(0x81, 0x81);
        cpu.setRegA(0x99);
        cpu.setRegC(0x83);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_znhC();
        assertEquals(0x1c, cpu.getRegA());
        cpu.setRegC(0xe4);
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFlagsAre_ZnHC();
        assertEquals(0, cpu.getRegA());
    }

    @Test public void testOpCode0x82() {
        //ADD A,D
        // 1  4
        // Z 0 H C
        mem.init(0x82, 0x82);
        cpu.setRegA(0x99);
        cpu.setRegD(0x83);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_znhC();
        assertEquals(0x1c, cpu.getRegA());
        cpu.setRegD(0xe4);
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFlagsAre_ZnHC();
        assertEquals(0, cpu.getRegA());
    }

    @Test public void testOpCode0x83() {
        //ADD A,E
        // 1  4
        // Z 0 H C
        mem.init(0x83, 0x83);
        cpu.setRegA(0x99);
        cpu.setRegE(0x83);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_znhC();
        assertEquals(0x1c, cpu.getRegA());
        cpu.setRegE(0xe4);
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFlagsAre_ZnHC();
        assertEquals(0, cpu.getRegA());
    }

    @Test public void testOpCode0x84() {
        //ADD A,H
        // 1  4
        // Z 0 H C
        mem.init(0x84, 0x84);
        cpu.setRegA(0x99);
        cpu.setRegH(0x83);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_znhC();
        assertEquals(0x1c, cpu.getRegA());
        cpu.setRegH(0xe4);
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFlagsAre_ZnHC();
        assertEquals(0, cpu.getRegA());
    }

    @Test public void testOpCode0x85() {
        //ADD A,L
        // 1  4
        // Z 0 H C
        mem.init(0x85, 0x85);
        cpu.setRegA(0x99);
        cpu.setRegL(0x83);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_znhC();
        assertEquals(0x1c, cpu.getRegA());
        cpu.setRegL(0xe4);
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFlagsAre_ZnHC();
        assertEquals(0, cpu.getRegA());
    }

    @Test public void testOpCode0x86() {
        //ADD A,(HL)
        // 1  8
        // Z 0 H C
        mem.init(0x86, 0x86);
        cpu.setRegA(0x99);
        cpu.setRegHL(100);
        mem.writeByte(100, 0x83);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertFlagsAre_znhC();
        assertEquals(0x1c, cpu.getRegA());
        mem.writeByte(100, 0xe4);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertFlagsAre_ZnHC();
        assertEquals(0, cpu.getRegA());
    }

    @Test public void testOpCode0x87() {
        //ADD A,A
        // 1  4
        // Z 0 H C
        mem.init(0x87, 0x87);
        cpu.setRegA(0x8e);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_znHC();
        assertEquals(0x1c, cpu.getRegA());
        cpu.setRegA(0x80);
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertTrue(cpu.getC());
        assertFalse(cpu.getH());
        assertTrue(cpu.getZ());
        assertEquals(0, cpu.getRegA());
    }

    @Test public void testOpCode0x88() {
        //ADC A,B
        // 1  4
        // Z 0 H C
        mem.init(0x88, 0x88);
        cpu.setRegA(0x98);
        cpu.setC(true);
        cpu.setRegB(0x83);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_znhC();
        assertEquals(0x1c, cpu.getRegA());
        cpu.setRegB(0xe3);
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFlagsAre_ZnHC();
        assertEquals(0, cpu.getRegA());
    }

    @Test public void testOpCode0x89() {
        //ADC A,C
        // 1  4
        // Z 0 H C
        mem.init(0x89, 0x89);
        cpu.setRegA(0x98);
        cpu.setC(true);
        cpu.setRegC(0x83);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_znhC();
        assertEquals(0x1c, cpu.getRegA());
        cpu.setRegC(0xe3);
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFlagsAre_ZnHC();
        assertEquals(0, cpu.getRegA());
    }

    @Test public void testOpCode0x8A() {
        //ADC A,D
        // 1  4
        // Z 0 H C
        mem.init(0x8A, 0x8A);
        cpu.setRegA(0x98);
        cpu.setC(true);
        cpu.setRegD(0x83);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_znhC();
        assertEquals(0x1c, cpu.getRegA());
        cpu.setRegD(0xe3);
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFlagsAre_ZnHC();
        assertEquals(0, cpu.getRegA());
        assertFalse(cpu.getN());
    }

    @Test public void testOpCode0x8B() {
        //ADC A,E
        // 1  4
        // Z 0 H C
        mem.init(0x8B, 0x8B);
        cpu.setRegA(0x98);
        cpu.setC(true);
        cpu.setRegE(0x83);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_znhC();
        assertEquals(0x1c, cpu.getRegA());
        cpu.setRegE(0xe3);
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFlagsAre_ZnHC();
        assertEquals(0, cpu.getRegA());
    }

    @Test public void testOpCode0x8C() {
        //ADC A,H
        // 1  4
        // Z 0 H C
        mem.init(0x8C, 0x8C);
        cpu.setRegA(0x98);
        cpu.setC(true);
        cpu.setRegH(0x83);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_znhC();
        assertEquals(0x1c, cpu.getRegA());
        cpu.setRegH(0xe3);
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFlagsAre_ZnHC();
        assertEquals(0, cpu.getRegA());
    }

    @Test public void testOpCode0x8D() {
        //ADC A,L
        // 1  4
        // Z 0 H C
        mem.init(0x8D, 0x8D);
        cpu.setRegA(0x98);
        cpu.setC(true);
        cpu.setRegL(0x83);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_znhC();
        assertEquals(0x1c, cpu.getRegA());
        cpu.setRegL(0xe3);
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFlagsAre_ZnHC();
        assertEquals(0, cpu.getRegA());
    }

    @Test public void testOpCode0x8E() {
        //ADC A,(HL)
        // 1  8
        // Z 0 H C
        mem.init(0x8E, 0x8E);
        cpu.setRegA(0x98);
        cpu.setC(true);
        cpu.setRegHL(100);
        mem.writeByte(100, 0x83);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertFlagsAre_znhC();
        assertEquals(0x1c, cpu.getRegA());
        mem.writeByte(100, 0xe3);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertFlagsAre_ZnHC();
        assertEquals(0, cpu.getRegA());
    }

    @Test public void testOpCode0x8F() {
        //ADC A,A
        // 1  4
        // Z 0 H C
        mem.init(0x8F, 0x8F);
        cpu.setRegA(0x8e);
        cpu.setC(true);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_znHC();
        assertEquals(0x1D, cpu.getRegA());
        cpu.setRegA(0x80);
        StepCPU_AssertPcValueAndCycles(2, 4);
        assertFlagsAre_znhC();
        assertEquals(1, cpu.getRegA());
    }

    @Test public void testOpCode0x90() {
        //SUB B
        // 1  4
        // Z 0 H C
        test8BitSub(new SourceRegSetter() {
            @Override void setReg(int s) {
                cpu.setRegB(s);
            }
        }, 0x90, 4);
    }

    @Test public void testOpCode0x91() {
        //SUB C
        // 1  4
        // Z 0 H C
        test8BitSub(new SourceRegSetter() {
            @Override void setReg(int s) {
                cpu.setRegC(s);
            }
        }, 0x91, 4);
    }

    @Test public void testOpCode0x92() {
        //SUB D
        // 1  4
        // Z 0 H C
        test8BitSub(new SourceRegSetter() {
            @Override void setReg(int s) {
                cpu.setRegD(s);
            }
        }, 0x92, 4);
    }

    @Test public void testOpCode0x93() {
        //SUB E
        // 1  4
        // Z 0 H C
        test8BitSub(new SourceRegSetter() {
            @Override void setReg(int s) {
                cpu.setRegE(s);
            }
        }, 0x93, 4);
    }

    @Test public void testOpCode0x94() {
        //SUB H
        // 1  4
        // Z 0 H C
        test8BitSub(new SourceRegSetter() {
            @Override void setReg(int s) {
                cpu.setRegH(s);
            }
        }, 0x94, 4);
    }

    @Test public void testOpCode0x95() {
        //SUB H
        // 1  4
        // Z 0 H C
        test8BitSub(new SourceRegSetter() {
            @Override void setReg(int s) {
                cpu.setRegL(s);
            }
        }, 0x95, 4);
    }

    @Test public void testOpCode0x96() {
        //SUB (HL)
        // 1  8
        // Z 0 H C
        test8BitSub(new SourceRegSetter() {
            @Override void setReg(int s) {
                mem.writeByte(100, s);
                cpu.setRegHL(100);
            }
        }, 0x96, 8);
    }

    @Test public void testOpCode0x97() {
        //SUB A
        // 1  4
        // Z 0 H C
        mem.init(0x97, 0x97, 0x97);
        cpu.setRegA(0x42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_ZNhc();
        assertEquals(0, cpu.getRegA());
    }

    @Test public void testOpCode0x98() {
        //SBC A,B
        // 1  4
        // Z 0 H C
        test8BitSbc(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegB(s);
            }
        }, 0x98, 4);
    }

    @Test public void testOpCode0x99() {
        //SBC A,C
        // 1  4
        // Z 0 H C
        test8BitSbc(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegC(s);
            }
        }, 0x99, 4);
    }

    @Test public void testOpCode0x9A() {
        //SBC A,D
        // 1  4
        // Z 0 H C
        test8BitSbc(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegD(s);
            }
        }, 0x9A, 4);
    }

    @Test public void testOpCode0x9B() {
        //SBC A,E
        // 1  4
        // Z 0 H C
        test8BitSbc(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegE(s);
            }
        }, 0x9b, 4);
    }

    @Test public void testOpCode0x9C() {
        //SBC A,H
        // 1  4
        // Z 0 H C
        test8BitSbc(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegH(s);
            }
        }, 0x9c, 4);
    }

    @Test public void testOpCode0x9D() {
        //SBC A,H
        // 1  4
        // Z 0 H C
        test8BitSbc(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegL(s);
            }
        }, 0x9d, 4);
    }

    @Test public void testOpCode0x9e() {
        //SBC A,(HL)
        // 1  8
        // Z 0 H C
        test8BitSbc(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                mem.writeByte(100, s);
                cpu.setRegHL(100);
            }
        }, 0x9e, 8);
    }

    @Test public void testOpCode0x9F() {
        //SBC A,A
        // 1  4
        // Z 0 H C
        mem.init(0x9F);
        cpu.setC(false);
        cpu.setRegA(0x42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_ZNhc();
        assertEquals(0, cpu.getRegA());
    }

    @Test public void testOpCode0xa0() {
        //AND B
        // 1  4
        // Z 0 1 0
        test8BitAnd(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegB(s);
            }
        }, 0xa0, 4);
    }

    @Test public void testOpCode0xa1() {
        //AND C
        // 1  4
        // Z 0 1 0
        test8BitAnd(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegC(s);
            }
        }, 0xa1, 4);
    }

    @Test public void testOpCode0xa2() {
        //AND D
        // 1  4
        // Z 0 1 0
        test8BitAnd(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegD(s);
            }
        }, 0xa2, 4);
    }

    @Test public void testOpCode0xa3() {
        //AND E
        // 1  4
        // Z 0 1 0
        test8BitAnd(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegE(s);
            }
        }, 0xa3, 4);
    }

    @Test public void testOpCode0xa4() {
        //AND H
        // 1  4
        // Z 0 1 0
        test8BitAnd(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegH(s);
            }
        }, 0xa4, 4);
    }

    @Test public void testOpCode0xa5() {
        //AND L
        // 1  4
        // Z 0 1 0
        test8BitAnd(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegL(s);
            }
        }, 0xa5, 4);
    }

    @Test public void testOpCode0xa6() {
        //AND (HL)
        // 1  8
        // Z 0 1 0
        test8BitAnd(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                mem.writeByte(100, s);
                cpu.setRegHL(100);
            }
        }, 0xa6, 8);
    }

    @Test public void testOpCode0xa7() {
        //AND A
        // 1  4
        // Z 0 1 0
        mem.init(0xa7);
        cpu.setRegA(0x42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x42, cpu.getRegA());
        assertFalse(cpu.getZ());
        assertTrue(cpu.getH());
    }

    @Test public void testOpCode0xa8() {
        //XOR B
        // 1  4
        // Z 0 1 0
        test8BitXor(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegB(s);
            }
        }, 0xa8, 4);
    }

    @Test public void testOpCode0xa9() {
        //XOR C
        // 1  4
        // Z 0 1 0
        test8BitXor(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegC(s);
            }
        }, 0xa9, 4);
    }

    @Test public void testOpCode0xaa() {
        //XOR D
        // 1  4
        // Z 0 1 0
        test8BitXor(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegD(s);
            }
        }, 0xaa, 4);
    }

    @Test public void testOpCode0xab() {
        //XOR E
        // 1  4
        // Z 0 1 0
        test8BitXor(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegE(s);
            }
        }, 0xab, 4);
    }

    @Test public void testOpCode0xac() {
        //XOR H
        // 1  4
        // Z 0 1 0
        test8BitXor(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegH(s);
            }
        }, 0xac, 4);
    }

    @Test public void testOpCode0xad() {
        //XOR L
        // 1  4
        // Z 0 1 0
        test8BitXor(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegL(s);
            }
        }, 0xad, 4);
    }

    @Test public void testOpCode0xae() {
        //XOR (HL)
        // 1  8
        // Z 0 1 0
        test8BitXor(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                mem.writeByte(100, s);
                cpu.setRegHL(100);
            }
        }, 0xae, 8);
    }

    @Test public void testOpCode0xaf() {
        //XOR A
        // 1  4
        // Z 0 1 0
        mem.init(0xaf);
        cpu.setRegA(0x42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0, cpu.getRegA());
        assertFlagsAre_Znhc();
    }

    @Test public void testOpCode0xB0() {
        //OR B
        // 1  4
        // Z 0 1 0
        test8BitOr(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegB(s);
            }
        }, 0xb0, 4);
    }

    @Test public void testOpCode0xB1() {
        //OR C
        // 1  4
        // Z 0 1 0
        test8BitOr(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegC(s);
            }
        }, 0xb1, 4);
    }

    @Test public void testOpCode0xB2() {
        //OR D
        // 1  4
        // Z 0 1 0
        test8BitOr(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegD(s);
            }
        }, 0xb2, 4);
    }

    @Test public void testOpCode0xB3() {
        //OR E
        // 1  4
        // Z 0 1 0
        test8BitOr(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegE(s);
            }
        }, 0xb3, 4);
    }

    @Test public void testOpCode0xB4() {
        //OR H
        // 1  4
        // Z 0 1 0
        test8BitOr(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegH(s);
            }
        }, 0xb4, 4);
    }

    @Test public void testOpCode0xb5() {
        //OR L
        // 1  4
        // Z 0 1 0
        test8BitOr(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegL(s);
            }
        }, 0xb5, 4);
    }

    @Test public void testOpCode0xb6() {
        //OR (HL)
        // 1  8
        // Z 0 1 0
        test8BitOr(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                mem.writeByte(100, s);
                cpu.setRegHL(100);
            }
        }, 0xb6, 8);
    }

    @Test public void testOpCode0xb7() {
        //OR A
        // 1  4
        // Z 0 1 0
        mem.init(0xb7);
        cpu.setRegA(0x42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertEquals(0x42, cpu.getRegA());
        assertFalse(cpu.getZ());
        assertFalse(cpu.getH());
    }


    @Test public void testOpCode0xb8() {
        //CP B
        // 1  4
        // Z 0 1 0
        test8BitCp(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegB(s);
            }
        }, 0xb8, 4);
    }

    @Test public void testOpCode0xb9() {
        //CP C
        // 1  4
        // Z 0 1 0
        test8BitCp(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegC(s);
            }
        }, 0xb9, 4);
    }

    @Test public void testOpCode0xba() {
        //CP D
        // 1  4
        // Z 0 1 0
        test8BitCp(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegD(s);
            }
        }, 0xba, 4);
    }

    @Test public void testOpCode0xbb() {
        //CP E
        // 1  4
        // Z 0 1 0
        test8BitCp(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegE(s);
            }
        }, 0xbb, 4);
    }

    @Test public void testOpCode0xbc() {
        //CP H
        // 1  4
        // Z 0 1 0
        test8BitCp(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegH(s);
            }
        }, 0xbc, 4);
    }

    @Test public void testOpCode0xbd() {
        //CP L
        // 1  4
        // Z 0 1 0
        test8BitCp(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                cpu.setRegL(s);
            }
        }, 0xbd, 4);
    }

    @Test public void testOpCode0xbe() {
        //CP (HL)
        // 1  8
        // Z 0 1 0
        test8BitCp(new SourceRegSetter() {
            @Override
            void setReg(int s) {
                mem.writeByte(100, s);
                cpu.setRegHL(100);
            }
        }, 0xbe, 8);
    }

    @Test public void testOpCode0xbf() {
        //XOR A
        // 1  4
        // Z 0 1 0
        mem.init(0xbf);
        cpu.setRegA(0x42);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFlagsAre_ZNhc();
    }

    @Test public void testOpCode0xC0() {
        // RET NZ
        // 1  20/8
        // - - - -
        testConditionalRet(new SourceFlagSetter() {
            public void setSourceFlag(boolean v) {
                cpu.setZ(!v);
            }
        }, 0xc0);
    }

    @Test public void testOpCode0xC1() {
       // POP BC
       // 1  12
       // - - - -
        mem.init(0xc1);
        mem.writeWord(0x100, 0x1234);
        cpu.setRegSP(0x100);
        StepCPU_AssertPcValueAndCycles(1, 12);
        assertEquals(0x1234, cpu.getRegBC());
        assertEquals(0x102, cpu.getRegSP());
    }

    @Test public void testOpCode0xC2() {
        //JP NZ,a16
        //3  16/12
        //- - - -
        testA16ConditionalJump(0xc2, new SourceFlagSetter() {
            public void setSourceFlag(boolean v) {
                cpu.setZ(!v);
            }
        });
    }

    @Test public void testOpCode0xC3() {
        //JP a16
        //3  16
        //- - - -
        mem.init(0xc3,0x34,0x12);
        StepCPU_AssertPcValueAndCycles(0x1234, 16);
    }

    @Test public void testOpCode0xC4() {
        //CALL NZ,a16
        //3  24/12
        //- - - -
        testA16ConditionalCall(0xc4, new SourceFlagSetter() {
            public void setSourceFlag(boolean v) {
                cpu.setZ(!v);
            }
        });
    }

    @Test public void testOpCode0xC5() {
       // PUSH BC
       // 1  16
       // - - - -
        mem.init(0xc5);
        cpu.setRegBC(0x1234);
        cpu.setRegSP(0x102);
        StepCPU_AssertPcValueAndCycles(1, 16);
        assertEquals(0x1234, mem.readWord(0x100));
        assertEquals(0x100, cpu.getRegSP());
    }

    @Test public void testOpCode0xC6() {
        //ADD A,d8
        //2  8
        //Z 0 H C
        mem.init(0xc6, 0x42, 0xc6, 0xbd);
        cpu.setRegA(0x01);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertEquals(0x43, cpu.getRegA());
        assertFlagsAre_znhc();
        StepCPU_AssertPcValueAndCycles(4, 8);
        assertEquals(0, cpu.getRegA());
        assertFlagsAre_ZnHC();
    }

    @Test public void testOpCode0xC7() {
        //RST 00H
        //1  16
        //- - - -
        testRST(0xc7, 0);
    }


    @Test public void testOpCode0xC8() {
        // RET Z
        // 1  20/8
        // - - - -
        testConditionalRet(new SourceFlagSetter() {
            public void setSourceFlag(boolean v) {
                cpu.setZ(v);
            }
        }, 0xc8);
    }

    @Test public void testOpCode0xC9() {
        // RET
        // 1  16
        // - - - -
        mem.init(0xC9, 0, 0x00, 0x01);
        cpu.setRegSP(0x02);
        cpu.step();
        assertEquals(0x100, cpu.getPc());
        assertEquals(0x04, cpu.getRegSP());
    }

    @Test public void testOpCode0xCA() {
        //JP Z,a16
        //3  16/12
        //- - - -
        testA16ConditionalJump(0xca, new SourceFlagSetter() {
            public void setSourceFlag(boolean v) {
                cpu.setZ(v);
            }
        });
    }

    @Test public void testOpCode0xCC() {
        //CALL Z,a16
        //3  24/12
        //- - - -
        testA16ConditionalCall(0xcc, new SourceFlagSetter() {
            public void setSourceFlag(boolean v) {
                cpu.setZ(v);
            }
        });
    }

    @Test public void testOpCode0xCD() {
        //CALL a16
        //3  24
        //- - - -
        mem.init(0xcd, 0x34, 0x12);
        cpu.setRegSP(0x200);
        StepCPU_AssertPcValueAndCycles(0x1234, 24);
        assertEquals(0x1fe, cpu.getRegSP());
        assertEquals(0x03, mem.readWord(0x1fe));
    }

    @Test public void testOpCode0xCE() {
        //ADC A,d8
        //2  8
        //Z 0 H C
        mem.init(0xce, 0x42, 0xce, 0xbc);
        cpu.setRegA(0x01);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertEquals(0x43, cpu.getRegA());
        assertFlagsAre_znhc();
        cpu.setC(true);
        StepCPU_AssertPcValueAndCycles(4, 8);
        assertEquals(0, cpu.getRegA());
        assertFlagsAre_ZnHC();
    }

    @Test public void testOpCode0xCF() {
        //RST 08H
        //1  16
        //- - - -
        testRST(0xcf, 8);
    }

    @Test public void testOpCode0xD0() {
        // RET NC
        // 1  20/8
        // - - - -
        testConditionalRet(new SourceFlagSetter() {
            public void setSourceFlag(boolean v) {
                cpu.setC(!v);
            }
        }, 0xD0);
    }

    @Test public void testOpCode0xD1() {
       // POP DE
       // 1  12
       // - - - -
        mem.init(0xd1);
        mem.writeWord(0x100, 0x1234);
        cpu.setRegSP(0x100);
        StepCPU_AssertPcValueAndCycles(1, 12);
        assertEquals(0x1234, cpu.getRegDE());
        assertEquals(0x102, cpu.getRegSP());
    }

    @Test public void testOpCode0xD2() {
        //JP NC,a16
        //3  16/12
        //- - - -
        testA16ConditionalJump(0xd2, new SourceFlagSetter() {
            public void setSourceFlag(boolean v) {
                cpu.setC(!v);
            }
        });
    }

    @Test public void testOpCode0xD4() {
        //CALL NC,a16
        //3  24/12
        //- - - -
        testA16ConditionalCall(0xd4, new SourceFlagSetter() {
            public void setSourceFlag(boolean v) {
                cpu.setC(!v);
            }
        });
    }

    @Test public void testOpCode0xD5() {
       // PUSH DE
       // 1  16
       // - - - -
        mem.init(0xd5);
        cpu.setRegDE(0x1234);
        cpu.setRegSP(0x102);
        StepCPU_AssertPcValueAndCycles(1, 16);
        assertEquals(0x1234, mem.readWord(0x100));
        assertEquals(0x100, cpu.getRegSP());
    }

    @Test public void testOpCode0xD6() {
        //SUB d8
        //2  8
        //Z 1 H C
        mem.init(0xd6, 0x42, 0xd6, 0x02);
        cpu.setRegA(0x43);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertEquals(0x1, cpu.getRegA());
        assertFlagsAre_zNhc();
        StepCPU_AssertPcValueAndCycles(4, 8);
        assertEquals(0xff, cpu.getRegA());
        assertFlagsAre_zNHC();
    }

    @Test public void testOpCode0xD7() {
        //RST 10H
        //1  16
        //- - - -
        testRST(0xd7, 0x10);
    }

    @Test public void testOpCode0xD8() {
        // RET C
        // 1  20/8
        // - - - -
        testConditionalRet(new SourceFlagSetter() {
            public void setSourceFlag(boolean v) {
                cpu.setC(v);
            }
        }, 0xd8);
    }

    @Test public void testOpCode0xD9() {
        // RETI
        // 1  16
        // - - - -
        mem.init(0xD9, 0, 0x00, 0x01);
        cpu.setRegSP(0x02);
        cpu.step();
        assertEquals(0x100, cpu.getPc());
        assertEquals(0x04, cpu.getRegSP());
        assertTrue(cpu.getI());
    }

    @Test public void testOpCode0xDA() {
        //JP C,a16
        //3  16/12
        //- - - -
        testA16ConditionalJump(0xda, new SourceFlagSetter() {
            public void setSourceFlag(boolean v) {
                cpu.setC(v);
            }
        });
    }

    @Test public void testOpCode0xDC() {
        //CALL C,a16
        //3  24/12
        //- - - -
        testA16ConditionalCall(0xdc, new SourceFlagSetter() {
            public void setSourceFlag(boolean v) {
                cpu.setC(v);
            }
        });
    }

    @Test public void testOpCode0xDE() {
        //SBC d8
        //2  8
        //Z 1 H C
        mem.init(0xde, 0x42, 0xde, 0x01);
        cpu.setRegA(0x43);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertEquals(0x1, cpu.getRegA());
        assertFlagsAre_zNhc();
        cpu.setC(true);
        StepCPU_AssertPcValueAndCycles(4, 8);
        assertEquals(0xff, cpu.getRegA());
        assertFlagsAre_zNHC();
    }

    @Test public void testOpCode0xDF() {
        //RST 18H
        //1  16
        //- - - -
        testRST(0xdf, 0x18);
    }

    @Test public void testOpCode0xE0() {
        // LDH (a8),A
        // 2  12
        // - - - -
        mem.init(0xe0, 0x42);
        cpu.setRegA(0x12);
        StepCPU_AssertPcValueAndCycles(2, 12);
        assertEquals(0x12, mem.readByte(0xff42));
    }

    @Test public void testOpCode0xE1() {
        // POP HL
        // 1  12
        // - - - -
        mem.init(0xe1);
        mem.writeWord(0x100, 0x1234);
        cpu.setRegSP(0x100);
        StepCPU_AssertPcValueAndCycles(1, 12);
        assertEquals(0x1234, cpu.getRegHL());
        assertEquals(0x102, cpu.getRegSP());
    }

    @Test public void testOpCode0xE2() {
        // LD (C),A
        // 1  8   ???
        // - - - -
        mem.init(0xe2);
        cpu.setRegA(0x12);
        cpu.setRegC(0x42);
        StepCPU_AssertPcValueAndCycles(1, 12);
        assertEquals(0x12, mem.readByte(0xff42));
    }

    @Test public void testOpCode0xE5() {
       // PUSH HL
       // 1  16
       // - - - -
        mem.init(0xe5);
        cpu.setRegHL(0x1234);
        cpu.setRegSP(0x102);
        StepCPU_AssertPcValueAndCycles(1, 16);
        assertEquals(0x1234, mem.readWord(0x100));
        assertEquals(0x100, cpu.getRegSP());
    }

    @Test public void testOpCode0xE6() {
        //AND d8
        //2  8
        //Z 0 1 0
        mem.init(0xe6, 0x0f, 0xe6, 0x20);
        cpu.setRegA(0x42);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertEquals(0x2, cpu.getRegA());
        assertFlagsAre_znHc();
        StepCPU_AssertPcValueAndCycles(4, 8);
        assertEquals(0x00, cpu.getRegA());
        assertFlagsAre_ZnHc();
    }

    @Test public void testOpCode0xE7() {
        //RST 20H
        //1  16
        //- - - -
        testRST(0xe7, 0x20);
    }

    @Test public void testOpCode0xE8() {
        //ADD SP,r8
        //2  16
        //0 0 H C
        mem.init(0xe8, 0x12, 0xe8, 0xfe);
        cpu.setRegSP(0x100);
        cpu.setZ(true);
        cpu.setN(true);
        StepCPU_AssertPcValueAndCycles(2, 16);
        assertEquals(0x112, cpu.getRegSP());
        assertFlagsAre_znhc();

        StepCPU_AssertPcValueAndCycles(4, 16);
        assertEquals(0x110, cpu.getRegSP());
        assertFlagsAre_znHC();
    }

    @Test public void testOpCode0xE9() {
        //JP (HL)
        //1  4
        //- - - -
        mem.init(0xe9);
        cpu.setRegHL(0x1234);
        StepCPU_AssertPcValueAndCycles(0x1234, 4);
    }

    @Test public void testOpCode0xEA() {
        //LD (a16),A
        //3  16
        //- - - -
        mem.init(0xea, 0x34, 0x12);
        cpu.setRegA(0x42);
        StepCPU_AssertPcValueAndCycles(3, 16);
        assertEquals(mem.readByte(0x1234), 0x42);
    }

    @Test public void testOpCode0xEE() {
        //XOR d8
        //2  8
        //Z 0 0 0
        mem.init(0xee, 0x0f, 0xee, 0x4d);
        cpu.setRegA(0x42);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertEquals(0x4d, cpu.getRegA());
        assertFlagsAre_znhc();
        StepCPU_AssertPcValueAndCycles(4, 8);
        assertEquals(0x00, cpu.getRegA());
        assertFlagsAre_Znhc();
    }

    @Test public void testOpCode0xEF() {
        //RST 28H
        //1  16
        //- - - -
        testRST(0xef, 0x28);
    }

    @Test public void testOpCode0xF0() {
        // LDH A, (a8)
        // 2  12
        // - - - -
        mem.init(0xf0, 0x42);
        mem.writeByte(0xff42, 0x12);
        StepCPU_AssertPcValueAndCycles(2, 12);
        assertEquals(0x12, cpu.getRegA());
    }

    @Test public void testOpCode0xF1() {
       // POP AF
       // 1  12
       // - - - -
        mem.init(0xf1);
        mem.writeWord(0x100, 0x12ff);
        cpu.setRegSP(0x100);
        StepCPU_AssertPcValueAndCycles(1, 12);
        assertEquals(0x12, cpu.getRegA());
        assertTrue(cpu.getN());
        assertTrue(cpu.getH());
        assertTrue(cpu.getZ());
        assertTrue(cpu.getC());
        assertEquals(0x102, cpu.getRegSP());
    }

    @Test public void testOpCode0xF2() {
        // LD A, (C)
        // 1  12  ???
        // - - - -
        mem.init(0xf2);
        cpu.setRegC(0x42);
        mem.writeByte(0xff42, 0x12);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(0x12, cpu.getRegA());
    }

    @Test public void testOpCode0xF3() {
        //DI
        //1  4
        //- - - -
        mem.init(0xf3);
        cpu.setI(true);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertFalse(cpu.getI());
    }

    @Test public void testOpCode0xF5() {
       // PUSH AF
       // 1  16
       // - - - -
        mem.init(0xf5);
        cpu.setRegA(0x12);
        cpu.setRegSP(0x102);
        cpu.setN(true);
        cpu.setH(true);
        cpu.setZ(true);
        cpu.setC(true);
        StepCPU_AssertPcValueAndCycles(1, 16);
        assertEquals(0x12f0, mem.readWord(0x100));
        assertEquals(0x100, cpu.getRegSP());
    }

    @Test public void testOpCode0xF6() {
        //OR d8
        //2  8
        //Z 0 0 0
        mem.init(0xf6, 0x0f, 0xf6, 0x20);
        cpu.setRegA(0x42);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertEquals(0x4f, cpu.getRegA());
        assertFlagsAre_znhc();
    }

    @Test public void testOpCode0xF7() {
        //RST 30H
        //1  16
        //- - - -
        testRST(0xf7, 0x30);
    }

    @Test public void testOpCode0xF8() {
        //LD HL,SP+r8
        //2  12
        //0 0 H C
        mem.init(0xf8, 0x12, 0xf8, 0xfe);
        cpu.setRegSP(0x100);
        cpu.setZ(true);
        cpu.setN(true);
        StepCPU_AssertPcValueAndCycles(2, 12);
        assertEquals(0x112, cpu.getRegHL());
        assertFlagsAre_znhc();

        cpu.setRegSP(0x112);
        StepCPU_AssertPcValueAndCycles(4, 12);
        assertEquals(0x110, cpu.getRegHL());
        assertFlagsAre_znHC();

        assertEquals(0x112, cpu.getRegSP());
    }
    
    @Test public void testOpCode0xF9() {
        //LD SP,HL
        //1  8
        //- - - -
        mem.init(0xf9);
        cpu.setRegHL(0x1234);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(0x1234, cpu.getRegSP());
    }

    @Test public void testOpCode0xFA() {
        //LD A,(a16)
        //3  16
        //- - - -
        mem.init(0xfa, 0x34, 0x12);
        mem.writeByte(0x1234, 0x44);
        StepCPU_AssertPcValueAndCycles(3, 16);
        assertEquals(0x44, cpu.getRegA());
    }

    @Test public void testOpCode0xFB() {
        //EI
        //1  4
        //- - - -
        mem.init(0xfB);
        cpu.setI(false);
        StepCPU_AssertPcValueAndCycles(1, 4);
        assertTrue(cpu.getI());
    }

    @Test public void testOpCode0xFE() {
        //CP d8
        //2  8
        //Z 1 H C
        mem.init(0xfe, 0x91, 0xfe, 0xff, 0xfe, 0x03);
        cpu.setRegA(0x90);
        StepCPU_AssertPcValueAndCycles(2, 8);
        assertFlagsAre_zNHC();
        cpu.setRegA(0xff);
        StepCPU_AssertPcValueAndCycles(4, 8);
        assertFlagsAre_ZNhc();
        cpu.setRegA(0x12);
        StepCPU_AssertPcValueAndCycles(6, 8);
        assertFlagsAre_zNHc();
    }

    @Test public void testOpCode0xFF() {
        //RST 38H
        //1  16
        //- - - -
        testRST(0xff, 0x38);
    }

    private void testRST(int opCode, int expectedPc) {
        mem.init(opCode);
        cpu.setSP(3);
        StepCPU_AssertPcValueAndCycles(expectedPc, 16);
        assertEquals(1, mem.readWord(1));
    }

    private void testConditionalRet(SourceFlagSetter sfs, int opCode) {
        mem.init(opCode);
        mem.writeWord(0x100, 0x200);
        cpu.setRegSP(0x100);
        sfs.setSourceFlag(false);
        StepCPU_AssertPcValueAndCycles(1, 8);
        assertEquals(0x100, cpu.getRegSP());
        cpu.setPC(0);
        sfs.setSourceFlag(true);
        StepCPU_AssertPcValueAndCycles(0x200, 20);
        assertEquals(0x102, cpu.getRegSP());
    }
    private void testA16ConditionalJump(int opCode, SourceFlagSetter sfs) {
        mem.init(opCode, 0x12,0x34, opCode, 0x21, 0x43);
        sfs.setSourceFlag(false);
        StepCPU_AssertPcValueAndCycles(3, 12);
        sfs.setSourceFlag(true);
        StepCPU_AssertPcValueAndCycles(0x4321, 16);
    }

    private void testA16ConditionalCall(int opCode, SourceFlagSetter sfs) {
        mem.init(opCode, 0x34, 0x12);
        sfs.setSourceFlag(true);
        cpu.setRegSP(0x200);
        StepCPU_AssertPcValueAndCycles(0x1234, 24);
        assertEquals(0x1fe, cpu.getRegSP());
        assertEquals(0x03, mem.readWord(0x1fe));

        sfs.setSourceFlag(false);
        cpu.setPC(0);
        StepCPU_AssertPcValueAndCycles(3, 12);
        assertEquals(0x1fe, cpu.getRegSP());
    }



    private void test8BitCp(SourceRegSetter srs, int opCode, int expectedCycles) {
        mem.init(opCode, opCode, opCode);
        cpu.setRegA(0x90);
        srs.setReg(0x91);
        StepCPU_AssertPcValueAndCycles(1, expectedCycles);
        assertFlagsAre_zNHC();
        cpu.setRegA(0xff);
        srs.setReg(0xff);
        StepCPU_AssertPcValueAndCycles(2, expectedCycles);
        assertFlagsAre_ZNhc();
        cpu.setRegA(0x12);
        srs.setReg(0x03);
        StepCPU_AssertPcValueAndCycles(3, expectedCycles);
        assertFlagsAre_zNHc();
    }

    private void test8BitOr(SourceRegSetter sourceRegSetter, int opCode, int expectedCycles) {
        mem.init(opCode, opCode);
        cpu.setRegA(0xf0);
        sourceRegSetter.setReg(0x0f);
        StepCPU_AssertPcValueAndCycles(1, expectedCycles);
        assertEquals(0xff, cpu.getRegA());
        assertFlagsAre_znhc();
        cpu.setRegA(0x00);
        sourceRegSetter.setReg(0x00);
        StepCPU_AssertPcValueAndCycles(2, expectedCycles);
        assertEquals(0x00, cpu.getRegA());
        assertFlagsAre_Znhc();
    }


    private void test8BitAnd(SourceRegSetter sourceRegSetter, int opCode, int expectedCycles) {
        mem.init(opCode, opCode);
        cpu.setRegA(0xf0);
        sourceRegSetter.setReg(0x0f);
        StepCPU_AssertPcValueAndCycles(1, expectedCycles);
        assertEquals(0, cpu.getRegA());
        assertFlagsAre_ZnHc();
        cpu.setRegA(0xf2);
        sourceRegSetter.setReg(0x0f);
        StepCPU_AssertPcValueAndCycles(2, expectedCycles);
        assertEquals(0x02, cpu.getRegA());
        assertFlagsAre_znHc();
    }

    private void test8BitXor(SourceRegSetter sourceRegSetter, int opCode, int expectedCycles) {
        mem.init(opCode, opCode);
        cpu.setRegA(0xf0);
        sourceRegSetter.setReg(0x0f);
        StepCPU_AssertPcValueAndCycles(1, expectedCycles);
        assertEquals(0xff, cpu.getRegA());
        assertFlagsAre_znhc();
        cpu.setRegA(0xf2);
        sourceRegSetter.setReg(0xf2);
        StepCPU_AssertPcValueAndCycles(2, expectedCycles);
        assertEquals(0x00, cpu.getRegA());
        assertFlagsAre_Znhc();
    }

    public abstract class SourceRegSetter {
        abstract void setReg(int s);
    }

    private void test8BitSub(SourceRegSetter srs, int opCode, int expectedCycles) {
        mem.init(opCode, opCode, opCode);
        cpu.setRegA(0x90);
        srs.setReg(0x91);
        StepCPU_AssertPcValueAndCycles(1, expectedCycles);
        assertFlagsAre_zNHC();
        assertEquals(0xff, cpu.getRegA());
        srs.setReg(0xff);
        StepCPU_AssertPcValueAndCycles(2, expectedCycles);
        assertFlagsAre_ZNhc();
        assertEquals(0, cpu.getRegA());
        cpu.setRegA(0x12);
        srs.setReg(0x03);
        StepCPU_AssertPcValueAndCycles(3, expectedCycles);
        assertEquals(0xf, cpu.getRegA());
        assertFlagsAre_zNHc();
    }

    private void test8BitSbc(SourceRegSetter srs, int opCode, int expectedCycles) {
        mem.init(opCode, opCode, opCode);
        cpu.setC(false);
        cpu.setRegA(0x90);
        srs.setReg(0x91);
        StepCPU_AssertPcValueAndCycles(1, expectedCycles);
        assertFlagsAre_zNHC();
        assertEquals(0xff, cpu.getRegA());
        srs.setReg(0xfe);
        StepCPU_AssertPcValueAndCycles(2, expectedCycles);
        assertFlagsAre_ZNhc();
        assertEquals(0, cpu.getRegA());
        cpu.setRegA(0x12);
        srs.setReg(0x03);
        cpu.setC(false);
        StepCPU_AssertPcValueAndCycles(3, expectedCycles);
        assertEquals(0xf, cpu.getRegA());
        assertFlagsAre_zNHc();
    }

    private void assertFlagsAre_znHc() {
        assertFalse(cpu.getZ());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getC());
    }
    private void assertFlagsAre_znhc() {
        assertFalse(cpu.getZ());
        assertFalse(cpu.getN());
        assertFalse(cpu.getH());
        assertFalse(cpu.getC());
    }

    private void assertFlagsAre_zNhc() {
        assertFalse(cpu.getZ());
        assertTrue(cpu.getN());
        assertFalse(cpu.getH());
        assertFalse(cpu.getC());
    }

    private void assertFlagsAre_Znhc() {
        assertTrue(cpu.getZ());
        assertFalse(cpu.getN());
        assertFalse(cpu.getH());
        assertFalse(cpu.getC());
    }

    private void assertFlagsAre_ZnhC() {
        assertTrue(cpu.getZ());
        assertFalse(cpu.getN());
        assertFalse(cpu.getH());
        assertTrue(cpu.getC());
    }

    private void assertFlagsAre_ZnHc() {
        assertTrue(cpu.getZ());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getC());
    }

    private void assertFlagsAre_ZNhc() {
        assertTrue(cpu.getZ());
        assertTrue(cpu.getN());
        assertFalse(cpu.getH());
        assertFalse(cpu.getC());
    }

    private void assertFlagsAre_zNHc() {
        assertFalse(cpu.getZ());
        assertTrue(cpu.getN());
        assertTrue(cpu.getH());
        assertFalse(cpu.getC());
    }

    private void assertFlagsAre_zNHC() {
        assertFalse(cpu.getZ());
        assertTrue(cpu.getN());
        assertTrue(cpu.getH());
        assertTrue(cpu.getC());
    }

    private void assertFlagsAre_znhC() {
        assertFalse(cpu.getZ());
        assertFalse(cpu.getN());
        assertFalse(cpu.getH());
        assertTrue(cpu.getC());
    }

    private void assertFlagsAre_znHC() {
        assertFalse(cpu.getZ());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertTrue(cpu.getC());
    }

    private void assertFlagsAre_ZnHC() {
        assertTrue(cpu.getZ());
        assertFalse(cpu.getN());
        assertTrue(cpu.getH());
        assertTrue(cpu.getC());
    }

    private interface SourceFlagSetter {
        void setSourceFlag(boolean v);
    }

    @Test public void testRegisterDecoder_B() {
        cpu.setRegB(0x42);
        int val = cpu.getRegisterValueForCBOpCode(0x00);
        cpu.setRegisterValueForCBOpCode(0x00, val + 1);
        assertEquals(0x43, cpu.getRegB());
    }

    @Test public void testRegisterDecoder_C() {
        cpu.setRegC(0x42);
        int val = cpu.getRegisterValueForCBOpCode(0x01);
        cpu.setRegisterValueForCBOpCode(0x01, val + 1);
        assertEquals(0x43, cpu.getRegC());
    }

    @Test public void testRegisterDecoder_D() {
        cpu.setRegD(0x42);
        int val = cpu.getRegisterValueForCBOpCode(0x02);
        cpu.setRegisterValueForCBOpCode(0x02, val + 1);
        assertEquals(0x43, cpu.getRegD());
    }

    @Test public void testRegisterDecoder_E() {
        cpu.setRegE(0x42);
        int val = cpu.getRegisterValueForCBOpCode(0x03);
        cpu.setRegisterValueForCBOpCode(0x03, val + 1);
        assertEquals(0x43, cpu.getRegE());
    }

    @Test public void testRegisterDecoder_H() {
        cpu.setRegH(0x42);
        int val = cpu.getRegisterValueForCBOpCode(0x04);
        cpu.setRegisterValueForCBOpCode(0x04, val + 1);
        assertEquals(0x43, cpu.getRegH());
    }

    @Test public void testRegisterDecoder_L() {
        cpu.setRegL(0x42);
        int val = cpu.getRegisterValueForCBOpCode(0x05);
        cpu.setRegisterValueForCBOpCode(0x05, val + 1);
        assertEquals(0x43, cpu.getRegL());
    }

    @Test public void testRegisterDecoder_HL() {
        cpu.setRegHL(0x1234);
        mem.writeByte(0x1234, 0x42);
        int val = cpu.getRegisterValueForCBOpCode(0x06);
        cpu.setRegisterValueForCBOpCode(0x06, val + 1);
        assertEquals(0x43, mem.readByte(0x1234));
    }

    @Test public void testRegisterDecoder_A() {
        cpu.setRegA(0x42);
        int val = cpu.getRegisterValueForCBOpCode(0x07);
        cpu.setRegisterValueForCBOpCode(0x07, val + 1);
        assertEquals(0x43, cpu.getRegA());
    }

    @Test public void testOpCodeCB00_CB07() {
        // RLC
        applyTestToAllRegisters(0x00, new OpCodeTester()  {
            @Override
            void performTest(int opCode, RegGetterSetter reg, int expectedCycles) {
                cpu.setPC(0);
                mem.init(0xcb, opCode, 0xcb, opCode, 0xcb, opCode);
                reg.set(0x82);
                cpu.setC(false);
                StepCPU_AssertPcValueAndCycles(2, expectedCycles);
                assertFlagsAre_znhC();
                assertEquals(0x04, reg.get());
                StepCPU_AssertPcValueAndCycles(4, expectedCycles);
                assertEquals(0x09, reg.get());
                assertFlagsAre_znhc();
                reg.set(0x80);
                StepCPU_AssertPcValueAndCycles(6, expectedCycles);
                assertEquals(0x00, reg.get());
                assertFlagsAre_ZnhC();
            }
        });
    }

    @Test public void testOpCodeCB08_CB0F() {
        // RRC
        applyTestToAllRegisters(0x08, new OpCodeTester()  {
            @Override
            void performTest(int opCode, RegGetterSetter reg, int expectedCycles) {
                cpu.setPC(0);
                mem.init(0xcb, opCode, 0xcb, opCode, 0xcb, opCode);
                reg.set(0x81);
                cpu.setC(false);
                StepCPU_AssertPcValueAndCycles(2, expectedCycles);
                assertFlagsAre_znhC();
                assertEquals(0x40, reg.get());
                StepCPU_AssertPcValueAndCycles(4, expectedCycles);
                assertEquals(0xa0, reg.get());
                assertFlagsAre_znhc();
                reg.set(0x01);
                StepCPU_AssertPcValueAndCycles(6, expectedCycles);
                assertEquals(0x00, reg.get());
                assertFlagsAre_ZnhC();
            }
        });
    }

    @Test public void testOpCodeCB10_CB17() {
        // RL
        applyTestToAllRegisters(0x10, new OpCodeTester()  {
            @Override
            void performTest(int opCode, RegGetterSetter reg, int expectedCycles) {
                cpu.setPC(0);
                mem.init(0xcb, opCode, 0xcb, opCode, 0xcb, opCode);
                reg.set(0x82);
                cpu.setC(true);
                StepCPU_AssertPcValueAndCycles(2, expectedCycles);
                assertFlagsAre_znhC();
                assertEquals(0x05, reg.get());
                StepCPU_AssertPcValueAndCycles(4, expectedCycles);
                assertEquals(0x0a, reg.get());
                assertFlagsAre_znhc();
                reg.set(0x80);
                StepCPU_AssertPcValueAndCycles(6, expectedCycles);
                assertEquals(0x01, reg.get());
                assertFlagsAre_znhC();
            }
        });
    }

    @Test public void testOpCodeCB18_CB1F() {
        // RR
        applyTestToAllRegisters(0x18, new OpCodeTester()  {
            @Override
            void performTest(int opCode, RegGetterSetter reg, int expectedCycles) {
                cpu.setPC(0);
                mem.init(0xcb, opCode, 0xcb, opCode, 0xcb, opCode);
                reg.set(0x81);
                cpu.setC(true);
                StepCPU_AssertPcValueAndCycles(2, expectedCycles);
                assertFlagsAre_znhC();
                assertEquals(0xc0, reg.get());
                StepCPU_AssertPcValueAndCycles(4, expectedCycles);
                assertEquals(0x60, reg.get());
                assertFlagsAre_znhc();
                reg.set(0x01);
                StepCPU_AssertPcValueAndCycles(6, expectedCycles);
                assertEquals(0x80, reg.get());
                assertFlagsAre_znhC();
            }
        });
    }

    @Test public void testOpCodeCB20_CB27() {
        // SLA
        applyTestToAllRegisters(0x20, new OpCodeTester()  {
            @Override
            void performTest(int opCode, RegGetterSetter reg, int expectedCycles) {
                cpu.setPC(0);
                mem.init(0xcb, opCode, 0xcb, opCode, 0xcb, opCode);
                reg.set(0x82);
                cpu.setC(true);
                StepCPU_AssertPcValueAndCycles(2, expectedCycles);
                assertFlagsAre_znhC();
                assertEquals(0x04, reg.get());
                StepCPU_AssertPcValueAndCycles(4, expectedCycles);
                assertEquals(0x08, reg.get());
                assertFlagsAre_znhc();
                reg.set(0x80);
                StepCPU_AssertPcValueAndCycles(6, expectedCycles);
                assertEquals(0x00, reg.get());
                assertFlagsAre_ZnhC();
            }
        });
    }

    @Test public void testOpCodeCB28_CB2F() {
        // SRA
        applyTestToAllRegisters(0x28, new OpCodeTester()  {
            @Override
            void performTest(int opCode, RegGetterSetter reg, int expectedCycles) {
                cpu.setPC(0);
                mem.init(0xcb, opCode, 0xcb, opCode, 0xcb, opCode);
                reg.set(0x81);
                cpu.setC(true);
                StepCPU_AssertPcValueAndCycles(2, expectedCycles);
                assertFlagsAre_znhC();
                assertEquals(0xc0, reg.get());
                StepCPU_AssertPcValueAndCycles(4, expectedCycles);
                assertEquals(0xe0, reg.get());
                assertFlagsAre_znhc();
                reg.set(0x01);
                StepCPU_AssertPcValueAndCycles(6, expectedCycles);
                assertEquals(0x00, reg.get());
                assertFlagsAre_ZnhC();
            }
        });
    }

    @Test public void testOpCodeCB30_CB38() {
        // SWAP
        applyTestToAllRegisters(0x30, new OpCodeTester()  {
            @Override
            void performTest(int opCode, RegGetterSetter reg, int expectedCycles) {
                cpu.setPC(0);
                mem.init(0xcb, opCode, 0xcb, opCode, 0xcb, opCode);
                reg.set(0x42);
                cpu.setC(true);
                StepCPU_AssertPcValueAndCycles(2, expectedCycles);
                assertFlagsAre_znhc();
                assertEquals(0x24, reg.get());
                reg.set(0x00);
                StepCPU_AssertPcValueAndCycles(4, expectedCycles);
                assertEquals(0x00, reg.get());
                assertFlagsAre_Znhc();
            }
        });
    }

    @Test public void testOpCodeCB38_CB3F() {
        // SRL
        applyTestToAllRegisters(0x38, new OpCodeTester()  {
            @Override
            void performTest(int opCode, RegGetterSetter reg, int expectedCycles) {
                cpu.setPC(0);
                mem.init(0xcb, opCode, 0xcb, opCode, 0xcb, opCode);
                reg.set(0x81);
                cpu.setC(true);
                StepCPU_AssertPcValueAndCycles(2, expectedCycles);
                assertFlagsAre_znhC();
                assertEquals(0x40, reg.get());
                StepCPU_AssertPcValueAndCycles(4, expectedCycles);
                assertEquals(0x20, reg.get());
                assertFlagsAre_znhc();
                reg.set(0x01);
                StepCPU_AssertPcValueAndCycles(6, expectedCycles);
                assertEquals(0x00, reg.get());
                assertFlagsAre_ZnhC();
            }
        });
    }

    int counter;

    @Test public void testOpCodeCB_40_7F() {
        // BIT
        // Z 0 1 -

        OpCodeTester oct = new OpCodeTester()  {
            @Override
            void performTest(int opCode, RegGetterSetter reg, int expectedCycles) {
                cpu.setPC(0);
                mem.init(0xcb, opCode, 0xcb, opCode, 0xcb, opCode);
                reg.set(1 << counter);
                cpu.setC(true);
                cpu.setN(true);
                cpu.setH(false);
                StepCPU_AssertPcValueAndCycles(2, expectedCycles);
                assertFlagsAre_znHC();
                reg.set(2 << counter);
                StepCPU_AssertPcValueAndCycles(4, expectedCycles);
                assertFlagsAre_ZnHC();
            }
        };
        for(int i = 0; i < 8 ; i++) {
            counter = i;
            applyTestToAllRegisters(0x40 + 8*i, oct);
        }

    }

    @Test public void testOpCodeCB_80_BF() {
        // RES
        // - - - -

        OpCodeTester oct = new OpCodeTester()  {
            @Override
            void performTest(int opCode, RegGetterSetter reg, int expectedCycles) {
                cpu.setPC(0);
                mem.init(0xcb, opCode, 0xcb, opCode, 0xcb, opCode);
                reg.set(1 << counter);
                StepCPU_AssertPcValueAndCycles(2, expectedCycles);
                assertEquals(0, reg.get());
                reg.set((1 << counter) | 0x42);
                StepCPU_AssertPcValueAndCycles(4, expectedCycles);
                assertNotSame(0, reg.get());
            }
        };
        for(int i = 0; i < 8 ; i++) {
            counter = i;
            applyTestToAllRegisters(0x80 + 8*i, oct);
        }

    }

    @Test public void testOpCodeCB_C0_FF() {
        // SET
        // - - - -

        OpCodeTester oct = new OpCodeTester()  {
            @Override
            void performTest(int opCode, RegGetterSetter reg, int expectedCycles) {
                cpu.setPC(0);
                mem.init(0xcb, opCode, 0xcb, opCode, 0xcb, opCode);
                reg.set(1 << counter);
                StepCPU_AssertPcValueAndCycles(2, expectedCycles);
                assertEquals(1 << counter, reg.get());
                reg.set(0x42);
                StepCPU_AssertPcValueAndCycles(4, expectedCycles);
                assertEquals((1 << counter) | 0x42, reg.get());
            }
        };
        for(int i = 0; i < 8 ; i++) {
            counter = i;
            applyTestToAllRegisters(0xc0 + 8*i, oct);
        }

    }

    @Test public void testGetBitFromOpCode() {
        assertEquals(0, cpu.getBitFromOpCode(0x46));
        assertEquals(1, cpu.getBitFromOpCode(0x48));
        assertEquals(2, cpu.getBitFromOpCode(0x50));
        assertEquals(3, cpu.getBitFromOpCode(0x5f));
        assertEquals(4, cpu.getBitFromOpCode(0x62));
        assertEquals(5, cpu.getBitFromOpCode(0x69));
        assertEquals(6, cpu.getBitFromOpCode(0x72));
        assertEquals(7, cpu.getBitFromOpCode(0x7a));

        assertEquals(0, cpu.getBitFromOpCode(0x86));
        assertEquals(1, cpu.getBitFromOpCode(0x88));
        assertEquals(2, cpu.getBitFromOpCode(0x90));
        assertEquals(3, cpu.getBitFromOpCode(0x9f));
        assertEquals(4, cpu.getBitFromOpCode(0xa2));
        assertEquals(5, cpu.getBitFromOpCode(0xa9));
        assertEquals(6, cpu.getBitFromOpCode(0xb2));
        assertEquals(7, cpu.getBitFromOpCode(0xba));

        assertEquals(0, cpu.getBitFromOpCode(0xc6));
        assertEquals(1, cpu.getBitFromOpCode(0xc8));
        assertEquals(2, cpu.getBitFromOpCode(0xd0));
        assertEquals(3, cpu.getBitFromOpCode(0xdf));
        assertEquals(4, cpu.getBitFromOpCode(0xe2));
        assertEquals(5, cpu.getBitFromOpCode(0xe9));
        assertEquals(6, cpu.getBitFromOpCode(0xf2));
        assertEquals(7, cpu.getBitFromOpCode(0xfa));
    }


    private void applyTestToAllRegisters(int baseOpCode, OpCodeTester opCodeTester) {
            opCodeTester.performTest(baseOpCode, new RegGetterSetter() {
                @Override int get() { return cpu.getRegB(); }
                @Override void set(int value) { cpu.setRegB(value); }}, 8);
        opCodeTester.performTest(baseOpCode + 1, new RegGetterSetter() {
            @Override int get() { return cpu.getRegC(); }
            @Override void set(int value) { cpu.setRegC(value); }}, 8);
        opCodeTester.performTest(baseOpCode + 2, new RegGetterSetter() {
            @Override int get() { return cpu.getRegD(); }
            @Override void set(int value) { cpu.setRegD(value); }}, 8);
        opCodeTester.performTest(baseOpCode + 3, new RegGetterSetter() {
            @Override int get() { return cpu.getRegE(); }
            @Override void set(int value) { cpu.setRegE(value); }}, 8);
        opCodeTester.performTest(baseOpCode + 4, new RegGetterSetter() {
            @Override int get() { return cpu.getRegH(); }
            @Override void set(int value) { cpu.setRegH(value); }}, 8);
        opCodeTester.performTest(baseOpCode + 5, new RegGetterSetter() {
            @Override int get() { return cpu.getRegL(); }
            @Override void set(int value) { cpu.setRegL(value); }}, 8);
            cpu.setRegHL(0x1234);
        opCodeTester.performTest(baseOpCode + 6, new RegGetterSetter() {
            @Override int get() { return mem.readByte(cpu.getRegHL()); }
            @Override void set(int value) { mem.writeByte(cpu.getRegHL(), value); }}, 16);
        opCodeTester.performTest(baseOpCode + 7, new RegGetterSetter() {
            @Override int get() { return cpu.getRegA(); }
            @Override void set(int value) { cpu.setRegA(value); }}, 8);
    }

    private abstract class OpCodeTester {
        abstract void performTest(int opCode, RegGetterSetter reg, int expectedCycles);
    }

    private abstract class RegGetterSetter {
        abstract int get();
        abstract void set(int value);
    }
}

