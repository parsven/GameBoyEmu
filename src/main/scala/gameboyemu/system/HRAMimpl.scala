package gameboyemu.system

/**
  * Created by IntelliJ IDEA.
  * User: par
  * Date: 2012-02-05
  * Time: 12:24
  * To change this template use File | Settings | File Templates.
  */
class HRAMimpl extends HRAM {
  private val mem = new Array[Int](0x7f)

  override def writeByte(address: Int, b: Int): Unit = mem(address - 0xff80) = b

  override def readByte(address: Int): Int = mem(address - 0xff80)

  def writeWord(address: Int, w: Int): Unit = {
    val lowByte = w & 0xff
    val hiByte = (w & 0xff00) >> 8
    writeByte(address, lowByte)
    writeByte(address + 1, hiByte)
  }

  def readWord(address: Int): Int = {
    val lowByte = readByte(address)
    val hiByte = readByte(address + 1)
    (hiByte << 8) | lowByte
  }
}