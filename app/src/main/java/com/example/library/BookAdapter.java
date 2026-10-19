package com.example.library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    // משתנה ששומר את רשימת הספרים
    private List<String> bookList;

    // בנאי (Constructor) - מקבל את רשימת הספרים מבחוץ ומאתחל אותם באדפטר
    public BookAdapter(List<String> bookList) {
        this.bookList = bookList;
    }

    @NonNull
    @Override
    // פעולה מרכזית 1: יוצרת את התבנית החזותית של שורה בודדת (מחברת את קובץ ה-XML של הפריט)
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    // פעולה מרכזית 2: מקשרת ומכניסה את הנתונים האמיתיים (שם הספר) לתוך הרכיבים הגרפיים עבור כל שורה לפי המיקום שלה
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        String bookTitle = bookList.get(position);
        holder.textViewBookTitle.setText(bookTitle);
    }

    @Override
    // פעולה מרכזית 3: מחזירה את כמות הפריטים הכוללת ברשימה, כדי שהרשימה תדע כמה שורות לייצר
    public int getItemCount() {
        return bookList.size();
    }

    // מחלקה פנימית שאחראית לשמור את ההפניות לרכיבי ה-UI של השורה (כדי לחסוך חיפושים בזיכרון)
    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView textViewBookTitle;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            // קישור בין משתנה ה-Java לרכיב ה-TextView מתוך קובץ ה-XML
            textViewBookTitle = itemView.findViewById(R.id.tvBookTitle);
        }
    }
}