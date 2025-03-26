package com.example.first;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;

public class WordFetcher {
    private static final String TAG = "WordFetcher";
    private static final String API_URL = "https://api.datamuse.com/words?sp=?????";
    private static final String[] FALLBACK_WORDS = {
        "WATER", "STARE", "CRANE", "SLATE", "SHARE",
        "STORE", "SPARE", "SCARE", "SHAPE", "SPACE"
    };

    public interface WordFetchCallback {
        void onWordFetched(String word);
        void onError(Exception e);
    }

    public static void fetchWord(WordFetchCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Log.d(TAG, "Starting word fetch from: " + API_URL);
                
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    String errorMessage = "HTTP error code: " + responseCode;
                    Log.e(TAG, errorMessage);
                    throw new Exception(errorMessage);
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Log.d(TAG, "Raw response: " + response.toString());

                JSONArray jsonArray = new JSONArray(response.toString());
                if (jsonArray.length() == 0) {
                    throw new Exception("Empty response from API");
                }

                // Get a random word from the response
                Random random = new Random();
                int randomIndex = random.nextInt(jsonArray.length());
                JSONObject wordObj = jsonArray.getJSONObject(randomIndex);
                String word = wordObj.getString("word").toUpperCase();
                
                // Validate the word
                if (word.length() != 5 || !word.matches("[A-Z]+")) {
                    throw new Exception("Invalid word received: " + word);
                }

                Log.d(TAG, "Successfully fetched word: " + word);
                handler.post(() -> callback.onWordFetched(word));

            } catch (Exception e) {
                Log.e(TAG, "Error fetching word: " + e.getMessage(), e);
                // Use a random fallback word instead of always "WATER"
                Random random = new Random();
                String fallbackWord = FALLBACK_WORDS[random.nextInt(FALLBACK_WORDS.length)];
                handler.post(() -> {
                    Log.d(TAG, "Using fallback word: " + fallbackWord);
                    callback.onWordFetched(fallbackWord);
                });
            } finally {
                executor.shutdown();
            }
        });
    }
}
