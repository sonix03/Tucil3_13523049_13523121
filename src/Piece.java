import java.util.ArrayList;
import java.util.List;

public class Piece {
    public char name;
    public List<int[]> cells;
    public PieceOrientation orientation; // Field untuk menyimpan orientasi

    public Piece(char name) {
        this.name = name;
        this.cells = new ArrayList<>();
        // Inisialisasi default, akan di-set kemudian di Board.java
        this.orientation = PieceOrientation.OTHER;
    }

    public void addCell(int row, int col) {
        cells.add(new int[]{row, col});
    }

    // Setter untuk orientasi
    public void setOrientation(PieceOrientation orientation) {
        this.orientation = orientation;
    }

    // Getter untuk orientasi
    public PieceOrientation getOrientation() {
        return this.orientation;
    }

    @Override
    public Piece clone() {
        Piece copy = new Piece(this.name);
        for (int[] cell : this.cells) {
            copy.addCell(cell[0], cell[1]);
        }
        copy.setOrientation(this.orientation); // Salin orientasi saat cloning
        return copy;
    }
}