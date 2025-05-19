import java.util.*;

public class AStar implements Solver {
    private final char[] priorityDirs = {'U', 'D', 'L', 'R'};
    private int heuristicType;
    public int nodesExpanded = 0;
    public int lastSummarizedStepCount = 0;

    private static class SummarizedStep {
        char piece;
        char direction;
        int moveCount;
        Board boardState; 
        int g;
        int h;
        // f = g + h

        public SummarizedStep(char piece, char direction, int moveCount, Board boardState, int g, int h) {
            this.piece = piece;
            this.direction = direction;
            this.moveCount = moveCount;
            this.boardState = boardState;
            this.g = g;
            this.h = h;
        }

        public String getDisplay(String dirName) {
            return piece + "-" + dirName + (moveCount > 1 ? " " + moveCount + " kali" : "") +
                   " (g=" + g + ", h=" + h + ", f=" + (g + h) + ")";
        }
    }

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

        Node solutionNode = null;

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            nodesExpanded++;

            if (isGoalState(current.board)) {
                solutionNode = current;
                break;
            }

            String boardKey = getBoardKey(current.board);

            if (closedSet.contains(boardKey) && current.g >= bestCost.getOrDefault(boardKey, Integer.MAX_VALUE)) {
                continue;
            }

            if (current.g > bestCost.getOrDefault(boardKey, Integer.MAX_VALUE)) { 
                 continue;
            }


            closedSet.add(boardKey);
           

