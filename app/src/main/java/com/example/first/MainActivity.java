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
import android.app.AlertDialog;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity implements GameLogic.GameLogicCallback, KeyboardFragment.KeyboardCallback, GameLogic.KeyboardColorCallback {
    private TextView[][] grid = new TextView[6][5];
    private GameLogic gameLogic;
    private boolean isGameReady = false;
    private KeyboardFragment keyboardFragment;
    private String username;

    // Statistics overlay views
    private View statisticsOverlay;
    private TextView textGamesPlayed, textWins, textLosses, textWinRate;
    private Button buttonPlayAgain;
    private ImageButton buttonReset;
    private SharedPreferences statsPrefs;

    private ImageButton buttonSettings;
    private boolean musicOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TEST: Start music service directly to check if music plays
        startService(new Intent(this, MusicService.class));

        username = getIntent().getStringExtra("username");
        if (username == null) username = "guest";

        initializeGrid();
        initializeKeyboardFragment();
        initializeStatisticsOverlay();
        gameLogic = new GameLogic(grid, this, this);
        disableGameInput();
        fetchNewWord();
        statsPrefs = getSharedPreferences("game_stats", MODE_PRIVATE);

        // Initialize settings button
        ImageButton buttonSettings = findViewById(R.id.buttonSettings);
        this.buttonSettings = buttonSettings;
        loadSettings();
        applySettings();
        buttonSettings.setOnClickListener(v -> showSettingsDialog());

        // Initialize reset button
        ImageButton buttonReset = findViewById(R.id.buttonReset);
        this.buttonReset = buttonReset;
        buttonReset.setOnClickListener(v -> showResetConfirmationDialog());

        // Initialize statistics button
        ImageButton buttonStats = findViewById(R.id.buttonStats);
        buttonStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStatisticsDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (musicOn) {
            startService(new Intent(this, MusicService.class));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(new Intent(this, MusicService.class));
    }

    private void initializeKeyboardFragment() {
        keyboardFragment = new KeyboardFragment();
        keyboardFragment.setCallback(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.keyboardContainer, keyboardFragment)
                .commit();
    }

    private void fetchNewWord() {
        WordFetcher.fetchWord(this, new WordFetcher.WordFetchCallback() {
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

        statisticsOverlay.setVisibility(View.GONE);

        buttonPlayAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statisticsOverlay.setVisibility(View.GONE);
                resetGame();
            }
        });
    }

    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Reset Game")
            .setMessage("Are you sure you want to reset the game?")
            .setPositiveButton("Yes", (dialog, which) -> resetGame())
            .setNegativeButton("No", null)
            .show();
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
        if (keyboardFragment != null) {
            keyboardFragment.resetKeyColors();
            }
        gameLogic = new GameLogic(grid, this, this);
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
    public void onGameLost(String answer) {
        disableGameInput();
        // Update the statistics overlay to show the answer
        TextView textAnswer = findViewById(R.id.textAnswer);
        if (textAnswer != null) {
            textAnswer.setText("The word was: " + answer);
            textAnswer.setVisibility(View.VISIBLE);
        }
        showStatisticsOverlay(false);
    }

    @Override
    public void onInvalidGuess() {
        Toast.makeText(this, "Enter a 5-letter word", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInvalidWord() {
        Toast.makeText(this, "Not a valid English word", Toast.LENGTH_SHORT).show();
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

    // Add this method to update keyboard key color
    @Override
    public void updateKeyboardKeyColor(char letter, int color) {
        if (keyboardFragment != null) {
            keyboardFragment.setKeyColor(letter, color);
        }
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (48 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        // Music switch
        Switch musicSwitch = new Switch(this);
        musicSwitch.setText("Background Music");
        musicSwitch.setTextSize(20);
        musicSwitch.setChecked(musicOn);
        
        // Store initial state
        final boolean initialMusicState = musicOn;
        
        // Only apply changes when OK is pressed
        musicSwitch.setOnCheckedChangeListener(null); // Remove any existing listener
        
        layout.addView(musicSwitch);

        // Spacer View
        View spacer = new View(this);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (16 * getResources().getDisplayMetrics().density) // 16dp height
        );
        spacer.setLayoutParams(spacerParams);
        layout.addView(spacer);

        // Logout Button
        Button logoutButton = new Button(this);
        logoutButton.setText("Logout");
        logoutButton.setOnClickListener(v -> {
            // Stop music
            stopService(new Intent(this, MusicService.class));

            // Clear saved user login
            SharedPreferences loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
            loginPrefs.edit().remove("last_user").apply();

            // Navigate to LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close MainActivity
        });
        layout.addView(logoutButton);

        builder.setView(layout);
        builder.setPositiveButton("OK", (dialog, which) -> {
            musicOn = musicSwitch.isChecked();
            saveSettings();
            applySettings();
        });
        builder.setNegativeButton("Cancel", null); // No need to revert since we haven't applied changes
        builder.show();
    }

    private void saveSettings() {
        String prefix = "settings_" + username + "_";
        statsPrefs.edit()
                .putBoolean(prefix + "music_on", musicOn)
                .apply();
    }

    private void loadSettings() {
        String prefix = "settings_" + username + "_";
        musicOn = statsPrefs.getBoolean(prefix + "music_on", true);
    }

    private void applySettings() {
        if (musicOn) {
            startService(new Intent(this, MusicService.class));
        } else {
            stopService(new Intent(this, MusicService.class));
        }
    }

    private void showStatisticsDialog() {
        String prefix = "stats_" + username + "_";
        int gamesPlayed = statsPrefs.getInt(prefix + "games_played", 0);
        int wins = statsPrefs.getInt(prefix + "wins", 0);
        int losses = statsPrefs.getInt(prefix + "losses", 0);
        float winRate = gamesPlayed > 0 ? (wins * 100f / gamesPlayed) : 0f;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Statistics for " + username);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (48 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        TextView statsText = new TextView(this);
        statsText.setTextSize(18);
        statsText.setGravity(Gravity.CENTER);
        statsText.setText(String.format(
            "Games Played: %d\nWins: %d\nLosses: %d\nWin Rate: %.1f%%",
            gamesPlayed, wins, losses, winRate
        ));
        layout.addView(statsText);

        builder.setView(layout);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}
