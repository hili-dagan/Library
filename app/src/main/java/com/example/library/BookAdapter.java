package com.example.library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    // ממשק להעברת אירוע הבלחיצה אל ה-Activity
    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    private List<Book> bookList;
    private OnBookClickListener listener;

    public BookAdapter(List<Book> bookList, OnBookClickListener listener) {
        this.bookList = bookList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.tvBookTitle.setText(book.getTitle());

        // הצגת כותב/עמודים במידה ויש תת-כותרת
        if (book.getAuthor() != null && !book.getAuthor().isEmpty()) {
            holder.tvBookSubtitle.setText("מאת: " + book.getAuthor());
        } else {
            holder.tvBookSubtitle.setText("לחצי לצפייה בפרטים");
        }

        // לחיצה על כל השורה במסך
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookTitle, tvBookSubtitle;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookSubtitle = itemView.findViewById(R.id.tvBookSubtitle);
        }
    }
}