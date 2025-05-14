import java.util.Scanner;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            Board board = new Board(new File("test/input2.txt"));
            System.out.println("Papan Awal");
            board.print();
            System.out.println();

            GBFS solver = new GBFS();
            solver.solve(board);

        } catch (Exception e) {
            System.err.println("Terjadi kesalahan: " + e.getMessage());
        }

        scanner.close();
    }
}