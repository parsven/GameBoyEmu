package gameboyemu.cpu

import java.util

trait Cpu {
  def step: Int

  @throws[BreakPointException]
  def executeScanline(rest: Int, breakPoints: util.Set[Integer]): Int
}