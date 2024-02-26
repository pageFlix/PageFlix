package com.example.pageflix.activities.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.pageflix.R;
import com.example.pageflix.activities.FirstScreen;
import com.example.pageflix.activities.librarian_activities.Update_Librarian_Profile;
import com.example.pageflix.activities.borrowedBooks.list_ordered_books;
import com.example.pageflix.activities.history.historyLibrary;
import com.example.pageflix.activities.addBook.screenAddBook;
import com.example.pageflix.activities.librarian_activities.blocked_list;
import com.example.pageflix.activities.librarian_activities.confirmationOrders;
import com.example.pageflix.activities.librarian_activities.inStockScreen;
import com.example.pageflix.activities.librarian_activities.outStockList;
import com.example.pageflix.activities.librarian_activities.screenRemoveBook;
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(""); // or getSupportActionBar().setTitle(null);
        showWelcomeText();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_lib, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item_1) {
            // Show popup for item_1
            FirebaseAuth user = FirebaseAuth.getInstance();
            String userId = user.getCurrentUser().getUid();
            DatabaseReference DBref = FirebaseDatabase.getInstance().getReference("Librarian").child(userId);
            DBref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    showPopup(user);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            return true;
        }
            if (id == R.id.item_2){
                Log.d("Update_Profile", "Update profile button clicked");
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                String userId = mAuth.getCurrentUser().getUid();

                Intent intent = new Intent(this, Update_Librarian_Profile.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        if (id == R.id.item_3){
            Intent intent = new Intent(this , historyLibrary.class);
            startActivity(intent);
        }
        if (id == R.id.item_4){
            Intent intent = new Intent(this, FirstScreen.class);// from Login Customer screen to First screen
            startActivity(intent);
        }
        if (id == R.id.item_5){
            Intent intent = new Intent(this, blocked_list.class);// from Login Customer screen to First screen
            startActivity(intent);
        }
        if (id == R.id.item_6){
            Intent intent = new Intent(this, outStockList.class);// from Login Customer screen to First screen
            startActivity(intent);
        }
            return super.onOptionsItemSelected(item);
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
                textView.setText("Welcome  \n"+ "\t"+ user.getLibraryName());
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopup(user);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void showPopup(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Data:");
        builder.setMessage("Library Name: "+user.getLibraryName()+"\nEmail: "+user.getEmail()
                +"\nAddress: "+user.getCity()+", "+user.getStreet()+", "+user.getNumber()
                +"\nCell Number: "+user.getCellNumber());
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // You can do something when OK button is clicked
                dialog.dismiss();
            }
        });
        builder.show();
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

    public void confirmationOrder(View v){
        Intent intent = new Intent(this, confirmationOrders.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
    public void borrowedBooks(View v) {
        Intent intent = new Intent(this , list_ordered_books.class);
        startActivity(intent);
    }


}