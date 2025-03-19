package com.example.first;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WordFetcher {
    private static final String TAG = "WordFetcher";
    private static final String API_URL = "https://api.datamuse.com/words?sp=?????&max=1&v=en&md=d&f=5";

    public interface WordFetchCallback {
        void onWordFetched(String word);
        void onError(Exception e);
    }

    public static void fetchWord(WordFetchCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Add timestamp to prevent caching
                String urlString = API_URL + "&t=" + System.currentTimeMillis();
                Log.d(TAG, "Starting word fetch from: " + urlString);
                
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000); // 5 seconds timeout
                connection.setReadTimeout(5000);    // 5 seconds timeout
                connection.setRequestProperty("Cache-Control", "no-cache");

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception("HTTP error code: " + responseCode);
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

                String word = jsonArray.getJSONObject(0).getString("word").toUpperCase();
                Log.d(TAG, "Successfully fetched word: " + word);

                // Send result back to UI thread
                handler.post(() -> callback.onWordFetched(word));

            } catch (Exception e) {
                Log.e(TAG, "Error fetching word", e);
                handler.post(() -> callback.onError(e));
            } finally {
                executor.shutdown();
            }
        });
    }
}
