package com.example.first;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements GameLogic.GameLogicCallback {
    private TextView[][] grid = new TextView[6][5];
    private Button[] keyboardButtons = new Button[26];
    private GameLogic gameLogic;
    private boolean isGameReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeGrid();
        initializeKeyboard();
        gameLogic = new GameLogic(grid, keyboardButtons, this);
        disableGameInput();
        fetchNewWord();
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
        Toast.makeText(this, "You guessed it!", Toast.LENGTH_LONG).show();
        gameLogic.disableAllButtons();
    }

    @Override
    public void onGameLost() {
        Toast.makeText(this, "Game Over!", Toast.LENGTH_LONG).show();
        gameLogic.disableAllButtons();
    }

    @Override
    public void onInvalidGuess() {
        Toast.makeText(this, "Enter a 5-letter word", Toast.LENGTH_SHORT).show();
    }
}
