package gameboyemu.system

import java.util

/**
  * Created by IntelliJ IDEA.
  * User: par
  * Date: 2012-02-23
  * Time: 21:41
  * To change this template use File | Settings | File Templates.
  */
class LCDController extends IoPorts {
  private var currentScanLine = 0
  private var lcdc = 0
  private var mc : MemoryController = _
  private var joyselect = 0

  def setMemoryController(mc: MemoryController): Unit = this.mc = mc

  def advanceScanLine(): Unit = currentScanLine += 1

  def resetScanLine(): Unit = currentScanLine = 0

  override def writeByte(adress: Int, b: Int): Unit = adress match {
    case 0xFF00 => //Joypad
      joyselect = b & 0x30
    case 0xFF40 => // LCDC
      lcdc = b
    case 0xFF46 =>
      System.err.println("OAM DMA!!")
    case _ => //Ignore sofar..
      System.err.println("writeByte(" + Utils.wordIntToHexString(adress) + " , " + Utils.byteIntToHexString(b) + ")")
  }

  override def readByte(adress: Int): Int = {
    if (adress == 0xff44) { //LY
      //        System.err.println("readByte(" + Utils.wordIntToHexString(adress) + " LY) -> " + currentScanLine);
      return currentScanLine
    }
    if (adress == 0xff00) return 0xcf | joyselect
    System.err.println("readByte(" + Utils.wordIntToHexString(adress) + ") -> 0")
    0 //To change body of implemented methods use File | Settings | File Templates.
  }

  def getWindowTileMapDisplayBase: Int = if ((lcdc & 0x40) == 0) 0x9800
  else 0x9c00

  private[system] def isWindowDisplayEnable = (lcdc & 0x20) != 0

  def getBGAndWindowTileDataBase: Int = if ((lcdc & 0x10) == 0) 0x8800
  else 0x8000

  def getBGTileMapDisplayBase: Int = if ((lcdc & 0x08) == 0) 0x9800
  else 0x9c00

  def renderLine(dest: Array[Int], o: Int): Unit = {
    var offset = o
    val tiles = new util.ArrayList[Integer](20)
    val base = (currentScanLine / 8) * 32
    // TODO: !!!
    val tileYOffset = currentScanLine - (currentScanLine / 8) * 8
    var i = 0
    while (i < 20) {
      tiles.add(mc.readByte(getBGTileMapDisplayBase + base + i))
      i += 1
    }
    import scala.collection.JavaConversions._
    for (tile <- tiles) {
      val i = getBGAndWindowTileDataBase + tile * 16 + tileYOffset * 2
      val hi = mc.readByte(i)
      val lo = mc.readByte(i + 1)
      var j = 7
      while (j >= 0) {
        val c = (hi & (1 << j)) >> j
        val d = (lo & (1 << j)) >> j
        val paletteEntry = c * 2 + d
        val color = getColorFromPaletteEntry(paletteEntry)
        dest({
          offset += 1; offset - 1
        }) = color
        dest({
          offset += 1; offset - 1
        }) = color
        dest({
          offset += 1; offset - 1
        }) = color
          j -= 1
      }
    }
  }

  def renderTile(tileNo: Int, tileBase: Int, dest: Array[Int], offset: Int, stride: Int): Unit = {
    val baseOfThisTile = tileBase + tileNo * 16
    //int color = new Random().nextInt(256);
    var i = 0
    while ( i < 8 ) {
      var hi = mc.readByte(baseOfThisTile + i * 2)
      var lo = mc.readByte(baseOfThisTile + i * 2 + 1)
      var j = 0
      while (j < 8) {
        val c = if ((hi & 0x80) != 0) 1 else 0
        val d = if ((lo & 0x80) != 0) 1 else 0
        hi <<= 1
        lo <<= 1
        val color = getColorFromPaletteEntry(c * 2 + d)
        dest(offset + j * 3 + stride * i * 3 + 0) = color
        dest(offset + j * 3 + stride * i * 3 + 1) = color
        dest(offset + j * 3 + stride * i * 3 + 2) = color
        j += 1
      }
      i += 1
    }
  }

  private def getColorFromPaletteEntry(paletteEntry: Int) = paletteEntry match {
    case 0 =>
      0x12
    case 1 =>
      0x40
    case 2 =>
      0x80
    case _ =>
      0xc0
  }

  def getLcdDisplayEnable: Boolean = (lcdc & 0x80) != 0

  def getLcdc: Int = lcdc

  def getWindowDisplayEnable: Boolean = (lcdc & 0x20) != 0

  def renderTiles(tileBuffer: Array[Int]): Unit = {
    var tileNo = 0
    var y = 0
    while (y < 16) {
      var x = 0
      while (x < 16) {
        val offset = 256 * 3 * 8 * y + 8 * x * 3
        renderTile({
          tileNo += 1; tileNo - 1
        }, 0x8000, tileBuffer, offset, 256)
          x += 1
      }
        y += 1
    }
  }
}