package grad

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class InstructionMemoryTest extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "InstructionMemory"

  it should "correctly read instructions from memory" in {
    // Define a simple test program
    val testProgram = Seq(
      "h12345678".U(32.W),  // Example instruction at address 0
      "h87654321".U(32.W),  // Example instruction at address 1
      // Add more instructions as needed
    )

    // Instantiate the InstructionMemory module with the test program
    test(new InstructionMemory(testProgram.size, testProgram)) { c =>
      // Iterate over each instruction in the program
      for ((instr, addr) <- testProgram.zipWithIndex) {
        // Set the input address
        c.io.address.poke(addr.U)

        // Check the output instruction
        c.io.instruction.expect(instr)

        // Add a small delay if needed
        c.clock.step(1)
      }
    }
  }

  behavior of "TopLevel"

  it should "fetch instructions in sequence" in {
    // Define a simple test program
    val testProgram = Seq(
      1.U(32.W),  // Example instruction at address 0
      2.U(32.W),  // Example instruction at address 1
      3.U(32.W),  // Example instruction at address 2
      // Add more instructions as needed
    )

    test(new TopLevel(testProgram.size, testProgram)) { c =>
      // Initially, the program counter should be reset
      c.io.resetPC.poke(true.B)
      c.clock.step(1)
      c.io.resetPC.poke(false.B)

      for (instr <- testProgram) {
        c.io.instruction.expect(instr) // Check if the instruction is as expected
        c.clock.step(1)                // Increment the clock to move to the next instruction
      }
    }
  }

}
