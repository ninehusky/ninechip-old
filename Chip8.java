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

    private void initializeOpcodes() {
        opcodeFuncs.put((char)(0x00E0), new ClearDisplay());
        opcodeFuncs.put((char)(0x00EE), new ReturnFromSubroutine());
        opcodeFuncs.put((char)(0x1000), new Jump());
        opcodeFuncs.put((char)(0x2000), new Call());
        opcodeFuncs.put((char)(0x3000), new SkipIfEqual());
        opcodeFuncs.put((char)(0x4000), new SkipIfUnequal());
    }

    /**
     * Trims opcode to skeleton and returns skeleton
     */
    private char trimOpcode() {
        if ((opcode & 0xF000) == 0x0000) {
            return opcode;
        } else if ((opcode & 0xF000) == 0x1000) {
            return (char)(0x1000);
        } else if ((opcode & 0xF000) == 0x2000) {
            return (char)(0x2000);
        } else if ((opcode & 0xF000) == 0x3000) {
            return (char)(0x3000);
        } else if ((opcode & 0xF000) == 0x4000) {
            return (char)(0x4000);
        }
        System.out.println(String.format("no opcode found for %04x", (int)opcode));
        return 'a';
    }

    private interface OpcodeFunction {
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
            System.out.println(String.format("Returning to address %04x", (int)stack[stackPointer]));
            programCounter = stack[stackPointer];
            stackPointer--;
        }
    }

    /**
     * 1nnn
     * Jump to location nnn.
     * The interpreter sets the program counter to nnn - 2,
     * since the programCounter is then incremented
     */
    private class Jump implements OpcodeFunction {
        public void exec() {
            char nnn = (char)(opcode & 0x0FFF);
            System.out.println(String.format("Jumping to %04x", (int)nnn));
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
            System.out.println(String.format("Calling and setting SP to %04x", (int)nnn));
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
            char kk = (char)(opcode & 0x00FF);
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
            char kk = (char)(opcode & 0x00FF);
            if (registers[x] == kk) {
                programCounter += 2;
            }
            programCounter += 2;
        }
    }




}