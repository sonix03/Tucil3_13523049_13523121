import java.util.*;

/**
 * Implementasi algoritma IDA* (Iterative Deepening A*)
 * Algoritma ini menggunakan iterative deepening dengan batas cost yang meningkat
 * untuk menemukan jalur optimal dengan penggunaan memori yang lebih efisien.
 */
public class IDAStar implements Solver {
    private final char[] priorityDirs = {'U', 'D', 'L', 'R'};
    private int heuristicType;  // 0: Manhattan, 1: Euclidean, 2: Obstacle-aware
    private int nodesExpanded;
    private int maxDepth;
    
    public IDAStar(int heuristicType) {
        this.heuristicType = heuristicType;
    }
    
    @Override
    public void solve(Board start) {
        nodesExpanded = 0;
        
        // Initial bound is the heuristic value of the start state
        int bound = calculateHeuristic(start);
        Path result = null;
        
        System.out.println("IDA* dengan heuristik " + getHeuristicName(heuristicType));
        System.out.println("Batas awal: " + bound);
        
        while (result == null) {
            // System.out.println("Menjelajah dengan batas: " + bound);
            SearchResult searchResult = search(start, 0, bound, new HashSet<>(), null, '\0', '\0');
            
            if (searchResult.isGoal) {
                result = searchResult.path;
                break;
            }
            
            if (searchResult.nextBound == Integer.MAX_VALUE) {
                // No solution found
                break;
            }
            
            bound = searchResult.nextBound;
            System.out.println("Batas ditingkatkan ke: " + bound);
        }
        
        if (result != null) {
            printSolution(result);
            System.out.println("Node yang dieksplorasi: " + nodesExpanded);
            System.out.println("Kedalaman maksimal: " + maxDepth);
        } else {
            System.out.println("Tidak ditemukan solusi!");
        }
    }
    
    private SearchResult search(Board board, int g, int bound, Set<String> visited, 
                               Path parent, char piece, char dir) {
        nodesExpanded++;
        maxDepth = Math.max(maxDepth, g);
        
        int h = calculateHeuristic(board);
        int f = g + h;
        
        // Jika f melebihi batas, return batas baru yang diusulkan
        if (f > bound) {
            return new SearchResult(false, null, f);
        }
        
        // Jika tujuan tercapai, kembalikan path
        if (isGoalState(board)) {
            Path path = new Path(board, parent, piece, dir, g, h);
            return new SearchResult(true, path, bound);
        }
        
        String boardKey = getBoardKey(board);
        if (visited.contains(boardKey)) {
            return new SearchResult(false, null, Integer.MAX_VALUE);
        }
        
        visited.add(boardKey);
        
        int min = Integer.MAX_VALUE;
        
        // Coba semua gerakan yang mungkin
        for (Piece p : board.pieces) {
            for (char direction : priorityDirs) {
                if (canMove(board, p.name, direction)) {
                    Board newBoard = move(board, p.name, direction);
                    String newBoardKey = getBoardKey(newBoard);
                    
                    if (!visited.contains(newBoardKey)) {
                        Path currentPath = new Path(board, parent, piece, dir, g, h);
                        SearchResult result = search(newBoard, g + 1, bound, 
                                                   new HashSet<>(visited), 
                                                   currentPath, p.name, direction);
                        
                        if (result.isGoal) {
                            return result;
                        }
                        
                        if (result.nextBound < min) {
                            min = result.nextBound;
                        }
                    }
                }
            }
        }
        
        visited.remove(boardKey);
        return new SearchResult(false, null, min);
    }
    
    private boolean isGoalState(Board board) {
        // Tujuan tercapai jika primary piece (P) berdekatan dengan exit (K)
        for (int[] cell : board.primaryPiece.cells) {
            // Cek di kiri, kanan, atas, dan bawah lokasi K
            if ((cell[0] == board.exitRow && Math.abs(cell[1] - board.exitCol) == 1) ||
                (cell[1] == board.exitCol && Math.abs(cell[0] - board.exitRow) == 1)) {
                return true;
            }
        }
        return false;
    }
    
    private int calculateHeuristic(Board board) {
        switch (heuristicType) {
            case 0:
                return calculateManhattanDistance(board);
            case 1:
                return calculateEuclideanDistance(board);
            case 2:
                return calculateObstacleAwareDistance(board);
            default:
                return calculateManhattanDistance(board);
        }
    }
    
    private int calculateManhattanDistance(Board board) {
        // Hitung jarak Manhattan terdekat dari primary piece ke exit
        int minDistance = Integer.MAX_VALUE;
        
        for (int[] cell : board.primaryPiece.cells) {
            int distance = Math.abs(cell[0] - board.exitRow) + Math.abs(cell[1] - board.exitCol);
            minDistance = Math.min(minDistance, distance);
        }
        
        return minDistance;
    }
    
    private int calculateEuclideanDistance(Board board) {
        // Hitung jarak Euclidean terdekat dari primary piece ke exit
        double minDistance = Double.MAX_VALUE;
        
        for (int[] cell : board.primaryPiece.cells) {
            double dx = cell[0] - board.exitRow;
            double dy = cell[1] - board.exitCol;
            double distance = Math.sqrt(dx * dx + dy * dy);
            minDistance = Math.min(minDistance, distance);
        }
        
        // Konversi ke int dengan pembulatan ke atas
        return (int) Math.ceil(minDistance);
    }
    
