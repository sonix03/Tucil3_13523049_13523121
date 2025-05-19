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
    public int exitRow, exitCol; // Akan diinisialisasi setelah validasi K

    public Board(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }

        if (lines.size() < 2) {
            throw new IllegalArgumentException("Format file tidak valid: terlalu sedikit baris (minimal 2 baris untuk ukuran dan jumlah bidak).");
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
            throw new IllegalArgumentException("Format file tidak valid: jumlah bidak (baris kedua) bukan angka.");
        }

        // Validasi jumlah dan posisi Pintu Keluar (K) - Poin 2
        // Inisialisasi exitRow/Col dengan nilai yang menandakan belum ditemukan
        this.exitRow = -2; // -2 menandakan belum diproses/ditemukan secara valid
        this.exitCol = -2;
        List<int[]> kLocationsInFile = new ArrayList<>(); // Menyimpan {barisDiFile, kolomDiFile}

        // Pindai semua baris yang relevan untuk 'K'
        // Baris sebelum grid utama (jika ada)
        if (lines.size() > 2 && lines.get(2).length() >= this.cols && lines.get(2).contains("K") && lines.indexOf(lines.get(2)) == 2) { // Cek apakah ini baris untuk K di atas
            for(int j=0; j < lines.get(2).length(); j++){
                if(lines.get(2).charAt(j) == 'K') kLocationsInFile.add(new int[]{2,j});
            }
        }
        // Baris grid utama dan sekitarnya
        // Indeks baris file tempat grid dimulai, setelah ukuran dan nPiece
        int gridDataStartLineIndex = 2; 
        // Perlu logika offset yang lebih hati-hati jika K menentukan offset grid
        // Untuk validasi ini, kita asumsikan K harus di *tepi* grid yang didefinisikan oleh rows/cols
        // Atau pada baris/kolom khusus di luar itu.

        int actualGridContentStartLine = 2; // Baris file tempat data grid dimulai
        List<String> potentialGridLines = new ArrayList<>();
        if (lines.size() > actualGridContentStartLine) {
            // Cek apakah baris pertama setelah header adalah baris 'K' di atas
            boolean firstGridLineIsExit = lines.get(actualGridContentStartLine).trim().chars().allMatch(c -> c == 'K' || c == ' ');
            if (firstGridLineIsExit && lines.get(actualGridContentStartLine).contains("K")) {
                 // Hitung K di baris ini
                for (int j = 0; j < lines.get(actualGridContentStartLine).length(); j++) {
                    if (lines.get(actualGridContentStartLine).charAt(j) == 'K') {
                        kLocationsInFile.add(new int[]{actualGridContentStartLine, j, 1}); // type 1: top exit
                    }
                }
                actualGridContentStartLine++; // Grid sebenarnya dimulai setelah baris K ini
            }
        }
        
        // Cek K di sisi kiri, kanan, dan bawah relatif terhadap grid yang didefinisikan
        // Cek sisi kiri dan kanan grid
        for (int i = 0; i < this.rows; i++) {
            if (lines.size() <= actualGridContentStartLine + i) break;
            String currentLine = lines.get(actualGridContentStartLine + i);
            // Kiri
            if (currentLine.length() > 0 && currentLine.charAt(0) == 'K') {
                 kLocationsInFile.add(new int[]{actualGridContentStartLine + i, 0, 2}); // type 2: left exit
            }
            // Kanan (setelah kolom terakhir grid)
            if (currentLine.length() > this.cols && currentLine.charAt(this.cols) == 'K') {
                kLocationsInFile.add(new int[]{actualGridContentStartLine + i, this.cols, 3}); // type 3: right exit
            }
        }

        // Cek sisi bawah grid
        if (lines.size() > actualGridContentStartLine + this.rows) {
            String bottomLine = lines.get(actualGridContentStartLine + this.rows);
            if (bottomLine.contains("K")) {
                 for (int j = 0; j < bottomLine.length(); j++) {
                    if (bottomLine.charAt(j) == 'K') {
                        kLocationsInFile.add(new int[]{actualGridContentStartLine + this.rows, j, 4}); // type 4: bottom exit
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

        // Tentukan offset berdasarkan posisi K jika K berada di baris/kolom "nol" file
        // Ini adalah simplifikasi dari logika offset asli. Kita anggap file input sekarang lebih ketat.
        int rowOffset = 0; // Offset baris pada file untuk membaca grid
        int colOffset = 0; // Offset kolom pada file untuk membaca grid

        if (kType == 1) { // Top exit, K di baris file 'kFileLine', grid di bawahnya
            this.exitRow = -1;
            this.exitCol = kFileCol; // Kolom relatif terhadap awal baris K
            rowOffset = kFileLine + 1 - 2; // kFileLine adalah indeks di `lines`. `lines.get(0)` adalah size, `lines.get(1)` adalah nPiece.
                                        // Jadi jika K di `lines.get(2)`, grid mulai di `lines.get(3)`. rowOffset = 1 (untuk file)
                                        // Jika kFileLine = 2, maka grid_file_start = 3. 
                                        // actualGridContentStartLine adalah indeks untuk grid.
            if (kFileLine == 2) rowOffset = 1; // Baris K adalah baris ke-3 file, maka grid mulai baris ke-4
                                              // berarti data grid dibaca dari `lines.get(2 + rowOffset)`
        } else if (kType == 2) { // Left exit
            this.exitRow = kFileLine - (2 + rowOffset); // Baris relatif thd grid
            this.exitCol = -1;
            if (kFileCol == 0) colOffset = 1; // Kolom K adalah kolom ke-1 file
        } else if (kType == 3) { // Right exit
            this.exitRow = kFileLine - (2 + rowOffset);
            this.exitCol = this.cols;
        } else if (kType == 4) { // Bottom exit
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
                     // Jika baris lebih pendek, anggap sel kosong, tapi bisa juga error
                    this.grid[i][j] = '.'; // Atau throw error jika baris harus penuh
                    // throw new IllegalArgumentException("Data grid tidak lengkap: baris " + i + " terlalu pendek.");
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

            // 5. Validasi Bentuk mobil
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

        // 4. Validasi Jumlah pieces
        if (this.pieces.size() != declaredNPiece) {
            throw new IllegalArgumentException("Jumlah bidak yang dideklarasikan (" + declaredNPiece + 
                                               ") tidak sesuai dengan jumlah bidak unik yang ditemukan di grid (" + this.pieces.size() + ").");
        }

        // 6. Validasi Primary harus sejajar dengan pintu keluar
        if (this.primaryPiece == null) { // Seharusnya sudah dicek di atas, tapi untuk keamanan
             throw new IllegalArgumentException("Bidak utama 'P' diperlukan untuk validasi keselarasan dengan pintu keluar.");
        }
        // Pengecekan exitRow/Col pastikan valid (bukan -2)
        if (this.exitRow == -2 || this.exitCol == -2) {
             throw new IllegalArgumentException("Posisi Pintu Keluar 'K' tidak berhasil ditentukan secara valid untuk pemeriksaan keselarasan.");
        }


        PieceOrientation pOrientation = this.primaryPiece.getOrientation();
        boolean aligned = false;
        if (pOrientation == PieceOrientation.HORIZONTAL) {
            int pRow = this.primaryPiece.cells.get(0)[0]; // Semua sel P horizontal berada di baris ini
            if (this.exitRow == pRow) {
                // K berada di baris yang sama, P bisa geser ke kiri/kanan untuk keluar
                // (exitCol == -1 (kiri), exitCol == this.cols (kanan), atau exitCol adalah kolom K jika K di dalam baris)
                aligned = true;
            }
        } else if (pOrientation == PieceOrientation.VERTICAL) {
            int pCol = this.primaryPiece.cells.get(0)[1]; // Semua sel P vertikal berada di kolom ini
            if (this.exitCol == pCol) {
                // K berada di kolom yang sama, P bisa geser ke atas/bawah untuk keluar
                // (exitRow == -1 (atas), exitRow == this.rows (bawah), atau exitRow adalah baris K jika K di dalam kolom)
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
            piece.setOrientation(PieceOrientation.OTHER); // Atau error, bidak tanpa sel tidak valid
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

        if (uniqueRows.size() == 1) { // Potensial Horizontal
            if ((maxCol - minCol + 1) == piece.cells.size() && uniqueCols.size() == piece.cells.size()) {
                 // Semua sel berurutan secara horizontal tanpa celah
                piece.setOrientation(PieceOrientation.HORIZONTAL);
            } else {
                piece.setOrientation(PieceOrientation.OTHER); // Mungkin terputus atau tidak linear
            }
        } else if (uniqueCols.size() == 1) { // Potensial Vertikal
            if ((maxRow - minRow + 1) == piece.cells.size() && uniqueRows.size() == piece.cells.size()) {
                // Semua sel berurutan secara vertikal tanpa celah
                piece.setOrientation(PieceOrientation.VERTICAL);
            } else {
                piece.setOrientation(PieceOrientation.OTHER); // Mungkin terputus atau tidak linear
            }
        } else {
            piece.setOrientation(PieceOrientation.OTHER); // Bukan garis lurus (misal L-shape)
        }
    }

    public void print() {
        // Cetak baris atas jika ada K di sana (exitRow == -1)
        if (this.exitRow == -1) {
            for (int j = 0; j < this.cols; j++) {
                System.out.print(j == this.exitCol ? 'K' : ' ');
            }
            System.out.println();
        }

        for (int i = 0; i < rows; i++) {
            // Cetak kolom kiri jika ada K di sana (exitCol == -1)
            if (this.exitCol == -1 && this.exitRow == i) {
                System.out.print('K');
            } else if (this.exitCol == -1) {
                 System.out.print(' '); // Spasi untuk alignment jika exit kiri di baris lain
            }

            for (int j = 0; j < cols; j++) {
                System.out.print(grid[i][j]);
            }

            // Cetak kolom kanan jika ada K di sana (exitCol == cols)
            if (this.exitCol == this.cols && this.exitRow == i) {
                System.out.print('K');
            }
            System.out.println();
        }
        // Cetak baris bawah jika ada K di sana (exitRow == rows)
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
        // Pastikan primaryPiece di copy ter-set jika ada, meskipun loop di atas sudah melakukannya
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