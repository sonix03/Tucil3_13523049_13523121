// Sesuaikan dengan struktur package Anda jika ada

import java.util.*;

public class AStar implements Solver {
    private final char[] priorityDirs = {'U', 'D', 'L', 'R'};
    private int heuristicType;
    private int nodesExpanded;
    
    public AStar(int heuristicType) {
        this.heuristicType = heuristicType;
    }
    
    @Override
    public void solve(Board start) {
        nodesExpanded = 0;
        
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<String> closedSet = new HashSet<>();
        Map<String, Integer> bestCost = new HashMap<>();
        
        int h = Heuristic.calculate(start, heuristicType);
        Node startNode = new Node(start, null, '\0', '\0', 0, h);
        openSet.add(startNode);
        String startKey = getBoardKey(start);
        bestCost.put(startKey, 0);
        
        System.out.println("A* dengan heuristik " + Heuristic.getName(heuristicType));
        
        Node solution = null;
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            nodesExpanded++;
            
            if (isGoalState(current.board)) {
                solution = current;
                break;
            }
            
            String boardKey = getBoardKey(current.board);
            
            if (closedSet.contains(boardKey)) {
                continue;
            }
            
            closedSet.add(boardKey);
            
            for (Piece piece : current.board.pieces) { // Iterasi semua bidak di board
                for (char dir : priorityDirs) {
                    if (canMove(current.board, piece.name, dir)) {
                        Board newBoard = move(current.board, piece.name, dir);
                        String newBoardKey = getBoardKey(newBoard);
                        
                        int newG = current.g + 1;
                        
                        if (!closedSet.contains(newBoardKey) && 
                            (!bestCost.containsKey(newBoardKey) || newG < bestCost.get(newBoardKey))) {
                            
                            bestCost.put(newBoardKey, newG);
                            int newH = Heuristic.calculate(newBoard, heuristicType);
                            Node neighbor = new Node(newBoard, current, piece.name, dir, newG, newH);
                            openSet.add(neighbor);
                        }
                    }
                }
            }
        }
        
        if (solution != null) {
            printSolution(solution);
            System.out.println("Node yang dieksplorasi: " + nodesExpanded);
        } else {
            System.out.println("Tidak ditemukan solusi!");
        }
    }

    @Override
