package grad


import chisel3._
import chisel3.util._
//import chisel3.stage.ChiselGeneratorAnnotation
//import circt.stage.ChiselStage
import _root_.circt.stage.ChiselStage
import _root_.circt.stage.FirtoolOption


// Define the opCode enumeration
object OpCode extends ChiselEnum {
  val FLUSH, INVALIDATE, UNLOCK, LOCK, ADD, ADDI = Value
  // Add additional opCodes as necessary
}


class Instruction extends Bundle {
  val opType = UInt(2.W)  // cache / remote / main
  val imm = Bool()
  val out = Bool()
  val vector = Bool()
  val numInp = UInt(2.W)  // numInp equals to src
  val opCode = OpCode()  
  val ext = UInt(4.W)  //  extra bits for some info
}

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




class RegisterFile(numBanks: Int, numRegsPerBank: Int, numReadPorts: Int, numWritePorts: Int) extends Module {
  val io = IO(new Bundle {
    val readAddr1 = Input(Vec(numReadPorts, UInt(log2Ceil(numRegsPerBank).W)))
    val readData1 = Output(Vec(numReadPorts, UInt(32.W)))
    val readAddr2 = Input(Vec(numReadPorts, UInt(log2Ceil(numRegsPerBank).W)))
    val readData2 = Output(Vec(numReadPorts, UInt(32.W)))
    val writeEnable = Input(Vec(numWritePorts, Bool()))
    val writeAddr = Input(Vec(numWritePorts, UInt(log2Ceil(numRegsPerBank).W)))
    val writeData = Input(Vec(numWritePorts, UInt(32.W)))
  })

  // Create a 2D array of registers: [numBanks][numRegsPerBank]
  //val regs = Seq.fill(numBanks)(Vec(numRegsPerBank, UInt(32.W)))
  val regs = Seq.fill(numBanks)(Reg(Vec(numRegsPerBank, UInt(32.W))))
  val regFile = RegInit(VecInit(regs))
  // Read logic
  for (i <- 0 until numReadPorts) {
    
    val regIdx = io.readAddr1(i) 
    io.readData1(i) := regFile(i)(regIdx)
    

  }
  
  for (i <- 0 until numReadPorts) {
    
    val regIdx = io.readAddr2(i) 
    io.readData2(i) := regFile(i)(regIdx)
    

  }

  // Write logic
  for (i <- 0 until numWritePorts) {

    //printf(p"Port $i - WriteEnable: ${io.writeEnable(i)}, WriteAddr: ${io.writeAddr(i)}, WriteData: ${io.writeData(i)}, ReadAddr: ${io.readAddr(0)}, ReadData: ${io.readData(0)}\n")
    when(io.writeEnable(i)) {
      
      val regIdx = io.writeAddr(i)
      regFile(i)(regIdx) := io.writeData(i)
      
    }
  }
}

class ControlUnit extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))   //TO DO make instruction size parameter
    val opType_sel = Output(UInt(2.W))  // cache / remote / main
    val imm_sel = Output(Bool())
    val out_sel = Output(Bool())
    val vector_sel = Output(Bool())
    val numInp_sel = Output(UInt(2.W))  // numInp equals to src
    val opCode_sel = Output(OpCode())
    val ext_sel = Output(UInt(4.W))  //  extra bits for some info
    val alu_op_sel = Output(ALUOp())



    // Additional control signals as required
  })

  // Instruction decode logic
  val decodedInstruction = Wire(new Instruction)

  decodedInstruction.opType := io.instruction(31, 30)
  decodedInstruction.imm := io.instruction(29)
  decodedInstruction.out := io.instruction(28)
  decodedInstruction.vector := io.instruction(27)
  decodedInstruction.numInp := io.instruction(26, 25)
  decodedInstruction.opCode := io.instruction(24, 20)
  decodedInstruction.ext := io.instruction(19, 16)


 

 switch(decodedInstruction.opCode) {
    is(OpCode.FLUSH) {
        
    }
    is(OpCode.INVALIDATE) {
      
    }
    is(OpCode.UNLOCK) {
      
    }
    is(OpCode.LOCK) {
      
    }
    is(OpCode.ADD) {
      io.alu_op_sel := ALUOp.ADD
    }
    is(OpCode.ADDI) {
      io.alu_op_sel := ALUOp.ADD
    }

    // Add additional opCodes as necessary
  }

  io.opType_sel := decodedInstruction.opType
  io.imm_sel := decodedInstruction.imm
  io.out_sel := decodedInstruction.out
  io.vector_sel := decodedInstruction.vector
  io.numInp_sel := decodedInstruction.numInp
  io.opCode_sel := decodedInstruction.opCode
  io.ext_sel := decodedInstruction.ext

  

}

