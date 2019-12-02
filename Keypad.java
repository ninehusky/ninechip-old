import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keypad implements KeyListener {

    private boolean[] pressed;
    /**
     * MAPPING:
     * 1 2 3 4
     * Q W E R
     * A S D F
     * Z X C V
     */
    
    public boolean[] getPressed() {
        return pressed;
    }

    /**
     * I understand this is very ugly. Possible fixes include using some
     * sort of HashMap to map KeyEvent (ints) to their booleans.
     */
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_1) {
            pressed[0] = true;
        } else if (code == KeyEvent.VK_2) {
            pressed[1] = true;
        } else if (code == KeyEvent.VK_3) {
            pressed[2] = true;
        } else if (code == KeyEvent.VK_4) {
            pressed[3] = true;
        } else if (code == KeyEvent.VK_Q) {
            pressed[4] = true;
        } else if (code == KeyEvent.VK_W) {
            pressed[5] = true;
        } else if (code == KeyEvent.VK_E) {
            pressed[6] = true;
        } else if (code == KeyEvent.VK_R) {
            pressed[7] = true;
        } else if (code == KeyEvent.VK_A) {
            pressed[8] = true;
        } else if (code == KeyEvent.VK_S) {
            pressed[9] = true;
        } else if (code == KeyEvent.VK_D) {
            pressed[10] = true;
        } else if (code == KeyEvent.VK_F) {
            pressed[11] = true;
        } else if (code == KeyEvent.VK_Z) {
            pressed[12] = true;
        } else if (code == KeyEvent.VK_X) {
            pressed[13] = true;
        } else if (code == KeyEvent.VK_C) {
            pressed[14] = true;
        } else if (code == KeyEvent.VK_V) {
            pressed[15] = true;
        }
    }

    public void keyTyped(KeyEvent e) {
        // do nothing
    }

    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_1) {
            pressed[0] = false;
        } else if (code == KeyEvent.VK_2) {
            pressed[1] = false;
        } else if (code == KeyEvent.VK_3) {
            pressed[2] = false;
        } else if (code == KeyEvent.VK_4) {
            pressed[3] = false;
        } else if (code == KeyEvent.VK_Q) {
            pressed[4] = false;
        } else if (code == KeyEvent.VK_W) {
            pressed[5] = false;
        } else if (code == KeyEvent.VK_E) {
            pressed[6] = false;
        } else if (code == KeyEvent.VK_R) {
            pressed[7] = false;
        } else if (code == KeyEvent.VK_A) {
            pressed[8] = false;
        } else if (code == KeyEvent.VK_S) {
            pressed[9] = false;
        } else if (code == KeyEvent.VK_D) {
            pressed[10] = false;
        } else if (code == KeyEvent.VK_F) {
            pressed[11] = false;
        } else if (code == KeyEvent.VK_Z) {
            pressed[12] = false;
        } else if (code == KeyEvent.VK_X) {
            pressed[13] = false;
        } else if (code == KeyEvent.VK_C) {
            pressed[14] = false;
        } else if (code == KeyEvent.VK_V) {
            pressed[15] = false;
        }
    }
}