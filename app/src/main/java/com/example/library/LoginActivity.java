package com.example.library;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginEmail, etLoginPassword;
    private MaterialButton btnLoginSubmit;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // אתחול Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // קישור הרכיבים מתוך ה-XML
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLoginSubmit = findViewById(R.id.btnLoginSubmit);

        // פעולה בלחיצה על כפתור ההתחברות
        btnLoginSubmit.setOnClickListener(v -> {
            String email = etLoginEmail.getText().toString().trim();
            String password = etLoginPassword.getText().toString().trim();

            // בדיקה שהשדות אינם ריקים
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            // התחברות מול פיירבייס
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // הצלחה! המשתמש התחבר בהצלחה
                            Toast.makeText(this, "התחברת בהצלחה!", Toast.LENGTH_SHORT).show();

                            // כאן אפשר לעבור למסך הראשי (MainActivity)
                            // Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            // startActivity(intent);
                            // finish();

                        } else {
                            // אם ההתחברות נכשלה (סיסמה שגויה או משתמש לא קיים)
                            Toast.makeText(this, "התחברות נכשלה: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

    }
}