object ALUOp extends ChiselEnum {
  val ADD = Value // Define ADD operation, additional operations can be added here
  // Other operations like SUB, MUL, etc., can be added as necessary
}

class ALU extends Module {
  val io = IO(new Bundle {
    val op = Input(ALUOp())          // Operation type
    val in1 = Input(UInt(32.W))      // First operand
    val in2 = Input(UInt(32.W))      // Second operand
    val out = Output(UInt(32.W))     // Output of the ALU
    val zero = Output(Bool())        // A flag that is true when the output is zero
  })

  // Default output values
  io.out := 0.U
  io.zero := false.B

  // Perform operation based on the input operation type
  switch(io.op) {
    is(ALUOp.ADD) {
      io.out := io.in1 + io.in2
    }
    // Additional cases for other operations can be added here
  }

  // Set the zero flag if the output is zero
  io.zero := (io.out === 0.U)
}


class TopLevel(size: Int, program: Seq[UInt], numBanks: Int, numRegsPerBank: Int, numReadPorts: Int, numWritePorts: Int) extends Module {
  val io = IO(new Bundle {
    val instruction = Output(UInt(32.W))
    val resetPC = Input(Bool()) // Input to reset the PC
  })

  val instructionMemory = Module(new InstructionMemory(size, program))
  val pc = Module(new ProgramCounter(size))
  val controlUnit = Module(new ControlUnit)
  val registerFile = Module(new RegisterFile(numBanks, numRegsPerBank, numReadPorts, numWritePorts))
  val alu = Module(new ALU)
  // Connect the Program Counter to the Instruction Memory
  pc.io.resetPC := io.resetPC
  instructionMemory.io.address := pc.io.nextPC

  // Fetch the instruction and pass it to the Control Unit
  val fetchedInstruction = instructionMemory.io.instruction
  controlUnit.io.instruction := fetchedInstruction

  val immediateValue = fetchedInstruction(3, 0) // Extract the immediate value from the instruction
  val readAddr1 = fetchedInstruction(7, 4) // Extract the read address 1 from the instruction
  val readAddr2 = fetchedInstruction(11, 8) // Extract the read address 2 from the instruction
  val writeAddr = fetchedInstruction(15, 12) // Extract the write address from the instruction
  
  alu.io.in1 := Mux(controlUnit.io.numInp_sel === 0.U,
                  0.U, // Replace with the actual value for the case numInp == 0
                  Mux(controlUnit.io.numInp_sel === 1.U,
                      registerFile.io.readData1(readAddr1), // Replace with the actual value for the case numInp == 1
                      Mux(controlUnit.io.numInp_sel === 2.U,
                          registerFile.io.readData1(readAddr1), // Value for numInp == 2
                          Mux(controlUnit.io.numInp_sel === 3.U,
                              registerFile.io.readData1(readAddr1), // Value for numInp == 3
                              0.U)))) // Default value

  
  alu.io.in2 := Mux(controlUnit.io.imm_sel && (controlUnit.io.numInp_sel === 0.U || controlUnit.io.numInp_sel === 1.U),
                  immediateValue, // Replace with the immediate value
                          Mux(controlUnit.io.numInp_sel === 2.U,
                              registerFile.io.readData2(readAddr2), // Value for numInp == 2
                              Mux(controlUnit.io.numInp_sel === 3.U,
                                  registerFile.io.readData2(readAddr2), // Value for numInp == 3
                                  0.U))) // Default value

  //using port = 0 for development
  registerFile.io.writeEnable(0) := controlUnit.io.out_sel
  registerFile.io.writeAddr(0) := writeAddr
  registerFile.io.writeData(0) := alu.io.out
  



  // Output the instruction from the memory
  io.instruction := instructionMemory.io.instruction
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