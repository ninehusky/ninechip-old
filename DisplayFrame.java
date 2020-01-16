import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener; 

public class DisplayFrame extends JFrame implements KeyListener {
    public static final int WIDTH = 64;
    public static final int HEIGHT = 32;
    public static final int PIXEL_SIZE = 10;
    public static final int NUM_OF_KEYS = 16;

    private DisplayPanel displayPanel;
    private boolean[] pressed;
    private Chip8 c8;

    public DisplayFrame(Chip8 c8) {
        this.c8 = c8;
        pressed = new boolean[NUM_OF_KEYS];

        setSize(WIDTH * PIXEL_SIZE, HEIGHT * PIXEL_SIZE + 20);
        setTitle("ninechip!");
        displayPanel = new DisplayPanel();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(displayPanel);
        setVisible(true);
        addKeyListener(this); 
    }

    public void updateGraphics(char[][] graphics) {
        displayPanel.updateGraphics(graphics);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_1) {
            pressed[1] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_2) {
            pressed[2] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_3) {
            pressed[3] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_4) {
            pressed[0xC] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_Q) {
            pressed[4] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_W) {
            pressed[5] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_E) {
            pressed[6] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_R) {
            pressed[0xD] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            pressed[7] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            pressed[8] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            pressed[9] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_F) {
            pressed[0xE] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_Z) {
            pressed[0xA] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_X) {
            pressed[0] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_C) {
            pressed[0xB] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_V) {
            pressed[0xF] = true;
        }
        c8.setPressed(pressed);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_1) {
            pressed[1] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_2) {
            pressed[2] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_3) {
            pressed[3] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_4) {
            pressed[0xC] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_Q) {
            pressed[4] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_W) {
            pressed[5] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_E) {
            pressed[6] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_R) {
            pressed[0xD] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            pressed[7] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            pressed[8] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            pressed[9] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_F) {
            pressed[0xE] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_Z) {
            pressed[0xA] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_X) {
            pressed[0] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_C) {
            pressed[0xB] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_V) {
            pressed[0xF] = false;
        }
        c8.setPressed(pressed);
    }

    public boolean[] getPressed() {
        return pressed;
    }
}