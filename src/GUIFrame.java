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

    public GUIFrame() {
        super("Rush Hour Solver GUI");
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
            JFileChooser fc = new JFileChooser("test/");
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
            if (currentBoard == null) {
                JOptionPane.showMessageDialog(this, "Pilih file dulu.");
                return;
            }
            runSolver();
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
        bottom.add(statusLabel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void runSolver() {
        int algoIdx = algoCombo.getSelectedIndex();
        int heurIdx = heuristicCombo.getSelectedIndex();

        Solver solver = null;
        String algoName = "";

        switch (algoIdx) {
            case 0 -> {
                solver = new GBFS(heurIdx);
                algoName = "Greedy BFS";
            }
            case 1 -> {
                solver = new UCS();
                algoName = "UCS";
            }
            case 2 -> {
                solver = new AStar(heurIdx);
                algoName = "A*";
            }
            case 3 -> {
                solver = new IDAStar(heurIdx);
                algoName = "IDA*";
            }
        }

        if (solver == null) {
            JOptionPane.showMessageDialog(this, "Solver belum tersedia.");
            return;
        }

        Board boardCopy = currentBoard.clone();

        long start = System.currentTimeMillis();
        List<Board> path = solver.solveAndReturnPath(boardCopy);
        long end = System.currentTimeMillis();

        if (path == null || path.size() <= 1) {
            JOptionPane.showMessageDialog(this, "Tidak ditemukan solusi.");
            statusLabel.setText("Solusi tidak ditemukan.");
            return;
        }
        
        statusLabel.setText(algoName + " selesai dalam " + (end - start) + " ms, langkah: " + (path.size() - 1));
        new AnimationManager(path, boardPanel).start();
        
    }
}
