import java.io.*;
import java.util.*;

public class Chip8 {
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

    private final int FONT_SET_SIZE = 80;

    private final int START_ADDRESS = 0x200;
    private final int FONT_START_ADDRESS = 0x50;
    


    private byte[] registers;
    private byte[] memory; // not sure if byte
    private char indexRegister;
    private char programCounter;
    private char[] stack;
    private byte stackPointer;
    private byte delayTimer;
    private byte soundTimer;
    private int[] video;
    private int opcode;

    /**
     * Creates new Chip8.
     */
    public Chip8() {
        registers = new byte[16];
        memory = new byte[4096];
        stack = new char[16];
        video = new int[64 * 32]; // consts
        programCounter = START_ADDRESS;

        loadFontSet();
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
     * @throws Exception if output.txt not existent
     */
    public void printHex() throws Exception {
        PrintStream output = new PrintStream(new File("output.txt"));
        int realIndex = 0;
        for (int i = FONT_START_ADDRESS; i < FONT_START_ADDRESS + FONT_SET_SIZE; i++) {
            output.print(String.format("%02x", memory[i]) + " ");
            realIndex++;
            if (realIndex % 5 == 0) {
                output.println();
            }
        }
    }
}