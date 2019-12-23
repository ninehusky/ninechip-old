import javax.swing.*;
import java.awt.*;

public class DisplayPanel extends JPanel {
    private char[][] graphics;

    public DisplayPanel() {
        graphics = new char[DisplayFrame.WIDTH][DisplayFrame.HEIGHT];
    }

    public void updateGraphics(char[][] graphics) {
        this.graphics = graphics;
    }

    public void paint(Graphics g) {
        char[][] graphics = this.graphics;
        for (int i = 0; i < graphics.length; i++) {
            for (int j = 0; j < graphics[i].length; j++) {
                if (graphics[i][j] == 0) {
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(i * DisplayFrame.PIXEL_SIZE, j * DisplayFrame.PIXEL_SIZE, DisplayFrame.PIXEL_SIZE,
                           DisplayFrame.PIXEL_SIZE);
            }
        }
    }
}