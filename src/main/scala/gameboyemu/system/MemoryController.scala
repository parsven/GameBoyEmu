package gameboyemu.system

import gameboyemu.cpu.{VirtualMemory, VirtualMemory16}
import gameboyemu.game.MBC

object MemoryController {
  private[system] def writeWord(vm: VirtualMemory, address: Int, value: Int) = {
    val loByte = value & 0xff
    val hiByte = (value & 0xff00) >> 8
    vm.writeByte(address, loByte)
    vm.writeByte(address + 1, hiByte)
  }

  private[system] def readWord(vm: VirtualMemory, address: Int) = {
    val loByte = vm.readByte(address) & 0xff
    val hiByte = vm.readByte(address + 1) & 0xff
    (hiByte << 8) | loByte
  }
}

class MemoryController(val mbc: MBC, val workRam: WRAM, val hiRam: HRAM, val videoRam: VRAM, val ioPorts: IoPorts) extends VirtualMemory16 {
  private var interruptReg = 0

  override def writeByte(address: Int, value: Int): Unit = if ((address & 0x8000) == 0) mbc.writeByte(address, value)
  else if (address == 0xffff) {interruptReg = value; System.out.println("0xffff = " + Utils.byteIntToHexString(value))}
  else if (address >= 0xff80) hiRam.writeByte(address, value)
  else if (address >= 0xff00) ioPorts.writeByte(address, value)
  else if ((address & 0x4000) != 0) workRam.writeByte(address & 0xdfff, value)
  else videoRam.writeByte(address, value)

  override def readByte(address: Int): Int = if ((address & 0x8000) == 0) mbc.readByte(address)
  else if (address == 0xffff) interruptReg
  else if (address >= 0xff80) hiRam.readByte(address)
  else if (address >= 0xff00) ioPorts.readByte(address)
  else if ((address & 0x4000) != 0) workRam.readByte(address & 0xdfff)
  else videoRam.readByte(address)

  override def writeWord(address: Int, value: Int): Unit = if ((address & 0x8000) == 0) MemoryController.writeWord(mbc, address, value)
  else if (address >= 0xff80) MemoryController.writeWord(hiRam, address, value)
  else if (address >= 0xff00) MemoryController.writeWord(ioPorts, address, value)
  else if ((address & 0x4000) != 0) MemoryController.writeWord(workRam, address & 0xdfff, value)
  else {
  }

  override def readWord(address: Int): Int = if ((address & 0x8000) == 0) MemoryController.readWord(mbc, address)
  else if (address >= 0xff80) MemoryController.readWord(hiRam, address)
  else if (address >= 0xff00) MemoryController.readWord(ioPorts, address)
  else if ((address & 0x4000) != 0) MemoryController.readWord(workRam, address & 0xdfff)
  else 0

  override def isVblankInteruptEnabled : Boolean = (interruptReg & 0x01) != 0
}