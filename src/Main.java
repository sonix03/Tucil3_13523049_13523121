import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.InputMismatchException; // Import untuk menangani input yang bukan integer

/**
 * Main class untuk menjalankan sliding block puzzle solver
 * dengan pilihan algoritma dan heuristik
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Pindahkan deklarasi scanner ke luar try-catch agar bisa di-close di finally

        try {
            // Input nama file dari user
            // System.out.print("Masukkan nama file input (contoh: input1.txt): ");
            // String filename = scanner.nextLine();

            String filePath = "test/input5.txt"; // Default file path

            if (args.length > 0) {
                filePath = args[0];
            }

            File file = new File(filePath);
            Board board = new Board(file); // Asumsikan constructor Board(File) melempar IOException jika file tidak ada/salah format

            System.out.println("Board awal:");
            board.print();
            System.out.println();

            int algoChoice;
            // Loop untuk validasi input algoritma
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
                        break; // Keluar dari loop jika input valid
                    } else {
                        System.out.println("Pilihan tidak valid. Masukkan angka antara 1 dan 4.");
                    }
                } 
                
                catch (InputMismatchException e) {
                    System.out.println("Input tidak valid. Harap masukkan angka.");
                    scanner.next(); // Bersihkan input yang salah dari scanner
                }
                System.out.println(); // Beri jarak untuk pembacaan yang lebih baik
            }

            Solver solver = null;
            int heuristicChoice = -1; // Inisialisasi dengan nilai default/tidak valid
            String algorithmName = ""; // Variabel untuk menyimpan nama algoritma

            if (algoChoice == 2) {
                // UCS tidak butuh heuristik
                algorithmName = "Uniform Cost Search (UCS)";
                solver = new UCS();
            } 
            
            else {
                // Algoritma GBFS, A*, atau IDA* membutuhkan heuristik
                // Loop untuk validasi input heuristik
                while (true) {
                    System.out.println("\nPilih heuristik:");
                    System.out.println("1. Manhattan Distance");
                    System.out.println("2. Euclidean Distance");
                    System.out.println("3. Obstacle-aware Distance");
                    System.out.print("Pilihan Anda (1-3): ");
                    try {
                        heuristicChoice = scanner.nextInt();
                        if (heuristicChoice >= 1 && heuristicChoice <= 3) {
                            break; // Keluar dari loop jika input valid
                        } else {
                            System.out.println("Pilihan tidak valid. Masukkan angka antara 1 dan 3.");
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Input tidak valid. Harap masukkan angka.");
                        scanner.next(); // Bersihkan input yang salah dari scanner
                    }
                    System.out.println(); // Beri jarak
                }

                // Menggunakan heuristicChoice - 1 karena biasanya array/list dimulai dari indeks 0
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

            // Pastikan solver terinisialisasi
            if (solver == null) {
                System.err.println("Error: Solver tidak terinisialisasi dengan benar. Program akan berhenti.");
                // scanner.close(); // Tutup scanner sebelum return jika terjadi error di sini
                return; // Keluar dari program
            }

            // Menampilkan nama algoritma yang dipilih sebelum memulai pencarian
            
            System.out.println("\nMencari solusi...");
            // Hitung waktu eksekusi
            long startTime = System.currentTimeMillis();
            solver.solve(board); // Asumsikan method solve ada di interface/kelas Solver
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
