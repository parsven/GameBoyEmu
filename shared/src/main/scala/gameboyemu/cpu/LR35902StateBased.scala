package gameboyemu.cpu

import java.util

import gameboyemu.system.Utils

/**
  * Created by IntelliJ IDEA.
  * User: par
  * Date: 2011-08-10
  * Time: 21:55
  * To change this template use File | Settings | File Templates.
  */

class LR35902StateBased(val vm: VirtualMemory16) extends CpuState with Cpu with InterruptLines {
  private var flagZ = false
  private var flagN = false
  private var flagH = false
  private var flagC = false
  private var flagI = false
  private var pc = 0
  private var A = 0
  private var BC = 0
  private var DE = 0
  private var HL = 0
  private var SP = 0
  private var stopped = false
  private var halted = false

  override def requestVblankIrq(): Unit = {
    if (flagI) {
      System.err.println("VBLANK IRQ HANDLED!")
      SP -= 2
      vm.writeWord(SP, pc)
      pc = 0x40
    }
  }
  def requestLCDCStatusIrq(): Unit = ???

  @throws[BreakPointException]
  override def executeScanline(carry: Int, breakPoints: util.Set[Integer]): Int = {
    var cyclesLeftOnScanLine = 456 - carry
    while ( cyclesLeftOnScanLine > 0 && !halted) {
      if (breakPoints.contains(pc)) throw new BreakPointException
      cyclesLeftOnScanLine -= step
    }
    -cyclesLeftOnScanLine
  }

  override def getAF: Int = {
    var h = 0
    h |= (if (flagZ) 0x80
    else 0x00)
    h |= (if (flagN) 0x40
    else 0x00)
    h |= (if (flagH) 0x20
    else 0x00)
    h |= (if (flagC) 0x10
    else 0x00)
    (A << 8) | h
  }

  override def getBC: Int = BC

  override def getDE: Int = DE

  override def getHL: Int = HL

  override def getSP: Int = SP

  def setSP(sp: Int): Unit = SP = sp

  override def getPC: Int = pc

  def getI: Boolean = flagI

  def setI(flagI: Boolean): Unit = this.flagI = flagI

  override def getZ: Boolean = flagZ

  override def setZ(z: Boolean): Unit = flagZ = z

  override def getN: Boolean = flagN

  override def setN(n: Boolean): Unit = flagN = n

  override def getH: Boolean = flagH

  override def setH(h: Boolean): Unit = flagH = h

  override def getC: Boolean = flagC

  override def setC(c: Boolean): Unit = flagC = c

  def setPC(pc: Int): Unit = this.pc = pc

  def getPc: Int = pc

