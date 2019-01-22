package gameboyemu.system

/**
  * User: par
  * Date: 2012-02-04
  */
class WRAMimpl extends WRAM {
  private val mem = new Array[Int](4096 * 2)

  override def writeByte(address: Int, b: Int): Unit = mem(address - 0xc000) = b

  override def readByte(address: Int): Int = mem(address - 0xc000)

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