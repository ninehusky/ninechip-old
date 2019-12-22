/**
 * Represents the Chip-8's keypad.
 */

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener; 

public class Keypad implements KeyListener {
    public static final int NUM_OF_KEYS = 16;
    private boolean[] pressed;

    public Keypad() {
        pressed = new boolean[NUM_OF_KEYS];
    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("PRESSED DOWN BITCH");
        System.exit(0);
        if (e.getKeyCode() == KeyEvent.VK_1) {
            pressed[0] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_2) {
            pressed[1] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_3) {
            pressed[2] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_4) {
            pressed[3] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_Q) {
            pressed[4] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_W) {
            pressed[5] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_E) {
            pressed[6] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_R) {
            pressed[7] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            pressed[8] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            pressed[9] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            pressed[0xA] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_F) {
            pressed[0xB] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_Z) {
            pressed[0xC] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_X) {
            pressed[0xD] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_C) {
            pressed[0xE] = true;
        } else if (e.getKeyCode() == KeyEvent.VK_V) {
            pressed[0xF] = true;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_1) {
            pressed[0] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_2) {
            pressed[1] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_3) {
            pressed[2] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_4) {
            pressed[3] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_Q) {
            pressed[4] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_W) {
            pressed[5] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_E) {
            pressed[6] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_R) {
            pressed[7] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            pressed[8] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            pressed[9] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            pressed[0xA] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_F) {
            pressed[0xB] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_Z) {
            pressed[0xC] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_X) {
            pressed[0xD] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_C) {
            pressed[0xE] = false;
        } else if (e.getKeyCode() == KeyEvent.VK_V) {
            pressed[0xF] = false;
        }
    }

    public boolean[] getPressed() {
        return pressed;
    }
}