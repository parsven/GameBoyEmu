package gameboyemu.cpu

/**
  * Created by IntelliJ IDEA.
  * User: noy
  * Date: 2/20/12
  * Time: 9:20 PM
  * To change this template use File | Settings | File Templates.
  */
trait VirtualMemory16 extends VirtualMemory {
  def writeWord(adress: Int, b: Int): Unit

  def readWord(adress: Int): Int
}