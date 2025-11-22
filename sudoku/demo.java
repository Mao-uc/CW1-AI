package run;

public class demo {

    public static final int SIZE = 9;
    private static final int SUBGRID_SIZE = 3;
    private long backtrackCount = 0;
    private long nodesVisitedCount = 0;

    /** Listener for visualization or cancellation. */
    public interface StepListener {
        void onStep(int row, int col, int value);
        boolean isCancelled();
    }

    public long getBacktrackCount() {
        return backtrackCount;
    }
    public long getNodesVisitedCount() {
        return nodesVisitedCount;
    }
    /** Solves the puzzle using backtracking with constraint tables. */
    public boolean solve(int[][] board, StepListener listener) {
        if (board == null || board.length != SIZE || board[0].length != SIZE)
            throw new IllegalArgumentException("Board must be 9x9.");

        boolean[][] rowUsed = new boolean[SIZE][SIZE + 1];
        boolean[][] colUsed = new boolean[SIZE][SIZE + 1];
        boolean[][] boxUsed = new boolean[SIZE][SIZE + 1];
        if (!initConstraintsAndValidate(board, rowUsed, colUsed, boxUsed))
            throw new IllegalArgumentException("Invalid initial puzzle.");

        nodesVisitedCount = 0;
        backtrackCount = 0;
        return backtrack(board, rowUsed, colUsed, boxUsed, listener);
    }

    /** Builds constraint tables and checks for conflicts. */
    private boolean initConstraintsAndValidate(int[][] board,
        boolean[][] rowUsed, boolean[][] colUsed, boolean[][] boxUsed) {

    for (int r = 0; r < SIZE; r++) {
        for (int c = 0; c < SIZE; c++) {
            int v = board[r][c];
            if (v == 0) continue;

            int b = getBoxIndex(r, c);
            if (rowUsed[r][v] || colUsed[c][v] || boxUsed[b][v])
                return false;

            rowUsed[r][v] = colUsed[c][v] = boxUsed[b][v] = true;
        }
    }
    return true;
}

    /** Finds the next empty cell (0). */
    private int[] findEmptyCell(int[][] board) {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (board[r][c] == 0)
                    return new int[]{r, c};
        return null;
    }

    /** Returns 0–8 index of 3×3 subgrid. */
    private int getBoxIndex(int r, int c) {
        return (r / SUBGRID_SIZE) * SUBGRID_SIZE + (c / SUBGRID_SIZE);
    }


    /** Main backtracking search. */
    private boolean backtrack(int[][] board,
            boolean[][] rowUsed, boolean[][] colUsed, boolean[][] boxUsed,
            StepListener listener) {
        nodesVisitedCount++;
        if (listener != null && listener.isCancelled()) return false;
        int[] cell = findEmptyCell(board);
        if (cell == null) return true;
        int row = cell[0], col = cell[1];
        int box = getBoxIndex(row, col);
        for (int num = 1; num <= 9; num++) {
            if (!rowUsed[row][num] && !colUsed[col][num] && !boxUsed[box][num]) {
                // place
                board[row][col] = num;
                rowUsed[row][num] = colUsed[col][num] = boxUsed[box][num] = true;
                if (listener != null) {
                    listener.onStep(row, col, num);
                    if (listener.isCancelled()) return false;
                }
                // recurse
                if (backtrack(board, rowUsed, colUsed, boxUsed, listener))
                    return true;
                // undo
                board[row][col] = 0;
                rowUsed[row][num] = colUsed[col][num] = boxUsed[box][num] = false;
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
