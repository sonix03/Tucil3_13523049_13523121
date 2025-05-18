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
    
        // Ukuran total grid termasuk jika K di luar
        int totalCols = Math.max(board.cols, board.exitCol + 1);
        int totalRows = Math.max(board.rows, board.exitRow + 1);
    
        for (int i = 0; i < totalRows; i++) {
            for (int j = 0; j < totalCols; j++) {
                // Ambil isi sel atau default '.'
                char c = (i < board.rows && j < board.cols) ? board.grid[i][j] : '.';
    
                // Jika ini titik exit 'K', override
                if (i == board.exitRow && j == board.exitCol) {
                    c = 'K';
                }
    
                Color fillColor;
    
                if (c == 'K') {
                    fillColor = Color.GREEN;
                }
 
                else if (board.exitCol >= board.cols && j == board.exitCol) {
                    fillColor = new Color(220, 220, 220);
                }
        
                else if (board.exitRow >= board.rows && i == board.exitRow) {
                    fillColor = new Color(220, 220, 220);
                }

                else if (board.exitRow < board.rows && board.exitCol < board.cols &&
                         i == board.exitRow && j != board.exitCol) {
                    fillColor = new Color(220, 220, 220);
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