  override def step: Int = {
    val opCode = vm.readByte(pc)
    var t = 0
    var r8 = 0
    opCode match {
      case 0x00 =>
        pc += 1
        return 4
      case 0x01 =>
        BC = vm.readWord(pc + 1)
        pc += 3
        pc &= 0xffff
        return 12
      case 0x02 =>
        vm.writeByte(BC, A)
        pc += 1
        pc &= 0xffff
        return 8
      case 0x03 =>
        BC += 1
        BC &= 0xffff
        pc += 1
        pc &= 0xffff
        return 8
      case 0x04 =>
        t = (getRegB + 1) & 0xff
        setRegB(t)
        flagZ = t == 0
        flagH = (t & 0x0f) == 0
        flagN = false
        pc += 1
        pc &= 0xffff
        return 4
      case 0x05 =>
        t = (getRegB - 1) & 0xff
        setRegB(t)
        flagZ = t == 0
        flagH = (t & 0x0f) == 0x0f
        flagN = true
        pc += 1
        pc &= 0xffff
        return 4
      case 0x06 =>
        setRegB(vm.readByte(pc + 1))
        pc += 2
        pc &= 0xffff
        return 8
      case 0x07 =>
        t = getRegA << 1
        if (flagC) {
          t = t + 1
        }
        flagC = (t & 0x100) == 0x100
        setRegA(t & 0xff)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x08 =>
        vm.writeWord(vm.readWord(pc + 1), SP)
        pc += 3
        pc &= 0xffff
        return 20
      case 0x09 =>
        t = getRegHL + getRegBC
        flagC = t > 0xffff
        flagH = (getRegHL & 0x0fff) + (getRegBC & 0x0fff) > 0x0fff
        flagN = false
        setRegHL(t & 0xffff)
        pc += 1
        pc &= 0xffff
        return 8
      case 0x0A =>
        setRegA(vm.readByte(BC))
        pc += 1
        pc &= 0xffff
        return 8
      case 0x0B =>
        BC -= 1
        BC &= 0xffff
        pc += 1
        pc &= 0xffff
        return 8
      case 0x0C =>
        t = (getRegC + 1) & 0xff
        setRegC(t)
        flagZ = t == 0
        flagH = (t & 0x0f) == 0
        flagN = false
        pc += 1
        pc &= 0xffff
        return 4
      case 0x0D =>
        t = (getRegC - 1) & 0xff
        setRegC(t)
        flagZ = t == 0
        flagH = (t & 0x0f) == 0x0f
        flagN = true
        pc += 1
        pc &= 0xffff
        return 4
      case 0x0E =>
        setRegC(vm.readByte(pc + 1))
        pc += 2
        pc &= 0xffff
        return 8
      case 0x0f =>
        t = getRegA >>> 1
        if (flagC) {
          t = t + 0x80
        }
        flagC = (getRegA & 0x01) == 0x01
        setRegA(t)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x10 =>
        stopped = true
        pc += 2
        pc &= 0xffff
        return 4
      case 0x11 =>
        DE = vm.readWord(pc + 1)
        pc += 3
        pc &= 0xffff
        return 12
      case 0x12 =>
        vm.writeByte(DE, A)
        pc += 1
        pc &= 0xffff
        return 8
      case 0x13 =>
        DE += 1
        DE &= 0xffff
        pc += 1
        pc &= 0xffff
        return 8
      case 0x14 =>
        t = (getRegD + 1) & 0xff
        setRegD(t)
        flagZ = t == 0
        flagH = (t & 0x0f) == 0
        flagN = false
        pc += 1
        pc &= 0xffff
        return 4
      case 0x15 =>
        t = (getRegD - 1) & 0xff
        setRegD(t)
        flagZ = t == 0
        flagH = (t & 0x0f) == 0x0f
        flagN = true
        pc += 1
        pc &= 0xffff
        return 4
      case 0x16 =>
        setRegD(vm.readByte(pc + 1))
        pc += 2
        pc &= 0xffff
        return 8
      case 0x17 =>
        t = getRegA << 1
        flagC = (t & 0x100) == 0x100
        setRegA(t & 0xff)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x18 =>
        t = vm.readByte(pc + 1)
        if (t > 0x7f) t |= 0xff00
        pc = pc + 2 + t
        pc &= 0xffff
        return 12
      case 0x19 =>
        t = getRegHL + getRegDE
        flagC = t > 0xffff
        flagH = (getRegHL & 0x0fff) + (getRegDE & 0x0fff) > 0x0fff
        flagN = false
        setRegHL(t & 0xffff)
        pc += 1
        pc &= 0xffff
        return 8
      case 0x1A =>
        setRegA(vm.readByte(DE))
        pc += 1
        pc &= 0xffff
        return 8
      case 0x1B =>
        DE -= 1
        DE &= 0xffff
        pc += 1
        pc &= 0xffff
        return 8
      case 0x1C =>
        t = (getRegE + 1) & 0xff
        setRegE(t)
        flagZ = t == 0
        flagH = (t & 0x0f) == 0
        flagN = false
        pc += 1
        pc &= 0xffff
        return 4
      case 0x1D =>
        t = (getRegE - 1) & 0xff
        setRegE(t)
        flagZ = t == 0
        flagH = (t & 0x0f) == 0x0f
        flagN = true
        pc += 1
        pc &= 0xffff
        return 4
      case 0x1E =>
        setRegE(vm.readByte(pc + 1))
        pc += 2
        pc &= 0xffff
        return 8
      case 0x1f =>
        t = getRegA >>> 1
        flagC = (getRegA & 0x01) == 0x01
        setRegA(t)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x20 =>
        t = vm.readByte(pc + 1)
        if (t > 0x7f) t |= 0xff00
        pc += 2
        if (!flagZ) {
          pc += t
          pc &= 0xffff
          return 12
        }
        else {
          pc &= 0xffff
          return 8
        }
      case 0x21 =>
        HL = vm.readWord(pc + 1)
        pc += 3
        pc &= 0xffff
        return 12
      case 0x22 =>
        vm.writeByte(HL, A)
        HL += 1
        HL &= 0xffff
        pc += 1
        pc &= 0xffff
        return 8
      case 0x23 =>
        HL += 1
        HL &= 0xffff
        pc += 1
        pc &= 0xffff
        return 8
      case 0x24 =>
        t = (getRegH + 1) & 0xff
        setRegH(t)
        flagZ = t == 0
        flagH = (t & 0x0f) == 0
        flagN = false
        pc += 1
        pc &= 0xffff
        return 4
      case 0x25 =>
        t = (getRegH - 1) & 0xff
        setRegH(t)
        flagZ = t == 0
        flagH = (t & 0x0f) == 0x0f
        flagN = true
        pc += 1
        pc &= 0xffff
        return 4
      case 0x26 =>
        setRegH(vm.readByte(pc + 1))
        pc += 2
        pc &= 0xffff
        return 8
      case 0x28 =>
        t = vm.readByte(pc + 1)
        if (t > 0x7f) t |= 0xff00
        pc += 2
        if (flagZ) {
          pc += t
          pc &= 0xffff
          return 12
        }
        else {
          pc &= 0xffff
          return 8
        }
      case 0x29 =>
        t = getRegHL + getRegHL
        flagC = t > 0xffff
        flagH = (getRegHL & 0x0fff) + (getRegHL & 0x0fff) > 0x0fff
        flagN = false
        setRegHL(t & 0xffff)
        pc += 1
        pc &= 0xffff
        return 8
      case 0x2A =>
        setRegA(vm.readByte({
          HL += 1; HL - 1
        }))
        pc += 1
        pc &= 0xffff
        return 8
      case 0x2B =>
        HL -= 1
        HL &= 0xffff
        pc += 1
        pc &= 0xffff
        return 8
      case 0x2C =>
        t = (getRegL + 1) & 0xff
        setRegL(t)
        flagZ = t == 0
        flagH = (t & 0x0f) == 0
        flagN = false
        pc += 1
        pc &= 0xffff
        return 4
      case 0x2D =>
        t = (getRegL - 1) & 0xff
        setRegL(t)
        flagZ = t == 0
        flagH = (t & 0x0f) == 0x0f
        flagN = true
        pc += 1
        pc &= 0xffff
        return 4
      case 0x2E =>
        setRegL(vm.readByte(pc + 1))
        pc += 2
        pc &= 0xffff
        return 8
      case 0x2F =>
        A = A ^ 0xff
        flagN = true
        flagH = true
        pc += 1
        pc &= 0xffff
        return 4
      case 0x30 =>
        t = vm.readByte(pc + 1)
        if (t > 0x7f) t |= 0xff00
        pc += 2
        if (!flagC) {
          pc += t
          pc &= 0xffff
          return 12
        }
        else {
          pc &= 0xffff
          return 8
        }
      case 0x31 =>
        SP = vm.readWord(pc + 1)
        pc += 3
        pc &= 0xffff
        return 12
      case 0x32 =>
        vm.writeByte(HL, A)
        HL -= 1
        HL &= 0xffff
        pc += 1
        pc &= 0xffff
        return 8
      case 0x33 =>
        SP += 1
        SP &= 0xffff
        pc += 1
        pc &= 0xffff
        return 8
      case 0x34 =>
        t = (vm.readByte(HL) + 1) & 0xff
        vm.writeByte(HL, t)
        flagZ = t == 0
        flagH = (t & 0x0f) == 0
        flagN = false
        pc += 1
        pc &= 0xffff
        return 12
      case 0x35 =>
        t = (vm.readByte(HL) - 1) & 0xff
        vm.writeByte(HL, t)
        flagZ = t == 0
        flagH = (t & 0x0f) == 0x0f
        flagN = true
        pc += 1
        pc &= 0xffff
        return 12
      case 0x36 =>
        vm.writeByte(HL, vm.readByte(pc + 1))
        pc += 2
        pc &= 0xffff
        return 12
      case 0x37 =>
        flagN = false
        flagH = false
        flagC = true
        pc += 1
        pc &= 0xffff
        return 4
      case 0x38 =>
        t = vm.readByte(pc + 1)
        if (t > 0x7f) t |= 0xff00
        pc += 2
        if (flagC) {
          pc += t
          pc &= 0xffff
          return 12
        }
        else {
          pc &= 0xffff
          return 8
        }
      case 0x39 =>
        t = getRegHL + getRegSP
        flagC = t > 0xffff
        flagH = (getRegHL & 0x0fff) + (getRegSP & 0x0fff) > 0x0fff
        flagN = false
        setRegHL(t & 0xffff)
        pc += 1
        pc &= 0xffff
        return 8
      case 0x3A =>
        setRegA(vm.readByte({
          HL -= 1; HL + 1
        }))
        HL &= 0xffff
        pc += 1
        pc &= 0xffff
        return 8
      case 0x3B =>
        SP -= 1
        SP &= 0xffff
        pc += 1
        pc &= 0xffff
        return 8
      case 0x3C =>
        t = (getRegA + 1) & 0xff
        setRegA(t)
        flagZ = t == 0
        flagH = (t & 0x0f) == 0
        flagN = false
        pc += 1
        pc &= 0xffff
        return 4
      case 0x3D =>
        t = (getRegA - 1) & 0xff
        setRegA(t)
        flagZ = t == 0
        flagH = (t & 0x0f) == 0x0f
        flagN = true
        pc += 1
        pc &= 0xffff
        return 4
      case 0x3E =>
        setRegA(vm.readByte(pc + 1))
        pc += 2
        pc &= 0xffff
        return 8
      case 0x3F =>
        flagN = false
        flagH = false
        flagC = !flagC
        pc += 1
        pc &= 0xffff
        return 4
      case 0x40 =>
        setRegB(getRegB)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x41 =>
        setRegB(getRegC)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x42 =>
        setRegB(getRegD)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x43 =>
        setRegB(getRegE)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x44 =>
        setRegB(getRegH)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x45 =>
        setRegB(getRegL)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x46 =>
        setRegB(vm.readByte(HL))
        pc += 1
        pc &= 0xffff
        return 8
      case 0x47 =>
        setRegB(getRegA)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x48 =>
        setRegC(getRegB)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x49 =>
        setRegC(getRegC)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x4A =>
        setRegC(getRegD)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x4B =>
        setRegC(getRegE)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x4C =>
        setRegC(getRegH)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x4D =>
        setRegC(getRegL)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x4E =>
        setRegC(vm.readByte(HL))
        pc += 1
        pc &= 0xffff
        return 8
      case 0x4F =>
        setRegC(getRegA)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x50 =>
        setRegD(getRegB)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x51 =>
        setRegD(getRegC)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x52 =>
        setRegD(getRegD)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x53 =>
        setRegD(getRegE)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x54 =>
        setRegD(getRegH)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x55 =>
        setRegD(getRegL)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x56 =>
        setRegD(vm.readByte(HL))
        pc += 1
        pc &= 0xffff
        return 8
      case 0x57 =>
        setRegD(getRegA)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x58 =>
        setRegE(getRegB)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x59 =>
        setRegE(getRegC)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x5A =>
        setRegE(getRegD)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x5B =>
        setRegE(getRegE)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x5C =>
        setRegE(getRegH)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x5D =>
        setRegE(getRegL)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x5E =>
        setRegE(vm.readByte(HL))
        pc += 1
        pc &= 0xffff
        return 8
      case 0x5F =>
        setRegE(getRegA)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x60 =>
        setRegH(getRegB)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x61 =>
        setRegH(getRegC)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x62 =>
        setRegH(getRegD)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x63 =>
        setRegH(getRegE)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x64 =>
        setRegH(getRegH)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x65 =>
        setRegH(getRegL)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x66 =>
        setRegH(vm.readByte(HL))
        pc += 1
        pc &= 0xffff
        return 8
      case 0x67 =>
        setRegH(getRegA)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x68 =>
        setRegL(getRegB)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x69 =>
        setRegL(getRegC)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x6A =>
        setRegL(getRegD)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x6B =>
        setRegL(getRegE)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x6C =>
        setRegL(getRegH)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x6D =>
        setRegL(getRegL)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x6E =>
        setRegL(vm.readByte(HL))
        pc += 1
        pc &= 0xffff
        return 8
      case 0x6F =>
        setRegL(getRegA)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x70 =>
        vm.writeByte(HL, getRegB)
        pc += 1
        pc &= 0xffff
        return 8
      case 0x71 =>
        vm.writeByte(HL, getRegC)
        pc += 1
        pc &= 0xffff
        return 8
      case 0x72 =>
        vm.writeByte(HL, getRegD)
        pc += 1
        pc &= 0xffff
        return 8
      case 0x73 =>
        vm.writeByte(HL, getRegE)
        pc += 1
        pc &= 0xffff
        return 8
      case 0x74 =>
        vm.writeByte(HL, getRegH)
        pc += 1
        pc &= 0xffff
        return 8
      case 0x75 =>
        vm.writeByte(HL, getRegL)
        pc += 1
        pc &= 0xffff
        return 8
      case 0x76 =>
        halted = true
        return 4
      case 0x77 =>
        vm.writeByte(HL, getRegA)
        pc += 1
        pc &= 0xffff
        return 8
      case 0x78 =>
        setRegA(getRegB)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x79 =>
        setRegA(getRegC)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x7A =>
        setRegA(getRegD)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x7B =>
        setRegA(getRegE)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x7C =>
        setRegA(getRegH)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x7D =>
        setRegA(getRegL)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x7E =>
        setRegA(vm.readByte(HL))
        pc += 1
        pc &= 0xffff
        return 8
      case 0x7F =>
        setRegA(getRegA)
        pc += 1
        pc &= 0xffff
        return 4
      case 0x80 =>
        addToA(getRegB)
        return 4
      case 0x81 =>
        addToA(getRegC)
        return 4
      case 0x82 =>
        addToA(getRegD)
        return 4
      case 0x83 =>
        addToA(getRegE)
        return 4
      case 0x84 =>
        addToA(getRegH)
        return 4
      case 0x85 =>
        addToA(getRegL)
        return 4
      case 0x86 =>
        addToA(vm.readByte(HL))
        return 8
      case 0x87 =>
        addToA(getRegA)
        return 4
      case 0x88 =>
        adcToA(getRegB)
        return 4
      case 0x89 =>
        adcToA(getRegC)
        return 4
      case 0x8A =>
        adcToA(getRegD)
        return 4
      case 0x8B =>
        adcToA(getRegE)
        return 4
      case 0x8C =>
        adcToA(getRegH)
        return 4
      case 0x8D =>
        adcToA(getRegL)
        return 4
      case 0x8E =>
        adcToA(vm.readByte(HL))
        return 8
      case 0x8F =>
        adcToA(getRegA)
        return 4
      case 0x90 =>
        subToA(getRegB)
        return 4
      case 0x91 =>
        subToA(getRegC)
        return 4
      case 0x92 =>
        subToA(getRegD)
        return 4
      case 0x93 =>
        subToA(getRegE)
        return 4
      case 0x94 =>
        subToA(getRegH)
        return 4
      case 0x95 =>
        subToA(getRegL)
        return 4
      case 0x96 =>
        subToA(vm.readByte(HL))
        return 8
      case 0x97 =>
        subToA(getRegA)
        return 4
      case 0x98 =>
        sbcToA(getRegB)
        return 4
      case 0x99 =>
        sbcToA(getRegC)
        return 4
      case 0x9A =>
        sbcToA(getRegD)
        return 4
      case 0x9B =>
        sbcToA(getRegE)
        return 4
      case 0x9C =>
        sbcToA(getRegH)
        return 4
      case 0x9D =>
        sbcToA(getRegL)
        return 4
      case 0x9E =>
        sbcToA(vm.readByte(HL))
        return 8
      case 0x9F =>
        sbcToA(getRegA)
        return 4
      case 0xa0 =>
        andToA(getRegB)
        return 4
      case 0xa1 =>
        andToA(getRegC)
        return 4
      case 0xa2 =>
        andToA(getRegD)
        return 4
      case 0xa3 =>
        andToA(getRegE)
        return 4
      case 0xa4 =>
        andToA(getRegH)
        return 4
      case 0xa5 =>
        andToA(getRegL)
        return 4
      case 0xa6 =>
        andToA(vm.readByte(HL))
        return 8
      case 0xa7 =>
        andToA(getRegA)
        return 4
      case 0xa8 =>
        xorToA(getRegB)
        return 4
      case 0xa9 =>
        xorToA(getRegC)
        return 4
      case 0xaa =>
        xorToA(getRegD)
        return 4
      case 0xab =>
        xorToA(getRegE)
        return 4
      case 0xac =>
        xorToA(getRegH)
        return 4
      case 0xad =>
        xorToA(getRegL)
        return 4
      case 0xae =>
        xorToA(vm.readByte(HL))
        return 8
      case 0xaf =>
        xorToA(getRegA)
        return 4
      case 0xb0 =>
        orToA(getRegB)
        return 4
      case 0xb1 =>
        orToA(getRegC)
        return 4
      case 0xb2 =>
        orToA(getRegD)
        return 4
      case 0xb3 =>
        orToA(getRegE)
        return 4
      case 0xb4 =>
        orToA(getRegH)
        return 4
      case 0xb5 =>
        orToA(getRegL)
        return 4
      case 0xb6 =>
        orToA(vm.readByte(HL))
        return 8
      case 0xb7 =>
        orToA(getRegA)
        return 4
      case 0xb8 =>
        cpToA(getRegB)
        return 4
      case 0xb9 =>
        cpToA(getRegC)
        return 4
      case 0xba =>
        cpToA(getRegD)
        return 4
      case 0xbb =>
        cpToA(getRegE)
        return 4
      case 0xbc =>
        cpToA(getRegH)
        return 4
      case 0xbd =>
        cpToA(getRegL)
        return 4
      case 0xbe =>
        cpToA(vm.readByte(HL))
        return 8
      case 0xbf =>
        cpToA(getRegA)
        return 4
      case 0xc0 =>
        if (!flagZ) {
          pc = vm.readWord(SP)
          SP += 2
          SP &= 0xffff
          return 20
        }
        else {
          pc += 1
          pc &= 0xffff
          return 8
        }
      case 0xc1 =>
        BC = vm.readWord(SP)
        SP += 2
        SP &= 0xffff
        pc += 1
        pc &= 0xffff
        return 12
      case 0xc2 =>
        if (!flagZ) {
          pc = vm.readWord(pc + 1)
          return 16
        }
        else {
          pc += 3
          pc &= 0xffff
          return 12
        }
      case 0xc3 =>
        pc = vm.readWord(pc + 1)
        return 16
      case 0xc4 =>
        t = vm.readWord(pc + 1)
        pc += 3
        pc &= 0xffff
        if (!flagZ) {
          SP -= 2
          vm.writeWord(SP, pc)
          pc = t
          return 24
        }
        else return 12
      case 0xc5 =>
        SP -= 2
        SP &= 0xffff
        vm.writeWord(SP, BC)
        pc += 1
        pc &= 0xffff
        return 16
      case 0xc6 =>
        pc += 1
        addToA(vm.readByte(pc))
        return 8
      case 0xc7 =>
        pc += 1
        pc &= 0xffff
        SP -= 2
        vm.writeWord(SP, pc)
        pc = 0x0000
        return 16
      case 0xc8 =>
        if (flagZ) {
          pc = vm.readWord(SP)
          SP += 2
          SP &= 0xffff
          return 20
        }
        else {
          pc += 1
          pc &= 0xffff
          return 8
        }
      case 0xc9 =>
        pc = vm.readWord(SP)
        SP += 2
        SP &= 0xffff
        return 16
      case 0xca =>
        if (flagZ) {
          pc = vm.readWord(pc + 1)
          return 16
        }
        else {
          pc += 3
          pc &= 0xffff
          return 12
        }
      case 0xcb =>
        return cbOpCode
      case 0xcc =>
        t = vm.readWord(pc + 1)
        pc += 3
        pc &= 0xffff
        if (flagZ) {
          SP -= 2
          vm.writeWord(SP, pc)
          pc = t
          return 24
        }
        else return 12
      case 0xcd =>
        t = vm.readWord(pc + 1)
        pc += 3
        pc &= 0xffff
        SP -= 2
        vm.writeWord(SP, pc)
        pc = t
        return 24
      case 0xce =>
        pc += 1
        adcToA(vm.readByte(pc))
        return 8
      case 0xcf =>
        pc += 1
        pc &= 0xffff
        SP -= 2
        vm.writeWord(SP, pc)
        pc = 0x0008
        return 16
      case 0xd0 =>
        if (!flagC) {
          pc = vm.readWord(SP)
          SP += 2
          SP &= 0xffff
          return 20
        }
        else {
          pc += 1
          pc &= 0xffff
          return 8
        }
      case 0xd1 =>
        DE = vm.readWord(SP)
        SP += 2
        SP &= 0xffff
        pc += 1
        pc &= 0xffff
        return 12
      case 0xd2 =>
        if (!flagC) {
          pc = vm.readWord(pc + 1)
          return 16
        }
        else {
          pc += 3
          pc &= 0xffff
          return 12
        }
      case 0xd4 =>
        t = vm.readWord(pc + 1)
        pc += 3
        pc &= 0xffff
        if (!flagC) {
          SP -= 2
          vm.writeWord(SP, pc)
          pc = t
          return 24
        }
        else return 12
      case 0xd5 =>
        SP -= 2
        SP &= 0xffff
        vm.writeWord(SP, DE)
        pc += 1
        pc &= 0xffff
        return 16
      case 0xd6 =>
        pc += 1
        subToA(vm.readByte(pc))
        return 8
      case 0xd7 =>
        pc += 1
        pc &= 0xffff
        SP -= 2
        vm.writeWord(SP, pc)
        pc = 0x0010
        return 16
      case 0xd8 =>
        if (flagC) {
          pc = vm.readWord(SP)
          SP += 2
          SP &= 0xffff
          return 20
        }
        else {
          pc += 1
          pc &= 0xffff
          return 8
        }
      case 0xd9 =>
        pc = vm.readWord(SP)
        SP += 2
        SP &= 0xffff
        flagI = true
        return 16
      case 0xda =>
        if (flagC) {
          pc = vm.readWord(pc + 1)
          return 16
        }
        else {
          pc += 3
          pc &= 0xffff
          return 12
        }
      case 0xdc =>
        t = vm.readWord(pc + 1)
        pc += 3
        pc &= 0xffff
        if (flagC) {
          SP -= 2
          vm.writeWord(SP, pc)
          pc = t
          return 24
        }
        else return 12
      case 0xde =>
        pc += 1
        sbcToA(vm.readByte(pc))
        return 8
      case 0xdf =>
        pc += 1
        pc &= 0xffff
        SP -= 2
        vm.writeWord(SP, pc)
        pc = 0x0018
        return 16
      case 0xE0 =>
        t = vm.readByte(pc + 1)
        vm.writeByte(0xff00 + t, A)
        pc += 2
        pc &= 0xffff
        return 12
      case 0xE1 =>
        HL = vm.readWord(SP)
        SP += 2
        SP &= 0xffff
        pc += 1
        pc &= 0xffff
        return 12
      case 0xE2 =>
        vm.writeByte(0xff00 + getRegC, A)
        pc += 1
        pc &= 0xffff
        return 12
      case 0xe5 =>
        SP -= 2
        SP &= 0xffff
        vm.writeWord(SP, HL)
        pc += 1
        pc &= 0xffff
        return 16
      case 0xe6 =>
        pc += 1
        andToA(vm.readByte(pc))
        return 8
      case 0xe7 =>
        pc += 1
        pc &= 0xffff
        SP -= 2
        vm.writeWord(SP, pc)
        pc = 0x0020
        return 16
      case 0xe8 =>
        flagN = false
        flagZ = false
        r8 = vm.readByte(pc + 1)
        if (r8 > 0x7f) r8 |= 0xff00
        t = SP + r8
        flagC = (vm.readByte(pc + 1) + (SP & 0xff)) > 0xff
        flagH = (SP & 0x0f) + (r8 & 0x0f) > 0x0f
        SP = t & 0xffff
        pc += 2
        pc &= 0xffff
        return 16
      case 0xe9 =>
        pc = HL
        return 4
      case 0xea =>
        vm.writeByte(vm.readWord(pc + 1), getRegA)
        pc += 3
        pc &= 0xffff
        return 16
      case 0xee =>
        pc += 1
        xorToA(vm.readByte(pc))
        return 8
      case 0xef =>
        pc += 1
        pc &= 0xffff
        SP -= 2
        vm.writeWord(SP, pc)
        pc = 0x0028
        return 16
      case 0xF0 =>
        t = vm.readByte(pc + 1)
        A = vm.readByte(0xff00 + t)
        pc += 2
        pc &= 0xffff
        return 12
      case 0xF1 =>
        t = vm.readByte(SP)
        A = vm.readByte(SP + 1)
        SP += 2
        SP &= 0xffff
        flagZ = (t & 0x80) != 0
        flagN = (t & 0x40) != 0
        flagH = (t & 0x20) != 0
        flagC = (t & 0x10) != 0
        pc += 1
        pc &= 0xffff
        return 12
      case 0xF2 =>
        A = vm.readByte(0xff00 + getRegC)
        pc += 1
        pc &= 0xffff
        return 8
      case 0xf3 =>
        flagI = false
        pc += 1
        pc &= 0xffff
        return 4
      case 0xf5 =>
        SP -= 2
        SP &= 0xffff
        t = A << 8
        if (flagZ) t |= 0x80
        if (flagN) t |= 0x40
        if (flagH) t |= 0x20
        if (flagC) t |= 0x10
        vm.writeWord(SP, t)
        pc += 1
        pc &= 0xffff
        return 16
      case 0xf6 =>
        pc += 1
        orToA(vm.readByte(pc))
        return 8
      case 0xf7 =>
        pc += 1
        pc &= 0xffff
        SP -= 2
        vm.writeWord(SP, pc)
        pc = 0x0030
        return 16
      case 0xf8 =>
        flagN = false
        flagZ = false
        r8 = vm.readByte(pc + 1)
        if (r8 > 0x7f) r8 |= 0xff00
        t = SP + r8
        flagC = (vm.readByte(pc + 1) + (SP & 0xff)) > 0xff
        flagH = (SP & 0x0fff) + (r8 & 0x0fff) > 0x0fff
        HL = t & 0xffff
        pc += 2
        pc &= 0xffff
        return 12
      case 0xf9 =>
        SP = HL
        pc += 1
        pc &= 0xffff
        return 8
      case 0xfa =>
        setRegA(vm.readByte(vm.readWord(pc + 1)))
        pc += 3
        pc &= 0xffff
        return 16
      case 0xfb =>
        flagI = true
        pc += 1
        pc &= 0xffff
        return 4
      case 0xfe =>
        pc += 1
        cpToA(vm.readByte(pc))
        return 8
      case 0xff =>
        pc += 1
        pc &= 0xffff
        SP -= 2
        vm.writeWord(SP, pc)
        pc = 0x0038
        return 16
      case _ =>
    }
    throw new IllegalStateException("OpCode:" + opCode + " not yet implemented.")
  }

