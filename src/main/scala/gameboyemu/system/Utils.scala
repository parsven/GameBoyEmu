package gameboyemu.system

import gameboyemu.cpu.VirtualMemory

object Utils {
  def byteIntToHexString(v: Int): String = "%02x".format(v)

  def wordIntToHexString(v: Int): String = "%04x".format( v)

  def hexStringToInt(v: String): Int = {
    val trimmed = v.trim
    Integer.parseInt(trimmed, 16)
  }

  class VMLogger(val delegate: VirtualMemory) extends VirtualMemory {
    override def writeByte(adress: Int, b: Int): Unit = delegate.writeByte(adress, b)

    override def readByte(adress: Int): Int = delegate.readByte(adress)
  }

}