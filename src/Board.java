import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Board {
    public int rows, cols;
    public char[][] grid;
    public List<Piece> pieces;
    public Piece primaryPiece;
    public int exitRow = -1, exitCol = -1;

    public Board(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));

        String[] size = br.readLine().trim().split(" ");
        int declaredRows = Integer.parseInt(size[0]);
        int declaredCols = Integer.parseInt(size[1]);

        int nPiece = Integer.parseInt(br.readLine().trim());

        // Untuk menyimpan semua baris input
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }

        // Deteksi apakah baris pertama hanya K (exit atas)
        boolean hasTopExit = lines.get(0).contains("K");
        boolean hasLeftExit = lines.stream().anyMatch(l -> l.length() > 0 && l.charAt(0) == 'K');

        // Hitung offset baris dan kolom
        int rowOffset = hasTopExit ? 1 : 0;
        int colOffset = hasLeftExit ? 1 : 0;

        rows = declaredRows;
        cols = declaredCols;
        grid = new char[rows][cols];
        pieces = new ArrayList<>();
        Map<Character, List<int[]>> tempPieceCells = new HashMap<>();

        for (int i = 0; i < rows; i++) {
            String content = lines.get(i + rowOffset);
            for (int j = 0; j < cols; j++) {
                int actualCol = j + colOffset;
                char c = (actualCol < content.length()) ? content.charAt(actualCol) : '.';
                grid[i][j] = c;

                if (c != '.' && c != 'K') {
                    tempPieceCells.putIfAbsent(c, new ArrayList<>());
                    tempPieceCells.get(c).add(new int[]{i, j});
                }
            }
        }

        // Cek posisi K
        if (hasTopExit) {
            exitRow = -1;
            exitCol = lines.get(0).indexOf('K') - colOffset;
        } else if (hasLeftExit) {
            for (int i = 0; i < declaredRows; i++) {
                if (lines.get(i + rowOffset).charAt(0) == 'K') {
                    exitRow = i;
                    exitCol = -1;
                    break;
                }
            }
        } else {
            // Cek jika K ada di luar bawah atau kanan
            for (int i = rowOffset + rows; i < lines.size(); i++) {
                int kIndex = lines.get(i).indexOf('K');
                if (kIndex != -1) {
                    exitRow = i - rowOffset;
                    exitCol = kIndex - colOffset;
                    break;
                }
            }
            for (int i = 0; i < rows; i++) {
                String content = lines.get(i + rowOffset);
                int kPos = cols + colOffset;
                if (content.length() > kPos && content.charAt(kPos) == 'K') {
                    exitRow = i;
                    exitCol = cols; // berada tepat di kanan grid
                    break;
                }
            }
        }

        // Proses piece
        for (Map.Entry<Character, List<int[]>> entry : tempPieceCells.entrySet()) {
            char pieceName = entry.getKey();
            List<int[]> cellLocations = entry.getValue();

            Piece currentPieceObject = pieceName == 'P' && this.primaryPiece == null
                ? (this.primaryPiece = new Piece(pieceName))
                : new Piece(pieceName);

            currentPieceObject.cells.clear();
            for (int[] cell : cellLocations) {
                currentPieceObject.addCell(cell[0], cell[1]);
            }

            determineAndSetOrientation(currentPieceObject);

            boolean found = false;
            for (Piece p : pieces) {
                if (p.name == pieceName) {
                    found = true;
                    p.cells = currentPieceObject.cells;
                    p.orientation = currentPieceObject.orientation;
                    break;
                }
            }
            if (!found) {
                pieces.add(currentPieceObject);
            }
        }

        if (this.primaryPiece == null) {
            System.err.println("Peringatan: Bidak utama 'P' tidak ditemukan.");
        }
        if (this.exitRow == -1 && this.exitCol == -1) {
            System.err.println("Peringatan: Titik keluar (K) tidak ditemukan.");
        }

        br.close();
    }
    

    private Board() {}

    private void determineAndSetOrientation(Piece piece) {
        if (piece.cells.isEmpty()) {
            piece.setOrientation(PieceOrientation.OTHER);
            return;
        }
        if (piece.cells.size() == 1) {
            piece.setOrientation(PieceOrientation.SINGLE_BLOCK);
            return;
        }

        Set<Integer> uniqueRows = new HashSet<>();
        Set<Integer> uniqueCols = new HashSet<>();
        for (int[] cell : piece.cells) {
            uniqueRows.add(cell[0]);
            uniqueCols.add(cell[1]);
        }

        if (uniqueRows.size() == 1 && uniqueCols.size() == piece.cells.size()) {
            piece.setOrientation(PieceOrientation.HORIZONTAL);
        } else if (uniqueCols.size() == 1 && uniqueRows.size() == piece.cells.size()) {
            piece.setOrientation(PieceOrientation.VERTICAL);
        } else {
            piece.setOrientation(PieceOrientation.OTHER);
        }
    }

    public void print() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(grid[i][j]);
            }
            System.out.println();
        }
        if (primaryPiece != null && primaryPiece.cells != null) {
            System.out.println("Primary piece (P) at: " + Arrays.deepToString(primaryPiece.cells.toArray()) +
                               " Orientation: " + primaryPiece.getOrientation());
        } else {
            System.out.println("Primary piece (P) not found or has no cells.");
        }
        System.out.println("Exit at: (" + exitRow + ", " + exitCol + ")");
    }

    @Override
    public Board clone() {
        Board copy = new Board();
        copy.rows = this.rows;
        copy.cols = this.cols;
        copy.grid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            copy.grid[i] = Arrays.copyOf(this.grid[i], cols);
        }

        copy.pieces = new ArrayList<>();
        for (Piece p : this.pieces) {
            Piece clonedPiece = p.clone();
            copy.pieces.add(clonedPiece);
            if (clonedPiece.name == 'P') {
                copy.primaryPiece = clonedPiece;
            }
        }
        copy.exitRow = this.exitRow;
        copy.exitCol = this.exitCol;
        return copy;
    }
}