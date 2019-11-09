import java.io.*;
import java.util.*;
import javax.swing.*;

public class Chip8 {
    /** VIDEO RELATED FIELDS **/
    private JFrame frame;
    private static int WIDTH;
    private static int HEIGHT;
    private static int PIXEL_SIZE;

    /** CPU RELATED FIELDS **/
    private byte[] registers;
    private byte[] memory; // not sure if byte
    private char indexRegister;
    private char programCounter;
    private char[] stack;
    private char stackPointer;
    private byte delayTimer;
    private byte soundTimer;
    private int[] video;
    private char opcode;

    /** FONT RELATED FIELDS */
    private final char[] FONT_SET = {
        0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
        0x20, 0x60, 0x20, 0x20, 0x70, // 1
        0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
        0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
        0x90, 0x90, 0xF0, 0x10, 0x10, // 4
        0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
        0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
        0xF0, 0x10, 0x20, 0x40, 0x40, // 7
        0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
        0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
        0xF0, 0x90, 0xF0, 0x90, 0x90, // A
        0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
        0xF0, 0x80, 0x80, 0x80, 0xF0, // C
        0xE0, 0x90, 0x90, 0x90, 0xE0, // D
        0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
        0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };

    private final int FONT_SET_SIZE;
    private final char START_ADDRESS;
    private final char FONT_START_ADDRESS;

    /**
     * Creates new Chip8.
     */
    public Chip8() {
        WIDTH = 64;
        HEIGHT = 32;
        PIXEL_SIZE = 10;

        FONT_SET_SIZE = 80;
        START_ADDRESS = 0x200;
        FONT_START_ADDRESS = 0x50;

        registers = new byte[16];
        memory = new byte[4096];
        stack = new char[16];
        video = new int[64 * 32]; // consts
        programCounter = START_ADDRESS;

        frame = new JFrame();

        loadFontSet();
        setupFrame();
    }