  private def cbOpCode: Int = {
    val opCode = vm.readByte(pc + 1)
    pc += 2
    pc &= 0xffff
    var t = 0
    var newRegValue = 0
    opCode & 0xf8 match {
      case 0x00 =>
        t = getRegisterValueForCBOpCode(opCode)
        t = t << 1
        if (flagC) t |= 0x01
        flagN = false
        flagH = false
        flagC = t > 0xff
        t &= 0xff
        flagZ = t == 0
        setRegisterValueForCBOpCode(opCode, t)
        return cyclesForCbOpCode(opCode)
      case 0x08 =>
        t = getRegisterValueForCBOpCode(opCode)
        val carry = (t & 0x01) != 0
        t = t >> 1
        if (flagC) t |= 0x80
        flagN = false
        flagH = false
        flagC = carry
        flagZ = t == 0
        setRegisterValueForCBOpCode(opCode, t)
        return cyclesForCbOpCode(opCode)
      case 0x10 =>
        t = getRegisterValueForCBOpCode(opCode)
        t = t << 1
        flagN = false
        flagH = false
        flagC = t > 0xff
        t &= 0xff
        if (flagC) t |= 0x01
        flagZ = t == 0
        setRegisterValueForCBOpCode(opCode, t)
        return cyclesForCbOpCode(opCode)
      case 0x18 =>
        t = getRegisterValueForCBOpCode(opCode)
        flagC = (t & 0x01) != 0
        t = t >> 1
        if (flagC) t |=0x80
        flagN = false
        flagH = false
        flagZ = t == 0
        setRegisterValueForCBOpCode(opCode, t)
        return cyclesForCbOpCode(opCode)
      case 0x20 =>
        t = getRegisterValueForCBOpCode(opCode)
        t = t << 1
        flagN = false
        flagH = false
        flagC = t > 0xff
        t &= 0xff
        flagZ = t == 0
        setRegisterValueForCBOpCode(opCode, t)
        return cyclesForCbOpCode(opCode)
      case 0x28 =>
        t = getRegisterValueForCBOpCode(opCode)
        val isNeg = (t & 0x80) != 0
        flagC = (t & 0x01) != 0
        t = t >> 1
        if (isNeg) t |= 0x80
        flagN = false
        flagH = false
        flagZ = t == 0
        setRegisterValueForCBOpCode(opCode, t)
        return cyclesForCbOpCode(opCode)
      case 0x30 =>
        t = getRegisterValueForCBOpCode(opCode)
        val hiNibble = (t & 0xf0) >> 4
        val loNibble = t & 0x0f
        flagC = false
        flagN = false
        flagH = false
        flagZ = t == 0
        setRegisterValueForCBOpCode(opCode, loNibble << 4 | hiNibble)
        return cyclesForCbOpCode(opCode)
      case 0x38 =>
        t = getRegisterValueForCBOpCode(opCode)
        flagC = (t & 0x01) != 0
        t = t >> 1
        flagN = false
        flagH = false
        flagZ = t == 0
        setRegisterValueForCBOpCode(opCode, t)
        return cyclesForCbOpCode(opCode)
      case 0x40 | 0x48 | 0x50 | 0x58 | 0x60 | 0x68 | 0x70 | 0x78 =>
        t = getBitFromOpCode(opCode)
        flagZ = ((1 << t) & getRegisterValueForCBOpCode(opCode)) == 0
        flagN = false
        flagH = true
        return cyclesForCbOpCode(opCode)
      case 0x80 | 0x88| 0x90 | 0x98| 0xa0 | 0xa8 | 0xb0 | 0xb8 =>
        t = getBitFromOpCode(opCode)
        newRegValue = (~(1 << t)) & getRegisterValueForCBOpCode(opCode)
        setRegisterValueForCBOpCode(opCode, newRegValue)
        return cyclesForCbOpCode(opCode)
      case 0xc0 | 0xc8 | 0xd0 | 0xd8 | 0xe0 | 0xe8 | 0xf0 | 0xf8 =>
        t = getBitFromOpCode(opCode)
        newRegValue = (1 << t) | getRegisterValueForCBOpCode(opCode)
        setRegisterValueForCBOpCode(opCode, newRegValue)
        return cyclesForCbOpCode(opCode)
    }
    throw new IllegalStateException
  }

