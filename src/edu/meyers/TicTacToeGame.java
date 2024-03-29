package edu.meyers;

import java.util.Random;

public class TicTacToeGame {

    public  

    enum DifficultyLevel {Easy, Harder, Expert
    }
    ;
	
	private DifficultyLevel mDifficultyLevel = DifficultyLevel.Expert;

    public static final int BOARD_SIZE = 9;

    // Characters used to represent the human, computer, and open spots
    public static final char HUMAN_PLAYER = 'X';
    public static final char COMPUTER_PLAYER = 'O';
    public static final char OPEN_SPOT = ' ';

    // Random number generator
    private Random mRand;

    // Represents the game board
    private char mBoard[];

    public TicTacToeGame() {
        mBoard = new char[BOARD_SIZE];
        mRand = new Random();
    }

    /**
     * Clear the board of all X's and O's.
     */
    public void clearBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            mBoard[i] = OPEN_SPOT;
        }
    }

    /**
     * Set the given player at the given location on the game board. The
     * location must be available, or the board will not be changed.
     *
     * @param player - The human or computer player
     * @param location - The location (0-8) to place the move
     */
    public boolean setMove(char player, int location) {
        if (location >= 0 && location < BOARD_SIZE
                && mBoard[location] == OPEN_SPOT) {
            mBoard[location] = player;
            return true;
        }
        return false;
    }

    public char getBoardOccupant(int location) {
        if (location >= 0 && location < BOARD_SIZE) {
            return mBoard[location];
        }
        return '?';
    }

    /**
     * Check for a winner. Return a status value indicating the board status.
     *
     * @return Return 0 if no winner or tie yet, 1 if it's a tie, 2 if X won, or
     * 3 if O won.
     */
    public int checkForWinner() {
        // Check horizontal wins
        for (int i = 0; i <= 6; i += 3) {
            if (mBoard[i] == HUMAN_PLAYER
                    && mBoard[i + 1] == HUMAN_PLAYER
                    && mBoard[i + 2] == HUMAN_PLAYER) {
                return 2;
            }
            if (mBoard[i] == COMPUTER_PLAYER
                    && mBoard[i + 1] == COMPUTER_PLAYER
                    && mBoard[i + 2] == COMPUTER_PLAYER) {
                return 3;
            }
        }

        // Check vertical wins
        for (int i = 0; i <= 2; i++) {
            if (mBoard[i] == HUMAN_PLAYER
                    && mBoard[i + 3] == HUMAN_PLAYER
                    && mBoard[i + 6] == HUMAN_PLAYER) {
                return 2;
            }
            if (mBoard[i] == COMPUTER_PLAYER
                    && mBoard[i + 3] == COMPUTER_PLAYER
                    && mBoard[i + 6] == COMPUTER_PLAYER) {
                return 3;
            }
        }

        // Check for diagonal wins
        if ((mBoard[0] == HUMAN_PLAYER
                && mBoard[4] == HUMAN_PLAYER
                && mBoard[8] == HUMAN_PLAYER)
                || (mBoard[2] == HUMAN_PLAYER
                && mBoard[4] == HUMAN_PLAYER
                && mBoard[6] == HUMAN_PLAYER)) {
            return 2;
        }
        if ((mBoard[0] == COMPUTER_PLAYER
                && mBoard[4] == COMPUTER_PLAYER
                && mBoard[8] == COMPUTER_PLAYER)
                || (mBoard[2] == COMPUTER_PLAYER
                && mBoard[4] == COMPUTER_PLAYER
                && mBoard[6] == COMPUTER_PLAYER)) {
            return 3;
        }

        // Check for tie
        for (int i = 0; i < BOARD_SIZE; i++) {
            // If we find a number, then no one has won yet
            if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER) {
                return 0;
            }
        }

        // If we make it through the previous loop, all places are taken, so it's a tie
        return 1;
    }

    /**
     * Return the best move for the computer to make. You must call setMove() to
     * actually make the computer move to that location.
     *
     * @return The best move for the computer to make.
     */
    public int getComputerMove() {
        int move = -1;

        if (mDifficultyLevel == DifficultyLevel.Easy) {
            move = getRandomMove();
        } else if (mDifficultyLevel == DifficultyLevel.Harder) {
            move = getWinningMove();
            if (move == -1) {
                move = getRandomMove();
            }
        } else if (mDifficultyLevel == DifficultyLevel.Expert) {

			// Try to win, but if that's not possible, block.
            // If that's not possible, move anywhere.
            move = getWinningMove();
            if (move == -1) {
                move = getBlockingMove();
            }
            if (move == -1) {
                move = getRandomMove();
            }
        }

        return move;

        /*
         // First see if there's a move O can make to win
         for (int i = 0; i < BOARD_SIZE; i++) {
         if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER) {
         char curr = mBoard[i];
         mBoard[i] = COMPUTER_PLAYER;
         if (checkForWinner() == 3) {
         mBoard[i] = OPEN_SPOT;
         return i;
         }
         else
         mBoard[i] = curr;
         }
         }

         // See if there's a move O can make to block X from winning
         for (int i = 0; i < BOARD_SIZE; i++) {
         if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER) {
         char curr = mBoard[i];   // Save the current number
         mBoard[i] = HUMAN_PLAYER;
         if (checkForWinner() == 2) {
         mBoard[i] = OPEN_SPOT;
         return i;
         }
         else
         mBoard[i] = curr;
         }
         }

         // Generate random move
         do
         {
         move = mRand.nextInt(BOARD_SIZE);
         } while (mBoard[move] != OPEN_SPOT);
			
         return move;
         */
    }

    private int getRandomMove() {
        int move;
        do {
            move = mRand.nextInt(BOARD_SIZE);
        } while (mBoard[move] != OPEN_SPOT);

        return move;
    }

    private int getBlockingMove() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            char curr = mBoard[i];

            if (curr != HUMAN_PLAYER && curr != COMPUTER_PLAYER) {
                // Simulates if X moved to this spot
                mBoard[i] = HUMAN_PLAYER;
                if (checkForWinner() == 2) {
                    mBoard[i] = OPEN_SPOT;   // Restore space
                    return i;
                } else {
                    mBoard[i] = OPEN_SPOT;
                }
            }
        }

        // No blocking move
        return -1;
    }

    private int getWinningMove() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            char curr = mBoard[i];

            if (curr != HUMAN_PLAYER && curr != COMPUTER_PLAYER) {
                // Simulates if O moved to this spot
                mBoard[i] = COMPUTER_PLAYER;
                if (checkForWinner() == 3) {
                    mBoard[i] = OPEN_SPOT;   // Restore space
                    return i;
                } else {
                    mBoard[i] = OPEN_SPOT;
                }
            }
        }

        // No winning move
        return -1;
    }

    public DifficultyLevel getDifficultyLevel() {
        return mDifficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        mDifficultyLevel = difficultyLevel;
    }

    public char[] getBoardState() {
        return mBoard;
    }

    public void setBoardState(char[] board) {
        mBoard = board.clone();
    }

    @Override
    public String toString() {
        return mBoard[0] + "|" + mBoard[1] + "|" + mBoard[2] + "\n"
                + mBoard[3] + "|" + mBoard[4] + "|" + mBoard[5] + "\n"
                + mBoard[6] + "|" + mBoard[7] + "|" + mBoard[8];

    }
}
