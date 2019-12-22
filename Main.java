public class Main extends Thread {
    private Chip8 chip8;

    public Main() {
        chip8 = new Chip8();
    }

    public void run() {
        while (true) {
            chip8.cycle();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {

            }
        }
    }
    public static void main(String[] args) {
        Main main = new Main();
        main.start();
        // DisplayFrame frame = new DisplayFrame(new Keypad());
    }
}