  private def cyclesForCbOpCode(opCode: Int) = if ((opCode & 0x07) == 0x06) 16
  else 8

  private def andToA(source: Int): Unit = {
    var t = 0
    t = getRegA & source
    flagC = false
    flagZ = t == 0
    flagH = true
    flagN = false
    setRegA(t)
    pc += 1
    pc &= 0xffff
  }

  private def xorToA(source: Int): Unit = {
    var t = 0
    t = getRegA ^ source
    flagC = false
    flagZ = t == 0
    flagH = false
    flagN = false
    setRegA(t)
    pc += 1
    pc &= 0xffff
  }

  private def orToA(source: Int): Unit = {
    var t = 0
    t = getRegA | source
    flagC = false
    flagZ = t == 0
    flagH = false
    flagN = false
    setRegA(t)
    pc += 1
    pc &= 0xffff
  }

  private def adcToA(source: Int): Unit = {
    var t = 0
    t = getRegA + source + (if (flagC) 1
    else 0)
    flagC = t > 0xff
    flagZ = (t & 0xff) == 0
    flagH = (getRegA & 0x0f) + (source & 0x0f) + (if (flagC) 1
    else 0) > 0x0f
    flagN = false
    setRegA(t & 0xff)
    pc += 1
    pc &= 0xffff
  }

