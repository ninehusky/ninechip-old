import javax.swing.JFrame;

public class Display {
    public static final int WIDTH = 64;
    public static final int HEIGHT = 32;
    public static final int PIXEL_SIZE = 10;
    
    private byte[] graphics;

    public Display() {
        graphics = new byte[WIDTH * HEIGHT];
    }

    public void initialize() {
        JFrame frame = new JFrame("ninechip!");
        frame.setSize(WIDTH * PIXEL_SIZE, HEIGHT * PIXEL_SIZE);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void setGraphics(int index, byte value) {
        graphics[index] = value;
    }

    public byte[] getGraphics() {
        return graphics;
    }
}