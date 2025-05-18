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
        rows = Integer.parseInt(size[0]);
        cols = Integer.parseInt(size[1]);

        @SuppressWarnings("unused")
        int nPiece = Integer.parseInt(br.readLine().trim());
    
        grid = new char[rows][cols];
        pieces = new ArrayList<>();
        Map<Character, List<int[]>> tempPieceCells = new HashMap<>();

        int currentRow = 0;
        String line;

        while ((line = br.readLine()) != null) {
            // Jika masih dalam grid, isi grid dan deteksi bidak
            if (currentRow < rows) {
                for (int j = 0; j < line.length(); j++) {
                    char c = line.charAt(j);

                    // Isi grid jika dalam batas
                    if (j < cols) {
                        grid[currentRow][j] = c;
                    }

                    // Catat posisi exit meskipun di luar grid
                    if (c == 'K') {
                        exitRow = currentRow;
                        exitCol = j;
                    }

                    // Catat bidak jika dalam grid
                    if (c != '.' && c != 'K' && j < cols) {
                        tempPieceCells.putIfAbsent(c, new ArrayList<>());
                        tempPieceCells.get(c).add(new int[]{currentRow, j});
                    }
                }

                // Jika baris lebih pendek dari grid, isi titik
                for (int j = line.length(); j < cols; j++) {
                    grid[currentRow][j] = '.';
                }
            } else {
                // Di luar area grid, tetap cari K
                for (int j = 0; j < line.length(); j++) {
                    if (line.charAt(j) == 'K') {
                        exitRow = currentRow;
                        exitCol = j;
                    }
                }
            }

            currentRow++;
        }

        // Proses bidak
        for (Map.Entry<Character, List<int[]>> entry : tempPieceCells.entrySet()) {
            char pieceName = entry.getKey();
            List<int[]> cellLocations = entry.getValue();

            Piece currentPieceObject;
            if (pieceName == 'P') {
                if (this.primaryPiece == null) {
                    this.primaryPiece = new Piece(pieceName);
                }
                currentPieceObject = this.primaryPiece;
            } else {
                currentPieceObject = new Piece(pieceName);
            }

            currentPieceObject.cells.clear();
            for (int[] cell : cellLocations) {
                currentPieceObject.addCell(cell[0], cell[1]);
            }

            determineAndSetOrientation(currentPieceObject);

            boolean pieceExists = false;
            for (Piece p : pieces) {
                if (p.name == pieceName) {
                    pieceExists = true;
                    p.cells = currentPieceObject.cells;
                    p.orientation = currentPieceObject.orientation;
                    if (pieceName == 'P') this.primaryPiece = p;
                    break;
                }
            }
            if (!pieceExists) {
                pieces.add(currentPieceObject);
            }
        }

        // Cek ulang primary piece
        if (this.primaryPiece == null && tempPieceCells.containsKey('P')) {
            for (Piece p : pieces) {
                if (p.name == 'P') {
                    this.primaryPiece = p;
                    break;
                }
            }
        }

        if (this.primaryPiece == null) {
            System.err.println("Peringatan: Bidak utama 'P' tidak ditemukan dalam file input.");
        }
        if (this.exitRow == -1 || this.exitCol == -1) {
            System.err.println("Peringatan: Titik keluar (K) tidak ditemukan dalam file input.");
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
