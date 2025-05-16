// Sesuaikan dengan struktur package Anda jika ada

import java.util.*;

public class IDAStar implements Solver {
    private final char[] priorityDirs = {'U', 'D', 'L', 'R'};
    private int heuristicType;
    private int nodesExpanded;
    private int maxDepthReachedInIteration; // Untuk melacak kedalaman max di satu iterasi
    
    public IDAStar(int heuristicType) {
        this.heuristicType = heuristicType;
    }
    
    @Override
    public void solve(Board start) {
        nodesExpanded = 0;
        
        int bound = Heuristic.calculate(start, heuristicType); // Batas awal adalah h(start)
        Path solutionPath = null;
        
        System.out.println("IDA* dengan heuristik " + getHeuristicName(heuristicType));
        
        while (solutionPath == null) {
            System.out.println("Menjelajah dengan batas f-cost: " + bound);
            maxDepthReachedInIteration = 0; // Reset untuk iterasi baru
            SearchResult result = search(start, 0, bound, new ArrayList<>(), null, '\0', '\0');
            nodesExpanded += result.nodesInThisPath; // Akumulasi node yang dieksplorasi

            if (result.isGoal) {
                solutionPath = result.path;
                break;
            }
            if (result.nextBound == Integer.MAX_VALUE) {
                System.out.println("Tidak ada batas berikutnya, solusi tidak ditemukan.");
                break; // Tidak ada solusi
            }
            bound = result.nextBound; // Tingkatkan batas ke nilai f terkecil berikutnya yang melebihi batas lama
            if (bound == result.previousBound && result.nextBound > result.previousBound) { 
                // Jika terjebak atau nextBound tidak meningkat signifikan, mungkin perlu penanganan khusus
                // atau ini hanya berarti kita perlu iterasi lagi dengan bound yang sama (jika ada path dgn f=bound tapi bukan goal)
            }
        }
        
        if (solutionPath != null) {
            printSolution(solutionPath);
            System.out.println("Total Node yang dieksplorasi: " + nodesExpanded);
            // maxDepth di IDA* adalah panjang solusi karena kita mencari solusi optimal
            // System.out.println("Kedalaman solusi: " + solutionPath.g);
        } else {
            System.out.println("Tidak ditemukan solusi!");
            System.out.println("Total Node yang dieksplorasi (hingga pencarian terakhir): " + nodesExpanded);
        }
    }

    @Override
    public List<Board> solveAndReturnPath(Board start) {
    nodesExpanded = 0;

    int bound = Heuristic.calculate(start, heuristicType); // Batas awal f-cost (heuristik start)
    Path solutionPath = null;

    System.out.println("IDA* dengan heuristik " + getHeuristicName(heuristicType));

    while (solutionPath == null) {
        System.out.println("Menjelajah dengan batas f-cost: " + bound);
        maxDepthReachedInIteration = 0; // Reset batas iterasi baru

        SearchResult result = search(start, 0, bound, new ArrayList<>(), null, '\0', '\0');
        nodesExpanded += result.nodesInThisPath;

        if (result.isGoal) {
            solutionPath = result.path;
            break;
        }
        if (result.nextBound == Integer.MAX_VALUE) {
            System.out.println("Tidak ada batas berikutnya, solusi tidak ditemukan.");
            break; // Tidak ada solusi
        }

        bound = result.nextBound;

        if (bound == result.previousBound && result.nextBound > result.previousBound) {
            // Penanganan jika terjebak (opsional)
        }
    }

    if (solutionPath == null) {
        System.out.println("Tidak ditemukan solusi!");
        System.out.println("Total Node yang dieksplorasi (hingga pencarian terakhir): " + nodesExpanded);
        return new ArrayList<>();
    }

    // Kumpulkan langkah solusi dari Path ke List<Board>
    List<Board> pathBoards = new ArrayList<>();
    Path current = solutionPath;
    while (current != null) {
        pathBoards.add(current.board);
        current = current.parent;
    }
    Collections.reverse(pathBoards);

    System.out.println("Solusi ditemukan dalam " + (pathBoards.size() - 1) + " langkah.");
    System.out.println("Total Node yang dieksplorasi: " + nodesExpanded);

    return pathBoards;
}


