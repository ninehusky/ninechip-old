/**
 * Represents a Chip-8.
 * @author Andrew Cheung
 */

 import java.util.*;
 import java.io.*;

public class Chip8 {
    private Memory memory;
    private char opcode;

    private DisplayFrame display;

    private Map<Character, OpcodeFunction> opcodeFuncs;

    private byte[] registers;
    private char indexRegister;
    private byte delayRegister;
    private byte soundRegister;

    private char programCounter;

    private char[] stack;
    private byte stackPointer;

    private char[][] graphics;
    private boolean needsDrawing;

    public Chip8(String ROM) {
        memory = new Memory();
        memory.loadROM(ROM);
        memory.printMemory();

        display = new DisplayFrame();

        opcodeFuncs = new HashMap<Character, OpcodeFunction>();
        initializeOpcodes();

        registers = new byte[16];
        indexRegister = 0;
        programCounter = Memory.START_ADDRESS;
        
        stack = new char[16];
        stackPointer = 0;
        // where should the stack pointer point?

        graphics = new char[64][32];
    }

    /**
     * Emulates one cycle of the CHIP-8.
     */
    public void cycle() {
        opcode = memory.getOpcode(programCounter);
        execOpcode();
        if (needsDrawing) {
            display.updateGraphics(graphics);
            display.repaint();
            needsDrawing = false;
        }
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
        opcodeFuncs.put((char)(0x8007), new SubnVxVy());
        opcodeFuncs.put((char)(0x800E), new BitShiftLeft());
        opcodeFuncs.put((char)(0x9000), new SNEVxVy());
        opcodeFuncs.put((char)(0xA000), new SetIToNnn());
        opcodeFuncs.put((char)(0xB000), new JumpV0());
        opcodeFuncs.put((char)(0xC000), new Rnd());
        opcodeFuncs.put((char)(0xD000), new Draw());
        opcodeFuncs.put((char)(0xE09E), new SkipIfPressed());
        opcodeFuncs.put((char)(0xE0A1), new SkipIfNotPressed());
        opcodeFuncs.put((char)(0xF007), new SetVxToDelayTimer());
        opcodeFuncs.put((char)(0xF00A), new WaitForKeyPress());
        opcodeFuncs.put((char)(0xF015), new SetDelayTimerToVx());
        opcodeFuncs.put((char)(0xF018), new SetSoundTimerToVx());
        opcodeFuncs.put((char)(0xF01E), new AddIAndVx());
        opcodeFuncs.put((char)(0xF029), new SetIToLocationOfSprite());
        opcodeFuncs.put((char)(0xF033), new StoreBCDRepresentationOfVx());
        opcodeFuncs.put((char)(0xF055), new StoreRegistersInMemory());
        opcodeFuncs.put((char)(0xF065), new StoreMemoryInRegisters());
    }

    /**
     * Trims opcode to skeleton and returns skeleton
     */
    private char trimOpcode() {
        if ((opcode & 0xF000) == 0x0000) {
            return opcode;
        } else if ((opcode & 0xF000) == 0x8000) {
            return (char)((opcode & 0xFFFF) & 0xF00F);
        } else if ((opcode & 0xF000) == 0xF000) {
            return (char)(opcode & 0xF0FF);
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
            for (int i = 0; i < graphics.length; i++) {
                for (int j = 0; j < graphics[i].length; j++) {
                    graphics[i][j] = 0x00;
                }
            }
            needsDrawing = true;
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
            programCounter += 2;
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
            } else {
                registers[0xF] = 0;
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
            // System.out.println(String.format("%04x is registers[x]", registers[x]));
            registers[0xF] = (byte)(registers[x] & 0x1);
            // System.out.println(String.format("%04x is registers[0xF]", registers[0xF]));
            registers[x] = (byte)((registers[x] & 0xFF) >>> 1);
            // System.out.println("Dividing!");
            // System.out.println(String.format("%04x is registers[x] post division", registers[x]));
            programCounter += 2;
        }   
    }

    /**
     * 8xy7 - SUBN Vx, Vy
     * Set Vx = Vy - Vx, set VF = NOT borrow.
     * If Vy > Vx, then VF is set to 1, otherwise 0.
     * Then Vx is subtracted from Vy, and the results stored in Vx.
     */
    private class SubnVxVy implements OpcodeFunction {
        public void exec() {
            byte x = (byte)((opcode & 0x0F00) >>> 8);
            byte y = (byte)((opcode & 0x00F0) >>> 4);
            if (registers[y] > registers[x]) {
                registers[0xF] = 1;
            } else {
                registers[0xF] = 0;
            }
            registers[x] -= registers[y];
            programCounter += 2;
        }
    }