            for (Piece piece : current.board.pieces) {
                for (char dir : priorityDirs) {
                    if (canMove(current.board, piece.name, dir)) {
                        Board newBoard = move(current.board, piece.name, dir);
                        String newBoardKey = getBoardKey(newBoard);

                        int newG = current.g + 1;

                        if (newG < bestCost.getOrDefault(newBoardKey, Integer.MAX_VALUE)) {
                            bestCost.put(newBoardKey, newG);
                            int newH = Heuristic.calculate(newBoard, heuristicType);
                            Node neighbor = new Node(newBoard, current, piece.name, dir, newG, newH);
                            openSet.add(neighbor);
                        }
                    }
                }
            }
        }

        if (solutionNode != null) {
            List<SummarizedStep> summarizedPath = getSummarizedPath(solutionNode);
            printSummarizedSolution(summarizedPath);
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
    
        int hVal = Heuristic.calculate(start, heuristicType);
        Node startNode = new Node(start, null, '\0', '\0', 0, hVal);
        openSet.add(startNode);
        String startKey = getBoardKey(start);
        bestCost.put(startKey, 0);
    
        System.out.println("A* dengan heuristik " + Heuristic.getName(heuristicType) + " (mencari path list)");
    
        Node solutionNode = null;
    
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            nodesExpanded++;
    
            if (isGoalState(current.board)) {
                solutionNode = current;
                break;
            }
    
            String boardKey = getBoardKey(current.board);
            if (closedSet.contains(boardKey) && current.g >= bestCost.getOrDefault(boardKey, Integer.MAX_VALUE)) {
                continue;
            }
            closedSet.add(boardKey);
    
            for (Piece piece : current.board.pieces) {
                for (char dir : priorityDirs) {
                    if (canMove(current.board, piece.name, dir)) {
                        Board newBoard = move(current.board, piece.name, dir);
                        String newBoardKey = getBoardKey(newBoard);
                        int newG = current.g + 1;
    
                        if (newG < bestCost.getOrDefault(newBoardKey, Integer.MAX_VALUE)) {
                            bestCost.put(newBoardKey, newG);
                            int newH = Heuristic.calculate(newBoard, heuristicType);
                            Node neighbor = new Node(newBoard, current, piece.name, dir, newG, newH);
                            openSet.add(neighbor);
                        }
                    }
                }
            }
        }
    
        if (solutionNode == null) {
            System.out.println("Tidak ditemukan solusi!");
            return new ArrayList<>();
        }
        
        List<Board> boardPath = new ArrayList<>();
        Node tempNode = solutionNode;
        while (tempNode != null) {
            boardPath.add(tempNode.board);
            tempNode = tempNode.parent;
        }
        Collections.reverse(boardPath);
        
        List<SummarizedStep> summarizedPath = getSummarizedPath(solutionNode);
        lastSummarizedStepCount = summarizedPath.size();
        System.out.println("Solusi ditemukan dalam " + summarizedPath.size() + " langkah (ringkas).");
        System.out.println("Node yang dieksplorasi: " + nodesExpanded);
        
        return boardPath;
    }

    @Override
    public int getLastSummarizedStepCount() {
        return lastSummarizedStepCount;
    }
    
    @Override
    public int getNodesExplored() {
        return nodesExpanded;
    }

    private List<SummarizedStep> getSummarizedPath(Node solutionNode) {
        List<SummarizedStep> summarizedSteps = new ArrayList<>();
        if (solutionNode == null) {
            return summarizedSteps;
        }

        List<Node> rawPathNodes = new ArrayList<>();
        Node current = solutionNode;
        while (current != null && current.parent != null) {
            rawPathNodes.add(current);
            current = current.parent;
        }
        Collections.reverse(rawPathNodes);

        if (rawPathNodes.isEmpty()) {
            return summarizedSteps;
        }

        char currentPiece = rawPathNodes.get(0).piece;
        char currentDirection = rawPathNodes.get(0).direction;
        int moveCount = 0;
        Board lastBoardInSequence = rawPathNodes.get(0).board;
        int gVal = rawPathNodes.get(0).g;
        int hVal = rawPathNodes.get(0).h;

        for (Node node : rawPathNodes) {
            if (node.piece == currentPiece && node.direction == currentDirection) {
                moveCount++;
                lastBoardInSequence = node.board;
                gVal = node.g;
                hVal = node.h;
            } else {
                summarizedSteps.add(new SummarizedStep(currentPiece, currentDirection, moveCount, lastBoardInSequence, gVal, hVal));
                currentPiece = node.piece;
                currentDirection = node.direction;
                moveCount = 1;
                lastBoardInSequence = node.board;
                gVal = node.g;
                hVal = node.h;
            }
        }
        if (moveCount > 0) {
             summarizedSteps.add(new SummarizedStep(currentPiece, currentDirection, moveCount, lastBoardInSequence, gVal, hVal));
        }

        return summarizedSteps;
    }

    private void printSummarizedSolution(List<SummarizedStep> summarizedPath) {
        int stepNumber = 1;
        for (SummarizedStep step : summarizedPath) {
            System.out.println("Langkah " + stepNumber + ": " + step.getDisplay(getDirName(step.direction)));
            step.boardState.print();
            System.out.println();
            stepNumber++;
        }
        System.out.println("Solusi ditemukan dalam " + summarizedPath.size() + " langkah (ringkas).");
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
            if (cell[0] >= 0 && cell[0] < newBoard.rows && cell[1] >=0 && cell[1] < newBoard.cols) {
                 newBoard.grid[cell[0]][cell[1]] = pieceName;
            } else {
                System.err.println("Error critical (AStar): Piece " + pieceName + " moved out of bounds. Row: " + cell[0] + ", Col: " + cell[1]);
                return board; 
            }
        }

        char KODE_BIDAK_UTAMA = 'P';
        if (pieceName == KODE_BIDAK_UTAMA) {
            newBoard.primaryPiece = targetPieceInNewBoard;
        }

        return newBoard;
    }

    private static class Node implements Comparable<Node> {
        Board board;
        Node parent;
        char piece;
        char direction;
        int g;
        int h;

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
            int fThis = this.g + this.h;
            int fOther = other.g + other.h;
            if (fThis == fOther) { // Tie-breaking: prefer smaller h
                return Integer.compare(this.h, other.h);
            }
            return Integer.compare(fThis, fOther);
        }
    }
}