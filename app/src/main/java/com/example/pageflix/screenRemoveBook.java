package com.example.pageflix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class screenRemoveBook extends AppCompatActivity {
    private EditText edAuthor, edTitle, edPublicationYear;
    private DatabaseReference dbRef;
    private String USER_KEY = "Books"; // DataBase name for Librarians
    private String LibrarianID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_remove_book);
        init();
    }
    private void init(){
        edAuthor = findViewById(R.id.edAuthor);
        edTitle = findViewById(R.id.edTitle);
        edPublicationYear = findViewById(R.id.edPublicationYear);
        LibrarianID = FirebaseAuth.getInstance().getCurrentUser().getUid(); //find LibrarianID (unique key)
        dbRef = FirebaseDatabase.getInstance().getReference("Librarian").child(LibrarianID);
    }
    /*
    Steps:
    1: Check in LibrarianID root if book exist
        if not print to user
    2: else:  count -1
    3: find this book in global Book DB : count-1
    4: print success
     */
    public void removeBook(View v){
        String author = edAuthor.getText().toString();
        String title = edTitle.getText().toString();
        String year = edPublicationYear.getText().toString();
        if (!TextUtils.isEmpty(author) &&  !TextUtils.isEmpty(title) && !TextUtils.isEmpty(year)) {
            checkBookInLocalDB(title, author, year, new CallbackFlag(){
                @Override
                public void checkBook(boolean bookFound, DatabaseReference db, int currentCount1) {
                    if(bookFound){
                        checkBookInGlobalDB(title, author, year, new CallbackFlag(){
                            @Override
                            public void checkBook(boolean bookFound, DatabaseReference db, int currentCount2) {
                                if(bookFound) {
                                    Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), "not Success", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "This Book Not Exist", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            Toast.makeText(this, "Write Author, Year and Title", Toast.LENGTH_SHORT).show();
        }
    }
    private void checkBookInLocalDB(String title,String author, String year,CallbackFlag callback) {
        DatabaseReference booksRef = dbRef.child("Books"); // Reference to the 'Books' node
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean flag = false;
                DatabaseReference bookToUpdateRef;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String titleFromDatabase = ds.child("title").getValue(String.class); // Access 'title' directly
                    String authorFromDatabase = ds.child("author").getValue(String.class); // Access 'title' directly
                    String yearFromDatabase = ds.child("year").getValue(String.class); // Access 'title' directly
                    if (title.equals(titleFromDatabase) && author.equals(authorFromDatabase)
                            && year.equals(yearFromDatabase)) {
                        //book count ++
                        flag = true; // Set flag to true if title exists
                        bookToUpdateRef = ds.getRef(); // Reference to the book node
                        int currentCount = ds.child("count").getValue(Integer.class); // Get current count
                        if (currentCount > 0){
                            decreaseCount(bookToUpdateRef,currentCount);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Book count  is 0", Toast.LENGTH_SHORT).show();
                        }
                        break; // No need to continue checking once found
                    }
                }
                if (callback != null){ callback.checkBook(flag,null,0);}
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        booksRef.addListenerForSingleValueEvent(vListener);
    }
    private void decreaseCount(DatabaseReference dataBase, int CurrentCount){
        dataBase.child("count").setValue(CurrentCount - 1); // Increment count by 1
    }
    private void checkBookInGlobalDB(String title,String author, String year,CallbackFlag callback) {
        DatabaseReference bookDB = FirebaseDatabase.getInstance().getReference(USER_KEY);
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference bookToUpdateRef = null;
                boolean flag = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String titleFromDatabase = ds.child("title").getValue(String.class); // Access 'title' directly
                    String authorFromDatabase = ds.child("author").getValue(String.class); // Access 'title' directly
                    String yearFromDatabase = ds.child("year").getValue(String.class); // Access 'title' directly
                    if (title.equals(titleFromDatabase) && author.equals(authorFromDatabase)
                            && year.equals(yearFromDatabase)) {
                        //book count ++
                        flag = true; // Set flag to true if title exists
                        bookToUpdateRef = ds.getRef(); // Reference to the book node
                        int currentCount = ds.child("count").getValue(Integer.class); // Get current count
                        decreaseCount(bookToUpdateRef, currentCount);
                        break; // No need to continue checking once found
                    }
                }
                if (callback != null) {
                    callback.checkBook(flag, bookToUpdateRef, 0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        bookDB.addListenerForSingleValueEvent(vListener);
    }
    public void backToPreviousScreen(View v){
        Intent intent = new Intent(this, mainLibrarian.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
}