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
    
        int totalCols = board.cols;
        int totalRows = board.rows;
    
        int boardWidth = totalCols * cellSize;
        int boardHeight = totalRows * cellSize;
    
        int offsetX = (getWidth() - boardWidth) / 2;
        int offsetY = (getHeight() - boardHeight) / 2;
    
        for (int i = 0; i < totalRows; i++) {
            for (int j = 0; j < totalCols; j++) {
                char c = board.grid[i][j];
    
                Color fillColor = switch (c) {
                    case '.' -> Color.WHITE;
                    case 'P' -> Color.RED;
                    default -> Color.LIGHT_GRAY;
                };
    
                int x = offsetX + j * cellSize;
                int y = offsetY + i * cellSize;
    
                g.setColor(fillColor);
                g.fillRect(x, y, cellSize, cellSize);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, cellSize, cellSize);
    
                if (c != '.' && c != 'K') {
                    g.setColor(Color.BLACK);
                    g.drawString(String.valueOf(c), x + 35, y + 45);
                }
            }
        }

        if (board.exitCol <= 0 && board.exitRow >= 0 && board.exitRow < board.rows) {
            // K di kiri luar grid
            int x = offsetX - cellSize / 2;
            int y = offsetY + board.exitRow * cellSize;
            g.setColor(Color.GREEN);
            g.fillRect(x, y, cellSize / 2, cellSize);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, cellSize / 2, cellSize);
        } else if (board.exitCol >= board.cols && board.exitRow >= 0 && board.exitRow < board.rows) {
            // K di kanan luar grid
            int x = offsetX + board.cols * cellSize;
            int y = offsetY + board.exitRow * cellSize;
            g.setColor(Color.GREEN);
            g.fillRect(x, y, cellSize / 2, cellSize);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, cellSize / 2, cellSize);
        } else if (board.exitRow <= 0 && board.exitCol >= 0 && board.exitCol < board.cols) {
            // K di atas luar grid
            int x = offsetX + board.exitCol * cellSize;
            int y = offsetY - cellSize / 2;
            g.setColor(Color.GREEN);
            g.fillRect(x, y, cellSize, cellSize / 2);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, cellSize, cellSize / 2);
        } else if (board.exitRow >= board.rows && board.exitCol >= 0 && board.exitCol < board.cols) {
            // K di bawah luar grid
            int x = offsetX + board.exitCol * cellSize;
            int y = offsetY + board.rows * cellSize;
            g.setColor(Color.GREEN);
            g.fillRect(x, y, cellSize, cellSize / 2);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, cellSize, cellSize / 2);
        }
    }
}
