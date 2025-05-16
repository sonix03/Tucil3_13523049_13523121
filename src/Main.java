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
            System.out.println("3. A* Search dengan Manhattan Distance");
            System.out.println("4. A* Search dengan Euclidean Distance");
            System.out.println("5. IDA* dengan Manhattan Distance");
            System.out.println("6. IDA* dengan Euclidean Distance"); 
            System.out.println("7. IDA* dengan Obstacle-aware Distance");
            
            System.out.print("Pilihan Anda (1-7): ");
            int choice = scanner.nextInt();
            
            Solver solver;
            
            switch (choice) {
                case 1:
                    solver = new GBFS();
                    break;
                case 2:
                    solver = new UCS();
                    break;
                case 3:
                    solver = new AStar(0); // Manhattan Distance
                    break;
                case 4:
                    solver = new AStar(1); // Euclidean Distance
                    break;
                case 5:
                    solver = new IDAStar(0); // Manhattan Distance
                    break;
                case 6:
                    solver = new IDAStar(1); // Euclidean Distance
                    break;
                case 7:
                    solver = new IDAStar(2); // Obstacle-aware Distance
                    break;
                default:
                    System.out.println("Pilihan tidak valid. Menggunakan GBFS sebagai default.");
                    solver = new GBFS();
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