package gameboyemu.cpu

/**
  * Created by IntelliJ IDEA.
  * User: par
  * Date: 2011-08-10
  * Time: 22:08
  * To change this template use File | Settings | File Templates.
  */
trait CpuState {
  def getZ: Boolean

  def setZ(z: Boolean): Unit

  def getN: Boolean

  def setN(n: Boolean): Unit

  def getH: Boolean

  def setH(h: Boolean): Unit

  def getC: Boolean

  def setC(c: Boolean): Unit

  def getAF: Int

  def getBC: Int

  def getDE: Int

  def getHL: Int

  def getSP: Int

  def getPC: Int
}