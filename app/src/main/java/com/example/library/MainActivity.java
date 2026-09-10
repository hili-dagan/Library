package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
             Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button btnNewUser = findViewById(R.id.btnNewUser);
        Button btnExistingUser = findViewById(R.id.btnExistingUser);

        btnNewUser.setOnClickListener(v -> {
            // מעבר למסך הרשמה למשתמש חדש
            // Intent intent = new Intent(LoginChoiceActivity.this, RegisterActivity.class);
           // startActivity(intent);
        });

        btnExistingUser.setOnClickListener(v -> {
            // מעבר למסך התחברות למשתמש קיים
           //  Intent intent = new Intent(LoginChoiceActivity.this, LoginActivity.class);
           //  startActivity(intent);
        });


    }
}