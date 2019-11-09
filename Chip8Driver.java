import java.io.File;

public class Chip8Driver {
    public static void main(String[] args) {
        Chip8 chip8 = new Chip8();
        try {
            chip8.loadROM(new File("demo.ch8"));
            // chip8.debugHex("rom");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}