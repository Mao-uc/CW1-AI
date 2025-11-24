package sudoku;

public class SudokuSolver {

    public static final int SIZE = 9;
    private static final int SUBGRID_SIZE = 3;

    private long backtrackCount = 0;
    private long nodesVisitedCount = 0;

    /** Listener used to visualize solving steps or support cancellation. */
    public interface StepListener {
        /**
         * Called when a number is placed or removed.
         * @param row row index (0-based)
         * @param col column index (0-based)
         * @param value value placed (1-9) or 0 when clearing the cell
         */
        void onStep(int row, int col, int value);

        /**
         * Returns true if the solving process should be cancelled.
         */
        boolean isCancelled();
    }

    /** Returns the number of backtracks performed in the last solve. */
    public long getBacktrackCount() {
        return backtrackCount;
    }

    /** Returns the number of nodes visited during the last solve. */
    public long getNodesVisitedCount() {
        return nodesVisitedCount;
    }

    /**
     * Solves the given Sudoku board in-place.
     *
     * @param board     9x9 board, 0 for empty cells, 1-9 for givens.
     * @param listener  optional step listener (can be null) for visualization and cancellation.
     * @return true if a solution was found, false otherwise.
     * @throws IllegalArgumentException if the initial board is invalid.
     */
    public boolean solve(int[][] board, StepListener listener) {
        if (board == null || board.length != SIZE || board[0].length != SIZE) {
            throw new IllegalArgumentException("Board must be 9x9.");
        }

        boolean[][] rowUsed = new boolean[SIZE][SIZE + 1];
        boolean[][] colUsed = new boolean[SIZE][SIZE + 1];
        boolean[][] boxUsed = new boolean[SIZE][SIZE + 1];

        if (!initConstraintsAndValidate(board, rowUsed, colUsed, boxUsed)) {
            throw new IllegalArgumentException("Initial puzzle is invalid (conflicting givens).");
        }

        backtrackCount = 0;
        nodesVisitedCount = 0;
        return backtrack(board, rowUsed, colUsed, boxUsed, listener);
    }

    /**
     * Initializes row/column/box usage tables and validates the initial puzzle.
     */
    private boolean initConstraintsAndValidate(int[][] board,
                                               boolean[][] rowUsed,
                                               boolean[][] colUsed,
                                               boolean[][] boxUsed) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int val = board[r][c];
                if (val == 0) continue;

                int boxIndex = getBoxIndex(r, c);
                if (rowUsed[r][val] || colUsed[c][val] || boxUsed[boxIndex][val]) {
                    return false;
                }
                rowUsed[r][val] = true;
                colUsed[c][val] = true;
                boxUsed[boxIndex][val] = true;
            }
        }
        return true;
    }

    /** Finds the next empty cell (0). Returns null if none left. */
    private int[] findEmptyCell(int[][] board) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == 0) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    /** Converts (row, col) to a 0..8 box index. */
    private int getBoxIndex(int r, int c) {
        return (r / SUBGRID_SIZE) * SUBGRID_SIZE + (c / SUBGRID_SIZE);
    }

    /**
     * Backtracking search using precomputed constraints and optional listener.
     */
    private boolean backtrack(int[][] board,
                              boolean[][] rowUsed,
                              boolean[][] colUsed,
                              boolean[][] boxUsed,
                              StepListener listener) {

        nodesVisitedCount++;

        if (listener != null && listener.isCancelled()) {
            return false;
        }

        int[] cell = findEmptyCell(board);
        if (cell == null) {
            return true;  // solved
        }

        int row = cell[0];
        int col = cell[1];
        int boxIndex = getBoxIndex(row, col);

        for (int num = 1; num <= 9; num++) {
            if (!rowUsed[row][num] && !colUsed[col][num] && !boxUsed[boxIndex][num]) {

                // Place number
                board[row][col] = num;
                rowUsed[row][num] = true;
                colUsed[col][num] = true;
                boxUsed[boxIndex][num] = true;

                if (listener != null) {
                    listener.onStep(row, col, num);
                    if (listener.isCancelled()) return false;
                }

                if (backtrack(board, rowUsed, colUsed, boxUsed, listener)) {
                    return true;
                }

                // Backtrack
                board[row][col] = 0;
                rowUsed[row][num] = false;
                colUsed[col][num] = false;
                boxUsed[boxIndex][num] = false;

                backtrackCount++;

                if (listener != null) {
                    listener.onStep(row, col, 0);
                    if (listener.isCancelled()) return false;
                }
            }
        }
        return false;
    }
}
