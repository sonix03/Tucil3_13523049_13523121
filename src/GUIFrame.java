import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import javax.swing.*;

public class GUIFrame extends JFrame {
    private JComboBox<String> algoCombo;
    private JComboBox<String> heuristicCombo;
    private JLabel statusLabel;
    private BoardGUI boardPanel;
    private File selectedFile;
    private Board currentBoard; // Papan asli yang dimuat dari file
    private boolean isRunning = false;
    private AnimationManager animationManager;
    private JSlider speedSlider;
    private Solver solver;

    public GUIFrame() {
        super("Rush Hour Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700); // Ukuran frame yang lebih representatif
        setLayout(new BorderLayout());

        initTopPanel();
        initBoardPanel();
        initBottomPanel();

        // Atur status awal heuristicCombo berdasarkan pilihan algo
        int initialIdx = algoCombo.getSelectedIndex();
        heuristicCombo.setEnabled(initialIdx != 1); // UCS tidak pakai heuristik

        setVisible(true);
    }

    private void initTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout());

        JButton fileButton = new JButton("Pilih File");
        algoCombo = new JComboBox<>(new String[]{"Greedy Best First Search", "Uniform Cost Search", "A*", "IDA*"});
        heuristicCombo = new JComboBox<>(new String[]{"Manhattan", "Euclidean", "Obstacle-aware"}); // Asumsi nama heuristik
        JButton runButton = new JButton("Jalankan");

        // Heuristik dinonaktifkan jika algoritma tidak membutuhkannya (misal UCS)
        heuristicCombo.setEnabled(false); // Default, akan diupdate oleh listener algoCombo

        algoCombo.addActionListener(e -> {
            int idx = algoCombo.getSelectedIndex();
            // 0: GBFS (needs H), 1: UCS (no H), 2: A* (needs H), 3: IDA* (needs H)
            heuristicCombo.setEnabled(idx != 1); // Nonaktifkan heuristik untuk UCS
        });

        fileButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser("test/input/"); // Set direktori awal
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFile = fc.getSelectedFile();
                try {
                    currentBoard = new Board(selectedFile); // Muat board asli
                    boardPanel.setBoard(currentBoard);    // Tampilkan board asli di GUI
                    statusLabel.setText("File dimuat: " + selectedFile.getName());
                    if (animationManager != null) { // Hentikan animasi sebelumnya jika ada
                        animationManager.stop();
                        animationManager = null;
                        isRunning = false;
                        runButton.setText("Jalankan");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal membaca file:\n" + ex.getMessage(), "Error File", JOptionPane.ERROR_MESSAGE);
                    selectedFile = null;
                    currentBoard = null;
                    boardPanel.setBoard(null); // Kosongkan board di GUI
                    statusLabel.setText("Gagal memuat file. Pilih file lain.");
                }
            }
        });

        runButton.addActionListener(e -> {
            if (isRunning) { // Jika sedang berjalan, tombol berfungsi sebagai "Hentikan"
                if (animationManager != null) {
                    animationManager.stop(); // Hentikan animasi
                }
                boardPanel.setBoard(currentBoard); // Kembalikan ke board awal yang dimuat
                runButton.setText("Jalankan");
                isRunning = false;
                statusLabel.setText("Dihentikan. Board dikembalikan ke kondisi awal.");
                return;
            }

            if (currentBoard == null) {
                JOptionPane.showMessageDialog(this, "Pilih file puzzle terlebih dahulu.", "File Belum Dipilih", JOptionPane.WARNING_MESSAGE);
                return;
            }

            runButton.setText("Hentikan"); // Ubah teks tombol menjadi "Hentikan"
            isRunning = true;
            statusLabel.setText("Menjalankan solver...");

            int algoIdx = algoCombo.getSelectedIndex();
            int heurIdx = heuristicCombo.getSelectedIndex(); // Indeks untuk Heuristic.java (0: Manhattan, dst)

            solver = switch (algoIdx) {
                case 0 -> new GBFS(heurIdx);
                case 1 -> new UCS(); // UCS tidak pakai heuristik, jadi heurIdx diabaikan
                case 2 -> new AStar(heurIdx);
                case 3 -> new IDAStar(heurIdx);
                default -> null;
            };

            if (solver == null) {
                JOptionPane.showMessageDialog(this, "Pilihan solver tidak valid.", "Error Solver", JOptionPane.ERROR_MESSAGE);
                runButton.setText("Jalankan");
                isRunning = false;
                statusLabel.setText("Gagal memulai solver.");
                return;
            }

            Board boardToSolve = currentBoard.clone(); // Selalu clone board asli untuk solver

            // Jalankan solver di thread terpisah agar GUI tidak freeze
            new SwingWorker<List<Board>, Void>() {
                long startTime;
                List<Board> pathResult = null;

                @Override
                protected List<Board> doInBackground() throws Exception {
                    startTime = System.currentTimeMillis();
                    pathResult = solver.solveAndReturnPath(boardToSolve);
                    return pathResult;
                }

                @Override
                protected void done() {
                    try {
                        List<Board> path = get(); // Dapatkan hasil dari doInBackground
                        long endTime = System.currentTimeMillis();

                        if (path == null || path.isEmpty()) {
                            JOptionPane.showMessageDialog(GUIFrame.this, "Tidak ditemukan solusi.", "Hasil Solver", JOptionPane.INFORMATION_MESSAGE);
                            statusLabel.setText("Solusi tidak ditemukan. Waktu: " + (endTime - startTime) + " ms. Node: " + solver.getNodesExplored());
                        } else {
                            statusLabel.setText("Selesai dalam " + (endTime - startTime) + " ms | Langkah (ringkas): " + solver.getLastSummarizedStepCount() + " | Node dieksplorasi: " + solver.getNodesExplored());
                            animationManager = new AnimationManager(path, boardPanel, speedSlider, () -> {
                                runButton.setText("Jalankan"); // Reset tombol setelah animasi selesai
                                isRunning = false;
                                showSavePrompt(); // Tawarkan simpan setelah animasi
                            });
                            animationManager.start();
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(GUIFrame.this, "Error saat menjalankan solver:\n" + ex.getMessage(), "Error Solver", JOptionPane.ERROR_MESSAGE);
                        statusLabel.setText("Error: " + ex.getMessage());
                        ex.printStackTrace(); // Untuk debugging
                    } finally {
                        // Jika animasi tidak dimulai (misal tidak ada solusi), reset tombol di sini
                        if (animationManager == null || (pathResult != null && pathResult.isEmpty())) {
                             runButton.setText("Jalankan");
                             isRunning = false;
                             if (pathResult != null && pathResult.isEmpty()) { // Jika tidak ada solusi tapi solver selesai
                                 showSavePrompt(); // Tetap tawarkan simpan (akan menyimpan info "tidak ada solusi")
                             }
                        }
                    }
                }
            }.execute();
        });

        topPanel.add(fileButton);
        topPanel.add(new JLabel("Algoritma:"));
        topPanel.add(algoCombo);
        topPanel.add(new JLabel("Heuristik:"));
        topPanel.add(heuristicCombo);
        topPanel.add(runButton);

        add(topPanel, BorderLayout.NORTH);
    }

    private void initBoardPanel() {
        boardPanel = new BoardGUI(); // Asumsi BoardGUI bisa handle null board
        add(boardPanel, BorderLayout.CENTER);
    }


    private void initBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout()); // Mengganti nama variabel agar tidak konflik

        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        speedSlider = new JSlider(100, 1000, 500); // Min, Max, Default delay (ms)
        speedSlider.setInverted(true); // Slider ke kanan = lebih cepat (delay lebih kecil)
        speedSlider.setMajorTickSpacing(300);
        speedSlider.setMinorTickSpacing(100);
        // speedSlider.setPaintTicks(true); // Opsional: tampilkan tick marks
        // speedSlider.setPaintLabels(true); // Opsional: tampilkan label pada major ticks

        JLabel speedLabel = new JLabel("Kecepatan Animasi:"); // Label yang lebih deskriptif
        speedPanel.add(speedLabel);
        speedPanel.add(speedSlider);

        statusLabel = new JLabel("Pilih file puzzle untuk memulai.");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding

        bottomPanel.add(speedPanel, BorderLayout.NORTH); // Pindahkan speed panel ke atas status label
        bottomPanel.add(statusLabel, BorderLayout.CENTER); // Status label di tengah

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void showSavePrompt() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Apakah Anda ingin menyimpan detail langkah solusi ke file teks?",
            "Simpan Solusi",
            JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION && selectedFile != null) {
            try {
                String originalFileName = selectedFile.getName();
                String outputFileNameBase = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
                
                String outputPath = "test/output/" + outputFileNameBase + ".txt";

                File outDir = new File("test/output");
                if (!outDir.exists()) outDir.mkdirs();

                try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
                    writer.println("Solusi disimpan dari file input: " + originalFileName);
                    writer.println("Algoritma: " + algoCombo.getSelectedItem().toString());
                    if (heuristicCombo.isEnabled()) {
                        writer.println("Heuristik: " + heuristicCombo.getSelectedItem().toString());
                    }

                    if (animationManager == null || animationManager.getSteps() == null || animationManager.getSteps().isEmpty()) {
                        writer.println("Tidak ditemukan solusi atau tidak ada langkah untuk disimpan.");
                    } else {
                        writer.println("------------------------------------------");
                        int stepNum = 0;
                        for (Board b : animationManager.getSteps()) {
                            if (stepNum == 0) {
                                writer.println("Initial Board:");
                            } else {
                                writer.println("Board setelah Langkah " + stepNum + ":");
                            }
                            
                            Board currentBoardState = b;

                            // Cetak 'K' untuk tepi atas jika ada
                            if (currentBoardState.exitRow == -1) {
                                for (int j = 0; j < currentBoardState.cols; j++) {
                                    writer.print(j == currentBoardState.exitCol ? 'K' : '.');
                                    if (j < currentBoardState.cols - 1) {
                                        writer.print(" ");
                                    }
                                }
                                writer.println();
                            }

                            for (int r = 0; r < currentBoardState.rows; r++) {
                                
                                if (currentBoardState.exitCol == -1 && currentBoardState.exitRow == r) {
                                    writer.print('K');
                                    if (currentBoardState.cols > 0) writer.print(" ");
                                } else if (currentBoardState.exitCol == -1) {
                                    writer.print(" "); 
                                    if (currentBoardState.cols > 0) writer.print(" ");
                                }

                            
                                for (int c = 0; c < currentBoardState.cols; c++) {
                                    writer.print(currentBoardState.grid[r][c]);
                                    if (c < currentBoardState.cols - 1) {
                                        writer.print(" ");
                                    }
                                }

                                
                                if (currentBoardState.exitCol == currentBoardState.cols && currentBoardState.exitRow == r) {
                                    if (currentBoardState.cols > 0) writer.print(" ");
                                    writer.print('K');
                                }
                                writer.println();
                            }

                            
                            if (currentBoardState.exitRow == currentBoardState.rows) {
                                if (currentBoardState.exitCol != -1) { 
                                     for (int j = 0; j < currentBoardState.cols; j++) {
                                        writer.print(j == currentBoardState.exitCol ? 'K' : '.');
                                        if (j < currentBoardState.cols - 1) {
                                            writer.print(" ");
                                        }
                                    }
                                    writer.println();
                                }
                            }
                            writer.println("------------------------------------------");
                            stepNum++;
                        }
                        writer.println("Langkah (ringkas): " + solver.getLastSummarizedStepCount() + " | Node dieksplorasi: " + solver.getNodesExplored());
                    }
                    
                } // PrintWriter akan otomatis close di sini
                JOptionPane.showMessageDialog(this, "Detail langkah solusi disimpan ke:\n" + outputPath, "Simpan Berhasil", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan detail langkah solusi:\n" + ex.getMessage(), "Error Simpan", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}