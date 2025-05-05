package com.example.first;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements GameLogic.GameLogicCallback, KeyboardFragment.KeyboardCallback {
    private TextView[][] grid = new TextView[6][5];
    private GameLogic gameLogic;
    private boolean isGameReady = false;
    private KeyboardFragment keyboardFragment;
    private String username;

    // Statistics overlay views
    private View statisticsOverlay;
    private TextView textGamesPlayed, textWins, textLosses, textWinRate;
    private Button buttonPlayAgain, buttonReset;
    private SharedPreferences statsPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = getIntent().getStringExtra("username");
        if (username == null) username = "guest";

        initializeGrid();
        initializeKeyboardFragment();
        initializeStatisticsOverlay();
        gameLogic = new GameLogic(grid, this);
        disableGameInput();
        fetchNewWord();
        statsPrefs = getSharedPreferences("game_stats", MODE_PRIVATE);
    }

    private void initializeKeyboardFragment() {
        keyboardFragment = new KeyboardFragment();
        keyboardFragment.setCallback(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.keyboardContainer, keyboardFragment)
                .commit();
    }

    private void fetchNewWord() {
        WordFetcher.fetchWord(new WordFetcher.WordFetchCallback() {
            @Override
            public void onWordFetched(String word) {
                gameLogic.setAnswer(word);
                enableGameInput();
                isGameReady = true;
            }

            @Override
            public void onError(Exception e) {
                String errorMessage = "Failed to fetch word: " + e.getMessage();
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                gameLogic.setAnswer("WATER"); // Fallback to default word
                enableGameInput();
                isGameReady = true;
            }
        });
    }

    private void initializeGrid() {
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 5; c++) {
                String tileId = "tile_" + r + c;
                int resId = getResources().getIdentifier(tileId, "id", getPackageName());
                grid[r][c] = findViewById(resId);
            }
        }
    }

    private void enableGameInput() {
        if (keyboardFragment != null) {
            keyboardFragment.enableKeyboard();
        }
    }

    private void disableGameInput() {
        if (keyboardFragment != null) {
            keyboardFragment.disableKeyboard();
        }
    }

    private void initializeStatisticsOverlay() {
        statisticsOverlay = findViewById(R.id.statisticsOverlay);
        textGamesPlayed = findViewById(R.id.textGamesPlayed);
        textWins = findViewById(R.id.textWins);
        textLosses = findViewById(R.id.textLosses);
        textWinRate = findViewById(R.id.textWinRate);
        buttonPlayAgain = findViewById(R.id.buttonPlayAgain);
        buttonReset = findViewById(R.id.buttonReset);

        statisticsOverlay.setVisibility(View.GONE);

        buttonPlayAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statisticsOverlay.setVisibility(View.GONE);
                resetGame();
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });
    }

    private void showStatisticsOverlay(boolean won) {
        // Update stats per user
        String prefix = "stats_" + username + "_";
        int gamesPlayed = statsPrefs.getInt(prefix + "games_played", 0) + 1;
        int wins = statsPrefs.getInt(prefix + "wins", 0);
        int losses = statsPrefs.getInt(prefix + "losses", 0);
        if (won) wins++;
        else losses++;
        float winRate = gamesPlayed > 0 ? (wins * 100f / gamesPlayed) : 0f;
        statsPrefs.edit()
                .putInt(prefix + "games_played", gamesPlayed)
                .putInt(prefix + "wins", wins)
                .putInt(prefix + "losses", losses)
                .apply();
        // Update UI
        textGamesPlayed.setText("Games Played: " + gamesPlayed);
        textWins.setText("Wins: " + wins);
        textLosses.setText("Losses: " + losses);
        textWinRate.setText("Win Rate: " + Math.round(winRate) + "%");
        statisticsOverlay.setVisibility(View.VISIBLE);
    }

    private void resetGame() {
        // Clear grid
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 5; c++) {
                grid[r][c].setText("");
                grid[r][c].setBackgroundResource(R.drawable.wordle_tile_border);
            }
        }
        gameLogic = new GameLogic(grid, this);
        disableGameInput();
        fetchNewWord();
        isGameReady = false;
    }

    @Override
    public void onGameWon() {
        disableGameInput();
        showStatisticsOverlay(true);
    }

    @Override
    public void onGameLost() {
        disableGameInput();
        showStatisticsOverlay(false);
    }

    @Override
    public void onInvalidGuess() {
        Toast.makeText(this, "Enter a 5-letter word", Toast.LENGTH_SHORT).show();
    }

    // KeyboardFragment.KeyboardCallback implementation
    @Override
    public void onKeyPressed(char letter) {
        if (isGameReady) {
            gameLogic.addLetter(letter);
        }
    }

    @Override
    public void onEnterPressed() {
        if (isGameReady) {
            gameLogic.checkWord();
        }
    }

    @Override
    public void onBackspacePressed() {
        if (isGameReady) {
            gameLogic.removeLetter();
        }
    }
}
