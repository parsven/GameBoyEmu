package gameboyemu.game

class Rom(var romContent: Array[Byte]) {
  def verifyLogoCorrect: Boolean = {
    val logo = Array[Byte](0xCE.toByte, 0xED.toByte, 0x66.toByte, 0x66.toByte, 0xCC.toByte, 0x0D.toByte, 0x00.toByte, 0x0B.toByte, 0x03.toByte, 0x73.toByte, 0x00.toByte, 0x83.toByte, 0x00.toByte, 0x0C.toByte, 0x00.toByte, 0x0D.toByte, 0x00.toByte, 0x08.toByte, 0x11.toByte, 0x1F.toByte, 0x88.toByte, 0x89.toByte, 0x00.toByte, 0x0E.toByte, 0xDC.toByte, 0xCC.toByte, 0x6E.toByte, 0xE6.toByte, 0xDD.toByte, 0xDD.toByte, 0xD9.toByte, 0x99.toByte, 0xBB.toByte, 0xBB.toByte, 0x67.toByte, 0x63.toByte, 0x6E.toByte, 0x0E.toByte, 0xEC.toByte, 0xCC.toByte, 0xDD.toByte, 0xDC.toByte, 0x99.toByte, 0x9F.toByte, 0xBB.toByte, 0xB9.toByte, 0x33.toByte, 0x3E.toByte)
    var i = 0
    while (i < 0x18) {
      if (romContent(0x104 + i) != logo(i))
        return false
      else
        i += 1
    }
    true
  }

  def getCartridgeType: Byte = romContent(0x147)

  def getRomSizekB: Int = 32 << romContent(0x148)

  def getRomBanks: Int = 2 << romContent(0x148)

  def getRamSizekB = 0

  def createMBC = new MBC0(romContent)
}