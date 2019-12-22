/**
 * Represents a Chip-8.
 * @author Andrew Cheung
 */

 import java.util.*;

public class Chip8 {
    private Memory memory;
    private char opcode;

    private Map<Character, OpcodeFunction> opcodeFuncs;

    private byte[] registers;
    private char indexRegister;
    private byte delayRegister;
    private byte soundRegister;

    private char programCounter;

    private char[] stack;
    private byte stackPointer;

    private byte[] display;

    public Chip8() {
        memory = new Memory();
        memory.loadROM("demo.ch8");
        memory.printMemory();

        opcodeFuncs = new HashMap<Character, OpcodeFunction>();
        initializeOpcodes();

        registers = new byte[16];
        indexRegister = 0;
        programCounter = Memory.START_ADDRESS;
        
        stack = new char[16];
        stackPointer = 0;
        // where should the stack pointer point?

        display = new byte[64 * 32];
    }

    /**
     * Emulates one cycle of the CHIP-8.
     */
    public void cycle() {
        opcode = memory.getOpcode(programCounter);
        execOpcode();
        if (delayRegister > 0) {
            delayRegister--;
        }
        if (soundRegister > 0) {
            soundRegister--;
        }
    }

    /**
     * Performs the task corresponding to the current opcode.
     */
    public void execOpcode() {
        System.out.println(String.format("Current opcode: %04x", (int)(opcode)));
        char opcodeSkeleton = trimOpcode();
        System.out.println(String.format("Current skeleton: %04x", (int)(opcodeSkeleton)));
        opcodeFuncs.get(opcodeSkeleton).exec();
    }

    /**
     * Populates the opcodeFuncs map with the opcode "skeletons" and their corresponding
     * OpcodeFunctions.
     */
    private void initializeOpcodes() {
        opcodeFuncs.put((char)(0x00E0), new ClearDisplay());
        opcodeFuncs.put((char)(0x00EE), new ReturnFromSubroutine());
        opcodeFuncs.put((char)(0x1000), new Jump());
        opcodeFuncs.put((char)(0x2000), new Call());
        opcodeFuncs.put((char)(0x3000), new SkipIfEqual());
        opcodeFuncs.put((char)(0x4000), new SkipIfUnequal());
        opcodeFuncs.put((char)(0x5000), new SkipIfVxEqualsVy());
        opcodeFuncs.put((char)(0x6000), new LoadKkIntoVx());
        opcodeFuncs.put((char)(0x7000), new AddVxAndByte());
        opcodeFuncs.put((char)(0x8000), new LoadVxVy());
        opcodeFuncs.put((char)(0x8001), new OrVxVy());
        opcodeFuncs.put((char)(0x8002), new AndVxVy());
        opcodeFuncs.put((char)(0x8003), new XorVxVy());
        opcodeFuncs.put((char)(0x8004), new AddVxVy());
        opcodeFuncs.put((char)(0x8005), new SubVxVy());
        opcodeFuncs.put((char)(0x8006), new BitShiftRight());
    }

    /**
     * Trims opcode to skeleton and returns skeleton
     */
    private char trimOpcode() {
        if ((opcode & 0xF000) == 0x0000) {
            return opcode;
        } else if ((opcode & 0xF000) == 0x8000) {
            return (char)((opcode & 0xFFFF) & 0xF00F);
        }
        return (char)(opcode & 0xF000);
    }

    /**
     * Interface representing an OpcodeFunction class
     */
    private interface OpcodeFunction {
        /**
         * Run the opcode.
         */
        public void exec();
    }

    /**
     * 00E0
     * Clears the display.
     */
    private class ClearDisplay implements OpcodeFunction {
        public void exec() {
            System.out.println("Clearing screen!");
            for (int i = 0; i < display.length; i++) {
                display[i] = 0x00;
            }
            programCounter += 2;
        }
    }

