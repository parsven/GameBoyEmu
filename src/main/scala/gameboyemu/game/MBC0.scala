package gameboyemu.game

import gameboyemu.system.Utils

class MBC0(val romContent: Array[Byte]) extends MBC {
  System.err.println("MBC0: Settings romcontent, size=" + Utils.wordIntToHexString(romContent.length))

  override def readByte(address: Int): Int = {
    val i = romContent(address).toInt & 0xff
    //    System.err.println("MBC0: readByte(" + Utils.wordIntToHexString(address) + ") -> " + Utils.byteIntToHexString(i));
    i
  }

  override def writeByte(address: Int, value: Int): Unit = {
  }

  def writeWord(address: Int, value: Int): Unit = {
  }

  def readWord(address: Int): Int = {
    val loByte = romContent(address).toInt & 0xff
    val hiByte = romContent(address + 1).toInt & 0xff
    (hiByte << 8) | loByte
  }
}