package gameboyemu.game

import java.io.{File, FileInputStream, IOException}
import java.nio.file.Paths

object Tetris {
  private var romContent__ : Array[Byte] = read("TETRIS.GB")

  def getRomContent: Array[Byte] = {
    val romContent = new Array[Byte](romContent__.length)
    System.arraycopy(romContent__, 0, romContent, 0, romContent__.length)
    romContent
  }

  @throws[IOException]
  private def read(fileName: String) = {
    val currentRelativePath = Paths.get("")
    val s = currentRelativePath.toAbsolutePath.toString
    System.out.println("Current relative path is: " + s)
    val file = new File(fileName)
    val fin = new FileInputStream(file)
    val len = file.length.toInt
    val fileContent = new Array[Byte](len)
    var readSoFar = 0
    do readSoFar += fin.read(fileContent, readSoFar, len - readSoFar) while ( {
      len - readSoFar > 0
    })
    fileContent
  }

}