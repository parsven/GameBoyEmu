package gameboyemu.system

/**
  * Created by IntelliJ IDEA.
  * User: par
  * Date: 2012-03-11
  * Time: 18:12
  * To change this template use File | Settings | File Templates.
  */
class VRAMimpl extends VRAM {
  private val mem = new Array[Int](4096 * 2)

  override def writeByte(address: Int, b: Int): Unit = { //   System.err.println("write VRAM(" + Utils.wordIntToHexString(address) + ", " + Utils.byteIntToHexString(b)+")");
    mem(address - 0x8000) = b
  }

  override def readByte(address: Int): Int = { //     System.err.println("read VRAM(" + Utils.wordIntToHexString(address) + ") -> " + Utils.byteIntToHexString(mem[address - 0x8000]));
    mem(address - 0x8000)
  }
}