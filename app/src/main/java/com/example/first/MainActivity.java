package com.example.first;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements GameLogic.GameLogicCallback {
    private TextView[][] grid = new TextView[6][5];
    private Button[] keyboardButtons = new Button[26];
    private GameLogic gameLogic;
    private boolean isGameReady = false;
    private View statsOverlay;
    private TextView gamesPlayedText;
    private TextView winPercentageText;
    private TextView currentStreakText;
    private SharedPreferences stats;
    private FloatingActionButton restartButton;
    private MaterialButton playAgainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stats = getSharedPreferences("wordle_stats", MODE_PRIVATE);
        initializeViews();
        initializeGrid();
        initializeKeyboard();
        gameLogic = new GameLogic(grid, keyboardButtons, this);
        disableGameInput();
        fetchNewWord();
    }

    private void initializeViews() {
        statsOverlay = findViewById(R.id.statsOverlay);
        gamesPlayedText = findViewById(R.id.gamesPlayedText);
        winPercentageText = findViewById(R.id.winPercentageText);
        currentStreakText = findViewById(R.id.currentStreakText);
        restartButton = findViewById(R.id.restartButton);
        playAgainButton = findViewById(R.id.playAgainButton);

        restartButton.setOnClickListener(v -> restartGame());
        playAgainButton.setOnClickListener(v -> {
            statsOverlay.setVisibility(View.GONE);
            restartGame();
        });
    }

    private void restartGame() {
        // Clear the grid
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                grid[i][j].setText("");
                grid[i][j].setBackgroundResource(R.drawable.wordle_tile_border);
            }
        }

        // Reset keyboard colors
        for (Button button : keyboardButtons) {
            if (button != null) {
                button.setBackgroundResource(R.drawable.keyboard_button_background);
            }
        }

        // Reset game logic
        gameLogic.resetGame();
        
        // Fetch new word
        disableGameInput();
        fetchNewWord();
    }

    private void updateStats(boolean won) {
        int gamesPlayed = stats.getInt("games_played", 0) + 1;
        int gamesWon = stats.getInt("games_won", 0) + (won ? 1 : 0);
        int currentStreak = won ? stats.getInt("current_streak", 0) + 1 : 0;

        SharedPreferences.Editor editor = stats.edit();
        editor.putInt("games_played", gamesPlayed);
        editor.putInt("games_won", gamesWon);
        editor.putInt("current_streak", currentStreak);
        editor.apply();

        // Update UI
        gamesPlayedText.setText(String.valueOf(gamesPlayed));
        winPercentageText.setText(String.valueOf((int)((gamesWon * 100.0f) / gamesPlayed)));
        currentStreakText.setText(String.valueOf(currentStreak));
    }

    private void showStats(boolean won) {
        updateStats(won);
        statsOverlay.setVisibility(View.VISIBLE);
        statsOverlay.setAlpha(0f);
        statsOverlay.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
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

    private void initializeKeyboard() {
        String keys = "QWERTYUIOPASDFGHJKLZXCVBNM";
        for (int i = 0; i < keys.length(); i++) {
            String buttonId = "btn" + keys.charAt(i);
            int resId = getResources().getIdentifier(buttonId, "id", getPackageName());
            keyboardButtons[i] = findViewById(resId);
        }

        findViewById(R.id.btnEnter).setOnClickListener(v -> {
            if (isGameReady) {
                gameLogic.checkWord();
            }
        });
        findViewById(R.id.btnBackspace).setOnClickListener(v -> {
            if (isGameReady) {
                gameLogic.removeLetter();
            }
        });
    }

    private void enableGameInput() {
        for (Button button : keyboardButtons) {
            if (button != null) {
                button.setEnabled(true);
            }
        }
        findViewById(R.id.btnEnter).setEnabled(true);
        findViewById(R.id.btnBackspace).setEnabled(true);
    }

    private void disableGameInput() {
        for (Button button : keyboardButtons) {
            if (button != null) {
                button.setEnabled(false);
            }
        }
        findViewById(R.id.btnEnter).setEnabled(false);
        findViewById(R.id.btnBackspace).setEnabled(false);
    }

    @Override
    public void onGameWon() {
        new Handler().postDelayed(() -> {
            showStats(true);
            Toast.makeText(this, "Congratulations!", Toast.LENGTH_LONG).show();
        }, 1500);
        gameLogic.disableAllButtons();
    }

    @Override
    public void onGameLost() {
        new Handler().postDelayed(() -> {
            showStats(false);
            Toast.makeText(this, "Game Over! The word was: " + gameLogic.getAnswer(), Toast.LENGTH_LONG).show();
        }, 1500);
        gameLogic.disableAllButtons();
    }

    @Override
    public void onInvalidGuess() {
        Toast.makeText(this, "Enter a 5-letter word", Toast.LENGTH_SHORT).show();
    }
}
