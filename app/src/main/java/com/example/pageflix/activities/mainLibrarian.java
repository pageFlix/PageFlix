package com.example.pageflix.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.pageflix.R;
import com.example.pageflix.activities.history.historyLibrary;
import com.example.pageflix.addBook.screenAddBook;
import com.example.pageflix.entities.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class mainLibrarian extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_librarian);
        showWelcomeText();
    }
    private void showWelcomeText(){
        textView = findViewById(R.id.textviewname);
        FirebaseAuth user = FirebaseAuth.getInstance();
        String userId = user.getCurrentUser().getUid();
        DatabaseReference DBref = FirebaseDatabase.getInstance().getReference("Librarian").child(userId);
        DBref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                textView.setText("Welcome "+ user.getLibraryName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void addBook(View v){
        Intent intent = new Intent(this, screenAddBook.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
    public void removeBook(View v){
        Intent intent = new Intent(this, screenRemoveBook.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
    public void inStock(View v){
        Intent intent = new Intent(this, inStockScreen.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
    public void signOut(View v){
        Intent intent = new Intent(this, FirstScreen.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
    public void Update_Profile(View v) {
        Log.d("Update_Profile", "Update profile button clicked");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        Intent intent = new Intent(this, Update_Librarian_Profile.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }
    public void historyOrders(View v) {
        Intent intent = new Intent(this , historyLibrary.class);
        startActivity(intent);
    }

}