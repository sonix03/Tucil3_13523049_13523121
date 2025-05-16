import java.util.*;

/**
 * Implementasi algoritma Uniform Cost Search (UCS)
 * Algoritma ini menggunakan cost sejauh ini (g) saja tanpa heuristik
 * untuk menemukan jalur dengan cost minimal
 */
public class UCS implements Solver {
    private final char[] priorityDirs = {'U', 'D', 'L', 'R'};
    private int nodesExpanded;
    
    @Override
    public void solve(Board start) {
        nodesExpanded = 0;
        
        // Buat priority queue berdasarkan nilai cost (g) saja
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        // Set untuk menyimpan state yang sudah dikunjungi
        Set<String> closedSet = new HashSet<>();
        // Map untuk menyimpan node dengan biaya terbaik untuk setiap state
        Map<String, Integer> bestCost = new HashMap<>();
        
        // Node awal dengan cost g = 0
        Node startNode = new Node(start, null, '\0', '\0', 0);
        openSet.add(startNode);
        String startKey = getBoardKey(start);
        bestCost.put(startKey, 0);
        
        System.out.println("Uniform Cost Search (UCS)");
        
        Node solution = null;
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            nodesExpanded++;
            
            // Cek apakah sudah mencapai tujuan
            if (isGoalState(current.board)) {
                solution = current;
                break;
            }
            
            String boardKey = getBoardKey(current.board);
            
            // Skip jika state ini sudah diproses dengan cost yang lebih rendah atau sama
            if (closedSet.contains(boardKey)) {
                continue;
            }
            
            closedSet.add(boardKey);
            
            // Coba semua gerakan yang mungkin untuk setiap bidak
            for (Piece piece : current.board.pieces) {
                for (char dir : priorityDirs) {
                    if (canMove(current.board, piece.name, dir)) {
                        Board newBoard = move(current.board, piece.name, dir);
                        String newBoardKey = getBoardKey(newBoard);
                        
                        // Jika state baru atau menemukan jalur yang lebih baik
                        int newG = current.g + 1;
                        
                        if (!closedSet.contains(newBoardKey) && 
                            (!bestCost.containsKey(newBoardKey) || newG < bestCost.get(newBoardKey))) {
                            
                            bestCost.put(newBoardKey, newG);
                            Node neighbor = new Node(newBoard, current, piece.name, dir, newG);
                            openSet.add(neighbor);
                        }
                    }
                }
            }
        }
        
        // Rekonstruksi dan cetak solusi
        if (solution != null) {
            printSolution(solution);
            System.out.println("Node yang dieksplorasi: " + nodesExpanded);
        } else {
            System.out.println("Tidak ditemukan solusi!");
        }
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
    
    private String getBoardKey(Board board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.rows; i++) {
            for (int j = 0; j < board.cols; j++) {
                sb.append(board.grid[i][j]);
            }
        }
        return sb.toString();
    }
    
    private void printSolution(Node path) {
        List<Node> steps = new ArrayList<>();
        Node current = path;
        
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
        for (Node node : steps) {
            System.out.println("Gerakan " + step + ": " + node.piece + "-" + getDirName(node.direction) + 
                              " (cost=" + node.g + ")");
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
    
    // Class untuk node dalam pencarian UCS
    private static class Node implements Comparable<Node> {
        Board board;
        Node parent;
        char piece;
        char direction;
        int g;  // cost dari start node
        
        public Node(Board board, Node parent, char piece, char direction, int g) {
            this.board = board;
            this.parent = parent;
            this.piece = piece;
            this.direction = direction;
            this.g = g;
        }
        
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.g, other.g);
        }
    }
}