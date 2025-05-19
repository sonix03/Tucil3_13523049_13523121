public class Heuristic {
    // Tipe heuristik
    public static final int MANHATTAN = 0;
    public static final int EUCLIDEAN = 1;
    public static final int OBSTACLE_AWARE = 2;
    
    public static int calculate(Board board, int type) {
        switch (type) {
            case MANHATTAN:
                return calculateManhattan(board);
            case EUCLIDEAN:
                return calculateEuclidean(board);
            case OBSTACLE_AWARE:
                return calculateObstacleAware(board);
            default:
                return calculateManhattan(board);
        }
    }
    

    public static String getName(int type) {
        return switch (type) {
            case MANHATTAN -> "Manhattan Distance";
            case EUCLIDEAN -> "Euclidean Distance";
            case OBSTACLE_AWARE -> "Obstacle-aware Distance";
            default -> "Unknown";
        };
    }
    

    private static int calculateManhattan(Board board) {
        int minDistance = Integer.MAX_VALUE;
        
        for (int[] cell : board.primaryPiece.cells) {
            int distance = Math.abs(cell[0] - board.exitRow) + Math.abs(cell[1] - board.exitCol);
            minDistance = Math.min(minDistance, distance);
        }
        
        return minDistance;
    }
    

    private static int calculateEuclidean(Board board) {
        double minDistance = Double.MAX_VALUE;
        
        for (int[] cell : board.primaryPiece.cells) {
            double dx = cell[0] - board.exitRow;
            double dy = cell[1] - board.exitCol;
            double distance = Math.sqrt(dx * dx + dy * dy);
            minDistance = Math.min(minDistance, distance);
        }
        
        
        return (int) Math.ceil(minDistance);
    }
    

    private static int calculateObstacleAware(Board board) {
        int baseDistance = calculateManhattan(board);
        int obstacles = 0;
    
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
    
        int row = closestCell[0];
        int col = closestCell[1];
        int exRow = board.exitRow;
        int exCol = board.exitCol;
    
        // Vertical path
        if (col == exCol) {
            int start = Math.min(row, exRow);
            int end = Math.max(row, exRow);
            for (int i = start; i <= end; i++) {
                if (i >= 0 && i < board.rows && col >= 0 && col < board.cols) {
                    char ch = board.grid[i][col];
                    if (ch != '.' && ch != 'P' && ch != 'K') obstacles++;
                }
            }
        }
        // Horizontal path
        else if (row == exRow) {
            int start = Math.min(col, exCol);
            int end = Math.max(col, exCol);
            for (int j = start; j <= end; j++) {
                if (row >= 0 && row < board.rows && j >= 0 && j < board.cols) {
                    char ch = board.grid[row][j];
                    if (ch != '.' && ch != 'P' && ch != 'K') obstacles++;
                }
            }
        }
        // Diagonal/umum (cek kotak dari primary ke exit)
        else {
            int minRow = Math.min(row, exRow);
            int maxRow = Math.max(row, exRow);
            int minCol = Math.min(col, exCol);
            int maxCol = Math.max(col, exCol);
            for (int i = minRow; i <= maxRow; i++) {
                for (int j = minCol; j <= maxCol; j++) {
                    if (i >= 0 && i < board.rows && j >= 0 && j < board.cols) {
                        char ch = board.grid[i][j];
                        if (ch != '.' && ch != 'P' && ch != 'K') obstacles++;
                    }
                }
            }
        }
    
        return baseDistance + obstacles * 2;
    }
    
}