    /**
     * Loads the data from the given ROMFile into memory.
     * @param ROMFile - File that contains game data
     * @throws IllegalArgumentException - if file size too large for memory
     */
    public void loadROM(File ROMFile) {
        long length = ROMFile.length();
        if (length > 4096) {
            throw new IllegalArgumentException("File size too large for memory!");
        }
        try {
            InputStream inputStream = new FileInputStream(ROMFile);
            byte[] buffer = inputStream.readAllBytes();
            inputStream.close();
            for (int i = 0; i < buffer.length; i++) {
                memory[START_ADDRESS + i] = buffer[i];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupFrame() {
        frame.setVisible(true);
        frame.setTitle("ninechip!");
        frame.setSize(WIDTH * PIXEL_SIZE, HEIGHT * PIXEL_SIZE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
    }

    /**
     * Loads font set into memory.
     */
    private void loadFontSet() {
        for (int i = 0; i < FONT_SET_SIZE; i++) {
            memory[FONT_START_ADDRESS + i] = (byte)FONT_SET[i];
        }
        System.out.println("Fonts loaded!");
    }

    /**
     * Returns random byte between 0 and 255 inclusive.
     * @return random byte between 0 and 255 inclusive
     */
    private byte generateRandomByte() {
        Random r = new Random();
        return (byte)r.nextInt(256);
    }

    /**
     * Outputs memory data to output.txt
     * (I know this is ridiculously redundant)
     * @throws Exception if output.txt not existent
     */
    public void debugHex(String type) throws Exception {
        PrintStream output = new PrintStream(new File("output.txt"));
        int realIndex = 0;
        if (type.equals("fonts")) {
            for (int i = FONT_START_ADDRESS; i < FONT_START_ADDRESS + FONT_SET_SIZE; i++) {
                output.print(String.format("%02x", memory[i]) + " ");
                realIndex++;
                if (realIndex % 5 == 0) {
                    output.println();
                }
            }
        } else if (type.equals("rom")) {
            for (int i = START_ADDRESS; i < START_ADDRESS + 353; i++) { // 353 bytes size of game
                output.print(String.format("%02x", memory[i]) + " ");
                realIndex++;
                if (realIndex % 16 == 0) {
                    output.println();
                }
            }
        }
    }

    /** OPCODES */
    /**
     * CLS
     * Clears screen.
     */
    private void opcode00E0() {
        for (int i = 0; i < video.length; i++) {
            video[i] = 0;
        }
    }

    /**
     * RET
     * Return from a subroutine.
     * The interpreter sets the program counter to the address at
     * the top of the stack, then subtracts 1 from the stack pointer.
     */
    private void opcode00EE() {
        stackPointer--;
        programCounter = stackPointer;
    }

    /**
     * JP addr
     * Jump to location nnn.
     */
    private void opcode1nnn() {
        char address = (char)(opcode & 0x0FFF);
        programCounter = address;
    }

    /**
     * CALL addr
     * The interpreter increments the stack pointer,
     * then puts the current PC on the top of the stack.
     * The PC is then set to nnn.
     */
    private void opcode2nnn() {
        stackPointer++;
        stack[stackPointer] = programCounter;
        int address = programCounter & 0x0FFF;
        programCounter = (char)address;
    }

    /**
     * SE Vx, byte
     * Skip next instruction if Vx = kk
     * The interpreter compares register Vx to kk, and if they are equal,
     * increments the program counter by 2.
     */
    private void opcode3xkk() {
        char x = (char)((programCounter & 0x0F00) >> 8);
        char kk = (char)(programCounter & 0x00FF);
        if (registers[x] == kk) {
            programCounter += 2;
            // skip two bytes, each opcode is 2 bytes so we're skipping one instruction
        }
    }

    /**
     * SNE Vx, byte
     * Skip next instruction if Vx != kk
     * The interpreter compares register Vx to kk, if not equal
     * increments program counter by 2.
     */
    private void opcode4xkk() {
        char x = (char)((opcode & 0x0F00) >> 8);
        char kk = (char)(opcode & 0x00FF);
        if (registers[x] != kk) {
            programCounter += 2;
        }
    }

    /**
     * SE Vx, Vy
     * Skip next instruction if Vx = Vy.
     * Interpreter compares register Vx to register Vy, if they
     * are equal increments program counter by 2.
     */
    private void opcode5xy0() {
        char x = (char)((opcode & 0x0F00) >> 8);
        char y = (char)((opcode & 0x00F0) >> 4);
        if (registers[x] == registers[y]) {
            programCounter += 2;
        }
    }

    /**
     * LD Vx, byte
     * Set Vx = kk.
     * The interpreter puts the value kk into register Vx.
     */
    private void opcode6xkk() {
        byte kk = (byte)(programCounter & 0x00FF);
        char x = (char)((programCounter & 0x0F00) >> 8);
        // TODO: i don't think I need to use a char, since 0 <= x <= 16
        registers[x] = kk;
    }

    /**
     * ADD Vx, byte
     * Set Vx = Vx + kk.
     * Adds the value kk to the value of register Vx,
     * then stores the result in Vx.
     */
    private void opcode7xkk() {
        byte kk = (byte)(opcode & 0x00FF);
        byte x = (byte)((opcode & 0x0F00) >> 8);
        registers[x] += kk;
    }

    /**
     * LD Vx, Vy
     * Set Vx = Vy
     * Stores the value of register Vy into register Vx.
     */
    private void opcode8xy0() {
        byte x = (byte)((opcode & 0x0F00) >> 8);
        byte y = (byte)((opcode & 0x00F0) >> 4);
        registers[x] = registers[y];
    }

    /**
     * OR Vx, Vy
     * Set Vx = Vx OR Vy
     * Performs a bitwise OR on the values of Vx and Vy, then stores
     * the result in Vx.
     */
    private void opcode8xy1() {
        byte x = (byte)((opcode & 0x0F00) >> 8);
        byte y = (byte)((opcode & 0x00F0) >> 4);
        registers[x] |= registers[y];
    }

    /**
     * AND Vx, Vy
     * Set Vx = Vx AND Vy
     */
    private void opcode8xy2() {
        byte x = (byte)((opcode & 0x0F00) >> 8);
        byte y = (byte)((opcode & 0x00F0) >> 4);
        registers[x] &= registers[y];
    }

    /**
     * XOR Vx, Vy
     * Set Vx = Vx XOR Vy
     */
    private void opcode8xy3() {
        byte x = (byte)((opcode & 0x0F00) >> 8);
        byte y = (byte)((opcode & 0x00F0) >> 4);
        registers[x] ^= registers[y];
    }

    /**
     * ADD Vx, Vy
     * Set Vx = Vx + Vy, Set VF = carry
     * The values of Vx and Vy are added together.
     * If the result is greater than 8 bits (i.e., > 255,) VF is set to 1,
     * (VF --> registers[15])
     * otherwise 0. Only the lowest 8 bits of the result are kept, and stored in Vx.
     */
    private void opcode8xy4() {
        byte x = (byte)((opcode & 0x0F00) >> 8);
        byte y = (byte)((opcode & 0x00F0) >> 4);
        int sum = registers[x] + registers[y];
        if (sum > 255) {
            registers[0xF] = 1;
        } else {
            registers[0xF] = 0;
        }
        registers[x] = (byte)sum;
    }

    /**
     * SUB Vx, Vy
     * Set Vx = Vx - Vy, set VF = NOT borrow.
     * If Vx > Vy, then VF is set to 1, otherwise 0.
     * Then Vy is subtracted from Vx, and the results stored in Vx.
     */
    private void opcode8xy5() {
        byte x = (byte)((opcode & 0x0F00) >> 8);
        byte y = (byte)((opcode & 0x00F0) >> 4);
        registers[0xF] = (byte)(registers[x] > registers[y] ? 1 : 0);
        registers[x] -= registers[y];
    }

    /**
     * SHR Vx {, Vy}
     * Set Vx = Vx SHR 1
     * If the least-significant bit of Vx is 1, then VF is set to 1,
     * otherwise 0. Then Vx is divided by 2.
     * In this case, least-significant bit is rightmost bit.
     * TODO: look over this, it might not be good
     */
    private void opcode8xy6() {
        byte x = (byte)((opcode & 0x0F00) >> 8);
        registers[0xF] = (byte)(registers[x] & 0x1);
        registers[x] >>= 1;
    }

    /**
     * SUBN Vx, Vy
     * Set Vx = Vy - Vx, set VF = NOT borrow
     * If Vy > Vx, VF Set to 1, else 0.
     * Then Vx subtracted from Vy, results stored in Vx.
     */
    private void opcode8xy7() {
        byte x = (byte)((opcode & 0x0F00) >> 8);
        byte y =  (byte)((opcode & 0x00F0) >> 4);
        registers[0xF] = (byte)(registers[y] > registers[x] ? 1 : 0);
        registers[x] = (byte)(registers[y] - registers[x]);
    }

    /**
     * SHL Vx {, Vy}
     * Vx = Vx SHL 1
     * If most significant bit of Vx is 1, then VF is set to 1, else 0
     * Then Vx multiplied by 2.
     * TODO: look over this
     */
    private void opcode8xy7() {
        byte x = (byte)((opcode & 0x0F00) >> 8);
        byte mostSignificantBit = (byte)(registers[x] >> 7);
        registers[0xF] = (byte)(mostSignificantBit);
        registers[x] <<= 1; // multiply Vx by 2
    }

    /**
     * SNE Vx, Vy
     * Skip next instruction if Vx != Vy
     */
    private void opcode9xy0() {
        byte x = (byte)((programCounter & 0x0F00) >> 8);
        byte y = (byte)((programCounter & 0x00F0) >> 4);
        if (registers[x] != registers[x]) {
            programCounter += 2;
        }
    }

    /**
     * LD I, addr
     * Value of register I is set to nnn.
     */
    private void opcodeAnnn() {
        char value = (char)(programCounter & 0x0FFF);
        indexRegister = value;
    }

}