  private def addToA(source: Int): Unit = {
    var t = 0
    t = getRegA + source
    flagC = t > 0xff
    flagZ = (t & 0xff) == 0
    flagH = (getRegA & 0x0f) + (source & 0x0f) > 0x0f
    flagN = false
    setRegA(t & 0xff)
    pc += 1
    pc &= 0xffff
  }

  private def subToA(source: Int): Unit = {
    var t = 0
    t = getRegA - source
    flagC = t < 0
    flagZ = (t & 0xff) == 0
    flagH = (getRegA & 0x0f) - (source & 0x0f) < 0
    flagN = true
    setRegA(t & 0xff)
    pc += 1
    pc &= 0xffff
  }

  private def cpToA(source: Int): Unit = {
    val t = getRegA - source
    flagC = t < 0
    flagZ = (t & 0xff) == 0
    flagH = (getRegA & 0x0f) - (source & 0x0f) < 0
    flagN = true
    pc += 1
    pc &= 0xffff
  }

  private def sbcToA(source: Int): Unit = {
    var t = 0
    t = getRegA - source - (if (flagC) 1
    else 0)
    flagC = t < 0
    flagZ = (t & 0xff) == 0
    flagH = (getRegA & 0x0f) - (source & 0x0f) - (if (flagC) 1
    else 0) < 0
    flagN = true
    setRegA(t & 0xff)
    pc += 1
    pc &= 0xffff
  }

