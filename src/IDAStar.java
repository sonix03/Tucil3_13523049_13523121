import java.util.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;

public class IDAStar implements Solver {
    private final char[] priorityDirs = {'U', 'D', 'L', 'R'};
    private int heuristicType;
    private int nodesExpandedThisIteration;
    private int totalNodesExpanded;
    private int lastSummarizedStepCount = 0;

    private static class SummarizedStep {
        char piece;
        char direction;
        int moveCount;
        Board boardState;
        int g;
        int h;

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

    public IDAStar(int heuristicType) {
        this.heuristicType = heuristicType;
    }
    
    private String getAlgorithmName() {
        return "IDA* Search";
    }

    private boolean needsHeuristic() {
        return true;
    }

    private void ensureTestDirectoryExists() {
        File directory = new File("test");
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    public void solve(Board start) {
        totalNodesExpanded = 0;
        lastSummarizedStepCount = 0;
        int bound = Heuristic.calculate(start, heuristicType);
        Path solutionPathNode = null;

        System.out.println(getAlgorithmName() + " dengan heuristik " + getHeuristicName(heuristicType));

        while (true) { 
            System.out.println("Menjelajah dengan batas f-cost: " + bound);
            nodesExpandedThisIteration = 0;
            SearchResult result = search(new Path(start, null, '\0', '\0', 0, Heuristic.calculate(start, heuristicType)), bound, new HashSet<>());
            totalNodesExpanded += nodesExpandedThisIteration;

            if (result.isGoal) {
                solutionPathNode = result.path;
                break;
            }
            if (result.nextBound == Integer.MAX_VALUE) {
                System.out.println("Tidak ada batas berikutnya, solusi tidak ditemukan.");
                break;
            }
            if (result.nextBound <= bound) { 
                System.out.println("Peringatan: Batas f-cost tidak meningkat (lama: " + bound + ", baru: " + result.nextBound + "). Menaikkan batas minimal.");
                bound++; 
            } else {
                bound = result.nextBound;
            }
            if (bound > 1000 && solutionPathNode==null) { // Increased limit for potentially harder puzzles
                System.out.println("IDA* melebihi batas iterasi/f-cost maksimum ("+ bound +"), menghentikan.");
                break;
            }
        }

        if (solutionPathNode != null) {
            List<SummarizedStep> summarizedPath = getSummarizedPath(solutionPathNode);
            this.lastSummarizedStepCount = summarizedPath.size();
            printSummarizedSolution(summarizedPath);
        } else {
            System.out.println("Tidak ditemukan solusi!");
            System.out.println("Total Node yang dieksplorasi (hingga pencarian terakhir): " + totalNodesExpanded);
            ensureTestDirectoryExists();
            try (PrintWriter writer = new PrintWriter(new FileWriter("test/output.txt", false))) {
                writer.println(getAlgorithmName() + " dengan heuristik " + Heuristic.getName(heuristicType));
                writer.println("Tidak ditemukan solusi!");
                writer.println("Total Node yang dieksplorasi: " + totalNodesExpanded);
            } catch (IOException e) {
                System.err.println("Gagal menulis ke file output.txt (solusi tidak ditemukan): " + e.getMessage());
            }
        }
    }

    @Override
    public List<Board> solveAndReturnPath(Board start) {
        totalNodesExpanded = 0;
        lastSummarizedStepCount = 0;
        int bound = Heuristic.calculate(start, heuristicType);
        Path solutionPathNode = null;

        System.out.println(getAlgorithmName() + " dengan heuristik " + getHeuristicName(heuristicType) + " (mencari path list)");

        while (true) {
            System.out.println("Menjelajah dengan batas f-cost: " + bound);
            nodesExpandedThisIteration = 0;
            SearchResult result = search(new Path(start, null, '\0', '\0', 0, Heuristic.calculate(start, heuristicType)), bound, new HashSet<>());
            totalNodesExpanded += nodesExpandedThisIteration;

            if (result.isGoal) {
                solutionPathNode = result.path;
                break;
            }
            if (result.nextBound == Integer.MAX_VALUE) {
                System.out.println("Tidak ada batas berikutnya, solusi tidak ditemukan.");
                break;
            }
            if (result.nextBound <= bound) {
                System.out.println("Peringatan: Batas f-cost tidak meningkat (lama: " + bound + ", baru: " + result.nextBound + "). Menaikkan batas minimal.");
                bound++;
            } else {
                bound = result.nextBound;
            }
             if (bound > 1000 && solutionPathNode==null) { // Increased limit
                System.out.println("IDA* melebihi batas iterasi/f-cost maksimum ("+ bound +"), menghentikan.");
                break;
            }
        }

        if (solutionPathNode == null) {
            System.out.println("Tidak ditemukan solusi!");
            System.out.println("Total Node yang dieksplorasi (hingga pencarian terakhir): " + totalNodesExpanded);
            ensureTestDirectoryExists();
            try (PrintWriter writer = new PrintWriter(new FileWriter("test/output.txt", false))) {
                writer.println(getAlgorithmName() + " dengan heuristik " + Heuristic.getName(this.heuristicType));
                writer.println("Tidak ditemukan solusi!");
                writer.println("Total Node yang dieksplorasi: " + totalNodesExpanded);
            } catch (IOException e) {
                System.err.println("Gagal menulis ke file output.txt (solusi tidak ditemukan): " + e.getMessage());
            }
            return new ArrayList<>();
        }

        List<Board> boardPath = new ArrayList<>();
        Path tempPath = solutionPathNode;
        while (tempPath != null) {
            boardPath.add(tempPath.board);
            tempPath = tempPath.parent;
        }
        Collections.reverse(boardPath);

        List<SummarizedStep> summarizedPath = getSummarizedPath(solutionPathNode);
        this.lastSummarizedStepCount = summarizedPath.size();
        printSummarizedSolution(summarizedPath); // Cetak dan simpan solusi
        
        return boardPath;
    }

    @Override
    public int getLastSummarizedStepCount() {
        return lastSummarizedStepCount;
    }

    @Override
    public int getNodesExplored() {
        return totalNodesExpanded;
    }

    
    private SearchResult search(Path currentPath, int bound, Set<String> pathStates) {
        nodesExpandedThisIteration++;
        Board currentBoard = currentPath.board;
        int gCost = currentPath.g;
        int hCost = currentPath.h; 
        int fCost = gCost + hCost;

        if (fCost > bound) {
            return new SearchResult(false, null, fCost);
        }

        if (isGoalState(currentBoard)) {
            return new SearchResult(true, currentPath, fCost);
        }

        String boardKey = getBoardKey(currentBoard);
        if (pathStates.contains(boardKey)) {
            return new SearchResult(false, null, Integer.MAX_VALUE); 
        }
        pathStates.add(boardKey);

        int minNextBound = Integer.MAX_VALUE;

        for (Piece piece : currentBoard.pieces) {
            for (char dir : priorityDirs) {
                if (canMove(currentBoard, piece.name, dir)) {
                    Board newBoard = move(currentBoard, piece.name, dir);
                    int newGCost = gCost + 1;
                    int newHCost = Heuristic.calculate(newBoard, heuristicType);
                    Path newPath = new Path(newBoard, currentPath, piece.name, dir, newGCost, newHCost);

                    SearchResult recursiveResult = search(newPath, bound, pathStates);

                    if (recursiveResult.isGoal) {
                        pathStates.remove(boardKey); 
                        return recursiveResult;
                    }
                    minNextBound = Math.min(minNextBound, recursiveResult.nextBound);
                }
            }
        }

        pathStates.remove(boardKey); 
        return new SearchResult(false, null, minNextBound);
    }

    private List<SummarizedStep> getSummarizedPath(Path solutionPathNode) {
        List<SummarizedStep> summarizedSteps = new ArrayList<>();
        if (solutionPathNode == null) return summarizedSteps;

        List<Path> rawPathNodes = new ArrayList<>();
        Path current = solutionPathNode;
        while (current != null && current.parent != null) {
            rawPathNodes.add(current);
            current = current.parent;
        }
        Collections.reverse(rawPathNodes);

        if (rawPathNodes.isEmpty()) return summarizedSteps;

        char currentPiece = rawPathNodes.get(0).piece;
        char currentDirection = rawPathNodes.get(0).direction;
        int moveCount = 0;
        Board lastBoardInSequence = rawPathNodes.get(0).board;
        int gVal = rawPathNodes.get(0).g;
        int hVal = rawPathNodes.get(0).h;

        for (Path pathNode : rawPathNodes) {
            if (pathNode.piece == currentPiece && pathNode.direction == currentDirection) {
                moveCount++;
                lastBoardInSequence = pathNode.board;
                gVal = pathNode.g;
                hVal = pathNode.h;
            } else {
                summarizedSteps.add(new SummarizedStep(currentPiece, currentDirection, moveCount, lastBoardInSequence, gVal, hVal));
                currentPiece = pathNode.piece;
                currentDirection = pathNode.direction;
                moveCount = 1;
                lastBoardInSequence = pathNode.board;
                gVal = pathNode.g;
                hVal = pathNode.h;
            }
        }
         if (moveCount > 0) {
            summarizedSteps.add(new SummarizedStep(currentPiece, currentDirection, moveCount, lastBoardInSequence, gVal, hVal));
        }
        return summarizedSteps;
    }

    private void printSummarizedSolution(List<SummarizedStep> summarizedPath) {
        ensureTestDirectoryExists();
        try (PrintWriter writer = new PrintWriter(new FileWriter("test/outputIDAStar.txt", false))) {
            String algoHeader = getAlgorithmName() + " dengan heuristik " + getHeuristicName(heuristicType);
            System.out.println("\n" + algoHeader);
            writer.println(algoHeader);
            writer.println("------------------------------------");

            int stepNumber = 1;
            for (SummarizedStep step : summarizedPath) {
                String stepDisplay = "Langkah " + stepNumber + ": " + step.getDisplay(getDirName(step.direction));
                System.out.println(stepDisplay);
                writer.println(stepDisplay);

                step.boardState.print(); 
                for (int r = 0; r < step.boardState.rows; r++) {
                    for (int c = 0; c < step.boardState.cols; c++) {
                        writer.print(step.boardState.grid[r][c] + (c == step.boardState.cols - 1 ? "" : " "));
                    }
                    writer.println();
                }
                System.out.println();
                writer.println();
                stepNumber++;
            }
            String summary = "Solusi ditemukan dalam " + summarizedPath.size() + " langkah (ringkas).";
            System.out.println(summary);
            writer.println(summary);

            String nodesExploredStr = "Total Node yang dieksplorasi: " + totalNodesExpanded;
            System.out.println(nodesExploredStr);
            writer.println(nodesExploredStr);

        } catch (IOException e) {
            System.err.println("Gagal menulis langkah solusi ke file outputIDAStar.txt: " + e.getMessage());
        }
    }
    // ... sisa metode (isGoalState, getBoardKey, getDirName, getHeuristicName, canMove, move, SearchResult, Path classes) tetap sama ...
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
    
    private String getHeuristicName(int type) { // Already exists
        return Heuristic.getName(type);
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
                System.err.println("Error critical (IDA*): Piece " + pieceName + " moved out of bounds. Row: " + cell[0] + ", Col: " + cell[1]);
                return board; 
            }
        }
        
        char KODE_BIDAK_UTAMA = 'P';
        if (pieceName == KODE_BIDAK_UTAMA) {
            newBoard.primaryPiece = targetPieceInNewBoard;
        }
        return newBoard;
    }

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

    private static class Path {
        Board board;
        Path parent;
        char piece;
        char direction;
        int g;
        int h;

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