import java.util.*;

public class GBFS implements Solver {
    // Prioritas gerakan berdasarkan arah
    private final char[] priorityDirs = {'U', 'D', 'L', 'R'};
    
    public void solve(Board start) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<String> closedSet = new HashSet<>();
        Map<String, Node> cameFrom = new HashMap<>();
        
        Node startNode = new Node(start, null, '\0', '\0', 0);
        openSet.add(startNode);
        
        Node solution = null;
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            
            // Cek apakah sudah mencapai tujuan
            if (isGoalState(current.board)) {
                solution = current;
                break;
            }
            
            String boardKey = getBoardKey(current.board);
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
                        
                        if (!closedSet.contains(newBoardKey)) {
                            int cost = calculateHeuristic(newBoard);
                            Node neighbor = new Node(newBoard, current, piece.name, dir, cost);
                            openSet.add(neighbor);
                            cameFrom.put(newBoardKey, current);
                        }
                    }
                }
            }
        }
        
        // Rekonstruksi jalur solusi
        if (solution != null) {
            List<Node> path = reconstructPath(solution);
            printSolution(path);
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
    
    private int calculateHeuristic(Board board) {
        // Hitung jarak Manhattan terdekat dari primary piece ke exit
        int minDistance = Integer.MAX_VALUE;
        
        for (int[] cell : board.primaryPiece.cells) {
            int distance = Math.abs(cell[0] - board.exitRow) + Math.abs(cell[1] - board.exitCol);
            minDistance = Math.min(minDistance, distance);
        }
        
        return minDistance;
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
    
    private List<Node> reconstructPath(Node goal) {
        List<Node> path = new ArrayList<>();
        Node current = goal;
        
        while (current != null) {
            if (current.piece != '\0') { // Skip start node yang tidak memiliki gerakan
                path.add(current);
            }
            current = current.parent;
        }
        
        Collections.reverse(path);
        return path;
    }
    
    private void printSolution(List<Node> path) {
        int step = 1;
        for (Node node : path) {
            System.out.println("Gerakan " + step + ": " + node.piece + "-" + getDirName(node.direction));
            node.board.print();
            System.out.println();
            step++;
        }
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
    
    // Class untuk node dalam pencarian GBFS
    private static class Node implements Comparable<Node> {
        Board board;
        Node parent;
        char piece;
        char direction;
        int cost;
        
        public Node(Board board, Node parent, char piece, char direction, int cost) {
            this.board = board;
            this.parent = parent;
            this.piece = piece;
            this.direction = direction;
            this.cost = cost;
        }
        
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.cost, other.cost);
        }
    }
}