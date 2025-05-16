import java.awt.*;
import javax.swing.*;

public class BoardGUI extends JPanel {
    private Board board;

    public void setBoard(Board board) {
        this.board = board;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (board == null) return;

        int cellSize = 80;
        for (int i = 0; i < board.rows; i++) {
            for (int j = 0; j < board.cols; j++) {
                char c = board.grid[i][j];
                g.setColor(switch (c) {
                    case '.' -> Color.WHITE;
                    case 'K' -> Color.GREEN;
                    case 'P' -> Color.RED;
                    default -> Color.LIGHT_GRAY;
                });
                g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                g.setColor(Color.BLACK);
                g.drawRect(j * cellSize, i * cellSize, cellSize, cellSize);

                if (c != '.' && c != 'K') {
                    g.setColor(Color.BLACK);
                    g.drawString(String.valueOf(c), j * cellSize + 35, i * cellSize + 45);
                }
            }
        }
    }
}