public List<Board> solveAndReturnPath(Board start) {
    nodesExpanded = 0;

    PriorityQueue<Node> openSet = new PriorityQueue<>();
    Set<String> closedSet = new HashSet<>();
    Map<String, Integer> bestCost = new HashMap<>();

    int h = Heuristic.calculate(start, heuristicType);
    Node startNode = new Node(start, null, '\0', '\0', 0, h);
    openSet.add(startNode);
    String startKey = getBoardKey(start);
    bestCost.put(startKey, 0);

    Node solution = null;

    while (!openSet.isEmpty()) {
        Node current = openSet.poll();
        nodesExpanded++;

        if (isGoalState(current.board)) {
            solution = current;
            break;
        }

        String boardKey = getBoardKey(current.board);
        if (closedSet.contains(boardKey)) {
            continue;
        }
        closedSet.add(boardKey);

        for (Piece piece : current.board.pieces) {
            for (char dir : priorityDirs) {
                if (canMove(current.board, piece.name, dir)) {
                    Board newBoard = move(current.board, piece.name, dir);
                    String newBoardKey = getBoardKey(newBoard);
                    int newG = current.g + 1;

                    if (!closedSet.contains(newBoardKey) &&
                        (!bestCost.containsKey(newBoardKey) || newG < bestCost.get(newBoardKey))) {

                        bestCost.put(newBoardKey, newG);
                        int newH = Heuristic.calculate(newBoard, heuristicType);
                        Node neighbor = new Node(newBoard, current, piece.name, dir, newG, newH);
                        openSet.add(neighbor);
                    }
                }
            }
        }
    }

    if (solution == null) {
        System.out.println("Tidak ditemukan solusi!");
        return new ArrayList<>(); // return list kosong jika gagal
    }

    // Traceback path dari solusi ke awal
    List<Board> path = new ArrayList<>();
    Node current = solution;
    while (current != null) {
        path.add(current.board);
        current = current.parent;
    }
    Collections.reverse(path); // agar urut dari awal ke solusi

    System.out.println("Solusi ditemukan dalam " + (path.size() - 1) + " langkah.");
    System.out.println("Node yang dieksplorasi: " + nodesExpanded);

    return path;
}

    
    private boolean isGoalState(Board board) {
        if (board.primaryPiece == null || board.exitRow == -1) return false; // Guard clause
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
    
    private void printSolution(Node path) {
        List<Node> steps = new ArrayList<>();
        Node current = path;
        
        while (current != null) {
            if (current.piece != '\0') { 
                steps.add(current);
            }
            current = current.parent;
        }
        
        Collections.reverse(steps);
        
        int step = 1;
        for (Node node : steps) {
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
            return false; // Bidak tidak ditemukan atau tidak punya sel
        }
        
        PieceOrientation orientation = pieceToMove.getOrientation();

        if (orientation == PieceOrientation.HORIZONTAL) {
            if (dir == 'U' || dir == 'D') {
                return false; // Bidak horizontal tidak boleh bergerak atas/bawah
            }
        } else if (orientation == PieceOrientation.VERTICAL) {
            if (dir == 'L' || dir == 'R') {
                return false; // Bidak vertikal tidak boleh bergerak kiri/kanan
            }
        }
        // Untuk PieceOrientation.SINGLE_BLOCK atau PieceOrientation.OTHER,
        // biarkan semua arah dicoba, validasi selanjutnya akan menangani.

        // Cek apakah setiap sel dari bidak bisa bergerak ke arah yang diinginkan
        for (int[] cell : pieceToMove.cells) {
            int currentRow = cell[0];
            int currentCol = cell[1];
            int newRow = currentRow;
            int newCol = currentCol;
            
            switch (dir) {
                case 'L' -> newCol--;
                case 'R' -> newCol++;
                case 'U' -> newRow--;
                case 'D' -> newRow++;
            }
            
            // Cek batasan board
            if (newRow < 0 || newRow >= board.rows || newCol < 0 || newCol >= board.cols) {
                return false; // Keluar dari board
            }
            
            // Cek isi sel tujuan
            char destinationCellContent = board.grid[newRow][newCol];
            // Boleh pindah jika sel tujuan kosong ('.'), atau adalah exit ('K'), 
            // atau merupakan bagian dari bidak itu sendiri (untuk kasus multi-sel pindah ke salah satu selnya sendiri).
            boolean partOfItself = false;
            for(int[] ownCell : pieceToMove.cells){
                if(ownCell[0] == newRow && ownCell[1] == newCol){
                    partOfItself = true;
                    break;
                }
            }

            if (destinationCellContent != '.' && destinationCellContent != 'K' && !partOfItself) {
                return false; // Terhalang oleh bidak lain
            }
        }
        return true;
    }
    // --- AKHIR METODE CANMOVE ---
    
    private Board move(Board board, char pieceName, char dir) {
        Board newBoard = board.clone(); // Penting untuk mendapatkan salinan piece yang benar
        Piece targetPieceInNewBoard = null;
        for(Piece p : newBoard.pieces){
            if(p.name == pieceName){
                targetPieceInNewBoard = p;
                break;
            }
        }

        if (targetPieceInNewBoard == null) return newBoard; // Seharusnya tidak terjadi jika canMove true
        
        // Bersihkan posisi lama bidak di grid baru
        for (int[] cell : targetPieceInNewBoard.cells) {
            newBoard.grid[cell[0]][cell[1]] = '.';
        }
        
        // Geser posisi sel-sel bidak
        for (int[] cell : targetPieceInNewBoard.cells) {
            switch (dir) {
                case 'L' -> cell[1]--;
                case 'R' -> cell[1]++;
                case 'U' -> cell[0]--;
                case 'D' -> cell[0]++;
            }
        }
        
        // Isi posisi baru bidak di grid baru
        for (int[] cell : targetPieceInNewBoard.cells) {
            newBoard.grid[cell[0]][cell[1]] = pieceName;
        }
        
        // Pastikan primaryPiece di newBoard diupdate jika itu yang bergerak
        if (pieceName == 'P') {
            newBoard.primaryPiece = targetPieceInNewBoard;
        }
        
        return newBoard;
    }
    
    private static class Node implements Comparable<Node> {
        Board board;
        Node parent;
        char piece;
        char direction;
        int g;  // cost dari start node
        int h;  // heuristic value
        
        public Node(Board board, Node parent, char piece, char direction, int g, int h) {
            this.board = board;
            this.parent = parent;
            this.piece = piece;
            this.direction = direction;
            this.g = g;
            this.h = h;
        }
        
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.g + this.h, other.g + other.h);
        }
    }
}