package com.example.pageflix.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pageflix.activities.customer_activities.BookPopupActivity;
import com.example.pageflix.R;
import com.example.pageflix.entities.Book;

import java.util.List;

// BookAdapter class for RecyclerView
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
    List<Book> bookList ;
    public BookAdapter(List<Book> bookList){
        this.bookList = bookList ;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout for each item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }

    // Inside onBindViewHolder method of BookAdapter
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to ViewHolder
        Book book = bookList.get(position);
        holder.titleTextView.setText(book.getTitle());
        holder.authorTextView.setText("Author: " + book.getAuthor());
        holder.yearTextView.setText("Publication Year: " + book.getYear());
        holder.categoryTextView.setText("Category: " + book.getCategory());

        // Set click listener for book item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to open popup window
                Intent intent = new Intent(v.getContext(), BookPopupActivity.class);
                // Pass book details to popup window
                intent.putExtra("title", book.getTitle());
                intent.putExtra("author", book.getAuthor());
                intent.putExtra("category", book.getCategory()) ;
                intent.putExtra("year", book.getYear());
                intent.putExtra("description", book.getDescription());
                intent.putExtra("bookID", book.ID);

                // Start activity to display popup window
                v.getContext().startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        // Return the size of the bookList
        return bookList.size();
    }

    // ViewHolder class for each item in RecyclerView
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, authorTextView, yearTextView, categoryTextView ;

        ViewHolder(View itemView) {
            super(itemView);
            // Initialize views
            titleTextView = itemView.findViewById(R.id.titleTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView) ;
            yearTextView = itemView.findViewById(R.id.yearTextView);
        }
    }
    public void setBookList(List<Book> bookList){
        this.bookList = bookList ;
        notifyDataSetChanged();
    }
}