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
    private FloatingActionButton btnProfile; // עודכן ל-FloatingActionButton

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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
        btnProfile = findViewById(R.id.btnProfile); // אתחול כפתור הפרופיל הצף

        // הגדרת ה-RecyclerView וה-Adapter עם האזנה ללחיצה על ספר
        recyclerViewBooks.setLayoutManager(new LinearLayoutManager(this));
        bookList = new ArrayList<>();

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

        // האזנה ללחיצה על כפתור הפרופיל הצף מעבר למסך הפרופיל
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreen.this, ProfileActivity.class);
            startActivity(intent);
        });

        // טעינת הספרים בזמן אמת
        loadUserBooks();
    }

    private void loadUserBooks() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(HomeScreen.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        String userId = currentUser.getUid();

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
                            Book book = document.toObject(Book.class);
                            book.setId(document.getId());
                            bookList.add(book);
                        }
                        bookAdapter.notifyDataSetChanged();
                    }
                });
    }
}