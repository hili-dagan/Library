package com.example.library;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddBookActivity extends AppCompatActivity {

    private EditText etBookTitle;
    private Button btnSaveBook;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        // אתחול Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // קישור רכיבים מן ה-XML
        etBookTitle = findViewById(R.id.etBookTitle);
        btnSaveBook = findViewById(R.id.btnSaveBook);

        // האזנה ללחיצה על כפתור השמירה
        btnSaveBook.setOnClickListener(v -> saveBookToFirestore());
    }

    private void saveBookToFirestore() {
        String title = etBookTitle.getText().toString().trim();

        if (title.isEmpty()) {
            etBookTitle.setError("נא להזין שם ספר");
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "שגיאה: משתמש לא מחובר", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // יצירת מבנה הנתונים לשמירה ב-Firestore
        Map<String, Object> book = new HashMap<>();
        book.put("title", title);
        book.put("userId", userId); // שיוך מדויק למשתמש המחובר לטובת פרטיות

        // הוספה לקולקשן "books"
        db.collection("books")
                .add(book)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddBookActivity.this, "הספר נוסף בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish(); // סגירת המסך וחזרה אוטומטית למסך הבית (שיירענן את הרשימה בזכות ה-onResume)
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddBookActivity.this, "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
