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
    public int exitRow, exitCol;

    public Board(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }

        if (lines.size() < 2) {
            throw new IllegalArgumentException("Format file tidak valid.");
        }

        // 1. Ukuran kolom dan baris
        String[] sizeParts = lines.get(0).trim().split(" ");
        if (sizeParts.length != 2) {
            throw new IllegalArgumentException("Format file tidak valid: baris pertama harus berisi '<rows> <cols>'.");
        }
        try {
            this.rows = Integer.parseInt(sizeParts[0]);
            this.cols = Integer.parseInt(sizeParts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Format file tidak valid: ukuran baris/kolom bukan angka.");
        }
        if (this.rows <= 0 || this.cols <= 0) {
            throw new IllegalArgumentException("Ukuran baris dan kolom harus positif.");
        }

        int declaredNPiece;
        try {
            declaredNPiece = Integer.parseInt(lines.get(1).trim()) + 1;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Format file tidak valid: jumlah bidak di baris kedua bukan angka.");
        }

        
        this.exitRow = -2; 
        this.exitCol = -2;
        List<int[]> kLocationsInFile = new ArrayList<>(); 

        
        // if (lines.size() > 2 && lines.get(2).length() >= this.cols && lines.get(2).contains("K") && lines.indexOf(lines.get(2)) == 2) { // Cek apakah ini baris untuk K di atas
        //     for(int j=0; j < lines.get(2).length(); j++){
        //         if(lines.get(2).charAt(j) == 'K') kLocationsInFile.add(new int[]{2,j});
        //     }
        // }
       
        int gridDataStartLineIndex = 2; 
        

        int actualGridContentStartLine = 2; 
        List<String> potentialGridLines = new ArrayList<>();
        if (lines.size() > actualGridContentStartLine) {
            
            boolean firstGridLineIsExit = lines.get(actualGridContentStartLine).trim().chars().allMatch(c -> c == 'K' || c == ' ');
            if (firstGridLineIsExit && lines.get(actualGridContentStartLine).contains("K")) {
                 
                for (int j = 0; j < lines.get(actualGridContentStartLine).length(); j++) {
                    if (lines.get(actualGridContentStartLine).charAt(j) == 'K') {
                        kLocationsInFile.add(new int[]{actualGridContentStartLine, j, 1});
                    }
                }
                actualGridContentStartLine++; 
            }
        }
        
        for (int i = 0; i < this.rows; i++) {
            if (lines.size() <= actualGridContentStartLine + i) break;
            String currentLine = lines.get(actualGridContentStartLine + i);
            // Kiri
            if (currentLine.length() > 0 && currentLine.charAt(0) == 'K') {
                 kLocationsInFile.add(new int[]{actualGridContentStartLine + i, 0, 2});
            }
            
            if (currentLine.length() > this.cols && currentLine.charAt(this.cols) == 'K') {
                kLocationsInFile.add(new int[]{actualGridContentStartLine + i, this.cols, 3}); 
            }
        }

        if (lines.size() > actualGridContentStartLine + this.rows) {
            String bottomLine = lines.get(actualGridContentStartLine + this.rows);
            if (bottomLine.contains("K")) {
                 for (int j = 0; j < bottomLine.length(); j++) {
                    if (bottomLine.charAt(j) == 'K') {
                        kLocationsInFile.add(new int[]{actualGridContentStartLine + this.rows, j, 4});
                    }
                }
            }
        }

        if (kLocationsInFile.size() != 1) {
            throw new IllegalArgumentException("Papan harus memiliki tepat satu pintu keluar (K). Ditemukan: " + kLocationsInFile.size());
        }

        int[] kPos = kLocationsInFile.get(0);
        int kType = kPos[2];
        int kFileLine = kPos[0];
        int kFileCol = kPos[1];

        
        int rowOffset = 0; 
        int colOffset = 0; 

        if (kType == 1) { 
            this.exitRow = -1;
            this.exitCol = kFileCol; 
            rowOffset = kFileLine + 1 - 2;
                                        
            if (kFileLine == 2) rowOffset = 1; 
                                              
        } else if (kType == 2) {
            this.exitRow = kFileLine - (2 + rowOffset); 
            this.exitCol = -1;
            if (kFileCol == 0) colOffset = 1; 
        } else if (kType == 3) { 
            this.exitRow = kFileLine - (2 + rowOffset);
            this.exitCol = this.cols;
        } else if (kType == 4) { 
            this.exitRow = this.rows;
            this.exitCol = kFileCol - colOffset;
        } else {
             throw new IllegalArgumentException("Posisi pintu keluar K tidak valid atau tidak di tepi.");
        }


        this.grid = new char[this.rows][this.cols];
        this.pieces = new ArrayList<>();
        Map<Character, List<int[]>> tempPieceCells = new HashMap<>();

        for (int i = 0; i < this.rows; i++) {
            int currentFileLineIndex = 2 + rowOffset + i;
            if (currentFileLineIndex >= lines.size()) {
                throw new IllegalArgumentException("Data grid tidak lengkap: baris tidak cukup untuk ukuran " + this.rows + "x" + this.cols);
            }
            String gridContentLine = lines.get(currentFileLineIndex);
            for (int j = 0; j < this.cols; j++) {
                int currentColInFileLine = colOffset + j;
                if (currentColInFileLine >= gridContentLine.length()) {
                     
                    this.grid[i][j] = '.'; 
                    
                    continue; 
                }
                char c = gridContentLine.charAt(currentColInFileLine);
                if (c == 'K') {
                    throw new IllegalArgumentException("Karakter 'K' tidak boleh berada di dalam area grid permainan.");
                }
                this.grid[i][j] = c;
                if (c != '.') {
                    tempPieceCells.putIfAbsent(c, new ArrayList<>());
                    tempPieceCells.get(c).add(new int[]{i, j});
                }
            }
        }
        
        // 3. Jumlah primary (P)
        if (!tempPieceCells.containsKey('P')) {
            throw new IllegalArgumentException("Bidak utama 'P' tidak ditemukan di grid.");
        }

        for (Map.Entry<Character, List<int[]>> entry : tempPieceCells.entrySet()) {
            char pieceName = entry.getKey();
            List<int[]> cellLocations = entry.getValue();
            Piece currentPieceObject = new Piece(pieceName);

            for (int[] cell : cellLocations) {
                currentPieceObject.addCell(cell[0], cell[1]);
            }
            determineAndSetOrientation(currentPieceObject);

            // 4. Validasi Bentuk mobil
            if (currentPieceObject.getOrientation() == PieceOrientation.SINGLE_BLOCK) {
                throw new IllegalArgumentException("Bidak '" + pieceName + "' tidak valid: tidak boleh berukuran 1x1.");
            }
            if (currentPieceObject.getOrientation() == PieceOrientation.OTHER) {
                throw new IllegalArgumentException("Bidak '" + pieceName + "' tidak valid: harus berbentuk garis lurus (Nx1 atau 1xN). Ditemukan: " + currentPieceObject.getOrientation());
            }

            this.pieces.add(currentPieceObject);
            if (pieceName == 'P') {
                if (this.primaryPiece != null) {
                    // Seharusnya tidak terjadi jika P dikelompokkan oleh tempPieceCells
                    throw new IllegalArgumentException("Lebih dari satu definisi bidak utama 'P' ditemukan.");
                }
                this.primaryPiece = currentPieceObject;
            }
        }

        // 5. Validasi Jumlah pieces
        if (this.pieces.size() != declaredNPiece) {
            throw new IllegalArgumentException("Jumlah bidak yang dideklarasikan (" + declaredNPiece + 
                                               ") tidak sesuai dengan jumlah bidak unik yang ditemukan di grid (" + this.pieces.size() + ").");
        }

        // 6. Validasi Primary harus sejajar dengan pintu keluar
        if (this.primaryPiece == null) { 
             throw new IllegalArgumentException("Bidak utama 'P' diperlukan untuk validasi keselarasan dengan pintu keluar.");
        }
        
        if (this.exitRow == -2 || this.exitCol == -2) {
             throw new IllegalArgumentException("Posisi Pintu Keluar 'K' tidak berhasil ditentukan secara valid untuk pemeriksaan keselarasan.");
        }


        PieceOrientation pOrientation = this.primaryPiece.getOrientation();
        boolean aligned = false;
        if (pOrientation == PieceOrientation.HORIZONTAL) {
            int pRow = this.primaryPiece.cells.get(0)[0]; 
            if (this.exitRow == pRow) {
                
                aligned = true;
            }
        } else if (pOrientation == PieceOrientation.VERTICAL) {
            int pCol = this.primaryPiece.cells.get(0)[1]; 
            if (this.exitCol == pCol) {
                
                aligned = true;
            }
        }

        if (!aligned) {
            String pPosInfo = (pOrientation == PieceOrientation.HORIZONTAL) ? 
                                "baris " + this.primaryPiece.cells.get(0)[0] : 
                                "kolom " + this.primaryPiece.cells.get(0)[1];
            String kPosInfo = String.format("baris=%d, kolom=%d", this.exitRow, this.exitCol);
            String expectedKPos = (pOrientation == PieceOrientation.HORIZONTAL) ?
                                  "pada baris " + this.primaryPiece.cells.get(0)[0] :
                                  "pada kolom " + this.primaryPiece.cells.get(0)[1];

            throw new IllegalArgumentException(
                String.format("Bidak utama 'P' (orientasi: %s, posisi: %s) tidak sejajar dengan pintu keluar 'K' (posisi: %s). 'K' seharusnya berada %s.",
                    pOrientation, pPosInfo, kPosInfo, expectedKPos
                ));
        }
    }
    
    // Konstruktor private untuk clone
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
        int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;

        for (int[] cell : piece.cells) {
            uniqueRows.add(cell[0]);
            uniqueCols.add(cell[1]);
            minRow = Math.min(minRow, cell[0]);
            maxRow = Math.max(maxRow, cell[0]);
            minCol = Math.min(minCol, cell[1]);
            maxCol = Math.max(maxCol, cell[1]);
        }

        if (uniqueRows.size() == 1) {
            if ((maxCol - minCol + 1) == piece.cells.size() && uniqueCols.size() == piece.cells.size()) {
                 
                piece.setOrientation(PieceOrientation.HORIZONTAL);
            } else {
                piece.setOrientation(PieceOrientation.OTHER); 
            }
        } else if (uniqueCols.size() == 1) { 
            if ((maxRow - minRow + 1) == piece.cells.size() && uniqueRows.size() == piece.cells.size()) {
                
                piece.setOrientation(PieceOrientation.VERTICAL);
            } else {
                piece.setOrientation(PieceOrientation.OTHER); 
            }
        } else {
            piece.setOrientation(PieceOrientation.OTHER); 
        }
    }

    public void print() {
        
        if (this.exitRow == -1) {
            for (int j = 0; j < this.cols; j++) {
                System.out.print(j == this.exitCol ? 'K' : ' ');
            }
            System.out.println();
        }

        for (int i = 0; i < rows; i++) {
            
            if (this.exitCol == -1 && this.exitRow == i) {
                System.out.print('K');
            } else if (this.exitCol == -1) {
                 System.out.print(' '); 
            }

            for (int j = 0; j < cols; j++) {
                System.out.print(grid[i][j]);
            }

            
            if (this.exitCol == this.cols && this.exitRow == i) {
                System.out.print('K');
            }
            System.out.println();
        }
       
        if (this.exitRow == this.rows) {
             for (int j = 0; j < this.cols; j++) {
                System.out.print(j == this.exitCol ? 'K' : ' ');
            }
            System.out.println();
        }

        if (primaryPiece != null && primaryPiece.cells != null && !primaryPiece.cells.isEmpty()) {
            System.out.println("Primary piece (P) at: " + Arrays.deepToString(primaryPiece.cells.toArray()) +
                               " Orientation: " + primaryPiece.getOrientation());
        } else {
            System.out.println("Primary piece (P) not found or has no cells.");
        }
        System.out.println("Exit at: (row=" + exitRow + ", col=" + exitCol + ") relative to grid (0-indexed, -1 or size means border)");
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
        
        if (this.primaryPiece != null && copy.primaryPiece == null) {
             for(Piece p : copy.pieces) {
                 if (p.name == this.primaryPiece.name) { // Seharusnya 'P'
                     copy.primaryPiece = p;
                     break;
                 }
             }
        }

        copy.exitRow = this.exitRow;
        copy.exitCol = this.exitCol;
        return copy;
    }
}