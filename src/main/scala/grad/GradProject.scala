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


object GradProj extends App {
  // Sample program with two instructions (NOP in this case)
  

  // Create a Chisel driver for the InstructionMemory
  
  val program = Seq(0.U(32.W), 0.U(32.W)) // Your program
  
  
  
  println(
    ChiselStage.emitSystemVerilog(
      new TopLevel(1024, program),
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