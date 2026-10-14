package com.example.library;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeScreen extends AppCompatActivity {

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

        // הגדרת ה-RecyclerView וה-Adapter
        recyclerViewBooks.setLayoutManager(new LinearLayoutManager(this));
        bookList = new ArrayList<>();
        bookAdapter = new BookAdapter(bookList);
        recyclerViewBooks.setAdapter(bookAdapter);

        // בדיקה מי המשתמש שמחובר כרגע ושליפת הספרים שלו בלבד מהענן
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            loadUserBooks(currentUserId);
        } else {
            // אם אין משתמש מחובר, החזרה למסך ההתחברות
            Intent intent = new Intent(HomeScreen.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // האזנה ללחיצה על הכפתור הצף (+) מעבר למסך הוספת ספר
        fabAddBook.setOnClickListener(v -> {
            // ודאי שיש לך אקטיביטי בשם AddBookActivity בפרויקט (או שימי את השם הנכון של מסך ההוספה שלך)
            Intent intent = new Intent(HomeScreen.this, AddBookActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserBooks(String userId) {
        // שאילתה מול CloudFirestore: שליפת כל הספרים שבהם שדה ה-userId שווה בדיוק ל-UID של המשתמש הנוכחי
        db.collection("books")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // שליפת שם הספר מתוך המסמך
                            String title = document.getString("title");
                            if (title != null) {
                                bookList.add(title);
                            }
                        }
                        // עדכון ה-Adapter שהגיעו נתונים חדשים כדי שירענן את הרשימה במסך
                        bookAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "שגיאה בטעינת הספרים: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }
}