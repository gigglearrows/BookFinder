package com.example.android.bookfinder;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookActivity extends AppCompatActivity implements LoaderCallbacks<List<Book>>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = BookActivity.class.getName();

    private static final String BOOK_REQUEST_URL =
            "https://www.googleapis.com/books/v1/volumes";

    private static final int BOOK_LOADER_ID = 1;
    /**
     * TextView that is displayed when the list is empty + rest of the views
     */
    @BindView(R.id.empty_view) TextView emptyStateTextView;
    @BindView(R.id.list) ListView bookListView;
    @BindView(R.id.loading_indicator) View loadingIndicator;
    private BookAdapter adapter;
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        ButterKnife.bind(this);

        bookListView.setEmptyView(emptyStateTextView);

        adapter = new BookAdapter(this, new ArrayList<Book>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        bookListView.setAdapter(adapter);

        // Obtain a reference to the SharedPreferences file for this app
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // And register to be notified of preference changes
        // So we know when the user has adjusted the query settings
        prefs.registerOnSharedPreferenceChangeListener(this);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected earthquake.
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Book currentBook = adapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri bookUri = Uri.parse(currentBook.getBookUrl());

                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, bookUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        // If there is a network connection, fetch data
        if (checkNetwork()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(BOOK_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            emptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(getString(R.string.settings_max_results_key)) ||
                key.equals(getString(R.string.settings_order_by_key))) {

            // Clear the ListView as a new query will be kicked off
            adapter.clear();

            if (checkNetwork()) {
                // Hide the empty state text view as the loading indicator will be displayed
                emptyStateTextView.setVisibility(View.GONE);

                // Show the loading indicator while new data is being fetched
                loadingIndicator.setVisibility(View.VISIBLE);

                Bundle bundle = new Bundle();
                if (!searchQuery.isEmpty()) {
                    bundle.putString("searchQuery", searchQuery);
                } else {
                    bundle = null;
                }

                // Restart the loader to requery as the query settings have been updated
                getLoaderManager().restartLoader(BOOK_LOADER_ID, bundle, this);
            } else {
                // First, hide loading indicator so error message will be visible
                loadingIndicator.setVisibility(View.GONE);

                // Update empty state with no connection error message
                emptyStateTextView.setVisibility(View.VISIBLE);
                emptyStateTextView.setText(R.string.no_internet_connection);
            }
        }
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String maxResults = sharedPrefs.getString(
                getString(R.string.settings_max_results_key),
                getString(R.string.settings_max_results_default));

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        String query;
        if (bundle != null) {
            query = bundle.getString("searchQuery");
        } else {
            query = "android";
        }

        Uri baseUri = Uri.parse(BOOK_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("q", query);
        uriBuilder.appendQueryParameter("maxResults", maxResults);
        uriBuilder.appendQueryParameter("orderBy", orderBy);
        Log.d(TAG, uriBuilder.toString());
        return new BookLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {
        // Hide loading indicator because the data has been loaded
        loadingIndicator.setVisibility(View.GONE);

        emptyStateTextView.setText(R.string.no_books);

        // Clear the adapter of previous data
        adapter.clear();

        // If there is a valid list of {@link Book}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (books != null && !books.isEmpty()) {
            adapter.addAll(books);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        // Loader reset, so we can clear out our existing data.
        adapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                searchBooks(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchBooks(String searchQuery) {
        Bundle bundle = new Bundle();
        bundle.putString("searchQuery", searchQuery);

        //use the query to search your data somehow

        // Clear the ListView as a new query will be kicked off
        adapter.clear();

        if (checkNetwork()) {

            // Hide the empty state text view as the loading indicator will be displayed
            emptyStateTextView.setVisibility(View.GONE);

            // Show the loading indicator while new data is being fetched
            loadingIndicator.setVisibility(View.VISIBLE);

            // Restart the loader to requery as the query settings have been updated
            getLoaderManager().restartLoader(BOOK_LOADER_ID, bundle, this);
        } else {
            // First, hide loading indicator so error message will be visible
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            emptyStateTextView.setVisibility(View.VISIBLE);
            emptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    private boolean checkNetwork() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        return (networkInfo != null && networkInfo.isConnected());
    }
}
