package com.example.library;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_book);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // אתחול Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // קישור רכיבי ה-UI
        etBookTitle = findViewById(R.id.etBookTitle);
        btnSaveBook = findViewById(R.id.btnSaveBook);

        // האזנה לכפתור שמירת הספר
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

        // יצירת מבנה נתונים לשמירה במסד הנתונים
        Map<String, Object> bookMap = new HashMap<>();
        bookMap.put("title", title);
        bookMap.put("userId", userId);
        // אפשר להוסיף שדה זמן יצירה אם נרצה בעתיד
        bookMap.put("timestamp", System.currentTimeMillis());

        // שמירה באוסף "books" ב-Firestore
        db.collection("books")
                .add(bookMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddBookActivity.this, "הספר נוסף בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish(); // סגירת המסך וחזרה למסך הבית
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddBookActivity.this, "שגיאה בשמירת הספר: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}