    // ArrayList<String> pathStates untuk melacak state dalam path saat ini untuk menghindari siklus
    private SearchResult search(Board currentBoard, int gCost, int currentBound, List<String> currentPathStates, Path parentPathNode, char pieceMoved, char moveDir) {
        int hCost = Heuristic.calculate(currentBoard, heuristicType);
        int fCost = gCost + hCost;
        
        int currentSearchNodes = 1; // Hitung node ini

        if (fCost > currentBound) {
            return new SearchResult(false, null, fCost, currentBound, currentSearchNodes); // Kembalikan fCost sebagai batas berikutnya
        }
        
        if (isGoalState(currentBoard)) {
            Path goalPath = new Path(currentBoard, parentPathNode, pieceMoved, moveDir, gCost, hCost);
            return new SearchResult(true, goalPath, currentBound, currentBound, currentSearchNodes);
        }
        
        String boardKey = getBoardKey(currentBoard);
        if (currentPathStates.contains(boardKey)) { // Deteksi siklus sederhana dalam path saat ini
            return new SearchResult(false, null, Integer.MAX_VALUE, currentBound, currentSearchNodes);
        }
        currentPathStates.add(boardKey); // Tambahkan state saat ini ke path

        int minNextBound = Integer.MAX_VALUE;
        
        Path currentPathObject = new Path(currentBoard, parentPathNode, pieceMoved, moveDir, gCost, hCost);

        for (Piece piece : currentBoard.pieces) {
            for (char dir : priorityDirs) {
                if (canMove(currentBoard, piece.name, dir)) {
                    Board newBoard = move(currentBoard, piece.name, dir);
                    
                    SearchResult recursiveResult = search(newBoard, gCost + 1, currentBound, currentPathStates, currentPathObject, piece.name, dir);
                    currentSearchNodes += recursiveResult.nodesInThisPath;

                    if (recursiveResult.isGoal) {
                        // Penting: sertakan node dari path yang berhasil
                        return new SearchResult(true, recursiveResult.path, currentBound, currentBound, currentSearchNodes);
                    }
                    minNextBound = Math.min(minNextBound, recursiveResult.nextBound);
                }
            }
        }
        
        currentPathStates.remove(boardKey); // Hapus state saat backtrack
        return new SearchResult(false, null, minNextBound, currentBound, currentSearchNodes);
    }
    
    private boolean isGoalState(Board board) {
        if (board.primaryPiece == null || board.exitRow == -1) return false;
        for (int[] cell : board.primaryPiece.cells) {
            if ((cell[0] == board.exitRow && Math.abs(cell[1] - board.exitCol) == 1) ||
                (cell[1] == board.exitCol && Math.abs(cell[0] - board.exitRow) == 1)) {
                return true;
            }
        }
        return false;
    }
    
