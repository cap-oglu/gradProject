file:///C:/Users/ramazan/Desktop/chisel-template-main/src/main/scala/grad/GradProject.scala
### java.lang.IndexOutOfBoundsException: 0

occurred in the presentation compiler.

action parameters:
offset: 2409
uri: file:///C:/Users/ramazan/Desktop/chisel-template-main/src/main/scala/grad/GradProject.scala
text:
```scala
package grad


import chisel3._
import chisel3.util._
//import chisel3.stage.ChiselGeneratorAnnotation
//import circt.stage.ChiselStage
import _root_.circt.stage.ChiselStage
import _root_.circt.stage.FirtoolOption

class InstructionMemory(size: Int, program: Seq[UInt] ) extends Module {
  val io = IO(new Bundle {
    val address = Input(UInt(log2Ceil(size).W))
    val instruction = Output(UInt(32.W))
  })

  // Creating a memory array
  val memory = Mem(size, UInt(32.W))
  

  // Initialize memory with the program
  program.zipWithIndex.foreach { case (instr, addr) =>
     memory.write(addr.U, instr)
  }

  // Simple read-only memory
  io.instruction := memory(io.address)
  
}

class ProgramCounter(size: Int) extends Module {
  val io = IO(new Bundle {
    val resetPC = Input(Bool())       // Input to reset the PC
    val nextPC = Output(UInt(log2Ceil(size).W)) // Output the next PC value
  })

  val pcReg = RegInit(0.U(log2Ceil(size).W)) // Program counter register

  // Increment the PC each clock cycle
  when(io.resetPC) {
    pcReg := 0.U // Reset PC to 0
  } .otherwise {
    pcReg := pcReg + 1.U // Increment PC
  }

  // Output the current value of PC
  io.nextPC := pcReg
}


class TopLevel(size: Int, program: Seq[UInt]) extends Module {
  val io = IO(new Bundle {
    val instruction = Output(UInt(32.W))
    val resetPC = Input(Bool()) // Input to reset the PC
  })

  val memory = Module(new InstructionMemory(size, program))
  val pc = Module(new ProgramCounter(size))

  // Connect the Program Counter to the Instruction Memory
  pc.io.resetPC := io.resetPC
  memory.io.address := pc.io.nextPC

  // Output the instruction from the memory
  io.instruction := memory.io.instruction
}

class RegisterFile(numBanks: Int, numRegsPerBank: Int, numReadPorts: Int, numWritePorts: Int) extends Module {
  val io = IO(new Bundle {
    val readAddr = Input(Vec(numReadPorts, UInt(log2Ceil(numBanks * numRegsPerBank).W)))
    val readData = Output(Vec(numReadPorts, UInt(32.W)))
    val writeEnable = Input(Vec(numWritePorts, Bool()))
    val writeAddr = Input(Vec(numWritePorts, UInt(log2Ceil(numBanks * numRegsPerBank).W)))
    val writeData = Input(Vec(numWritePorts, UInt(32.W)))
  })

  // Create a 2D array of registers: [numBanks][numRegsPerBank]
   val regs = Seq.fill(numBanks)(Seq.fill(numRegsPerBank) (@@UInt(32.W))))
  val regs = Seq.fill(numBanks)(Reg(Vec(numRegsPerBank, UInt(32.W))))
  val regFile = RegInit(VecInit(regs))
  // Read logic
  for (i <- 0 until numReadPorts) {
    val bank = io.readAddr(i) / numBanks.U
    val regIdx = io.readAddr(i) % numBanks.U
    io.readData(i) := regFile(bank)(regIdx)
    

  }

  // Write logic
  for (i <- 0 until numWritePorts) {
    
      val bank = io.writeAddr(i) / numBanks.U
      val regIdx = io.writeAddr(i) % numBanks.U
      printf(p"Port $i - WriteEnable: ${io.writeEnable(i)}, WriteAddr: ${io.writeAddr(i)}, WriteData: ${io.writeData(i)}, ReadAddr: ${io.readAddr(0)}, ReadData: ${io.readData(0)}\n")
    when(io.writeEnable(i)) {
      regFile(bank)(regIdx) := io.writeData(i)
      
    }
  }
}



object GradProj extends App {
  // Sample program with two instructions (NOP in this case)
  

  // Create a Chisel driver for the InstructionMemory
  
  val program = Seq(0.U(32.W), 0.U(32.W)) // Your program
  
  
  
  /*println(
    ChiselStage.emitSystemVerilog(
      new TopLevel(1024, program),
      firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
    )
  )*/
  println(
    ChiselStage.emitSystemVerilog(
      new RegisterFile(9, 8, 9, 9),
      firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
    )
  )
 
  
/*(new ChiselStage).execute(
  Array("--target", "systemverilog"),
  Seq(ChiselGeneratorAnnotation(() => new InstructionMemory(1024, program)),
    FirtoolOption("--disable-all-randomization"))
)*/
//circt.stage.ChiselStage.emitSystemVerilog(new InstructionMemory(1024, program))
  
  
  
}
```



#### Error stacktrace:

```
scala.collection.LinearSeqOps.apply(LinearSeq.scala:131)
	scala.collection.LinearSeqOps.apply$(LinearSeq.scala:128)
	scala.collection.immutable.List.apply(List.scala:79)
	dotty.tools.dotc.util.Signatures$.countParams(Signatures.scala:501)
	dotty.tools.dotc.util.Signatures$.applyCallInfo(Signatures.scala:186)
	dotty.tools.dotc.util.Signatures$.computeSignatureHelp(Signatures.scala:94)
	dotty.tools.dotc.util.Signatures$.signatureHelp(Signatures.scala:63)
	scala.meta.internal.pc.MetalsSignatures$.signatures(MetalsSignatures.scala:17)
	scala.meta.internal.pc.SignatureHelpProvider$.signatureHelp(SignatureHelpProvider.scala:51)
	scala.meta.internal.pc.ScalaPresentationCompiler.signatureHelp$$anonfun$1(ScalaPresentationCompiler.scala:388)
```
#### Short summary: 

java.lang.IndexOutOfBoundsException: 0