    /**
     * 8xyE - SHL Vx {, Vy}
     * Set Vx = Vx SHL 1.
     * If the most-significant bit of Vx is 1, then VF is set to 1, otherwise to 0.
     * Then Vx is multiplied by 2.
     */
    private class BitShiftLeft implements OpcodeFunction {
        public void exec() {
            int x = (byte)((opcode & 0x0F00) >> 8);
            System.out.println(String.format("registers[x] is %02x", (registers[x])));
            registers[0xF] = (byte)((registers[x] >> 7) & 0x01);
            System.out.println("MOST SIGNIFICANT BIT: " + String.format("%04x", registers[0xF]));
            System.out.println(String.format("%02x is old registers[x]", registers[x]));
            registers[x] = (byte)(registers[x] << 1);
            System.out.println(String.format("%02x is registers[F]", registers[0xF]));
            System.out.println(String.format("%02x is registers[x]", registers[x]));
            // (byte)((registers[x] << 1) & 0xFF);
            // System.out.println(String.format("%04x", (byte)registers[x]));
            programCounter += 2;
        }
    }

    /**
     * 9xy0 - SNE Vx, Vy
     * Skip next instruction if Vx != Vy.
     * The values of Vx and Vy are compared, and if they are not equal,
     * the program counter is increased by 2.
     */
    private class SNEVxVy implements OpcodeFunction {
        public void exec() {
            byte x = (byte)((opcode & 0x0F00) >>> 8);
            byte y = (byte)((opcode & 0x00F0) >>> 4);
            if (registers[x] != registers[y]) {
                programCounter += 2;
            }
            programCounter += 2;
        }
    }

    /**
     * Annn - LD I, addr
     * Set I = nnn
     * Value of register I is set to nnn.
     */
    private class SetIToNnn implements OpcodeFunction {
        public void exec() {
            char nnn = (char)((opcode & 0x0FFF));
            indexRegister = nnn;
            programCounter += 2;
        }
    }

    /**
     * Bnnn - JP V0, addr
     * Jump to location nnn + V0
     * Program counter set to nnn plus the value of V0.
     */
    private class JumpV0 implements OpcodeFunction {
        public void exec() {
            char nnn = (char)(opcode & 0x0FFF);
            programCounter = (char)(nnn + registers[0]);
        }
    }

    /**
     * Cxkk - RND Vx, byte
     * Set Vx = random byte AND kk
     */
    private class Rnd implements OpcodeFunction {
        public void exec() {
            Random r = new Random();
            byte randByte = (byte)(r.nextInt(256));
            byte kk = (byte)(opcode & 0x00FF);
            byte x = (byte)((opcode & 0x0F00) >>> 8);
            registers[x] = (byte)(kk & randByte);
            programCounter += 2;
        }
    }

    /**
     * Dxyn - DRW Vx, Vy, nibble
     * Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision.
     * The interpreter reads n bytes from memory, starting at the address stored in I.
     * These bytes are then displayed as sprites on screen at coordinates (Vx, Vy).
     * Sprites are XORed onto the existing screen. If this causes any pixels to be erased,
     * VF is set to 1, otherwise it is set to 0. If the sprite is positioned so part of it
     * is outside the coordinates of the display, it wraps around to the opposite side of the screen.
     */
    private class Draw implements OpcodeFunction {
        public void exec() {
            int n = (opcode & 0x00F);
            int x = (opcode & 0xF00) >>> 8;
            int y = (opcode & 0x0F0) >>> 4;
            int xVal = registers[x] % 64;
            int yVal = registers[y] % 32;

            registers[0xF] = 0;
            for (int row = 0; row < n; row++) {
                System.out.println(String.format("indexRegister pointing to %04x", (int)indexRegister));
                byte pixel = memory.read((char)(indexRegister + row));
                for (int col = 0; col < 8; col++) {
                    int bit = pixel & (0b10000000 >>> col);
                    if (bit != 0) { // we need to draw
                        if (graphics[xVal + col][yVal + row] != 0) { // pixel already taken
                            registers[0xF] = 1;                            
                        }
                        graphics[xVal + col][yVal + row] ^= 1;
                        needsDrawing = true;
                    }
                }
            }
            programCounter += 2;
        }
    }

    /**
     * Ex9E - SKP Vx
     * Skip next instruction if key with the value of Vx is pressed.
     * Checks the keyboard, and if the key corresponding to the value of Vx is currently in the down position,
     * PC is increased by 2.
     */
    private class SkipIfPressed implements OpcodeFunction {
        public void exec() {
            byte x = (byte)((opcode & 0x0F00) >>> 8);
            if (display.getPressed()[x]) {
                programCounter += 2;
            }
            programCounter += 2;
        }
    }

    /**
     * ExA1 - SKNP Vx
     * Skip next instruction if key with the value of Vx is not pressed.
     * Checks the keyboard, and if the key corresponding to the value of Vx is
     * currently in the up position, PC is increased by 2.
     */
    private class SkipIfNotPressed implements OpcodeFunction {
        public void exec() {
            byte x = (byte)((opcode & 0x0F00) >>> 8);
            if (!display.getPressed()[x]) {
                programCounter += 2;
            }
            programCounter += 2;
        }
    }

