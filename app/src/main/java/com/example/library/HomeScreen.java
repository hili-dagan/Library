package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeScreen extends AppCompatActivity {

    private RecyclerView recyclerViewBooks;
    private FloatingActionButton fabAddBook;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // שינוי ל-List של אובייקטי Book ולא סתם מחרוזות טקסט
    private List<Book> bookList;
    private BookAdapter bookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // אתחול רכיבי Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // קישור רכיבי ה-UI מתוך ה-XML
        recyclerViewBooks = findViewById(R.id.recyclerViewBooks);
        fabAddBook = findViewById(R.id.fabAddBook);

        // הגדרת ה-RecyclerView וה-Adapter עם האזנה ללחיצה על ספר
        recyclerViewBooks.setLayoutManager(new LinearLayoutManager(this));
        bookList = new ArrayList<>();

        // כאן משולבת השורה המעודקת שמעבירה את הרשימה ואת פעולת הלחיצה למסך הפרטים!
        bookAdapter = new BookAdapter(bookList, book -> {
            Intent intent = new Intent(HomeScreen.this, BookDetailsActivity.class);
            intent.putExtra("BOOK_ID", book.getId());
            startActivity(intent);
        });

        recyclerViewBooks.setAdapter(bookAdapter);

        // האזנה ללחיצה על הכפתור הצף (+) מעבר למסך הוספת ספר
        fabAddBook.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreen.this, AddBookActivity.class);
            startActivity(intent);
        });

        // טעינת הספרים בזמן אמת
        loadUserBooks();
    }

    private void loadUserBooks() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // אם אין משתמש מחובר, חזרה למסך ההתחברות
            Intent intent = new Intent(HomeScreen.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        String userId = currentUser.getUid();

        // שימוש ב-addSnapshotListener כדי שהספרים יופיעו מיד ברגע שמוסיפים אותם!
        db.collection("books")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "שגיאה בטעינת הספרים: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (value != null) {
                        bookList.clear();
                        for (QueryDocumentSnapshot document : value) {
                            // המרת המסמך ישירות לאובייקט Book
                            Book book = document.toObject(Book.class);
                            book.setId(document.getId()); // שמירת מזהה המסמך לצורך מעבר למסך הפרטים
                            bookList.add(book);
                        }
                        // עדכון ה-Adapter שהגיעו נתונים חדשים
                        bookAdapter.notifyDataSetChanged();
                    }
                });
    }
}