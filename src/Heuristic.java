/**
 * Class untuk menghitung berbagai fungsi heuristik
 * yang digunakan dalam algoritma pencarian
 */
public class Heuristic {
    // Tipe heuristik
    public static final int MANHATTAN = 0;
    public static final int EUCLIDEAN = 1;
    public static final int OBSTACLE_AWARE = 2;
    
    /**
     * Menghitung nilai heuristik sesuai dengan tipe yang dipilih
     * 
     * @param board Board saat ini
     * @param type Tipe heuristik: 0=Manhattan, 1=Euclidean, 2=Obstacle-aware
     * @return Nilai heuristik
     */
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
    
    /**
     * @param type Tipe heuristik
     * @return Nama heuristik
     */
    public static String getName(int type) {
        return switch (type) {
            case MANHATTAN -> "Manhattan Distance";
            case EUCLIDEAN -> "Euclidean Distance";
            case OBSTACLE_AWARE -> "Obstacle-aware Distance";
            default -> "Unknown";
        };
    }
    
    /**
     * Menghitung jarak Manhattan dari primary piece ke exit
     */
    private static int calculateManhattan(Board board) {
        int minDistance = Integer.MAX_VALUE;
        
        for (int[] cell : board.primaryPiece.cells) {
            int distance = Math.abs(cell[0] - board.exitRow) + Math.abs(cell[1] - board.exitCol);
            minDistance = Math.min(minDistance, distance);
        }
        
        return minDistance;
    }
    
    /**
     * Menghitung jarak Euclidean dari primary piece ke exit
     */
    private static int calculateEuclidean(Board board) {
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
    
    /**
     * Menghitung jarak dengan mempertimbangkan rintangan
     */
    private static int calculateObstacleAware(Board board) {
        // Hitung jarak Manhattan dan tambahkan penalti untuk setiap bidak lain yang menghalangi
        int baseDistance = calculateManhattan(board);
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
}