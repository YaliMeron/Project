package com.example.first;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class GameLogic {
    private String answer;
    private StringBuilder currentGuess;
    private int row;
    private TextView[][] grid;
    private final GameLogicCallback callback;
    private final KeyboardColorCallback colorCallback;
    private Set<String> validWords;

    public interface GameLogicCallback {
        void onGameWon();
        void onGameLost(String answer);
        void onInvalidGuess();
        void onInvalidWord();
    }

    public interface KeyboardColorCallback {
        void updateKeyboardKeyColor(char letter, int color);
    }

    public GameLogic(TextView[][] grid, GameLogicCallback callback, KeyboardColorCallback colorCallback) {
        this.grid = grid;
        this.callback = callback;
        this.colorCallback = colorCallback;
        this.currentGuess = new StringBuilder();
        this.row = 0;
        loadValidWords();
    }

    private void loadValidWords() {
        validWords = new HashSet<>();
        try {
            InputStream is = grid[0][0].getContext().getResources().openRawResource(R.raw.words);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                validWords.add(line.toUpperCase());
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAnswer(String answer) {
        if (answer != null && answer.length() == 5) {
            this.answer = answer.toUpperCase();
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
        if (!validWords.contains(guess)) {
            shakeRow(row);
            callback.onInvalidWord();
            return;
        }

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

        // Animate the color changes and update keyboard
        for (int i = 0; i < 5; i++) {
            int finalColor = colors[i];
            int finalI = i;
            char letter = guess.charAt(i);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    flipTile(grid[row][finalI], letter, finalColor, 0);
                    if (colorCallback != null) colorCallback.updateKeyboardKeyColor(letter, finalColor);
                }
            }, i * 300);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (guess.equals(answer)) {
                    callback.onGameWon();
                } else if (row < 5) {
                    row++;
                    currentGuess.setLength(0);
                } else {
                    callback.onGameLost(answer);
                }
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
                    // Set the appropriate drawable based on the color
                    if (color == Color.GREEN) {
                        tile.setBackgroundResource(R.drawable.wordle_tile_green);
                    } else if (color == Color.YELLOW) {
                        tile.setBackgroundResource(R.drawable.wordle_tile_yellow);
                    } else {
                        tile.setBackgroundResource(R.drawable.wordle_tile_gray);
                    }
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
} 