  def setRegB(b: Int): Unit = BC = (BC & 0x00ff) | (b & 0xff) << 8

  def setRegC(c: Int): Unit = BC = (BC & 0xff00) | (c & 0xff)

  def getRegB: Int = BC >> 8

  def getRegC: Int = BC & 0xff

  def setRegD(d: Int): Unit = DE = (DE & 0x00ff) | (d & 0xff) << 8

  def setRegE(e: Int): Unit = DE = (DE & 0xff00) | (e & 0xff)

  def getRegD: Int = DE >> 8

  def getRegE: Int = DE & 0xff

  def setRegH(h: Int): Unit = HL = (HL & 0x00ff) | (h & 0xff) << 8

  def setRegL(l: Int): Unit = HL = (HL & 0xff00) | (l & 0xff)

  def getRegH: Int = HL >> 8

  def getRegL: Int = HL & 0xff

  def getRegBC: Int = BC

  def setRegBC(bc: Int): Unit = BC = bc

  def setRegA(a: Int): Unit = A = a

  def getRegDE: Int = DE

  def getRegHL: Int = HL

  def getRegSP: Int = SP

  def setRegHL(hl: Int): Unit = HL = hl

  def setRegDE(de: Int): Unit = DE = de

  def setRegSP(sp: Int): Unit = SP = sp

