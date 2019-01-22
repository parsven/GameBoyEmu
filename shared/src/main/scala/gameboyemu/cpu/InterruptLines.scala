package gameboyemu.cpu

trait InterruptLines {

   def requestVblankIrq() : Unit
   def requestLCDCStatusIrq(): Unit
}
