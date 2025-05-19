import java.awt.*;
import java.io.File;
import java.util.List;
import javax.swing.*;

public class GUIFrame extends JFrame {
    private JComboBox<String> algoCombo;
    private JComboBox<String> heuristicCombo;
    private JLabel statusLabel;
    private BoardGUI boardPanel;
    private File selectedFile;
    private Board currentBoard;
    private boolean isRunning = false;
    private AnimationManager animationManager;

    public GUIFrame() {
        super("Rush Hour Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLayout(new BorderLayout());

        initTopPanel();
        initBoardPanel();
        initBottomPanel();

        int initialIdx = algoCombo.getSelectedIndex();
        heuristicCombo.setEnabled(initialIdx != 1);

        setVisible(true);
    }

    private void initTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout());

        JButton fileButton = new JButton("Pilih File");
        algoCombo = new JComboBox<>(new String[]{"Greedy Best First Search", "Uniform Cost Search", "A*", "IDA*"});
        heuristicCombo = new JComboBox<>(new String[]{"Manhattan", "Euclidean", "Obstacle-aware"});
        JButton runButton = new JButton("Jalankan");

        heuristicCombo.setEnabled(false);

        algoCombo.addActionListener(e -> {
            int idx = algoCombo.getSelectedIndex();
            heuristicCombo.setEnabled(idx != 1); 
        });

        fileButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser("test/input/");
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFile = fc.getSelectedFile();
                try {
                    currentBoard = new Board(selectedFile);
                    boardPanel.setBoard(currentBoard);
                    statusLabel.setText("File dimuat: " + selectedFile.getName());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal membaca file:\n" + ex.getMessage());
                }
            }
        });

        runButton.addActionListener(e -> {
            if (isRunning) {
                if (animationManager != null) {
                    animationManager.stop(); 
                }
                boardPanel.setBoard(currentBoard); 
                runButton.setText("Jalankan");
                isRunning = false;
                statusLabel.setText("Dihentikan. Board dikembalikan.");
                return;
            }
        
            // Jika belum ada board
            if (currentBoard == null) {
                JOptionPane.showMessageDialog(this, "Pilih file dulu.");
                return;
            }
        
            // Jalankan solver
            runButton.setText("Hentikan");
            isRunning = true;
        
            int algoIdx = algoCombo.getSelectedIndex();
            int heurIdx = heuristicCombo.getSelectedIndex();
        
            Solver solver = switch (algoIdx) {
                case 0 -> new GBFS(heurIdx);
                case 1 -> new UCS();
                case 2 -> new AStar(heurIdx);
                case 3 -> new IDAStar(heurIdx);
                default -> null;
            };
        
            if (solver == null) {
                JOptionPane.showMessageDialog(this, "Solver belum tersedia.");
                runButton.setText("Jalankan");
                isRunning = false;
                return;
            }
        
            Board boardCopy = currentBoard.clone();
            long start = System.currentTimeMillis();
            List<Board> path = solver.solveAndReturnPath(boardCopy);
            long end = System.currentTimeMillis();
        
            if (path == null || path.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ditemukan solusi.");
                statusLabel.setText("Solusi tidak ditemukan.");
                runButton.setText("Jalankan");
                isRunning = false;
                return;
            }
        
            statusLabel.setText("Selesai dalam " + (end - start) + " ms | langkah: " + solver.getLastSummarizedStepCount() + "| Node yang dieksplorasi: " + solver.getNodesExplored());
            animationManager = new AnimationManager(path, boardPanel, () -> {
                // Callback setelah animasi selesai
                runButton.setText("Jalankan");
                isRunning = false;
            });
            animationManager.start();
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
        boardPanel = new BoardGUI();
        add(boardPanel, BorderLayout.CENTER);
    }

    private void initBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Pilih file untuk mulai.");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bottom.add(statusLabel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }
}
