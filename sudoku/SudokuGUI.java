package sudoku;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class SudokuGUI extends JFrame {

    // Use size from SudokuSolver
    private static final int SIZE = SudokuSolver.SIZE;

    // UI components
    private final JTextField[][] cells = new JTextField[SIZE][SIZE];
    private final JLabel infoLabel = new JLabel(" ");
    private final JCheckBox showStepsCheckBox = new JCheckBox("Show steps", true);

    // Solver and state
    private final SudokuSolver solver = new SudokuSolver();
    private volatile boolean solving = false;
    private volatile boolean cancelRequested = false;

    public SudokuGUI() {
        setTitle("Sudoku Solver (BBCP)");
        setSize(600, 700);
        setLocationRelativeTo(null); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false); 

        initBoardUI();
        infoLabel.setOpaque(true);
        infoLabel.setBackground(new Color(250, 250, 250));
        infoLabel.setForeground(new Color(80, 80, 80));
        infoLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));

        initControlPanel();
    }

    /** Initializes the 9x9 grid UI. */
    private void initBoardUI() {
        JPanel gridPanel = new JPanel(new GridLayout(SIZE, SIZE));
        Font f = new Font("Arial", Font.BOLD, 20);

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                JTextField tf = new JTextField();
                tf.setHorizontalAlignment(JTextField.CENTER);
                tf.setFont(f);

                // Light background for alternating 3x3 subgrids
                if ((r / 3 + c / 3) % 2 == 0) {
                    tf.setBackground(new Color(235, 235, 235));
                }

                cells[r][c] = tf;
                gridPanel.add(tf);
            }
        }

        add(gridPanel, BorderLayout.CENTER);
        add(infoLabel, BorderLayout.SOUTH);
    }

    /** Initializes buttons and checkbox on the top panel. */
    private void initControlPanel() {
        JPanel btnPanel = new JPanel();
        JButton solveBtn = new JButton("Solve");
        JButton loadBtn = new JButton("Load CSV/TXT");
        JButton clearBtn = new JButton("Clear");

        btnPanel.add(solveBtn);
        btnPanel.add(loadBtn);
        btnPanel.add(clearBtn);
        btnPanel.add(showStepsCheckBox);

        solveBtn.addActionListener(e -> new Thread(this::solvePuzzle).start());
        loadBtn.addActionListener(e -> loadFromFile());
        clearBtn.addActionListener(e -> clearBoard());

        add(btnPanel, BorderLayout.NORTH);
    }

    /** Main flow: read board -> call solver -> show result. */
    private void solvePuzzle() {
        if (solving)
            return;
        solving = true;
        cancelRequested = false;

        int[][] board = readBoard();
        if (board == null) {
            solving = false;
            return;
        }

        setInfoText("Solving...");

        long start = System.currentTimeMillis();
        boolean solved;

        SudokuSolver.StepListener listener = createStepListener();

        try {
            solved = solver.solve(board, listener);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            setInfoText("Initial puzzle is invalid.");
            solving = false;
            return;
        }

        long end = System.currentTimeMillis();

        if (cancelRequested) {
            setInfoText("Solving canceled.");
            solving = false;
            return;
        }

        if (solved) {
            fillBoard(board);
            setInfoText("Solved in " + (end - start) +
                    " ms | Visited nodes: " + solver.getNodesVisitedCount() +
                    " | Backtracks: " + solver.getBacktrackCount());
        } else {
            showError("Puzzle has no solution.");
            setInfoText("No solution found.");
        }

        solving = false;
    }

    /** Reads the board from the UI. Returns null if invalid input. */
    private int[][] readBoard() {
        int[][] board = new int[SIZE][SIZE];
        try {
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    String text = cells[r][c].getText().trim();
                    if (text.isEmpty()) {
                        board[r][c] = 0;
                    } else {
                        int val = Integer.parseInt(text);
                        if (val < 1 || val > 9) {
                            showError("Invalid number at (" + (r + 1) + "," + (c + 1) + "). Only 1-9 allowed.");
                            return null;
                        }
                        board[r][c] = val;
                    }
                }
            }
        } catch (NumberFormatException ex) {
            showError("Invalid input. Please enter digits 1-9 only.");
            return null;
        }
        return board;
    }

    /**
     * Creates a StepListener that updates the Swing grid and
     * checks for cancellation and "show steps" setting.
     */
    private SudokuSolver.StepListener createStepListener() {
        return new SudokuSolver.StepListener() {
            @Override
            public void onStep(int row, int col, int value) {
                if (!showStepsCheckBox.isSelected())
                    return;
                if (cancelRequested || !solving)
                    return;

                SwingUtilities.invokeLater(() -> {
                    if (cancelRequested || !solving)
                        return;
                    cells[row][col].setText(value == 0 ? "" : String.valueOf(value));
                });

                // Small delay on the solving thread for visualization
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ignored) {
                }
            }

            @Override
            public boolean isCancelled() {
                return cancelRequested || !solving;
            }
        };
    }

    /** Fills the entire UI grid with the solved board. */
    private void fillBoard(int[][] b) {
        SwingUtilities.invokeLater(() -> {
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    cells[r][c].setText(String.valueOf(b[r][c]));
                }
            }
        });
    }

    /**
     * Clears the board and resets state.
     * Also requests cancellation of any ongoing solving.
     */
    private void clearBoard() {
        cancelRequested = true;
        solving = false;

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                cells[r][c].setText("");
            }
        }
        setInfoText(" ");
    }

    /**
     * * Loads a Sudoku puzzle from a CSV/TXT file. * Format: 9 lines, each with 9
     * numbers separated by commas or whitespace characters. * 0 or whitespace
     * characters means empty cell.
     */
    private void loadFromFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        File file = chooser.getSelectedFile();
        try (Scanner sc = new Scanner(file)) {
            for (int r = 0; r < SIZE; r++) {
                if (!sc.hasNextLine()) {
                    throw new RuntimeException("Not enough lines in file (expected 9).");
                }
                String line = sc.nextLine();
                if (line == null) {
                    throw new RuntimeException("Unexpected end of file at row " + (r + 1));
                }
                line = line.trim();
                if (line.isEmpty()) {
                    throw new RuntimeException("Empty line at row " + (r + 1));
                }

                String[] row;
                if (line.contains(",")) {
                    row = line.split(",", -1);
                } else {
                    row = line.split("\\s+");
                }

                if (row.length < SIZE) {
                    throw new RuntimeException("Not enough columns in line " + (r + 1));
                }

                for (int c = 0; c < SIZE; c++) {
                    String token = row[c].trim();

                    if (token.isEmpty() || "0".equals(token)) {
                        cells[r][c].setText("");
                    } else {
                        int val;
                        try {
                            val = Integer.parseInt(token);
                        } catch (NumberFormatException ex) {
                            throw new RuntimeException(
                                    "Invalid token \"" + token + "\" at (" + (r + 1) + "," + (c + 1) + ")");
                        }

                        if (val >= 1 && val <= 9) {
                            cells[r][c].setText(String.valueOf(val));
                        } else {
                            throw new RuntimeException("Invalid number " + val + " at (" + (r + 1) + "," + (c + 1) +
                                    "), only 0 or 1-9 allowed.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            showError("File format error: " + e.getMessage());
        }
    }

    /** Safely updates the info label text on the EDT. */
    private void setInfoText(String text) {
        SwingUtilities.invokeLater(() -> infoLabel.setText(text));
    }

    /** Shows an error dialog on the EDT. */
    private void showError(String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message));
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new SudokuGUI().setVisible(true));
    }

}
