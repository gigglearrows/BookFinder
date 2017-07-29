package com.example.android.bookfinder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An {@link com.example.android.bookfinder.BookAdapter} knows how to create a list item layout for each book
 * in the data source (a list of {@link Book} objects).
 * <p>
 * These list item layouts will be provided to an adapter view like ListView
 * to be displayed to the user.
 */
public class BookAdapter extends ArrayAdapter<Book> {

    /**
     * Constructs a new {@link com.example.android.bookfinder.BookAdapter}.
     *
     * @param context of the app
     * @param books   is the list of books, which is the data source of the adapter
     */
    public BookAdapter(Context context, List<Book> books) {
        super(context, 0, books);
    }

    /**
     * Returns a list item view that displays information about the book at the given position
     * in the list of books.
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        // Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if convertView is null, then inflate a new list item layout.
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.book_list_item, parent, false);
            holder = new ViewHolder(listItemView);
            listItemView.setTag(holder);
        } else {
            holder = (ViewHolder) listItemView.getTag();
        }

        // Find the book at the given position in the list of books
        Book currentBook = getItem(position);

        // Display the title of the current book in that TextView
        holder.titleView.setText(currentBook.getTitle());

        // Display the author of the current book in that TextView
        holder.authorView.setText(currentBook.getAuthor());

        // Display the genre of the current book in that TextView
        holder.genreView.setText(currentBook.getBookPrice());

        //holder.thumbnailView.setImageURI();
        Picasso.with(getContext()).load(currentBook.getImageUrl()).placeholder(R.drawable.placeholder_book).into(holder.thumbnailView);

        // Return the list item view that is now showing the appropriate data
        return listItemView;
    }

    static class ViewHolder {
        @BindView(R.id.book_title)
        TextView titleView;
        @BindView(R.id.book_author)
        TextView authorView;
        @BindView(R.id.book_genre)
        TextView genreView;
        @BindView(R.id.book_thumbnail)
        ImageView thumbnailView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
