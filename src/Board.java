import java.io.*;
import java.util.*;

public class Board {
    public int rows, cols;
    public char[][] grid;
    public List<Piece> pieces;
    public Piece primaryPiece;
    public int exitRow = -1, exitCol = -1;

    public Board(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));

        String[] size = br.readLine().trim().split(" ");
        rows = Integer.parseInt(size[0]);
        cols = Integer.parseInt(size[1]);

        int nPiece = Integer.parseInt(br.readLine().trim()); // not used yet

        grid = new char[rows][cols];
        pieces = new ArrayList<>();
        Map<Character, List<int[]>> tempPieces = new HashMap<>();

        for (int i = 0; i < rows; i++) {
            String line = br.readLine();
            for (int j = 0; j < cols; j++) {
                if (j >= line.length()) {
                    grid[i][j] = '.'; // Jika tidak ada karakter, dianggap kosong
                    continue;
                }
                char c = line.charAt(j);
                grid[i][j] = c;

                if (c != '.' && c != 'K') {
                    tempPieces.putIfAbsent(c, new ArrayList<>());
                    tempPieces.get(c).add(new int[]{i, j});
                }
                if (c == 'P') {
                    if (primaryPiece == null) primaryPiece = new Piece(c);
                    primaryPiece.addCell(i, j);
                } else if (c == 'K') {
                    exitRow = i;
                    exitCol = j;
                }
            }
        }

        for (Map.Entry<Character, List<int[]>> entry : tempPieces.entrySet()) {
            if (entry.getKey() != 'P') {
                Piece p = new Piece(entry.getKey());
                for (int[] cell : entry.getValue()) {
                    p.addCell(cell[0], cell[1]);
                }
                pieces.add(p);
            }
        }
        pieces.add(primaryPiece);
        br.close();
    }

    public void print() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(grid[i][j]);
            }
            System.out.println();
        }
        System.out.println("Primary piece (P) at: " + Arrays.deepToString(primaryPiece.cells.toArray()));
        System.out.println("Exit at: (" + exitRow + ", " + exitCol + ")");
    }

    public Board clone() {
        Board copy = new Board();
        copy.rows = this.rows;
        copy.cols = this.cols;
        copy.grid = new char[rows][cols];
        for (int i = 0; i < rows; i++)
            copy.grid[i] = Arrays.copyOf(this.grid[i], cols);

        copy.pieces = new ArrayList<>();
        for (Piece p : this.pieces) {
            copy.pieces.add(p.clone());
            if (p.name == 'P') copy.primaryPiece = copy.pieces.get(copy.pieces.size() - 1);
        }

        copy.exitRow = this.exitRow;
        copy.exitCol = this.exitCol;
        return copy;
    }

    private Board() {} // for cloning
}