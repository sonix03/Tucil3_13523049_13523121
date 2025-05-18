import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Board {
    public int rows, cols;
    public char[][] grid;
    public List<Piece> pieces; // Daftar semua bidak, termasuk primaryPiece
    public Piece primaryPiece; // Bidak utama 'P'
    public int exitRow = -1, exitCol = -1;

    public Board(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
    
        String[] size = br.readLine().trim().split(" ");
        rows = Integer.parseInt(size[0]);
        cols = Integer.parseInt(size[1]);
    
        @SuppressWarnings("unused")
        int nPiece = Integer.parseInt(br.readLine().trim()) + 1;
    
        grid = new char[rows][cols];
        pieces = new ArrayList<>();
        Map<Character, List<int[]>> tempPieceCells = new HashMap<>();
    
        for (int i = 0; i < rows; i++) {
            String line = br.readLine();
            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);
        
                // Simpan ke grid hanya jika masih dalam batas ukuran
                if (j < cols) {
                    grid[i][j] = c;
                }
        
                // Catat posisi exit meskipun di luar grid
                if (c == 'K') {
                    exitRow = i;
                    exitCol = j;
                }
        
                // Simpan cell bidak jika dalam batas grid
                if (c != '.' && c != 'K' && j < cols) {
                    tempPieceCells.putIfAbsent(c, new ArrayList<>());
                    tempPieceCells.get(c).add(new int[]{i, j});
                }
            }
        
            // Isi sisa grid jika baris kurang panjang
            for (int j = line.length(); j < cols; j++) {
                grid[i][j] = '.';
            }
        }
    
        // Sama seperti sebelumnya: buat objek Piece dari cell yang terkumpul
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
    

    // Konstruktor privat untuk proses cloning
    private Board() {}

    // Metode untuk menentukan dan mengatur orientasi bidak
    private void determineAndSetOrientation(Piece piece) {
        if (piece.cells.isEmpty()) {
            piece.setOrientation(PieceOrientation.OTHER);
            return;
        }
        if (piece.cells.size() == 1) {
            piece.setOrientation(PieceOrientation.SINGLE_BLOCK);
            return;
        }

        boolean isStrictlyHorizontal = true;
        boolean isStrictlyVertical = true;

        int firstRow = piece.cells.get(0)[0];
        int firstCol = piece.cells.get(0)[1];

        Set<Integer> uniqueRows = new HashSet<>();
        Set<Integer> uniqueCols = new HashSet<>();
        for(int[] cell : piece.cells){
            uniqueRows.add(cell[0]);
            uniqueCols.add(cell[1]);
        }

        // Cek Horizontal: semua sel di baris yang sama, dan kolomnya berbeda
        if (uniqueRows.size() == 1 && uniqueCols.size() == piece.cells.size()) {
            piece.setOrientation(PieceOrientation.HORIZONTAL);
            return;
        }

        // Cek Vertikal: semua sel di kolom yang sama, dan barisnya berbeda
        if (uniqueCols.size() == 1 && uniqueRows.size() == piece.cells.size()) {
            piece.setOrientation(PieceOrientation.VERTICAL);
            return;
        }
        
        piece.setOrientation(PieceOrientation.OTHER); // Jika tidak memenuhi kriteria di atas
    }

    public void print() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(grid[i][j]);
            }
            System.out.println();
        }
        if (primaryPiece != null && primaryPiece.cells != null) {
             System.out.println("Primary piece (P) at: " + Arrays.deepToString(primaryPiece.cells.toArray()) + " Orientation: " + primaryPiece.getOrientation());
        } else {
            System.out.println("Primary piece (P) not found or has no cells.");
        }
        System.out.println("Exit at: (" + exitRow + ", " + exitCol + ")");
        // Optional: Print all pieces and their orientations for debugging
        // System.out.println("All pieces:");
        // for (Piece p : pieces) {
        //     System.out.println("- Piece " + p.name + ": " + p.getOrientation() + " at " + Arrays.deepToString(p.cells.toArray()));
        // }
    }

    @Override
    public Board clone() {
        Board copy = new Board(); // Menggunakan konstruktor privat
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