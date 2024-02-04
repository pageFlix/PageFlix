package com.example.pageflix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class screenAddBook extends AppCompatActivity{
    private EditText edAuthor, edTitle, edPublicationYear;
    private DatabaseReference dbRef;
    private String USER_KEY = "Books"; // DataBase name for Librarians
    private String LibrarianID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_add_book);
        init();
    }
    private void init() {
        edAuthor = findViewById(R.id.edAuthor);
        edTitle = findViewById(R.id.edTitle);
        edPublicationYear = findViewById(R.id.edPublicationYear);
        LibrarianID = FirebaseAuth.getInstance().getCurrentUser().getUid(); //find LibrarianID (unique key)
        //Find right sub tree
        // Tree Librarian -> LibrarianID -> all info(name address...)
        dbRef = FirebaseDatabase.getInstance().getReference("Librarian").child(LibrarianID);
    }
    /*Here I take all book info and put in DataBase
    1: Check in Local DB : Librarian -> <LibrarianID> -> Books ->  author, title, year
    if (author, title, year  == found) add count ++
    else add in .... -> Books -> author, title, year
     2: Check in Global DB :  Books ->  author, title, year
    if (author, title, year  == found) add count ++
    else {
    go to second screen to add description , publication year
    add in Books -> author, title, year, description , publication year
    }
    */
    public void addBook(View v) {
        String author = edAuthor.getText().toString();
        String title = edTitle.getText().toString();
        String year = edPublicationYear.getText().toString();
        if (!TextUtils.isEmpty(author) &&  !TextUtils.isEmpty(title) && !TextUtils.isEmpty(year)) {
            checkBookInLocalDB(title, author, year, new CallbackFlag() {
                @Override
                public void checkBook(boolean bookFound,DatabaseReference db) {
                    // Use bookFound here to set your global flag or perform any other action
                    if (bookFound) {////////////////////////////////////////////////////////////////////////////////////////////////////////////---IN DATA BASE LOCAL
                        checkBookInGlobalDB(title, author, year, new CallbackFlag(){
                            @Override
                            public void checkBook(boolean bookFound,DatabaseReference db) {
                                if (bookFound) {////////////////////////////////////////////---IN DATA BASE GLOBAL
                                    Toast.makeText(getApplicationContext(), "The book was added", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), mainLibrarian.class);// from Login Customer screen to First screen
                                    startActivity(intent);
                                }
                            }
                        });
                    } else {////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////---NOT IN DATA BASE LOCAL
                        Book newBookINFO_local = new Book(title, author, year,  1);
                        dbRef.child("Books").push().setValue(newBookINFO_local);
                        checkBookInGlobalDB(title, author, year, new CallbackFlag(){
                            @Override
                            public void checkBook(boolean bookFound,DatabaseReference db) {
                                if (bookFound) {//////////////////////////////////////////////////////////////---IN DATA BASE GLOBAL
                                    Toast.makeText(getApplicationContext(), "The book was added", Toast.LENGTH_SHORT).show();
                                    db.child("LibraryID").push().setValue(LibrarianID);
                                }
                                else{//////////////////////////////////////////////////////////////////////////////---NOT IN DATA BASE GLOBAL
                                    Toast.makeText(getApplicationContext(), "Please add book description ", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), screenAddBookINFO.class);
                                    intent.putExtra("title", title);
                                    intent.putExtra("author", author);
                                    intent.putExtra("year", year);
                                    intent.putExtra("libID", LibrarianID);
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                }
            });
        }
        else{
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
                        addBookCounnt(bookToUpdateRef,currentCount);
                        break; // No need to continue checking once found
                    }
                }
                if (callback != null){ callback.checkBook(flag,null);}
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        booksRef.addListenerForSingleValueEvent(vListener);
    }
    private void addBookCounnt(DatabaseReference dataBase, int CurrentCount){
        dataBase.child("count").setValue(CurrentCount + 1); // Increment count by 1
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
                            addBookCounnt(bookToUpdateRef,currentCount);
                            break; // No need to continue checking once found
                        }
                    }
                    if (callback != null){ callback.checkBook(flag,bookToUpdateRef);}
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