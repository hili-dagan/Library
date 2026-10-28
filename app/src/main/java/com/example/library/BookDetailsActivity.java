package com.example.library;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class BookDetailsActivity extends AppCompatActivity {

    private EditText etTitle, etAuthor, etTotalPages, etCurrentPage, etGenre, etStatus;
    private ImageView ivBookCover;
    private Button btnSave, btnCamera, btnGallery;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String bookId;

    private Uri imageUri;

    // משגר לפתיחת הגלרייה ובחירת תמונה
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                            getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                        ivBookCover.setImageURI(imageUri);
                    }
                }
            }
    );

    // משגר לפתיחת המצלמה ושמירת הצילום כקובץ מקומי
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null && extras.get("data") != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        ivBookCover.setImageBitmap(imageBitmap);

                        // המרת צילום המצלמה לקובץ מקומי ושמירת ה-Uri שלו
                        imageUri = saveBitmapLocally(imageBitmap);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        etTotalPages = findViewById(R.id.etTotalPages);
        etCurrentPage = findViewById(R.id.etCurrentPage);
        etGenre = findViewById(R.id.etGenre);
        etStatus = findViewById(R.id.etStatus);
        ivBookCover = findViewById(R.id.ivBookCover);
        btnSave = findViewById(R.id.btnSave);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);

        bookId = getIntent().getStringExtra("BOOK_ID");

        if (bookId != null) {
            loadBookDetails();
        }

        btnSave.setOnClickListener(v -> saveBookDetails());

        btnCamera.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(takePictureIntent);
        });

        btnGallery.setOnClickListener(v -> {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pickPhotoIntent.addCategory(Intent.CATEGORY_OPENABLE);
            pickPhotoIntent.setType("image/*");
            galleryLauncher.launch(pickPhotoIntent);
        });
    }

    // פונקציית עזר לשמירת תמונת מצלמה בזיכרון המקומי של האפליקציה והחזרת ה-Uri שלה
    private Uri saveBitmapLocally(Bitmap bitmap) {
        try {
            File imagesFolder = new File(getFilesDir(), "book_covers");
            if (!imagesFolder.exists()) {
                imagesFolder.mkdirs();
            }
            File file = new File(imagesFolder, "cover_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // טעינת פרטי הספר מ-Firestore והצגת התמונה המקומית על המסך
    private void loadBookDetails() {
        db.collection("books").document(bookId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etTitle.setText(documentSnapshot.getString("title"));
                        etAuthor.setText(documentSnapshot.getString("author"));

                        Long totalPages = documentSnapshot.getLong("totalPages");
                        if (totalPages != null) etTotalPages.setText(String.valueOf(totalPages));

                        Long currentPage = documentSnapshot.getLong("currentPage");
                        if (currentPage != null) etCurrentPage.setText(String.valueOf(currentPage));

                        etGenre.setText(documentSnapshot.getString("genre"));

                        // המרת סטטוס שמור באנגלית לטקסט ידידותי בעברית כשמציגים במסך (אופציונלי ונוח)
                        String savedStatus = documentSnapshot.getString("status");
                        if (savedStatus != null) {
                            if (savedStatus.equals("finished")) {
                                etStatus.setText("סיימתי");
                            } else if (savedStatus.equals("reading")) {
                                etStatus.setText("קורא");
                            } else if (savedStatus.equals("want_to_read")) {
                                etStatus.setText("לקריאה");
                            } else {
                                etStatus.setText(savedStatus);
                            }
                        }

                        // שליפת נתיב התמונה המקומי שנשמר ב-Firestore והצגתו ב-ImageView
                        String savedImageUrl = documentSnapshot.getString("coverImageUrl");
                        if (savedImageUrl != null && !savedImageUrl.isEmpty()) {
                            imageUri = Uri.parse(savedImageUrl);
                            ivBookCover.setImageURI(imageUri);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show());
    }

    // שמירת הנתונים וכתובת התמונה המקומית ב-Firestore
    private void saveBookDetails() {
        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        int totalPages = etTotalPages.getText().toString().isEmpty() ? 0 : Integer.parseInt(etTotalPages.getText().toString().trim());
        int currentPage = etCurrentPage.getText().toString().isEmpty() ? 0 : Integer.parseInt(etCurrentPage.getText().toString().trim());
        String genre = etGenre.getText().toString().trim();

        // קליטת הסטטוס שהמשתמש כתב והמרה חכמה לאנגלית עבור מסד הנתונים
        String rawStatus = etStatus.getText().toString().trim().toLowerCase();
        String dbStatus = "reading"; // ברירת מחדל

        if (rawStatus.contains("סיימתי") || rawStatus.contains("finish")) {
            dbStatus = "finished";
        } else if (rawStatus.contains("קורא") || rawStatus.contains("read")) {
            dbStatus = "reading";
        } else if (rawStatus.contains("לקריאה") || rawStatus.contains("want")) {
            dbStatus = "want_to_read";
        } else {
            dbStatus = rawStatus; // גיבוי אם המשתמש הקליד משהו אחר
        }

        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        Map<String, Object> updates = new HashMap<>();
        updates.put("bookId", bookId);
        updates.put("userId", currentUserId);
        updates.put("title", title);
        updates.put("author", author);
        updates.put("totalPages", totalPages);
        updates.put("currentPage", currentPage);
        updates.put("genre", genre);
        updates.put("status", dbStatus); // שומר את הסטטוס המתוקן ("finished")

        // אם יש תמונה (מהגלרייה או מהמצלמה), נשמור את המחרוזת של ה-Uri במסד הנתונים
        if (imageUri != null) {
            updates.put("coverImageUrl", imageUri.toString());
        }

        db.collection("books").document(bookId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "הפרטים והתמונה נשמרו בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בעדכון הנתונים", Toast.LENGTH_SHORT).show());
    }
}