  def getRegA: Int = A

  def isStopped: Boolean = stopped

  def isHalted: Boolean = halted

  def getRegisterValueForCBOpCode(opCode: Int): Int = {
    opCode & 0x07 match {
      case 0x00 =>
        return getRegB
      case 0x01 =>
        return getRegC
      case 0x02 =>
        return getRegD
      case 0x03 =>
        return getRegE
      case 0x04 =>
        return getRegH
      case 0x05 =>
        return getRegL
      case 0x06 =>
        return vm.readByte(HL)
      case 0x07 =>
        return getRegA
    }
    throw new IllegalStateException
  }

  def setRegisterValueForCBOpCode(opCode: Int, value: Int): Unit = opCode & 0x07 match {
    case 0x00 =>
      setRegB(value)
    case 0x01 =>
      setRegC(value)
    case 0x02 =>
      setRegD(value)
    case 0x03 =>
      setRegE(value)
    case 0x04 =>
      setRegH(value)
    case 0x05 =>
      setRegL(value)
    case 0x06 =>
      vm.writeByte(HL, value)
    case 0x07 =>
      setRegA(value)
  }

  def getBitFromOpCode(opCode: Int): Int = (opCode & 0x38) >> 3

  def startup(): Unit = {
    pc = 0x100
    setRegA(0x01)
    setRegBC(0x0013)
    setRegDE(0x00d8)
    setRegHL(0x14d)
    setRegSP(0xfffe)
  }
}