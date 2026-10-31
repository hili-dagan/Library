package com.example.library;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfileImage;
    private TextView tvEmail, tvTotalBooks, tvFinishedBooks;
    private Button btnChangeProfileImage, btnSaveProfile, btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    private Uri profileImageUri;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri pickedUri = result.getData().getData();
                    if (pickedUri != null) {
                        // שמירת עותק של התמונה בתוך האפליקציה, כדי שלא נאבד את ההרשאה אליה
                        Uri localUri = ImageUtils.copyToAppStorage(this, pickedUri, "profile_images");
                        profileImageUri = (localUri != null) ? localUri : pickedUri;
                        ImageUtils.showImage(ivProfileImage, profileImageUri, R.drawable.placeholder_avatar);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        userId = currentUser.getUid();

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvEmail = findViewById(R.id.tvEmail);
        tvTotalBooks = findViewById(R.id.tvTotalBooks);
        tvFinishedBooks = findViewById(R.id.tvFinishedBooks);
        btnChangeProfileImage = findViewById(R.id.btnChangeProfileImage);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // הצגת האימייל של המשתמש המחובר
        tvEmail.setText(currentUser.getEmail());

        // טעינת תמונת הפרופיל והסטטיסטיקות
        loadUserProfile();
        loadUserStatistics();

        btnChangeProfileImage.setOnClickListener(v -> {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pickPhotoIntent.addCategory(Intent.CATEGORY_OPENABLE);
            pickPhotoIntent.setType("image/*");
            galleryLauncher.launch(pickPhotoIntent);
        });

        btnSaveProfile.setOnClickListener(v -> saveUserProfile());

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserProfile() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("profileImageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            profileImageUri = Uri.parse(imageUrl);
                            // אם אי אפשר לפתוח את התמונה השמורה, מציגים ברירת מחדל ולא קורסים
                            if (!ImageUtils.showImage(ivProfileImage, profileImageUri, R.drawable.placeholder_avatar)) {
                                profileImageUri = null;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בטעינת תמונת הפרופיל", Toast.LENGTH_SHORT).show());
    }

    private void loadUserStatistics() {
        db.collection("books")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalBooks = queryDocumentSnapshots.size();
                    int finishedBooks = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String status = doc.getString("status");
                        if ("finished".equals(status)) {
                            finishedBooks++;
                        }
                    }

                    tvTotalBooks.setText(String.valueOf(totalBooks));
                    tvFinishedBooks.setText(String.valueOf(finishedBooks));
                });
    }

    private void saveUserProfile() {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("userId", userId);
        userUpdates.put("email", mAuth.getCurrentUser().getEmail());

        if (profileImageUri != null) {
            userUpdates.put("profileImageUrl", profileImageUri.toString());
        }

        db.collection("users").document(userId).set(userUpdates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "תמונת הפרופיל נשמרה בהצלחה!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בשמירת התמונה", Toast.LENGTH_SHORT).show());
    }
}