    /**
     * Fx07 - LD Vx, DT
     * Set Vx = delay timer value.
     * The value of DT is placed into Vx.
     */
    private class SetVxToDelayTimer implements OpcodeFunction {
        public void exec() {
            int x = (opcode & 0x0F00) >> 8;
            registers[x] = delayRegister;
            programCounter += 2;
        }
    }

    /**
     * Fx0A - LD Vx, K
     * Wait for a key press, store the value of the key in Vx.
     * All execution stops until a key is pressed, then the value of that key is stored in Vx.
     */
    private class WaitForKeyPress implements OpcodeFunction {
        public void exec() {
            boolean[] pressed = display.getPressed();
            int x = (opcode & 0x0F00) >> 8;
            for (int i = 0; i < pressed.length; i++) {
                if (pressed[i]) {
                    registers[x] = (byte)i;
                    programCounter += 2;
                    return;
                }
            }
        }
    }

    /**
     * Fx15 - LD DT, Vx
     * Set delay timer = Vx.
     * DT is set equal to the value of Vx.
     */
    private class SetDelayTimerToVx implements OpcodeFunction {
        public void exec() {
            int x = (opcode & 0x0F00) >> 8;
            delayRegister = registers[x];
            programCounter += 2;
        }
    }

    /**
     * Fx18 - LD ST, Vx
     * Set sound timer = Vx.
     * ST is set equal to the value of Vx.
     */
    private class SetSoundTimerToVx implements OpcodeFunction {
        public void exec() {
            int x = (opcode & 0x0F00) >> 8;
            soundRegister = registers[x];
            programCounter += 2;
        }
    }

    /**
     * Fx1E - ADD I, Vx
     * Set I = I + Vx.
     * The values of I and Vx are added,
     * and the results are stored in I.
     */
    private class AddIAndVx implements OpcodeFunction {
        public void exec() {
            int x = (opcode & 0x0F00) >> 8;
            indexRegister = (char)((indexRegister + registers[x]) & 0xFFFF);
            programCounter += 2;
        }
    }

    /**
     * Fx29 - LD F, Vx
     * Set I = location of sprite for digit Vx.
     * The value of I is set to the location for the hexadecimal sprite corresponding to the value of Vx.
     */
    private class SetIToLocationOfSprite implements OpcodeFunction {
        public void exec() {
            int x = (opcode & 0x0F00) >> 8;
            indexRegister = (char)(Memory.FONT_START_ADDRESS + (5 * registers[x]));
            programCounter += 2;
        }
    }

    /**
     * Fx33 - LD B, Vx
     * Store BCD representation of Vx in memory locations I, I+1, and I+2.
     * The interpreter takes the decimal value of Vx,
     * and places the hundreds digit in memory at location in I,
     * the tens digit at location I+1, and the ones digit at location I+2.
     */
    private class StoreBCDRepresentationOfVx implements OpcodeFunction {
        public void exec() {
            int x = (opcode & 0x0F00) >> 8;
            int num = registers[x];
            System.out.println(num);
            int ones = num % 10;
            num /= 10;
            int tens = num % 10;
            num /= 10;
            int hundreds = num % 10;
            System.out.println("hundreds: " + (hundreds & 0xF));
            System.out.println("tens: " + (tens & 0xF));
            System.out.println("ones: " + (ones & 0xF));
            memory.write(indexRegister, (byte)(hundreds & 0xF));
            memory.write((char)(indexRegister + 1), (byte)(tens & 0xF));
            memory.write((char)(indexRegister + 2), (byte)(ones & 0xF));
            programCounter += 2;
        }
    }

    /**
     * Fx55 - LD [I], Vx
     * Store registers V0 through Vx in memory starting at location I.
     * The interpreter copies the values of registers V0 through Vx into memory,
     * starting at the address in I.
     */
    private class StoreRegistersInMemory implements OpcodeFunction {
        public void exec() {
            int x = (opcode & 0x0F00) >> 8;
            for (int i = 0; i <= x; i++) {
                memory.write((char)(indexRegister + i), registers[i]);
            }
            programCounter += 2;
        }
    }

    /**
     * Fx65 - LD Vx, [I]
     * Read registers V0 through Vx from memory starting at location I.
     * The interpreter reads values from memory starting at location I into registers V0 through Vx.
     */
    private class StoreMemoryInRegisters implements OpcodeFunction {
        public void exec() {
            int x = (opcode & 0x0F00) >> 8;
            for (int i = 0; i <= x; i++) {
                registers[i] = memory.read((char)(indexRegister + i));
                System.out.println("Putting " + registers[i] + " into registers[" + i + "]");
            }
            // i have no idea why he's doing this
            indexRegister = (char)(indexRegister + x + 1);
            programCounter += 2;
        }
    }

}