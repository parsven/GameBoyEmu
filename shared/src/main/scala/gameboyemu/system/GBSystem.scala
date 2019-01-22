package gameboyemu.system

import java.util

import gameboyemu.cpu.{BreakPointException, Cpu}

/**
  * Created by IntelliJ IDEA.
  * User: par
  * Date: 2012-02-23
  * Time: 21:21
  */
object GBSystem {
   val NOOFSCANLINES = 154
}

class GBSystem(val memoryController: MemoryController, val cpu: Cpu, val lcdController: LCDController) {
  @throws[BreakPointException]
  def executeFrame(o: Int, backbuffer: Array[Int], breakPoints: util.Set[Integer]): Int = {
    var offset = o
    lcdController.resetScanLine()
    var i = 0
    while ( i < 154 ) {
      lcdController.advanceScanLine()
      offset = cpu.executeScanline(offset, breakPoints)
      if (i < 144) {
        lcdController.renderLine(backbuffer, i * 160 * 3)
      }
      i += 1
    }
    offset
  }
}