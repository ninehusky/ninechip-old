import javax.swing.*;

public class DisplayFrame extends JFrame {
    private DisplayPanel displayPanel;
    public static final int WIDTH = 64;
    public static final int HEIGHT = 32;
    public static final int PIXEL_SIZE = 10;

    public DisplayFrame(Keypad keypad) {
        setSize(WIDTH * PIXEL_SIZE, HEIGHT * PIXEL_SIZE);
        setTitle("ninechip!");
        displayPanel = new DisplayPanel();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().add(displayPanel);
        pack();
        setLocationByPlatform(true);
        setVisible(true);
        displayPanel.addKeyListener(keypad);
        displayPanel.setFocusable(true);
        displayPanel.requestFocusInWindow();        
    }

    public void updateGraphics(char[][] graphics) {
        displayPanel.updateGraphics(graphics);
    }
}