/**
 * Represents a Chip-8.
 * @author Andrew Cheung
 */

public class Chip8 {
    private Memory memory;
    private char opcode;

    private byte[] registers;
    private char indexRegister;
    private byte delayRegister;
    private byte soundRegister;

    private char programCounter;

    private byte[] stack;
    private byte stackPointer;

    private byte[] display;

    public Chip8() {
        memory = new Memory();
        memory.loadROM("demo.ch8");
        memory.printMemory();

        registers = new byte[16];
        indexRegister = 0;
        programCounter = Memory.START_ADDRESS;
        
        stack = new byte[16];
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
        programCounter += 2;
    }

    /**
     * Performs the task corresponding to the current opcode.
     */
    public void execOpcode() {
        System.out.println(String.format("Current opcode: %04x", (int)(opcode)));
    }
}