    private int calculateObstacleAwareDistance(Board board) {
        // Hitung jarak Manhattan dan tambahkan penalti untuk setiap bidak lain yang menghalangi
        int baseDistance = calculateManhattanDistance(board);
        int obstacles = 0;
        
        // Temukan sel primary piece yang terdekat ke exit
        int[] closestCell = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (int[] cell : board.primaryPiece.cells) {
            int distance = Math.abs(cell[0] - board.exitRow) + Math.abs(cell[1] - board.exitCol);
            if (distance < minDistance) {
                minDistance = distance;
                closestCell = cell;
            }
        }
        
        if (closestCell == null) return baseDistance;
        
        // Hitung jumlah bidak yang menghalangi jalur langsung ke exit
        // Vertical path
        if (closestCell[1] == board.exitCol) {
            int start = Math.min(closestCell[0], board.exitRow);
            int end = Math.max(closestCell[0], board.exitRow);
            
            for (int i = start; i <= end; i++) {
                if (board.grid[i][closestCell[1]] != '.' && 
                    board.grid[i][closestCell[1]] != 'P' && 
                    board.grid[i][closestCell[1]] != 'K') {
                    obstacles++;
                }
            }
        }
        // Horizontal path
        else if (closestCell[0] == board.exitRow) {
            int start = Math.min(closestCell[1], board.exitCol);
            int end = Math.max(closestCell[1], board.exitCol);
            
            for (int j = start; j <= end; j++) {
                if (board.grid[closestCell[0]][j] != '.' && 
                    board.grid[closestCell[0]][j] != 'P' && 
                    board.grid[closestCell[0]][j] != 'K') {
                    obstacles++;
                }
            }
        }
        // Diagonal path (penalti untuk bidak di persegi panjang antara primary piece dan exit)
        else {
            int minRow = Math.min(closestCell[0], board.exitRow);
            int maxRow = Math.max(closestCell[0], board.exitRow);
            int minCol = Math.min(closestCell[1], board.exitCol);
            int maxCol = Math.max(closestCell[1], board.exitCol);
            
            for (int i = minRow; i <= maxRow; i++) {
                for (int j = minCol; j <= maxCol; j++) {
                    if (board.grid[i][j] != '.' && 
                        board.grid[i][j] != 'P' && 
                        board.grid[i][j] != 'K') {
                        obstacles++;
                    }
                }
            }
        }
        
        // Tambahkan penalti untuk setiap bidak yang menghalangi
        return baseDistance + obstacles * 2;
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
        
        // Rekonstruksi jalur dari goal ke start
        while (current != null) {
            if (current.piece != '\0') { // Skip start node
                steps.add(current);
            }
            current = current.parent;
        }
        
        // Balik urutan untuk mendapatkan jalur dari start ke goal
        Collections.reverse(steps);
        
        // Print jalur
        int step = 1;
        for (Path node : steps) {
            System.out.println("Gerakan " + step + ": " + node.piece + "-" + getDirName(node.direction) + 
                              " (g=" + node.g + ", h=" + node.h + ", f=" + (node.g + node.h) + ")");
            node.board.print();
            System.out.println();
            step++;
        }
        
        System.out.println("Solusi ditemukan dalam " + (steps.size()) + " langkah.");
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
        return switch (type) {
            case 0 -> "Manhattan Distance";
            case 1 -> "Euclidean Distance";
            case 2 -> "Obstacle-aware Distance";
            default -> "Unknown";
        };
    }
    
    private boolean canMove(Board board, char pieceName, char dir) {
        Piece piece = null;
        for (Piece p : board.pieces) {
            if (p.name == pieceName) {
                piece = p;
                break;
            }
        }
        
        if (piece == null) return false;
        
        // Cek apakah bisa bergerak ke arah yang diinginkan
        for (int[] cell : piece.cells) {
            int newRow = cell[0];
            int newCol = cell[1];
            
            switch (dir) {
                case 'L' -> newCol--;
                case 'R' -> newCol++;
                case 'U' -> newRow--;
                case 'D' -> newRow++;
            }
            
            // Cek apakah posisi baru valid
            if (newRow < 0 || newRow >= board.rows || newCol < 0 || newCol >= board.cols) {
                return false;
            }
            
            // Cek apakah posisi baru kosong atau milik piece yang sama
            char cellContent = board.grid[newRow][newCol];
            if (cellContent != '.' && cellContent != 'K' && cellContent != pieceName) {
                return false;
            }
        }
        
        return true;
    }
    
    private Board move(Board board, char pieceName, char dir) {
        Board newBoard = board.clone();
        Piece target = null;
        for (Piece p : newBoard.pieces) {
            if (p.name == pieceName) {
                target = p;
                break;
            }
        }
        
        if (target == null) return newBoard;
        
        // bersihkan posisi lama
        for (int[] cell : target.cells) {
            newBoard.grid[cell[0]][cell[1]] = '.';
        }
        
        // geser posisi
        for (int[] cell : target.cells) {
            switch (dir) {
                case 'L' -> cell[1]--;
                case 'R' -> cell[1]++;
                case 'U' -> cell[0]--;
                case 'D' -> cell[0]++;
            }
        }
        
        // isi posisi baru
        for (int[] cell : target.cells) {
            newBoard.grid[cell[0]][cell[1]] = pieceName;
        }
        
        return newBoard;
    }
    
    // Class untuk hasil pencarian IDA*
    private static class SearchResult {
        boolean isGoal;
        Path path;
        int nextBound;
        
        public SearchResult(boolean isGoal, Path path, int nextBound) {
            this.isGoal = isGoal;
            this.path = path;
            this.nextBound = nextBound;
        }
    }
    
    // Class untuk path dalam pencarian IDA*
    private static class Path {
        Board board;
        Path parent;
        char piece;
        char direction;
        int g;  // cost dari start node
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
