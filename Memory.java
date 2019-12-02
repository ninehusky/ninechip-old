import java.io.*;

public class Memory {

    // CONSTANTS
    public static final char START_ADDRESS = 0x200;
    public static final char FONT_START_ADDRESS = 0x50;
    public static final int MEM_SIZE_IN_BYTES = 4096;

    public static final char[] FONT_SET = { 
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
    // TODO: make this private
    public byte[] memory;

    public Memory() {
        memory = new byte[MEM_SIZE_IN_BYTES];
        loadROM(new File("demo.ch8"));
        loadFontSet();
        System.out.println("memory loaded!");
    }

    /**
     * Writes the given value to the given address.
     * @param char address - address to write to
     * @param byte value - value to be written
     * 
     */
    public void write(char address, byte value) {
        if (address < START_ADDRESS || address > 0xFFF) {
            throw new IllegalArgumentException("That spot in memory is out of bounds!");
        }
        memory[address] = value;
    }

    /**
     * Returns byte from given address.
     * @param char address - address to read from
     * @return byte - byte from given address
     */
    public byte read(char address) {
        if (address < START_ADDRESS || address > 0xFFF) {
            throw new IllegalArgumentException("That spot in memory is out of bounds!");
        }
        return memory[address];
    }


    /**
     * Returns the opcode (2 byte instruction) at the given address
     * @param char programCounter - address from which to look
     * @return char - opcode
     */
    public char fetchOpcode(char programCounter) {
        // System.out.println(String.format("first: %02x", (byte)memory[programCounter]));
        // System.out.println(String.format("second: %02x", (byte)memory[programCounter + 1]));
        // System.out.println(String.format("%04x", (int)((char)(memory[programCounter] << 8 | memory[programCounter + 1]))));
        return (char)(memory[programCounter] << 8 | memory[programCounter + 1]);
    }

    /**
     * Loads the font set into memory.
     */
    public void loadFontSet() {
        for (int i = 0; i < FONT_SET.length; i++) {
            memory[i] = (byte)FONT_SET[i];
        }
    }

    /**
     * Reads the given ROM file into memory.
     * @param File ROMFile - given .rom file to read
     * @throws Exception if file size (in bytes) is larger than
     *                   MEM_SIZE_IN_BYTES
     */
    public void loadROM(File ROMFile) {
        long length = ROMFile.length();
        if (length > MEM_SIZE_IN_BYTES - START_ADDRESS) {
            throw new IllegalArgumentException("File too large for memory!");
        }
        try {
            FileInputStream inputStream = new FileInputStream(ROMFile);
            byte[] iHateJava8WhyIsThisHappening = new byte[(int)(ROMFile.length())];
            inputStream.read(iHateJava8WhyIsThisHappening);
            inputStream.close();
            for (int i = 0; i < iHateJava8WhyIsThisHappening.length; i++) {
                 memory[START_ADDRESS + i] = iHateJava8WhyIsThisHappening[i];
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints the contents of memory to a file named output.rom.
     */
    public void printMemory() {
        try {
            int realIndex = 0;
            PrintStream output = new PrintStream("output.rom");
            for (int i = START_ADDRESS; i < START_ADDRESS + 353; i++) { // 353 bytes size of game
                output.print(String.format("%02x", memory[i]) + " ");
                realIndex++;
                if (realIndex % 16 == 0) {
                    output.println();
                }
            }
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}