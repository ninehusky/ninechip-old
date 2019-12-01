import java.util.*;

public class Chip8 {

    private byte[] registers;
    private char indexRegister;
    private byte soundRegister;
    private byte delayRegister;

    private char[] stack;
    private byte stackPointer;
    private char programCounter;

    private char opcode;

    private Memory memory;
    private Keypad keypad;

    public Chip8() {
        memory = new Memory();
        keypad = new Keypad();
        programCounter = Memory.START_ADDRESS;
        opcode = 0;
        // Clear display
        // Clear stack
        stack = new char[16];
        // Clear registers
        registers = new byte[16];
        indexRegister = 0;
        stackPointer = 0;
    }

    public void cycle() {
        opcode = memory.fetchOpcode(programCounter);
        decodeOpcode(opcode);
        // TODO: increment programCounter here
        if (delayRegister > 0) {
            delayRegister--;
        }
        if (soundRegister > 0) {
            if (soundRegister >= 0x02) {
                System.out.println("boop");
            }
            soundRegister--;
        }
    }

    /**
     * WARNING: This is a real mess.
     * Decodes the given opcode and updates CPU information.
     * @param opcode - opcode to perform.
     */
    public void decodeOpcode(char opcode) {
        switch (opcode & 0xF000) { // opcode begins with something
            case 0x0000:
                switch(opcode) {
                    case 0x00E0:
                        // TODO: clear screen
                        break;
                    case 0x00EE:
                        programCounter = stack[stackPointer--];
                        break;
                    default:
                        System.out.println(String.format("Invalid opcode %04x", opcode));
                        break;
                }
                break;
            case 0x1000:
                // 1nnn - sets program counter to nnn
                programCounter = (char)(opcode & 0x0FFF);
                break;
            case 0x2000:
                // 2nnn - increments the stack pointer,
                // then puts the current PC on the top of the stack.
                // The PC is then set to nnn.
                stackPointer++;
                stack[stackPointer] = programCounter;
                programCounter = (char)(opcode & 0x0FFF);
                break;
            case 0x3000:
                // Skip next instruction if Vx = kk.
                byte x = (byte)((opcode & 0x0F00) >> 8);
                byte Vx = registers[x];
                byte kk = (byte)(opcode & 0x00FF);
                if (Vx == kk) {
                    programCounter += 2;
                }
                break;
            case 0x4000:
                // Skip next instruction if Vx != kk.
                x = (byte)((opcode & 0x0F00) >> 8);
                Vx = registers[x];
                kk = (byte)(opcode & 0x00FF);
                if (Vx != kk) {
                    programCounter += 2;
                }
                programCounter += 2; // i think i still need this
                break;
            case 0x5000:
                // Skip next instruction if Vx = Vy.
                x = (byte)((opcode & 0x0F00) >> 8);
                byte y = (byte)((opcode & 0x00F0) >> 4);
                if (registers[x] == registers[y]) {
                    programCounter += 2;
                }
                programCounter += 2;
                break;
            case 0x6000:
                // The interpreter puts the value kk into register Vx.
                kk = (byte)(opcode & 0x00FF);
                x = (byte)((opcode & 0x0F00) >> 8);
                registers[x] = kk;
                programCounter += 2;
                break;
            case 0x7000:
                // Set Vx = Vx + kk.
                kk = (byte)(opcode & 0x00FF);
                x = (byte)((opcode & 0x0F00) >> 8);
                registers[x] += kk;
                programCounter += 2;
                break;
            case 0x8000:
                x = (byte)((opcode & 0x0F00) >> 8);
                y = (byte)((opcode & 0x00F0) >> 4);
                switch (opcode & 0x000F) {
                    case 0x0000:
                        // Stores the value of register Vy in register Vx.
                        registers[x] = registers[y];
                        programCounter += 2;
                        break;
                    case 0x0001:
                        // Set Vx = Vx OR Vy.
                        registers[x] |= registers[y];
                        programCounter += 2;
                        break;
                    case 0x0002:
                        // Set Vx = Vx AND Vy.
                        registers[x] &= registers[y];
                        programCounter += 2;
                        break;
                    case 0x0003:
                        // Set Vx = Vx XOR Vy.
                        registers[x] ^= registers[y];
                        programCounter += 2;
                        break;
                    case 0x0004:
                        // Set Vx = Vx + Vy, set VF = carry.
                        if (registers[x] + registers[y] > 255) {
                            registers[0xF] = 1;
                        } else {
                            registers[0xF] = 0;
                        }
                        registers[x] = (byte)((registers[x] + registers[y]) & 0x00FF);
                        programCounter += 2;
                        break;
                    case 0x0005:
                        // If Vx > Vy, then VF is set to 1, otherwise 0.
                        // Then Vy is subtracted from Vx, and the results stored in Vx.
                        if (registers[x] > registers[y]) {
                            registers[0xF] = 1;
                        } else {
                            registers[0xF] = 0;
                        }
                        registers[x] -= registers[y];
                        programCounter += 2;
                        break;
                    case 0x0006:
                        // Set Vx = Vx SHR 1.
                        // If the least-significant bit of Vx is 1, then VF is set to 1,
                        // otherwise 0. Then Vx is divided by 2.
                        if ((registers[x] & 0x0F) == 1) {
                            registers[0xF] = 1;
                        } else {
                            registers[0xF] = 0;
                        }
                        registers[x] /= 2;
                        programCounter += 2;
                        break;
                    case 0x0007:
                        // Set Vx = Vy - Vx, set VF = NOT borrow.
                        // If Vy > Vx, then VF is set to 1, otherwise 0.
                        // Then Vx is subtracted from Vy, and the results stored in Vx.
                        if (registers[y] > registers[x]) {
                            registers[0xF] = 1;
                        } else {
                            registers[0xF] = 0;
                        }
                        registers[x] = (byte)(registers[y] - registers[x]);
                        programCounter += 2;
                        break;
                    case 0x000E:
                        // If the most-significant bit of Vx is 1, then VF is set to 1
                        // otherwise to 0. Then Vx is multiplied by 2.
                        byte MSB = (byte)((registers[x] & 0xF0) >> 7);
                        if (MSB == 1) {
                            registers[0xF] = 1;
                        } else {
                            registers[0xF] = 0;
                        }
                        registers[x] *= 2;
                        programCounter += 2;
                        break;
                    default:
                        System.out.println(String.format("Invalid opcode %04x", opcode));
                        break;
                }
            case 0x9000:
                // Skip next instruction if Vx != Vy.
                // The values of Vx and Vy are compared, and if they are not equal,
                // the program counter is increased by 2.
                x = (byte)((opcode & 0x0F00) >> 8);
                y = (byte)((opcode & 0x00F0) >> 4);
                if (registers[x] != registers[y]) {
                    programCounter += 2;
                }
                programCounter += 2;
                break;
            case 0xA000:
                // The value of register I is set to nnn.
                indexRegister = (char)(opcode & 0x0FFF);
                programCounter += 2;
                break;
            case 0xB000:
                // Jump to location nnn + V0.
                // The program counter is set to nnn plus the value of V0.
                programCounter = (char)((opcode & 0x0FFF) + registers[0]);
                programCounter += 2;
                break;
            case 0xC000:
                // Set Vx = random byte AND kk.
                // TODO: see if x has any funky behavior
                byte randByte = generateRandomByte();
                kk = (byte)(opcode & 0x00FF);
                x = (byte)((opcode & 0x0F00) >> 8);
                registers[x] = (byte)(randByte & kk);
                programCounter += 2;
                break;
            case 0xD000:
                // Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision.
                break;
            case 0xE000:
                switch (opcode & 0x000F) {
                    case 0x000E:
                        x = (byte)((opcode & 0x0F00) >> 8);
                        // Checks the keyboard,
                        // and if the key corresponding to the value of Vx is currently in the down
                        // position, PC is increased by 2.
                        if (keypad.getPressed()[registers[x]]) {
                            programCounter += 2;
                        }
                        break;
                    case 0x0001:
                        // Skip next instruction if key with the value of Vx is not pressed.
                        // Checks the keyboard, and if the key corresponding to the value of Vx
                        // is currently in the up position, PC is increased by 2.
                        x = (byte)((opcode & 0x0F00) >> 8);
                        if (!keypad.getPressed()[registers[x]]) {
                            programCounter += 2;
                        }
                        break;
                    default:
                        break;
                }
                break;
            case 0xF000:
                x = (byte)((opcode & 0x0F00) >> 8);
                switch (opcode & 0x00FF) {
                    case 0x0007:
                        // The value of DT is placed into Vx.
                        registers[x] = delayRegister;
                        programCounter += 2;
                        break;
                    case 0x000A:
                        // All execution stops until a key is pressed,
                        // then the value of that key is stored in Vx.
                        boolean[] pressedKeys = keypad.getPressed();
                        boolean hasPressed = false;
                        for (int i = 0; i < pressedKeys.length; i++) {
                            if (pressedKeys[i]) {
                                registers[x] = (byte)i;
                                hasPressed = true;
                                break;
                            }
                        }
                        if (!hasPressed) {
                            programCounter -= 2;
                        }
                        break;
                    case 0x0015:
                        // Set delay timer = Vx.
                        delayRegister = registers[x];
                        programCounter += 2;
                        break;
                    case 0x0018:
                        // Set sound timer = Vx.
                        soundRegister = registers[x];
                        programCounter += 2;
                        break;
                    case 0x001E:
                        // Set I = I + Vx;
                        indexRegister += registers[x];
                        programCounter += 2;
                        break;
                    case 0x0029:
                        // The value of I is set to the location for the hexadecimal sprite
                        // corresponding to the value of Vx.
                        // this particular solution inspired by austin
                        indexRegister = (char)(Memory.FONT_START_ADDRESS + (5 * registers[x]));
                        break;
                    case 0x0033:
                        // Store BCD representation of Vx in memory locations I, I+1, and I+2.
                        // The interpreter takes the decimal value of Vx,
                        // and places the hundreds digit in memory at location in I,
                        // the tens digit at location I+1, and the ones digit at location I+2.
                        int value = registers[x];
                        memory.write(indexRegister, (byte)(value % 10));
                        value /= 10;
                        memory.write((char)(indexRegister + 1), (byte)(value % 10));
                        value /= 10;
                        memory.write((char)(indexRegister + 2), (byte)(value % 10));
                        break;
                    case 0x0055:
                        // Store registers V0 through Vx in memory starting at location I.
                        // The interpreter copies the values of registers V0 through Vx into memory,
                        // starting at the address in I.
                        for (int i = 0; i <= x; i++) {
                            memory.write((char)(indexRegister + i), registers[i]);
                        }
                        programCounter += 2;
                        break;
                    case 0x0065:
                        // Read registers V0 through Vx from memory starting at location I.
                        // The interpreter reads values from memory starting at location I
                        // into registers V0 through Vx.
                        for (int i = 0; i <= x; i++) {
                            registers[i] = memory.read((char)(indexRegister + i));
                        }
                        programCounter += 2;
                        break;
                }

            default:
                System.out.println(String.format("Invalid opcode %04x", opcode));
                programCounter += 2;
                break;
        }
    }

    private byte generateRandomByte() {
        Random r = new Random();
        int randomNum = r.nextInt(256);
        return (byte)randomNum;
    }
}