    /**
     * 00EE
     * Returns from a subroutine.
     * The interpreter sets the program counter to the address at the top of the stack,
     * then subtracts 1 from the stack pointer.
     */
    private class ReturnFromSubroutine implements OpcodeFunction {
        public void exec() {
            // System.out.println(String.format("Returning to address %04x", (int)stack[stackPointer]));
            programCounter = stack[stackPointer];
            stackPointer--;
        }
    }

    /**
     * 1nnn
     * Jump to location nnn.
     * The interpreter sets the program counter to nnn,
     * since the programCounter is then incremented
     */
    private class Jump implements OpcodeFunction {
        public void exec() {
            char nnn = (char)(opcode & 0x0FFF);
            // System.out.println(String.format("Jumping to %04x", (int)nnn));
            programCounter = nnn;
        }
    }

    /**
     * 2nnn
     * Call subroutine at nnn.
     * The implementer increments the SP, and then puts the current PC on top of the stack.
     * The PC is then set to nnn.
     */
    private class Call implements OpcodeFunction {
        public void exec() {
            char nnn = (char)(opcode & 0x0FFF);
            stackPointer++;
            // System.out.println(String.format("Calling and setting SP to %04x", (int)nnn));
            stack[stackPointer] = programCounter;
            programCounter = nnn;
        }
    }

    /**
     * 3xkk - SE Vx, byte
     * Skip next instruction if Vx = kk.
     * Compare Vx to kk, if equal, increments pc by 2.
     */
    private class SkipIfEqual implements OpcodeFunction {
        public void exec() {
            byte x = (byte)((opcode & 0x0F00) >> 8);
            byte kk = (byte)(opcode & 0x00FF);
            // System.out.println(String.format("%08x", kk));
            // System.out.println(String.format("%08x", registers[x]));
            if (registers[x] == kk) {
                programCounter += 2;
            }
            programCounter += 2;
        }
    }

    /**
     * 4xkk - SNE Vx, byte
     * Skip next instruction if Vx != kk
     * Compare Vx to kk, if unequal, increments pc by 2.
     */
    private class SkipIfUnequal implements OpcodeFunction {
        public void exec() {
            byte x = (byte)((opcode & 0x0F00) >> 8);
            byte kk = (byte)(opcode & 0x00FF);
            if (registers[x] != kk) {
                programCounter += 2;
            }
            programCounter += 2;
        }
    }

    /**
     * 5xy0 - SE Vx, Vy
     * Skip next instruction if Vx = Vy.
     * The interpreter compares register Vx to register Vy,
     * and if they are equal, increments the program counter by 2.
     */
    private class SkipIfVxEqualsVy implements OpcodeFunction {
        public void exec() {
            byte x = (byte)((opcode & 0x0F00) >> 8);
            byte y = (byte)((opcode & 0x00F0) >> 4);
            if (registers[x] == registers[y]) {
                programCounter += 2;
            }
            programCounter += 2;
        }
    }

    /**
     * 6xkk - LD Vx, byte
     * Set Vx = kk.
     * The interpreter puts the value kk into register Vx.
     */
    private class LoadKkIntoVx implements OpcodeFunction {
        public void exec() {
            byte kk = (byte)(opcode & 0x00FF);
            byte x = (byte)((opcode & 0x0F00) >> 8);
            registers[x] = kk;
            programCounter += 2;
        }
    }

    /**
     * 7xkk - ADD Vx, byte
     * Set Vx = Vx + kk
     * Adds the value kk to the value of register Vx, then stores the result in Vx.
     */
    private class AddVxAndByte implements OpcodeFunction {
        public void exec() {
            byte x = (byte)((opcode & 0x0F00) >> 8);
            byte kk = (byte)(opcode & 0x00FF);
            registers[x] += kk;
            programCounter += 2;
        }
    }

