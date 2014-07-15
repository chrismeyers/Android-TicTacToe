package edu.meyers;

import java.io.File;
import java.io.FileWriter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.*;
import android.preference.*;
import android.content.*;
import android.graphics.Color;
import edu.meyers.TicTacToeGame.DifficultyLevel;

public class TicTacToeActivity extends Activity {

    // Buttons making up the board
    private Button mBoardButtons[];

    // Represents the internal state of the game
    private TicTacToeGame mGame;

    // Various text displayed
    private TextView mInfoTextView;
    private TextView mHumanWins;
    private TextView mTies;
    private TextView mAndroidWins;

    // Win counters
    private int humanWins;
    private int ties;
    private int androidWins;

    // Who goes first
    private boolean humanFirst = true;

    // Determines if more moves can be made
    private boolean mGameOver = false;

	// Menu variables
    //static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;
    static final int DIALOG_ABOUT = 2;

    // New BoardView
    private BoardView mBoardView;

    // Sounds
    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;

    // Who's turn
    private char mTurn = TicTacToeGame.COMPUTER_PLAYER;

    // Save scores
    private SharedPreferences mPrefs;

    // Write to file
    public static final String TAG = "TTT_Activity";
    public FileWriter outputStream;

    // Sound on or off?
    private boolean mSoundOn;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mGame = new TicTacToeGame();
        mBoardView = (BoardView) findViewById(R.id.board);
        mBoardView.setGame(mGame);

        // Touch Listener
        mBoardView.setOnTouchListener(mTouchListener);

        mInfoTextView = (TextView) findViewById(R.id.information);
        mHumanWins = (TextView) findViewById(R.id.human_wins);
        mTies = (TextView) findViewById(R.id.ties);
        mAndroidWins = (TextView) findViewById(R.id.android_wins);

        //Log.d("onCreate", "created game");					
        //Toast.makeText(getApplicationContext(), "onCreate",
        //						Toast.LENGTH_SHORT).show();
        // Open file for debugging info info on the SD card
        String outFile = Environment.getExternalStorageDirectory() + File.separator + "Activity.txt";
        // Display the file path
        Toast.makeText(getApplicationContext(), outFile, Toast.LENGTH_SHORT).show();
        // Create the file writer to be used in methods
        try {
            outputStream = new FileWriter(outFile);
        } catch (java.io.IOException ex) {
        }

        //mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSoundOn = mPrefs.getBoolean("sound", true);
        humanWins = mPrefs.getInt("humanWins", 0);
        ties = mPrefs.getInt("ties", 0);
        androidWins = mPrefs.getInt("androidWins", 0);
        mBoardView.setBoardColor(mPrefs.getInt(Settings.BOARD_COLOR_PREF_KEY, Color.GRAY));

        String difficultyLevel = mPrefs.getString("difficulty_level", getResources().getString(R.string.difficulty_harder));
        if (difficultyLevel.equals(getResources().getString(R.string.difficulty_easy))) {
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
        } else if (difficultyLevel.equals(getResources().getString(R.string.difficulty_harder))) {
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
        } else {
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
        }

		//int difficultyLevel = mPrefs.getInt("difficultyLevel", TicTacToeGame.DifficultyLevel.Easy.ordinal());
        //mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.values()[difficultyLevel]);
        if (savedInstanceState == null) {
            startNewGame();
        } else {
            // Debug info
            try {
                outputStream.write(TAG + ": " + "Restoring saved Scores" + "\n");
            } catch (java.io.IOException ex) {
            }

            // Restore game state
            mGame.setBoardState(savedInstanceState.getCharArray("board"));
            mGameOver = savedInstanceState.getBoolean("mGameOver");
            mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
            humanFirst = savedInstanceState.getBoolean("humanFirst");
            mTurn = savedInstanceState.getChar("mTurn");

            if (!mGameOver && mTurn == TicTacToeGame.COMPUTER_PLAYER) {
                int move = mGame.getComputerMove();
                setMove(TicTacToeGame.COMPUTER_PLAYER, move);
            }
        }
        displayScores();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Save the current scores
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("humanWins", humanWins);
        ed.putInt("ties", ties);
        ed.putInt("androidWins", androidWins);

        ed.putInt("difficultyLevel", mGame.getDifficultyLevel().ordinal());

        ed.commit();

        // Close the outFile when we quit the app
        try {
            outputStream.write(TAG + ": " + "I am closing the debug file" + "\n");
            outputStream.write(TAG + ": " + "I am exiting the game" + "\n");
            outputStream.close();
        } catch (java.io.IOException ex) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         //super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

