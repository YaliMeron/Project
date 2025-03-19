package com.example.first;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GameLogic {
    private String answer;
    private StringBuilder currentGuess;
    private int row;
    private TextView[][] grid;
    private Button[] keyboardButtons;
    private final GameLogicCallback callback;

    public interface GameLogicCallback {
        void onGameWon();
        void onGameLost();
        void onInvalidGuess();
    }

    public GameLogic(TextView[][] grid, Button[] keyboardButtons, GameLogicCallback callback) {
        this.grid = grid;
        this.keyboardButtons = keyboardButtons;
        this.callback = callback;
        this.currentGuess = new StringBuilder();
        this.row = 0;
        initializeKeyboard();
    }

    public void setAnswer(String answer) {
        if (answer != null && answer.length() == 5) {
            this.answer = answer.toUpperCase();
        }
    }

    private void initializeKeyboard() {
        String keys = "QWERTYUIOPASDFGHJKLZXCVBNM";
        for (int i = 0; i < keys.length(); i++) {
            final int index = i;
            keyboardButtons[i].setOnClickListener(v -> addLetter(keys.charAt(index)));
        }
    }

    public void addLetter(char letter) {
        if (currentGuess.length() < 5) {
            int col = currentGuess.length();
            grid[row][col].setText(String.valueOf(letter));
            currentGuess.append(letter);

            grid[row][col].setScaleX(0.8f);
            grid[row][col].setScaleY(0.8f);
            grid[row][col].animate().scaleX(1f).scaleY(1f).setDuration(100).start();
        }
    }

    public void removeLetter() {
        if (currentGuess.length() > 0) {
            int col = currentGuess.length() - 1;
            grid[row][col].setText("");
            currentGuess.deleteCharAt(col);
        }
    }

    public void checkWord() {
        if (answer == null || answer.length() != 5) {
            callback.onInvalidGuess();
            return;
        }

        if (currentGuess.length() < 5) {
            shakeRow(row);
            callback.onInvalidGuess();
            return;
        }

        String guess = currentGuess.toString().toUpperCase();
        boolean[] correct = new boolean[5];
        boolean[] used = new boolean[5];
        int[] colors = new int[5];

        // First pass: Mark correct letters (green)
        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                correct[i] = true;
                used[i] = true;
                colors[i] = Color.GREEN;
            }
        }

        // Second pass: Mark yellow letters (correct letter, wrong position)
        for (int i = 0; i < 5; i++) {
            if (!correct[i]) {
                for (int j = 0; j < 5; j++) {
                    if (!used[j] && guess.charAt(i) == answer.charAt(j)) {
                        used[j] = true;
                        colors[i] = Color.YELLOW;
                        break;
                    }
                }
            }
        }

        // Third pass: Mark remaining letters as gray
        for (int i = 0; i < 5; i++) {
            if (!correct[i] && colors[i] != Color.YELLOW) {
                colors[i] = Color.GRAY;
            }
        }

        // Animate the color changes
        for (int i = 0; i < 5; i++) {
            int finalColor = colors[i];
            int finalI = i;
            char letter = guess.charAt(i);
            
            new Handler().postDelayed(() -> {
                flipTile(grid[row][finalI], letter, finalColor, 0);
                updateKeyboardColor(letter, finalColor);
            }, i * 300);
        }

        new Handler().postDelayed(() -> {
            if (guess.equals(answer)) {
                callback.onGameWon();
            } else if (row < 5) {
                row++;
                currentGuess.setLength(0);
            } else {
                callback.onGameLost();
            }
        }, 5 * 300 + 300);
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

                // Only update keyboard color if:
                // 1. Current color is transparent (not set yet)
                // 2. New color is green (always show green)
                // 3. Current color is gray and new color is yellow (upgrade to yellow)
                if (currentColor == Color.TRANSPARENT || 
                    newColor == Color.GREEN || 
                    (currentColor == Color.GRAY && newColor == Color.YELLOW)) {
                    button.setBackgroundTintList(null);
                    button.setBackgroundColor(newColor);
                }
                break;
            }
        }
    }

    public void disableAllButtons() {
        for (Button button : keyboardButtons) {
            if (button != null) {
                button.setEnabled(false);
            }
        }
    }
} 