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

/**
 * Graphical user interface for the Sudoku BBCP solver.
 * <p>
 * This class builds a 9x9 editable grid using Swing, provides controls
 * for solving, clearing, and loading puzzles from CSV/TXT files, and
 * visualizes the solving process using a {@link SudokuSolver.StepListener}.
 */
public class SudokuGUI extends JFrame {

    /** Sudoku board size (9x9), taken from {@link SudokuSolver}. */
    private static final int SIZE = SudokuSolver.SIZE;

    /** 9x9 text fields representing the Sudoku grid. */
    private final JTextField[][] cells = new JTextField[SIZE][SIZE];

    /** Label at the bottom used to display status and performance metrics. */
    private final JLabel infoLabel = new JLabel(" ");

    /** Checkbox to enable or disable step-by-step visualization. */
    private final JCheckBox showStepsCheckBox = new JCheckBox("Show steps", true);

    /** Core BBCP solver instance. */
    private final SudokuSolver solver = new SudokuSolver();

    /** Flag indicating whether a solving process is currently running. */
    private volatile boolean solving = false;

    /** Flag indicating that the user has requested cancellation. */
    private volatile boolean cancelRequested = false;

    /**
     * Constructs the Sudoku GUI window, initializes layout and components.
     */
    public SudokuGUI() {
        setTitle("Sudoku Solver (BBCP)");
        setSize(600, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // Initialize board and control panel
        initBoardUI();

        // Configure info label at the bottom
        infoLabel.setOpaque(true);
        infoLabel.setBackground(new Color(250, 250, 250));
        infoLabel.setForeground(new Color(80, 80, 80));
        infoLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));

        initControlPanel();
    }

    /**
     * Initializes the 9x9 board UI, creating text fields and styling the grid.
     */
    private void initBoardUI() {
        JPanel gridPanel = new JPanel(new GridLayout(SIZE, SIZE));
        Font f = new Font("Arial", Font.BOLD, 20);

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                JTextField tf = new JTextField();
                tf.setHorizontalAlignment(JTextField.CENTER);
                tf.setFont(f);

                // Light background for alternating 3x3 subgrids for better readability
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

    /**
     * Initializes the control panel with Solve, Load, Clear buttons
     * and the "Show steps" checkbox.
     */
    private void initControlPanel() {
        JPanel btnPanel = new JPanel();
        JButton solveBtn = new JButton("Solve");
        JButton loadBtn = new JButton("Load CSV/TXT");
        JButton clearBtn = new JButton("Clear");

        btnPanel.add(solveBtn);
        btnPanel.add(loadBtn);
        btnPanel.add(clearBtn);
        btnPanel.add(showStepsCheckBox);

        // Run solver in a background thread to keep UI responsive
        solveBtn.addActionListener(e -> new Thread(this::solvePuzzle).start());
        loadBtn.addActionListener(e -> loadFromFile());
        clearBtn.addActionListener(e -> clearBoard());

        add(btnPanel, BorderLayout.NORTH);
    }

    /**
     * Main solving flow:
     * <ol>
     *     <li>Read the board from the UI</li>
     *     <li>Call the solver with a {@link SudokuSolver.StepListener}</li>
     *     <li>Display the result and performance metrics</li>
     * </ol>
     */
    private void solvePuzzle() {
        if (solving) {
            return;
        }
        solving = true;
        cancelRequested = false;

        int[][] board = readBoard();
        if (board == null) {
            // Invalid input already reported to user
            solving = false;
            return;
        }

        setInfoText("Solving...");

        long start = System.currentTimeMillis();
        boolean solved;

        // Listener for visualization and cancellation
        SudokuSolver.StepListener listener = createStepListener();

        try {
            solved = solver.solve(board, listener);
        } catch (IllegalArgumentException ex) {
            // Initial puzzle is invalid (e.g., conflicting givens)
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

    /**
     * Reads the Sudoku board from the UI text fields.
     *
     * @return a 9x9 int array representing the board (0 = empty),
     *         or {@code null} if the input is invalid.
     */
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
     * Creates a {@link SudokuSolver.StepListener} that:
     * <ul>
     *     <li>Updates the UI grid during solving when "Show steps" is enabled</li>
     *     <li>Checks for cancellation requests</li>
     * </ul>
     *
     * @return a {@link SudokuSolver.StepListener} instance used by the solver
     */
    private SudokuSolver.StepListener createStepListener() {
        return new SudokuSolver.StepListener() {
            @Override
            public void onStep(int row, int col, int value) {
                // Skip visualization when "Show steps" is unchecked
                if (!showStepsCheckBox.isSelected()) {
                    return;
                }
                if (cancelRequested || !solving) {
                    return;
                }

                // Update UI on the Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    if (cancelRequested || !solving) {
                        return;
                    }
                    cells[row][col].setText(value == 0 ? "" : String.valueOf(value));
                });

                // Small delay for smoother step-by-step animation
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

    /**
     * Fills the entire grid with values from the given board.
     *
     * @param b a 9x9 solved Sudoku board
     */
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
     * Clears the board, resets the status label, and requests
     * cancellation of any ongoing solving process.
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
     * Loads a Sudoku puzzle from a CSV or TXT file into the UI grid.
     * <p>
     * Expected format: 9 lines, each containing at least 9 tokens
     * separated by commas or whitespace. The value {@code 0} or an empty
     * token is interpreted as an empty cell.
     */
    private void loadFromFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

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

                // Determine separator type: comma or whitespace
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
                        // Treat empty or 0 as an empty cell
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
                            throw new RuntimeException(
                                    "Invalid number " + val + " at (" + (r + 1) + "," + (c + 1) +
                                            "), only 0 or 1-9 allowed.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            showError("File format error: " + e.getMessage());
        }
    }

    /**
     * Safely updates the info label text on the Swing Event Dispatch Thread.
     *
     * @param text the message to display in the status bar
     */
    private void setInfoText(String text) {
        SwingUtilities.invokeLater(() -> infoLabel.setText(text));
    }

    /**
     * Shows an error dialog on the Swing Event Dispatch Thread.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message));
    }

    /**
     * Application entry point. Sets the Look & Feel (Nimbus if available)
     * and launches the Sudoku GUI on the Event Dispatch Thread.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        try {
            // Try to use Nimbus L&F for a modern UI style
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        // Create and show GUI on the EDT
        SwingUtilities.invokeLater(() -> new SudokuGUI().setVisible(true));
    }

}
