package com.example.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvBooks;
    private BookAdapter bookAdapter;
    private List<String> bookList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public HomeFragment() {
        // בנאי ריק נדרש לפרגמנט
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // ניפוח קובץ ה-XML של הפרגמנט
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // אתחול רכיבי ה-UI
        rvBooks = view.findViewById(R.id.rvBooks);
        rvBooks.setLayoutManager(new LinearLayoutManager(getContext()));

        bookList = new ArrayList<>();
        bookAdapter = new BookAdapter(bookList);
        rvBooks.setAdapter(bookAdapter);

        // אתחול פיירבייס
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // טעינת הנתונים מ-Firestore
        loadUserBooks();

        return view;
    }

    private void loadUserBooks() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("books")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "שגיאה בטעינת ספרים: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (value != null) {
                        bookList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            String title = doc.getString("title");
                            if (title != null) {
                                bookList.add(title);
                            }
                        }
                        bookAdapter.notifyDataSetChanged();
                    }
                });
    }
}