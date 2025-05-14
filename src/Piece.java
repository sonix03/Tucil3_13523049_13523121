import java.util.*;

public class Piece {
    public char name;
    public List<int[]> cells;

    public Piece(char name) {
        this.name = name;
        this.cells = new ArrayList<>();
    }

    public void addCell(int row, int col) {
        cells.add(new int[]{row, col});
    }

    public Piece clone() {
        Piece copy = new Piece(this.name);
        for (int[] cell : this.cells) {
            copy.addCell(cell[0], cell[1]);
        }
        return copy;
    }
}