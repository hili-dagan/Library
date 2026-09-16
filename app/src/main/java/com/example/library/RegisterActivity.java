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

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegisterEmail, etRegisterPassword;
    private MaterialButton btnRegisterSubmit;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // אתחול Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // קישור הרכיבים מתוך ה-XML
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);

        // פעולה בלחיצה על כפתור ההרשמה
        btnRegisterSubmit.setOnClickListener(v -> {
            String email = etRegisterEmail.getText().toString().trim();
            String password = etRegisterPassword.getText().toString().trim();

            // בדיקה שהשדות אינם ריקים
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            // בדיקת אורך סיסמה מינימלי שפיירבייס דורש (6 תווים לפחות)
            if (password.length() < 6) {
                Toast.makeText(this, "הסיסמה חייבת להכיל לפחות 6 תווים", Toast.LENGTH_SHORT).show();
                return;
            }

            // יצירת המשתמש ב-Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // הצלחה! החשבון נוצר ונשמר בשרת של פיירבייס
                            Toast.makeText(this, "החשבון נוצר בהצלחה!", Toast.LENGTH_SHORT).show();

                            // כאן אפשר להוסיף בהמשך מעבר למסך הראשי או חזרה להתחברות
                            finish();
                        } else {
                            // אם הייתה שגיאה (למשל אימייל שכבר קיים במערכת)
                            Toast.makeText(this, "שגיאה בהרשמה: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

    }
}