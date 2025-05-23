import java.io.File;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner; 

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Input 
            
            System.out.print("Masukkan nama file (test/input/): ");
            String filePath = "test/input" + scanner.nextLine();
            

            if (args.length > 0) {
                filePath = args[0];
            }

            File file = new File(filePath);
            Board board = new Board(file); 

            System.out.println("Board awal:");
            board.print();
            System.out.println();

            int algoChoice;
            
            while (true) {
                System.out.println("Pilih algoritma:");
                System.out.println("1. Greedy Best First Search (GBFS)");
                System.out.println("2. Uniform Cost Search (UCS)");
                System.out.println("3. A* Search");
                System.out.println("4. IDA* Search");
                System.out.print("Pilihan Anda (1-4): ");
                try {
                    algoChoice = scanner.nextInt();
                    if (algoChoice >= 1 && algoChoice <= 4) {
                        break; 
                    } else {
                        System.out.println("Pilihan tidak valid. Masukkan angka antara 1 dan 4.");
                    }
                } 
                
                catch (InputMismatchException e) {
                    System.out.println("Input tidak valid. Harap masukkan angka.");
                    scanner.next(); 
                }
                System.out.println(); 
            }

            Solver solver = null;
            int heuristicChoice = -1; 
            String algorithmName = ""; 

            if (algoChoice == 2) {
                
                algorithmName = "Uniform Cost Search (UCS)";
                solver = new UCS();
            } 
            
            else {
                
                while (true) {
                    System.out.println("\nPilih heuristik:");
                    System.out.println("1. Manhattan Distance");
                    System.out.println("2. Euclidean Distance");
                    System.out.println("3. Obstacle-aware Distance");
                    System.out.print("Pilihan Anda (1-3): ");
                    try {
                        heuristicChoice = scanner.nextInt();
                        if (heuristicChoice >= 1 && heuristicChoice <= 3) {
                            break;
                        } else {
                            System.out.println("Pilihan tidak valid. Masukkan angka antara 1 dan 3.");
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Input tidak valid. Harap masukkan angka.");
                        scanner.next(); 
                    }
                    System.out.println(); 
                }

                int heuristicIndex = heuristicChoice - 1;

                switch (algoChoice) {
                    case 1:
                        algorithmName = "Greedy Best First Search (GBFS)";
                        solver = new GBFS(heuristicIndex);
                        break;
                    case 3:
                        algorithmName = "A* Search";
                        solver = new AStar(heuristicIndex);
                        break;
                    case 4:
                        algorithmName = "IDA* Search";
                        solver = new IDAStar(heuristicIndex);
                        break;
                }
            }

            
            if (solver == null) {
                System.err.println("Error: Solver tidak terinisialisasi dengan benar. Program akan berhenti.");
            
                return; 
            }

            
            
            System.out.println("\nMencari solusi...");
            
            long startTime = System.currentTimeMillis();
            solver.solve(board); 
            long endTime = System.currentTimeMillis();
            
            System.out.println("Waktu eksekusi: " + (endTime - startTime) + " ms");
            System.out.println("Algoritma: " + algorithmName); 
        } 

        catch (IOException e) {
            System.err.println("Error membaca file: " + e.getMessage());
        } 

        catch (Exception e) { 
            System.err.println("error: " + e.getMessage());
            e.printStackTrace();
        } 

        finally {
            if (scanner != null) {
                scanner.close(); //tutup scanner
            }
        }
    }
}
