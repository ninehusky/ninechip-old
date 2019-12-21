/**
 * Represents Chip-8's RAM.
 * @author Andrew Cheung
 */

import java.io.*;

public class Memory {

    public static final char START_ADDRESS = 0x200;
    public static final char END_ADDRESS = 0xFFF;
    public static final int MEM_SIZE_IN_BYTES = 4096;
    
    private byte[] memory;
    private int gameLength;

    /**
     * Constructs new Memory object.
     */
    public Memory() {
        memory = new byte[MEM_SIZE_IN_BYTES];
        gameLength = -1;
    }

    /**
     * Returns byte at given address
     * @param address - address at which memory is read
     * @return byte at given address
     * @throws IllegalStateException if ROM File not yet loaded
     * @throws IllegalArgumentException if address is out of bounds
     *                                  (addr < START_ADDRESS || addr > END_ADDRESS)
     */
    public byte read(char address) {
        if (gameLength == -1) {
            throw new IllegalStateException("ROM File not yet loaded!");
        } else if (address < START_ADDRESS || address > END_ADDRESS) {
            throw new IllegalArgumentException("Given address out of bounds!");
        }
        return memory[address];
    }

    /**
     * Writes the given value to the given address in memory.
     * @param address - address to write to
     * @param value - value that will be replacing old value
     * @throws IllegalStateException if ROM File not yet loaded
     * @throws IllegalArgumentException if address is out of bounds
     *                                  (addr < START_ADDRESS || addr > END_ADDRESS)
     */
    public void write(char address, byte value) {
        if (gameLength == -1) {
            throw new IllegalStateException("ROM File not yet loaded!");
        } else if (address < START_ADDRESS || address > END_ADDRESS) {
            throw new IllegalArgumentException("Given address out of bounds!");
        }
        memory[address] = value;
    }

    /**
     * Returns the current opcode.
     * @param address - address at which to read opcode
     * @return char opcode at given address
     * @throws IllegalStateException if ROM File not yet loaded
     * @throws IllegalArgumentException if address is out of bounds
     *                                  (addr < START_ADDRESS || addr > END_ADDRESS)
     * TODO: investigate that weird stupid Java conversion stuff
     */
    public char getOpcode(char address) {
        if (gameLength == -1) {
            throw new IllegalStateException("ROM File not yet loaded!");
        } else if (address < START_ADDRESS || address > END_ADDRESS) {
            throw new IllegalArgumentException(String.format("Given address %04x out of bounds!", 
                                                              (int)address));
        }
        return (char)(((memory[address] & 0xFF) << 8) | (memory[address + 1] & 0xFF));
    }

    /**
     * Loads contents of given ROM file into memory.
     * @param fileName - String of file name
     * @throws IllegalArgumentException if ROM file is too small (0 bytes), or too large, i.e.,
     *                                  if its size in bytes is larger than MEM_SIZE_IN_BYTES - START_ADDRESS.
     */
    public void loadROM(String fileName) {
        try {
            System.out.println("Loading ROM File...");

            File ROMFile = new File(fileName);
            int fileLength = (int)ROMFile.length();
            if (fileLength > MEM_SIZE_IN_BYTES - START_ADDRESS) {
                throw new IllegalArgumentException("ROM file too big for memory!");
            } else if (fileLength == 0) {
                throw new IllegalArgumentException("ROM file too small!");
            }
            InputStream inputStream = new FileInputStream(ROMFile);
            byte[] gameData = new byte[fileLength];
            inputStream.read(gameData);
            for (int i = 0; i < gameData.length; i++) {
                memory[START_ADDRESS + i] = gameData[i];
            }
            gameLength = fileLength;
            inputStream.read(gameData);
            inputStream.close();
            System.out.println("File loaded!");
        } catch (Exception e) {
            gameLength = -1;
            e.printStackTrace();
        }
    }

    /**
     * Outputs values of RAM containing game to file named output.rom
     * @throws IllegalStateException if ROM File has not yet loaded successfully
     */
    public void printMemory() {
        if (gameLength < 0) {
            throw new IllegalStateException("ROM File not yet loaded!");
        }
        try {
            PrintStream output = new PrintStream("output.rom");
            int elementsPrinted = 0;
            for (int i = START_ADDRESS; i < START_ADDRESS + gameLength; i++) {
                output.print(String.format("%02x ", memory[i]).toUpperCase());
                elementsPrinted++;
                if (elementsPrinted % 16 == 0) {
                    output.println();
                }
            }
            output.close();
            System.out.println("Memory output to output.rom!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}