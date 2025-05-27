package com.example.first;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
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

    public static void fetchWord(Context context, WordFetchCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
        Handler handler = new Handler(Looper.getMainLooper());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                API_URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        try {
                if (jsonArray.length() == 0) {
                    throw new Exception("Empty response from API");
                }
                Random random = new Random();
                int randomIndex = random.nextInt(jsonArray.length());
                JSONObject wordObj = jsonArray.getJSONObject(randomIndex);
                String word = wordObj.getString("word").toUpperCase();
                if (word.length() != 5 || !word.matches("[A-Z]+")) {
                    throw new Exception("Invalid word received: " + word);
                }
                            Log.d(TAG, "Successfully fetched a word: \"" + word + "\"");
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onWordFetched(word);
                                }
                            });
            } catch (Exception e) {
                            Log.e(TAG, "Error parsing word: " + e.getMessage(), e);
                            useFallbackWord(handler, callback);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley error: " + error.getMessage(), error);
                        useFallbackWord(handler, callback);
                    }
                }
        );
        queue.add(jsonArrayRequest);
    }

    private static void useFallbackWord(Handler handler, WordFetchCallback callback) {
                Random random = new Random();
                String fallbackWord = FALLBACK_WORDS[random.nextInt(FALLBACK_WORDS.length)];
        handler.post(() -> callback.onWordFetched(fallbackWord));
    }
}
