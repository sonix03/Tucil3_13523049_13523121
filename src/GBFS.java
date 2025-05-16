 // Sesuaikan dengan struktur package Anda jika ada
 
 import java.util.*;

public class GBFS implements Solver {
    private final char[] priorityDirs = {'U', 'D', 'L', 'R'};
    private int heuristicType;
    private int nodesExpanded;
    
    public GBFS() {
        this.heuristicType = 0; // Default Manhattan
    }
    
    public GBFS(int heuristicType) {
        this.heuristicType = heuristicType;
    }
    
    @Override
    public void solve(Board start) {
        nodesExpanded = 0;
        
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<String> closedSet = new HashSet<>();
        
        int h = Heuristic.calculate(start, heuristicType);
        Node startNode = new Node(start, null, '\0', '\0', h);
        openSet.add(startNode);
        
        System.out.println("Greedy Best First Search dengan heuristik " + 
                          Heuristic.getName(heuristicType));
        
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
                        
                        if (!closedSet.contains(newBoardKey)) {
                            int newH = Heuristic.calculate(newBoard, heuristicType);
                            Node neighbor = new Node(newBoard, current, piece.name, dir, newH);
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
    
    int h = Heuristic.calculate(start, heuristicType);
    Node startNode = new Node(start, null, '\0', '\0', h);
    openSet.add(startNode);
    
    System.out.println("Greedy Best First Search dengan heuristik " + Heuristic.getName(heuristicType));
    
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
                    
                    if (!closedSet.contains(newBoardKey)) {
                        int newH = Heuristic.calculate(newBoard, heuristicType);
                        Node neighbor = new Node(newBoard, current, piece.name, dir, newH);
                        openSet.add(neighbor);
                    }
                }
            }
        }
    }
    
    if (solution == null) {
        System.out.println("Tidak ditemukan solusi!");
        return new ArrayList<>(); // kosongkan jika gagal
    }
    
    // Kumpulkan langkah solusi dari node tujuan ke awal
    List<Board> path = new ArrayList<>();
    Node current = solution;
    while (current != null) {
        path.add(current.board);
        current = current.parent;
    }
    Collections.reverse(path); // agar urut dari awal ke tujuan
    
    System.out.println("Solusi ditemukan dalam " + (path.size() - 1) + " langkah.");
    System.out.println("Node yang dieksplorasi: " + nodesExpanded);
    
    return path;
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
                              " (h=" + node.h + ")");
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
    
    private static class Node implements Comparable<Node> {
        Board board;
        Node parent;
        char piece;
        char direction;
        int h;
        
        public Node(Board board, Node parent, char piece, char direction, int h) {
            this.board = board;
            this.parent = parent;
            this.piece = piece;
            this.direction = direction;
            this.h = h;
        }
        
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.h, other.h);
        }
    }
}