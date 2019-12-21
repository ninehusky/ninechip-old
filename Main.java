public class Main {
    public static void main(String[] args) {
        Chip8 chip8 = new Chip8();
        for (int i = 0; i < 4; i++) {
            chip8.cycle();
        }
    }
}