package com.example.first;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewPropertyAnimator;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private final String ANSWER = "WATER";
    private StringBuilder currentGuess = new StringBuilder();
    private int row = 0;
    private TextView[][] grid = new TextView[6][5];
    private Button[] keyboardButtons = new Button[26];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeGrid();
        initializeKeyboard();
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
            final int index = i;
            keyboardButtons[i].setOnClickListener(v -> addLetter(keys.charAt(index)));
        }

        findViewById(R.id.btnEnter).setOnClickListener(v -> checkWord());
        findViewById(R.id.btnBackspace).setOnClickListener(v -> removeLetter());
    }

    private void addLetter(char letter) {
        if (currentGuess.length() < 5) {
            int col = currentGuess.length();
            grid[row][col].setText(String.valueOf(letter));
            currentGuess.append(letter);

            // Pop animation
            grid[row][col].setScaleX(0.8f);
            grid[row][col].setScaleY(0.8f);
            grid[row][col].animate().scaleX(1f).scaleY(1f).setDuration(100).start();
        }
    }

    private void removeLetter() {
        if (currentGuess.length() > 0) {
            int col = currentGuess.length() - 1;
            grid[row][col].setText("");
            currentGuess.deleteCharAt(col);
        }
    }

    private void checkWord() {
        if (currentGuess.length() < 5) {
            shakeRow(row);
            Toast.makeText(this, "Enter a 5-letter word", Toast.LENGTH_SHORT).show();
            return;
        }

        String guess = currentGuess.toString();
        boolean[] correct = new boolean[5];
        boolean[] used = new boolean[5];

        // First pass: check for correct (green)
        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == ANSWER.charAt(i)) {
                correct[i] = true;
                used[i] = true;
            }
        }

        // Second pass: check for yellow
        for (int i = 0; i < 5; i++) {
            if (!correct[i]) {
                for (int j = 0; j < 5; j++) {
                    if (!used[j] && guess.charAt(i) == ANSWER.charAt(j)) {
                        used[j] = true;
                        break;
                    }
                }
            }
        }

        // Animate flip for each tile
        for (int i = 0; i < 5; i++) {
            int color = Color.GRAY;
            char letter = guess.charAt(i);

            if (guess.charAt(i) == ANSWER.charAt(i)) {
                color = Color.GREEN;
            } else {
                for (int j = 0; j < 5; j++) {
                    if (guess.charAt(i) == ANSWER.charAt(j) && !correct[i]) {
                        color = Color.YELLOW;
                        break;
                    }
                }
            }

            int finalColor = color;
            int finalI = i;
            new Handler().postDelayed(() -> {
                flipTile(grid[row][finalI], letter, finalColor, 0);
                updateKeyboardColor(letter, finalColor);
            }, i * 300);
        }

        // Next row or end
        new Handler().postDelayed(() -> {
            if (guess.equals(ANSWER)) {
                Toast.makeText(this, "You guessed it!", Toast.LENGTH_LONG).show();
                disableAllButtons();
            } else if (row < 5) {
                row++;
                currentGuess.setLength(0);
            } else {
                Toast.makeText(this, "Game Over!", Toast.LENGTH_LONG).show();
                disableAllButtons();
            }
        }, 5 * 300 + 300); // after all flips
    }

    private void flipTile(TextView tile, char letter, int color, int delay) {
        tile.animate()
                .rotationY(90)
                .setDuration(150)
                .setStartDelay(delay)
                .withEndAction(() -> {
                    tile.setText(String.valueOf(letter));
                    tile.setBackgroundColor(color);
                    tile.setRotationY(-90);
                    tile.animate().rotationY(0).setDuration(150).start();
                })
                .start();
    }

    private void shakeRow(int rowIndex) {
        for (int c = 0; c < 5; c++) {
            TextView tile = grid[rowIndex][c];
            tile.animate().translationXBy(10).setDuration(50)
                    .withEndAction(() -> tile.animate().translationXBy(-20).setDuration(50)
                            .withEndAction(() -> tile.animate().translationXBy(10).setDuration(50).start())
                            .start())
                    .start();
        }
    }

    private void updateKeyboardColor(char letter, int newColor) {
        for (Button button : keyboardButtons) {
            if (button != null && button.getText().toString().equals(String.valueOf(letter))) {
                int currentColor = Color.TRANSPARENT;
                if (button.getBackground() instanceof ColorDrawable) {
                    currentColor = ((ColorDrawable) button.getBackground()).getColor();
                }

                // Prioritize stronger colors
                if (currentColor == Color.GREEN) return;
                if (currentColor == Color.YELLOW && newColor == Color.GRAY) return;

                // Clear theme tint and apply color directly
                button.setBackgroundTintList(null);
                button.setBackgroundColor(newColor);

                if (newColor == Color.GRAY) {
                    button.setEnabled(false);
                }
                break;
            }
        }
    }

    private void disableAllButtons() {
        for (Button button : keyboardButtons) {
            if (button != null) {
                button.setEnabled(false);
            }
        }
        findViewById(R.id.btnBackspace).setEnabled(false);
        findViewById(R.id.btnEnter).setEnabled(false);
    }
}
