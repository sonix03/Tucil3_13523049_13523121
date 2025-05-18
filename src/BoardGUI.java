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
    
        int totalCols = Math.max(board.cols, board.exitCol + 1);
        int totalRows = Math.max(board.rows, board.exitRow + 1);
    
        for (int i = 0; i < totalRows; i++) {
            for (int j = 0; j < totalCols; j++) {
                // Default isi '.'
                char c = (i < board.rows && j < board.cols) ? board.grid[i][j] : '.';
    
                // Override jika sel ini adalah titik keluar
                boolean isExit = (i == board.exitRow && j == board.exitCol);
                if (isExit) c = 'K';
    
                Color fillColor;
    
                if (c == 'K') {
                    fillColor = Color.GREEN;
                }
                // Jika K berada di luar kolom kiri
                else if (board.exitCol < 0 && j == 0) {
                    fillColor = Color.LIGHT_GRAY;
                }
                // Jika K berada di luar kolom kanan
                else if (board.exitCol >= board.cols && j == board.exitCol) {
                    fillColor = Color.LIGHT_GRAY;
                }
                // Jika K berada di luar baris atas
                else if (board.exitRow < 0 && i == 0) {
                    fillColor = Color.LIGHT_GRAY;
                }
                // Jika K berada di luar baris bawah
                else if (board.exitRow >= board.rows && i == board.exitRow) {
                    fillColor = Color.LIGHT_GRAY;
                }
                else {
                    fillColor = switch (c) {
                        case '.' -> Color.WHITE;
                        case 'P' -> Color.RED;
                        default -> Color.LIGHT_GRAY;
                    };
                }
    
                g.setColor(fillColor);
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
