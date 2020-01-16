public class Main extends Thread {
    private Chip8 chip8;

    public Main(String ROM) {
        chip8 = new Chip8(ROM);
    }

    public void run() {
        while (true) {    
            chip8.cycle();
            chip8.decrementTimers();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {

            }
        }
    }
    public static void main(String[] args) {
        try {
            String ROM = args[0];
            Main main = new Main(ROM);
            main.start();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Usage: java Main file.rom");
        }        
        // DisplayFrame frame = new DisplayFrame(new Keypad());
    }
}