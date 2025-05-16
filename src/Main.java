import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Main class untuk menjalankan sliding block puzzle solver
 * dengan pilihan algoritma dan heuristik
 */
public class Main {
    public static void main(String[] args) {
        try {
            // Default file path
            String filePath = "test/input1.txt";

            if (args.length > 0) {
                filePath = args[0];
            }

            File file = new File(filePath);
            Board board = new Board(file);

            System.out.println("Board awal:");
            board.print();
            System.out.println();

            Scanner scanner = new Scanner(System.in);

            System.out.println("Pilih algoritma:");
            System.out.println("1. Greedy Best First Search (GBFS)");
            System.out.println("2. Uniform Cost Search (UCS)");
            System.out.println("3. A* Search");
            System.out.println("4. IDA* Search");

            System.out.print("Pilihan Anda (1-4): ");
            int algoChoice = scanner.nextInt();

            Solver solver = null;
            int heuristic = -1;

            if (algoChoice == 2) {
                // UCS tidak butuh heuristik
                solver = new UCS();
            } else if (algoChoice >= 1 && algoChoice <= 4) {
                // Tampilkan pilihan heuristik
                System.out.println("Pilih heuristik:");
                System.out.println("1. Manhattan Distance");
                System.out.println("2. Euclidean Distance");
                System.out.println("3. Obstacle-aware Distance");
                System.out.print("Pilihan Anda (0-2): ");
                heuristic = scanner.nextInt();

                switch (algoChoice) {
                    case 1:
                        solver = new GBFS(heuristic-1);
                        break;
                    case 3:
                        solver = new AStar(heuristic-1);
                        break;
                    case 4:
                        solver = new IDAStar(heuristic-1);
                        break;
                }
            } else {
                System.out.println("Pilihan algoritma tidak valid. Menggunakan GBFS dengan Manhattan Distance sebagai default.");
                solver = new GBFS(0);
            }

            // Hitung waktu eksekusi
            long startTime = System.currentTimeMillis();
            solver.solve(board);
            long endTime = System.currentTimeMillis();

            System.out.println("Waktu eksekusi: " + (endTime - startTime) + " ms");

            scanner.close();

        } catch (IOException e) {
            System.err.println("Error membaca file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
