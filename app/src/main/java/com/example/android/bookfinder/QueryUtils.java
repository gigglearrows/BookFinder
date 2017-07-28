/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.bookfinder;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Helper methods related to requesting and receiving book data.
 */
public final class QueryUtils {
    private static final String TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the USGS dataset and return a list of {@link Book} objects.
     */
    public static List<Book> fetchBooks(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Book}s
        // Return the list of {@link Book}s
        return extractFromJson(jsonResponse);
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpsURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Book} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<Book> extractFromJson(String earthquakeJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(earthquakeJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding books to
        List<Book> books = new ArrayList<>();

        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(earthquakeJSON);

            // Extract the JSONArray associated with the key called "items",
            // which represents a list of books.
            if (baseJsonResponse.has("items")) {
                JSONArray bookArray = baseJsonResponse.getJSONArray("items");

                // For each book in the bookArray, create a {@link Book} object
                for (int i = 0; i < bookArray.length(); i++) {
                    JSONObject currentBook = bookArray.getJSONObject(i);
                    JSONObject bookInfo = currentBook.getJSONObject("volumeInfo");

                    // Extract the value for the key called "title"
                    String title = bookInfo.getString("title");

                    // Extract the value of the first element in the JSONArray called "authors"
                    String author;
                    if (bookInfo.has("authors")) {
                        author = bookInfo.getJSONArray("authors").getString(0);
                    } else {
                        author = "Unknown author";
                    }

                    // Extract the value for the key called "smallThumbnail"
                    String imageUrl;
                    if (bookInfo.has("imageLinks")) {
                        imageUrl = bookInfo.getJSONObject("imageLinks").getString("smallThumbnail");
                    } else {
                        imageUrl = null;
                    }

                    // Extract the value for the key called "url"
                    String bookUrl = bookInfo.getString("infoLink");

                    JSONObject bookPriceInfo = currentBook.getJSONObject("saleInfo");

                    String bookPrice = "";
                    try {
                        if (bookPriceInfo.getString("saleability").equals("FOR_SALE")) {
                            JSONObject bookRetailPrice = bookPriceInfo.getJSONObject("retailPrice");
                            bookPrice = bookRetailPrice.getString("amount") + bookRetailPrice.getString("currencyCode");
                        } else {
                            bookPrice = "Not for sale";
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Unable to get JSON objects");
                    }

                    // Create a new {@link Book} object with the title, author, imageUrl,
                    // and bookUrl from the JSON response.
                    Book book = new Book(title, author, imageUrl, bookUrl, bookPrice);

                    // Add the new {@link Book} to the list of books.
                    books.add(book);
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "Problem parsing the JSON results", e);
        }

        // Return the list of books
        return books;
    }

}
