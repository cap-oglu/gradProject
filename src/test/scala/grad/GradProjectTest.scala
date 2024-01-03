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

  it should "operate correctly" in {
    // Define a simple test program
    val testProgram = Seq(
      0x04030201.U(32.W),  // Example instruction at address 0
      0x08070605.U(32.W),  // Example instruction at address 1
    )

    test(new TopLevel(size = 256, program = testProgram, numBanks = 4, numRegsPerBank = 4, numReadPorts = 2, numWritePorts = 1)) { c =>
      // Reset the program counter
      c.io.resetPC.poke(true.B)
      c.clock.step(1)
      c.io.resetPC.poke(false.B)

      // Step through the program
      for(_ <- 0 until 2) {
        c.clock.step(1)
        // Here you can add checks for the expected behavior at each step.
        // For example, check the output of the ALU, the current instruction, etc.
      }

      // Additional tests can be added here to validate specific operations or behaviors
    }
  }

  behavior of "RegisterFile"

  it should "write and read values correctly" in {
    test(new RegisterFile(numBanks = 9, numRegsPerBank = 8, numReadPorts = 9, numWritePorts = 9)) { c =>
      // Write phase
        c.io.writeEnable(0).poke(true.B)
        c.io.writeAddr(0).poke(0.U) // Write to the 0th register
        c.io.writeData(0).poke(123.U)
        c.clock.step(1)

        // Disable writing and allow some time for the value to settle
        c.io.writeEnable(0).poke(false.B)
        c.clock.step(2)

        // Read phase
        c.io.readAddr1(0).poke(0.U) // Read from the 0th register
        c.clock.step(1)
        c.io.readData1(0).expect(123.U) // Check if the read data is correct
    }
  }



}