    private String getBoardKey(Board board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.rows; i++) {
            for (int j = 0; j < board.cols; j++) {
                sb.append(board.grid[i][j]);
            }
        }
        return sb.toString();
    }
    
    private void printSolution(Path path) {
        List<Path> steps = new ArrayList<>();
        Path current = path;
        while (current != null && current.piece != '\0') { // Jangan tambahkan start node (yang parentnya null)
            steps.add(current);
            current = current.parent;
        }
        Collections.reverse(steps);
        int step = 1;
        for (Path node : steps) {
            System.out.println("Gerakan " + step + ": " + node.piece + "-" + getDirName(node.direction) + 
            " (g=" + node.g + ", h=" + node.h + ", f=" + (node.g + node.h) + ")");
            node.board.print();
            System.out.println();
            step++;
        }
        System.out.println("Solusi ditemukan dalam " + steps.size() + " langkah.");
    }
    
    private String getDirName(char d) {
        return switch (d) {
            case 'L' -> "kiri";
            case 'R' -> "kanan";
            case 'U' -> "atas";
            case 'D' -> "bawah";
            default -> "?";
        };
    }

    private String getHeuristicName(int type) {
        return Heuristic.getName(type); // Menggunakan metode dari kelas Heuristic
    }
    
    // --- METODE CANMOVE YANG DIMODIFIKASI ---
    private boolean canMove(Board board, char pieceName, char dir) {
        Piece pieceToMove = null;
        for (Piece p : board.pieces) {
            if (p.name == pieceName) {
                pieceToMove = p;
                break;
            }
        }

        if (pieceToMove == null || pieceToMove.cells.isEmpty()) {
            return false;
        }
        
        PieceOrientation orientation = pieceToMove.getOrientation();

        if (orientation == PieceOrientation.HORIZONTAL) {
            if (dir == 'U' || dir == 'D') {
                return false;
            }
        } else if (orientation == PieceOrientation.VERTICAL) {
            if (dir == 'L' || dir == 'R') {
                return false;
            }
        }

        for (int[] cell : pieceToMove.cells) {
            int newRow = cell[0];
            int newCol = cell[1];
            switch (dir) {
                case 'L' -> newCol--;
                case 'R' -> newCol++;
                case 'U' -> newRow--;
                case 'D' -> newRow++;
            }
            if (newRow < 0 || newRow >= board.rows || newCol < 0 || newCol >= board.cols) {
                return false;
            }
            char destinationCellContent = board.grid[newRow][newCol];
            boolean partOfItself = false;
            for(int[] ownCell : pieceToMove.cells){
                if(ownCell[0] == newRow && ownCell[1] == newCol){
                    partOfItself = true;
                    break;
                }
            }
            if (destinationCellContent != '.' && destinationCellContent != 'K' && !partOfItself) {
                return false;
            }
        }
        return true;
    }
    // --- AKHIR METODE CANMOVE ---
    
    private Board move(Board board, char pieceName, char dir) {
        Board newBoard = board.clone();
        Piece targetPieceInNewBoard = null;
        for(Piece p : newBoard.pieces){
            if(p.name == pieceName){
                targetPieceInNewBoard = p;
                break;
            }
        }

        if (targetPieceInNewBoard == null) return newBoard;
        
        for (int[] cell : targetPieceInNewBoard.cells) {
            newBoard.grid[cell[0]][cell[1]] = '.';
        }
        for (int[] cell : targetPieceInNewBoard.cells) {
            switch (dir) {
                case 'L' -> cell[1]--;
                case 'R' -> cell[1]++;
                case 'U' -> cell[0]--;
                case 'D' -> cell[0]++;
            }
        }
        for (int[] cell : targetPieceInNewBoard.cells) {
            newBoard.grid[cell[0]][cell[1]] = pieceName;
        }
        if (pieceName == 'P') {
            newBoard.primaryPiece = targetPieceInNewBoard;
        }
        return newBoard;
    }
    
    private static class SearchResult {
        boolean isGoal;
        Path path;
        int nextBound; // Nilai f terkecil yang melebihi bound saat ini
        int previousBound; // Bound yang digunakan untuk pencarian ini
        int nodesInThisPath; // Node yang dieksplorasi dalam pencarian spesifik ini
        
        public SearchResult(boolean isGoal, Path path, int nextBound, int previousBound, int nodesInThisPath) {
            this.isGoal = isGoal;
            this.path = path;
            this.nextBound = nextBound;
            this.previousBound = previousBound;
            this.nodesInThisPath = nodesInThisPath;
        }
    }
    
    private static class Path {
        Board board;
        Path parent;
        char piece;
        char direction;
        int g;  // cost dari start node (jumlah langkah)
        int h;  // heuristic value
        
        public Path(Board board, Path parent, char piece, char direction, int g, int h) {
            this.board = board;
            this.parent = parent;
            this.piece = piece;
            this.direction = direction;
            this.g = g;
            this.h = h;
        }
    }
}