		//Log.d("onCreateOptionsMenu", "created menu");					
        //Toast.makeText(getApplicationContext(), "onCreateOptionsMenu",
        //			   Toast.LENGTH_SHORT).show();
        //menu.add("New Game");
        return true;
    }

    // Handles menu item selections
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.new_game:
                startNewGame();
                return true;
            /*case R.id.ai_difficulty:
             showDialog(DIALOG_DIFFICULTY_ID);
             return true;*/
            case R.id.settings:
                startActivityForResult(new Intent(this, Settings.class), 0);
                return true;
            case R.id.Reset:
                humanWins = 0;
                ties = 0;
                androidWins = 0;
                displayScores();
                return true;
            case R.id.about:
                showDialog(DIALOG_ABOUT);
                return true;
            /*case R.id.quit:
             showDialog(DIALOG_QUIT_ID);
             return true;*/
        }
        return false;

        //startNewGame();
        //return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RESULT_CANCELED) {
            // Apply potentially new settings

            mSoundOn = mPrefs.getBoolean("sound", true);

            String difficultyLevel = mPrefs.getString("difficulty_level", getResources().getString(R.string.difficulty_harder));

            if (difficultyLevel.equals(getResources().getString(R.string.difficulty_easy))) {
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
            } else if (difficultyLevel.equals(getResources().getString(R.string.difficulty_harder))) {
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
            } else {
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
            }

            mBoardView.setBoardColor(mPrefs.getInt(Settings.BOARD_COLOR_PREF_KEY, Color.GRAY));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharArray("board", mGame.getBoardState());
        outState.putBoolean("mGameOver", mGameOver);
        outState.putInt("humanWins", Integer.valueOf(humanWins));
        outState.putInt("androidWins", Integer.valueOf(androidWins));
        outState.putInt("ties", Integer.valueOf(ties));
        outState.putCharSequence("info", mInfoTextView.getText());
        outState.putBoolean("humanFirst", humanFirst);
        outState.putChar("mTurn", mTurn);

        // Debug info
        try {
            outputStream.write(TAG + ": " + "The orientation of the device changed, restoring game state" + "\n");
            outputStream.write("Board State: \n" + mGame.toString() + "\n");
            outputStream.write("Game over state: " + mGameOver + "\n"
                    + "Info state: " + mInfoTextView.getText() + "\n"
                    + "Who's turn state: " + mTurn + "\n"
                    + "Who goes first state: " + humanFirst + "\n"
                    + "Human Wins: " + humanWins + "\n"
                    + "Ties: " + ties + "\n"
                    + "Android Wins: " + androidWins + "\n\n"
            );
        } catch (java.io.IOException ex) {
        }
    }

    private void displayScores() {
        // Debug info
        try {
            outputStream.write(TAG + ": " + "Human Wins: " + humanWins + "\n");
            outputStream.write(TAG + ": " + "Ties: " + ties + "\n");
            outputStream.write(TAG + ": " + "Android Wins: " + androidWins + "\n\n");
        } catch (java.io.IOException ex) {
        }

        mHumanWins.setText(Integer.toString(humanWins));
        mTies.setText(Integer.toString(ties));
        mAndroidWins.setText(Integer.toString(androidWins));
    }

    // Set up the game board.
    private void startNewGame() {
        // Debug info
        try {
            outputStream.write(TAG + ": " + "STARTING NEW GAME" + "\n\n");
        } catch (java.io.IOException ex) {
        }

        mGame.clearBoard();
        mBoardView.invalidate(); //Redraw the board

        // Displays who goes first
        if (humanFirst) {
            mInfoTextView.setText("You go first.");
            mTurn = TicTacToeGame.HUMAN_PLAYER;
        } else {
            mInfoTextView.setText("Android goes first.");
            int move = mGame.getComputerMove();
            mTurn = TicTacToeGame.COMPUTER_PLAYER;
            setMove(TicTacToeGame.COMPUTER_PLAYER, move);
        }

		//Log.d("startNewGame", "new game created");					
        //Toast.makeText(getApplicationContext(), "startNewGame",
        //			   Toast.LENGTH_SHORT).show();
        mGameOver = false;
    }

    private boolean setMove(char player, int location) {

        if (player == TicTacToeGame.COMPUTER_PLAYER) {
            final int loc = location;

            Handler handler = new Handler();

            // Make Computers  move - 1 sec delay
            handler.postDelayed(new Runnable() {
                public void run() {
                    mGame.setMove(TicTacToeGame.COMPUTER_PLAYER, loc);
                    mBoardView.invalidate();   // Redraw the board

                    // Debug info
                    try {
                        outputStream.write(TAG + ": " + "I am in setMove(), about to make a move for "
                                + TicTacToeGame.COMPUTER_PLAYER + "\n");
                        outputStream.write(TAG + ": " + "Here is the board after the move for "
                                + TicTacToeGame.COMPUTER_PLAYER + "\n"
                                + mGame.toString() + "\n\n");
                    } catch (java.io.IOException ex) {
                    }

                    try {
                        if (mSoundOn) {
                            mComputerMediaPlayer.start();
                        }
                    } catch (IllegalStateException e) {
                    };

                	//mComputerMediaPlayer.start();	                                	
                    int winner = mGame.checkForWinner();
                    if (winner == 0) {
                        mTurn = TicTacToeGame.HUMAN_PLAYER;
                        mInfoTextView.setText(R.string.turn_human);
                    } else {
                        endGame(winner);
                    }
                }
            }, 1000);

            return true;
        } else if (mGame.setMove(TicTacToeGame.HUMAN_PLAYER, location)) {
            mTurn = TicTacToeGame.COMPUTER_PLAYER;
            mBoardView.invalidate();   // Redraw the board
            if (mSoundOn) {
                mHumanMediaPlayer.start();
            }

            // Debug info
            try {
                outputStream.write(TAG + ": " + "Here is the board after the move for "
                        + TicTacToeGame.HUMAN_PLAYER + "\n"
                        + mGame.toString() + "\n\n");
            } catch (java.io.IOException ex) {
            }

            return true;
        }

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sword);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.swish);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
    }

    @Override
    public Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {

            case DIALOG_QUIT_ID:
                builder.setMessage(R.string.quit_question).setCancelable(false).
                        setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                TicTacToeActivity.this.finish();
                            }
                        })
                        .setPositiveButton(R.string.no, null);

                dialog = builder.create();

                break;

            case DIALOG_ABOUT:
                Context context = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.about_dialog, null);
                builder.setView(layout);
                builder.setPositiveButton("OK", null);
                dialog = builder.create();
                break;
        }

		//Log.d("onCreateDialog", "created dialog");					
        //Toast.makeText(getApplicationContext(), "onCreateDialog",
        //			   Toast.LENGTH_SHORT).show();
        return dialog;
    }

    private void endGame(int winner) {
        if (winner == 1) {
            // Debug info
            try {
                outputStream.write(TAG + ": " + "There was a tie" + "\n\n");
            } catch (java.io.IOException ex) {
            }

            mInfoTextView.setText("It's a tie!");
            ties++; // Increment Tie count
        } else if (winner == 2) {
            // Debug info
            try {
                outputStream.write(TAG + ": " + "There was a winner after the last move for "
                        + TicTacToeGame.HUMAN_PLAYER + "\n\n");
            } catch (java.io.IOException ex) {
            }

            String defaultMsg = getResources().getString(R.string.result_human_wins);
            mInfoTextView.setText(mPrefs.getString(Settings.VICTORY_MESSAGE_PREF_KEY, defaultMsg));
            humanWins++; // Increment Human wins
        } else {
            // Debug info
            try {
                outputStream.write(TAG + ": " + "There was a winner after the last move for "
                        + TicTacToeGame.COMPUTER_PLAYER + "\n\n");
            } catch (java.io.IOException ex) {
            }

            mInfoTextView.setText("Android won!");
            androidWins++; // Increment Android wins
        }

        humanFirst = !humanFirst; // Change who starts next
        mGameOver = true; // Game is over

        // Update counters
        displayScores();
    }

    // Listen for touches on the board
    private OnTouchListener mTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {

            // Determine which cell was touched     	
            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;

            // Debug info
            try {
                outputStream.write(TAG + ": " + "Cell that was touched: Col= " + col
                        + " Row=" + row + " Pos=" + pos + "\n");
            } catch (java.io.IOException ex) {
            }

            if (!mGameOver && mTurn == TicTacToeGame.HUMAN_PLAYER && setMove(TicTacToeGame.HUMAN_PLAYER, pos)) {

                // If no winner yet, let the computer make a move
                int winner = mGame.checkForWinner();
                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_computer);
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                } else {
                    endGame(winner);
                }

            }

            // So we aren't notified of continued events when finger is moved
            return false;
        }
    };

}