    /**
     * 8xy0 - LD Vx, Vy
     * Set Vx = Vy.
     * Stores value of register Vy in register Vx.
     */
    private class LoadVxVy implements OpcodeFunction {
        public void exec() {
            byte x = (byte)((opcode & 0x0F00) >> 8);
            byte y = (byte)((opcode & 0x00F0) >> 4);
            registers[x] = registers[y];
            programCounter += 2;
        }
    }

    /**
     * 8xy1 - OR Vx, Vy
     * Set Vx = Vx OR Vy.
     * Performs a bitwise OR on the values of Vx and Vy,
     * then stores the result in Vx.
     * A bitwise OR compares the corrseponding bits from two values,
     * and if either bit is 1, then the same bit in the result is also 1.
     * Otherwise, it is 0.
     */
    private class OrVxVy implements OpcodeFunction {
        public void exec() {
            byte x = (byte)((opcode & 0x0F00) >> 8);
            byte y = (byte)((opcode & 0x00F0) >> 4);
            registers[x] |= registers[y];
            programCounter += 2;
        }
    }

    /**
     * 8xy2 - AND Vx, Vy
     * Set Vx = Vx AND Vy.
     * Performs a bitwise AND on the values of Vx and Vy,
     * then stores the result in Vx. A bitwise AND compares the corrseponding bits from two values,
     * and if both bits are 1, then the same bit in the result is also 1. Otherwise, it is 0.
     */
    private class AndVxVy implements OpcodeFunction {
        public void exec() {
            byte x = (byte)((opcode & 0x0F00) >> 8);
            byte y = (byte)((opcode & 0x00F0) >> 4);
            registers[x] &= registers[y];
            programCounter += 2;
        }
    }

    /**
     * 8xy3 - XOR Vx, Vy
     * Set Vx = Vx XOR Vy.
     */
    private class XorVxVy implements OpcodeFunction {
        public void exec() {
            byte x = (byte)((opcode & 0x0F00) >> 8);
            byte y = (byte)((opcode & 0x00F0) >> 4);
            registers[x] ^= registers[y];
            programCounter += 2;
        }
    }

    /**
     * 8xy4 - ADD Vx, Vy
     * Set Vx = Vx + Vy, Set VF = carry
     */
    private class AddVxVy implements OpcodeFunction {
        public void exec() {
            int x = ((opcode & 0x0F00) >> 8);
            int y = ((opcode & 0x00F0) >> 4);
            char sum = (char)(registers[x] + registers[y]); // i love unsigned 16 bit ints.
            // System.out.println(String.format("x: %02x", registers[x]));
            // System.out.println(String.format("y: %02x", registers[y]));
            // System.out.println(String.format("sum: %04x OR ", (int)sum) + (int)sum);
            if (sum > 255) {
                registers[0xF] = 1;
            } else {
                registers[0xF] = 0;
            }
            registers[x] = (byte)(sum & 0xFF);
            programCounter += 2;
        }
    }

    /**
     * 8xy5 - SUB Vx, Vy
     * Set Vx = Vx - Vy, set VF = NOT Borrow
     * If Vx > Vy, VF set to 1. 
     */
    private class SubVxVy implements OpcodeFunction {
        public void exec() {
            int x = ((opcode & 0x0F00) >> 8);
            int y = ((opcode & 0x00F0) >> 4);
            if (registers[x] > registers[y]) {
                registers[0xF] = 1;
            }
            registers[x] -= registers[y];
            programCounter += 2;
        }       
    }

    /**
     * 8xy6 - SHR Vx {, Vy}
     * Set Vx = Vx SHR 1
     * If least significant bit of Vx is 1, VF = 1, otherwise 0.
     * Vx then divided by 2.
     */
    private class BitShiftRight implements OpcodeFunction {
        public void exec() {
            byte x = (byte)((opcode & 0x0F00) >>> 8);
            registers[0xF] = (byte)(registers[x] & 0x1);
            registers[x] >>>= 1;
        }
    }




}