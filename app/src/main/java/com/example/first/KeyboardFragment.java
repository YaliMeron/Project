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
        String keys = "QWERTYUIOPASDFGHJKLZXCVBNM";
        for (int i = 0; i < keys.length(); i++) {
            String buttonId = "btn" + keys.charAt(i);
            int resId = getResources().getIdentifier(buttonId, "id", requireActivity().getPackageName());
            keyboardButtons[i] = view.findViewById(resId);
        }

        view.findViewById(R.id.btnEnter).setOnClickListener(v -> {
            if (callback != null) {
                callback.onEnterPressed();
            }
        });

        view.findViewById(R.id.btnBackspace).setOnClickListener(v -> {
            if (callback != null) {
                callback.onBackspacePressed();
            }
        });

        for (int i = 0; i < keys.length(); i++) {
            final int index = i;
            keyboardButtons[i].setOnClickListener(v -> {
                if (callback != null) {
                    callback.onKeyPressed(keys.charAt(index));
                }
            });
        }
    }

    public void enableKeyboard() {
        for (Button button : keyboardButtons) {
            if (button != null) {
                button.setEnabled(true);
            }
        }
        if (getView() != null) {
            getView().findViewById(R.id.btnEnter).setEnabled(true);
            getView().findViewById(R.id.btnBackspace).setEnabled(true);
        }
    }

    public void disableKeyboard() {
        for (Button button : keyboardButtons) {
            if (button != null) {
                button.setEnabled(false);
            }
        }
        if (getView() != null) {
            getView().findViewById(R.id.btnEnter).setEnabled(false);
            getView().findViewById(R.id.btnBackspace).setEnabled(false);
        }
    }
} 