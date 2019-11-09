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
        programCounter = stackPointer;
        stackPointer--;
    }

    /**
     * JP addr
     * Jump to location nnn.
     */
    private void opcode1nnn() {
        char address = (char)(programCounter & 0x0FFF);
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
        char x = (char)((programCounter & 0x0F00) >>> 2);
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
        char x = (char)((programCounter & 0x0F00) >>> 2);
        char kk = (char)(programCounter & 0x00FF);
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
        char x = (char)((programCounter & 0x0F00) >>> 2);
        char y = (char)((programCounter & 0x00F0) >>> 1);
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
        char x = (char)((programCounter & 0x0F00) >>> 2);
        // TODO: i don't think I need to use a char, since 0 <= x <= 16
        registers[x] = kk;
    }

    /**
     * Set Vx = Vx + kk.
     * Adds the value kk to the value of register Vx,
     * then stores the result in Vx.
     */
    private void opcode7xkk() {
        byte kk = (byte)(programCounter & 0x00FF);
        char x = (char)((programCounter & 0x0F00) >>> 2);
        registers[x] += kk;
    }


}