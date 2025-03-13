package com.example.first;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WordFetcher {
    private static final String API_URL = "https://random-word-api.herokuapp.com/word?length=5";

    public interface WordFetchCallback {
        void onWordFetched(String word);
        void onError(Exception e);
    }

    public static void fetchWord(WordFetchCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                String word = jsonArray.getString(0).toUpperCase(); // Convert to uppercase

                // Send result back to UI thread
                handler.post(() -> callback.onWordFetched(word));

            } catch (Exception e) {
                handler.post(() -> callback.onError(e));
            }
        });
    }
}
