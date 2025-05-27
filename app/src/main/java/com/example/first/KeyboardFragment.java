package com.example.first;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;

public class KeyboardFragment extends Fragment {
    private Button[] keyboardButtons = new Button[26];
    private KeyboardCallback callback;

    public interface KeyboardCallback {
        void onKeyPressed(char letter);
        void onEnterPressed();
        void onBackspacePressed();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_keyboard, container, false);
        initializeKeyboard(view);
        return view;
    }

    public void setCallback(KeyboardCallback callback) {
        this.callback = callback;
    }

    private void initializeKeyboard(View view) {
        // Initialize letter buttons
        String keys = "QWERTYUIOPASDFGHJKLZXCVBNM";
        for (int i = 0; i < keys.length(); i++) {
            String buttonId = "btn" + keys.charAt(i);
            int resId = getResources().getIdentifier(buttonId, "id", requireActivity().getPackageName());
            keyboardButtons[i] = view.findViewById(resId);
            if (keyboardButtons[i] != null) {
                final int index = i;  // Create a final copy of the index
                keyboardButtons[i].setText(String.valueOf(keys.charAt(i)));
                keyboardButtons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callback != null) {
                            callback.onKeyPressed(keys.charAt(index));
                        }
                    }
                });
            }
        }

        // Initialize Enter button
        Button enterButton = view.findViewById(R.id.btnEnter);
        if (enterButton != null) {
            enterButton.setText("ENTER");
            enterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null) {
                        callback.onEnterPressed();
                    }
                }
            });
        }

        // Initialize Backspace button
        Button backspaceButton = view.findViewById(R.id.btnBackspace);
        if (backspaceButton != null) {
            backspaceButton.setText("⌫");
            backspaceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null) {
                        callback.onBackspacePressed();
                    }
                }
            });
        }
    }

    public void enableKeyboard() {
        for (Button button : keyboardButtons) {
            if (button != null) {
                button.setEnabled(true);
                button.setAlpha(1.0f);
            }
        }
        if (getView() != null) {
            Button enterButton = getView().findViewById(R.id.btnEnter);
            Button backspaceButton = getView().findViewById(R.id.btnBackspace);
            if (enterButton != null) {
                enterButton.setEnabled(true);
                enterButton.setAlpha(1.0f);
            }
            if (backspaceButton != null) {
                backspaceButton.setEnabled(true);
                backspaceButton.setAlpha(1.0f);
            }
        }
    }

    public void disableKeyboard() {
        for (Button button : keyboardButtons) {
            if (button != null) {
                button.setEnabled(false);
                button.setAlpha(0.5f);
            }
        }
        if (getView() != null) {
            Button enterButton = getView().findViewById(R.id.btnEnter);
            Button backspaceButton = getView().findViewById(R.id.btnBackspace);
            if (enterButton != null) {
                enterButton.setEnabled(false);
                enterButton.setAlpha(0.5f);
            }
            if (backspaceButton != null) {
                backspaceButton.setEnabled(false);
                backspaceButton.setAlpha(0.5f);
            }
        }
    }

    public void setKeyColor(char letter, int color) {
        String keys = "QWERTYUIOPASDFGHJKLZXCVBNM";
        int index = keys.indexOf(Character.toUpperCase(letter));
        if (index >= 0 && keyboardButtons[index] != null) {
            int wordleColor = color;
            if (color == android.graphics.Color.GREEN) {
                wordleColor = 0xFF6AAA64; // Wordle green
            } else if (color == android.graphics.Color.YELLOW) {
                wordleColor = 0xFFC9B458; // Wordle yellow
            } else if (color == android.graphics.Color.GRAY) {
                wordleColor = 0xFF787C7E; // Wordle gray
            } else if (color == android.graphics.Color.WHITE) {
                wordleColor = 0xFFFFFFFF; // White
            }
            keyboardButtons[index].setBackgroundColor(wordleColor);
        }
    }

    public void resetKeyColors() {
        for (Button button : keyboardButtons) {
            if (button != null) {
                button.setBackgroundResource(R.drawable.keyboard_button_background);
            }
        }
    }
} 