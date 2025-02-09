package com.example.first;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView inputTextView;
    private StringBuilder currentInput = new StringBuilder();
    private static final int MAX_WORD_LENGTH = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure your XML file matches this

        inputTextView = findViewById(R.id.wordInput);
        setupKeyboard();
    }

    private void setupKeyboard() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for (char letter : letters.toCharArray()) {
            int buttonId = getResources().getIdentifier("btn" + letter, "id", getPackageName());
            if (buttonId != 0) {
                Button button = findViewById(buttonId);
                if (button != null) {
                    button.setOnClickListener(v -> onLetterClick(letter));
                }
            }
        }

        findViewById(R.id.btnEnter).setOnClickListener(v -> onEnterClick());
        findViewById(R.id.btnBackspace).setOnClickListener(v -> onBackspaceClick());
    }

    private void onLetterClick(char letter) {
        if (currentInput.length() < MAX_WORD_LENGTH) {
            currentInput.append(letter);
            updateDisplay();
        }
    }

    private void onBackspaceClick() {
        if (currentInput.length() > 0) {
            currentInput.deleteCharAt(currentInput.length() - 1);
            updateDisplay();
        }
    }

    private void onEnterClick() {
        if (currentInput.length() == MAX_WORD_LENGTH) {
            // Handle word submission logic
            validateWord(currentInput.toString());
            currentInput.setLength(0);
            updateDisplay();
        }
    }

    private void updateDisplay() {
        inputTextView.setText(currentInput.toString());
    }

    private void validateWord(String word) {
        // Implement your Wordle logic here (e.g., checking against a word list)
    }
}



















