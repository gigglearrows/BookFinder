package com.example.android.bookfinder;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Loads a list of books by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class BookLoader extends AsyncTaskLoader<List<Book>> {
    private static final String TAG = BookLoader.class.getName();

    private String url;

    /**
     * Constructs a new {@link BookLoader}.
     *
     * @param context of the activity
     * @param uurl    to load data from
     */
    public BookLoader(Context context, String uurl) {
        super(context);
        url = uurl;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<Book> loadInBackground() {
        if (url == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of books.
        return QueryUtils.fetchBooks(url);
    }
}
