package com.example.library;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class BookDetailsActivity extends AppCompatActivity {

    private EditText etTitle, etAuthor, etTotalPages, etCurrentPage;
    private ImageView ivBookCover;
    private Button btnSave, btnCamera, btnGallery;

    private FirebaseFirestore db;
    private String bookId;

    // משתנה לשמירת ה-Uri של התמונה שנבחרה או צולמה
    private Uri imageUri;

    // משגר לפתיחת הגלרייה ובחירת תמונה
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    ivBookCover.setImageURI(imageUri); // הצגת התמונה שנבחרה על גבי ה-ImageView
                }
            }
    );

    // משגר לפתיחת המצלמה וצילום תמונה
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null && extras.get("data") != null) {
                        android.graphics.Bitmap imageBitmap = (android.graphics.Bitmap) extras.get("data");
                        ivBookCover.setImageBitmap(imageBitmap); // הצגת התמונה שצולמה

                        // הערה: אם נרצה לשמור את הצילום מהמצלמה ישירות, נמיר אותו בהמשך ל-Uri או ל-Bytes
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        db = FirebaseFirestore.getInstance();

        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        etTotalPages = findViewById(R.id.etTotalPages);
        etCurrentPage = findViewById(R.id.etCurrentPage);
        ivBookCover = findViewById(R.id.ivBookCover);
        btnSave = findViewById(R.id.btnSave);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);

        // קבלת מזהה הספר שנשלח מ-HomeScreen
        bookId = getIntent().getStringExtra("BOOK_ID");

        if (bookId != null) {
            loadBookDetails();
        }

        btnSave.setOnClickListener(v -> saveBookDetails());

        // מאזין לכפתור המצלמה - פותח את אפליקציית המצלמה במכשיר
        btnCamera.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(takePictureIntent);
        });

        // מאזין לכפתור הגלרייה - פותח את אלבום התמונות במכשיר
        btnGallery.setOnClickListener(v -> {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(pickPhotoIntent);
        });
    }

    // טעינת פרטי הספר מ-Firestore ושיבוצם בתיבות הטקסט
    private void loadBookDetails() {
        db.collection("books").document(bookId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Book book = documentSnapshot.toObject(Book.class);
                        if (book != null) {
                            etTitle.setText(book.getTitle());
                            etAuthor.setText(book.getAuthor());
                            etTotalPages.setText(String.valueOf(book.getTotalPages()));
                            etCurrentPage.setText(String.valueOf(book.getCurrentPage()));
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show());
    }

    // עדכון השינויים ב-Firestore (כולל בדיקה האם נבחרה תמונה חדשה להעלאה לענן)
    private void saveBookDetails() {
        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        int totalPages = etTotalPages.getText().toString().isEmpty() ? 0 : Integer.parseInt(etTotalPages.getText().toString().trim());
        int currentPage = etCurrentPage.getText().toString().isEmpty() ? 0 : Integer.parseInt(etCurrentPage.getText().toString().trim());

        // אם המשתמש בחר תמונה חדשה מהגלרייה, נעלה אותה קודם ל-Firebase Storage
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference imageRef = storageRef.child("book_covers/" + bookId + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String imageUrl = downloadUri.toString();
                        // שמירת הנתונים יחד עם קישור התמונה ב-Firestore
                        updateBookDataInFirestore(title, author, totalPages, currentPage, imageUrl);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בהעלאת התמונה", Toast.LENGTH_SHORT).show());
        } else {
            // עדכון רגיל בלי להחליף תמונה
            updateBookDataInFirestore(title, author, totalPages, currentPage, null);
        }
    }

    private void updateBookDataInFirestore(String title, String author, int totalPages, int currentPage, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("author", author);
        updates.put("totalPages", totalPages);
        updates.put("currentPage", currentPage);

        if (imageUrl != null) {
            updates.put("coverImageUrl", imageUrl); // שומר את כתובת התמונה בענן אם קיימת
        }

        db.collection("books").document(bookId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "הפרטים עודכנו בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish(); // סגירת המסך וחזרה למסך הבית
                })
                .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בעידכון הנתונים", Toast.LENGTH_SHORT).show());
    }
}