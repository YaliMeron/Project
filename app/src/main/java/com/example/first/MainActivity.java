package com.example.first;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private final String ANSWER = "STAGA";
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
            Toast.makeText(this, "Enter a 5-letter word", Toast.LENGTH_SHORT).show();
            return;
        }

        String guess = currentGuess.toString();
        boolean[] correct = new boolean[5];

        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == ANSWER.charAt(i)) {
                grid[row][i].setBackgroundColor(Color.GREEN);
                correct[i] = true;
            }
        }

        for (int i = 0; i < 5; i++) {
            if (!correct[i] && ANSWER.contains(String.valueOf(guess.charAt(i)))) {
                grid[row][i].setBackgroundColor(Color.YELLOW);
            } else if (!correct[i]) {
                grid[row][i].setBackgroundColor(Color.GRAY);
                disableButton(guess.charAt(i));
            }
        }

        if (guess.equals(ANSWER)) {
            Toast.makeText(this, "You guessed it!", Toast.LENGTH_LONG).show();
        } else if (row < 5) {
            row++;
            currentGuess.setLength(0);
        } else {
            Toast.makeText(this, "Game Over!", Toast.LENGTH_LONG).show();
        }
    }

    private void disableButton(char letter) {
        for (Button button : keyboardButtons) {
            if (button.getText().toString().equals(String.valueOf(letter))) {
                button.setEnabled(false);
                button.setBackgroundColor(Color.DKGRAY);
                break;
            }
        }
    }
}



















