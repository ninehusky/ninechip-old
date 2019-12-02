public class Chip8Driver {
    public static void main(String[] args) {
        Chip8 chip8 = new Chip8();
        while (true) {
            chip8.cycle();
        }
        // Memory m = new Memory();
        // char opcode = m.fetchOpcode((char)0x200);
        // System.out.println(String.format("%04x", (int)opcode));
    }
}