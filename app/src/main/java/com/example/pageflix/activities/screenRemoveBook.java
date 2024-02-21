package com.example.pageflix.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pageflix.R;
import com.example.pageflix.activities.main.mainLibrarian;
import com.example.pageflix.entities.Book;
import com.example.pageflix.interfaces.CallbackFlag;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class screenRemoveBook extends AppCompatActivity {
    private EditText edAuthor, edTitle, edPublicationYear;
    private DatabaseReference libDB, bookDB;
    private String BOOKS = "Books"; // DataBase name for Librarians
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
        libDB = FirebaseDatabase.getInstance().getReference("Librarian").child(LibrarianID).child("BooksID");
        bookDB = FirebaseDatabase.getInstance().getReference(BOOKS);
    }
    /*
    Steps:
    1: find  book in global Book DB by Author, Title, PublicationYear if false -> exit print "not found"
    2: check in bookID root -> LibraryID if (LibraryID == this.LibrarianID   && count > 0): true- count -1 in all three place => bookDB in total count and sub count(in LibraryID) AND Librarian DB count -1
    else "not found"
*/
    public void removeBook(View v){
        String author = edAuthor.getText().toString();
        String title = edTitle.getText().toString();
        String year = edPublicationYear.getText().toString();
        if (!TextUtils.isEmpty(author) &&  !TextUtils.isEmpty(title) && !TextUtils.isEmpty(year)) {
            checkBookInGlobalDB(title, author, year, new CallbackFlag(){
                @Override
                public void checkBook(boolean bookFound, DatabaseReference db, int totalCount) {
                    if(!bookFound){
                        Toast.makeText(getApplicationContext(), "This Book Not Exist", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String bookKey = db.getKey();
                        libDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.hasChild(bookKey)) {
                                    int countBookInLib = snapshot.child(bookKey).child("count").getValue(Integer.class);
                                    if(countBookInLib >0 && totalCount>0) {
                                        decreaseCount(db, totalCount);
                                        decreaseCount(db.child("LibraryID").child(LibrarianID), countBookInLib);
                                        decreaseCount(snapshot.child(bookKey).getRef(), countBookInLib);
                                        Toast.makeText(getApplicationContext(), "Book removed", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(), "No more copies of this book available", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "You don't have access to remove this book", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                }
            });
        }
        else {
            Toast.makeText(this, "Write Author, Year and Title", Toast.LENGTH_SHORT).show();
        }
    }
    private void decreaseCount(DatabaseReference dataBase, int CurrentCount){
            dataBase.child("count").setValue(CurrentCount - 1); // Increment count by 1
    }
    private void checkBookInGlobalDB(String title, String author, String year, CallbackFlag callback) {
        DatabaseReference bookDB = FirebaseDatabase.getInstance().getReference(BOOKS);
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference bookToUpdateRef = null;
                int totalCount = 0;
                boolean bookFound = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Book book = ds.getValue(Book.class);
                    if (book!=null && title.equalsIgnoreCase(book.getTitle()) && author.equalsIgnoreCase(book.getAuthor())
                            && year.equalsIgnoreCase(book.getYear()) && book.getCount() > 0) {
                        //book count ++
//                        bookToUpdateRef = ds.getRef(); // Reference to the book node
//                        currentCount = ds.child("count").getValue(Integer.class); // Get current count
//                        decreaseCount(bookToUpdateRef, currentCount);
//                        String bookKey = ds.getKey();
                        bookToUpdateRef = ds.getRef(); // Reference to the book node
                        bookFound = true;
                        totalCount = book.getCount();
                        break;
                    }
                }
                if (callback != null) {
                    callback.checkBook(bookFound, bookToUpdateRef, totalCount);
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