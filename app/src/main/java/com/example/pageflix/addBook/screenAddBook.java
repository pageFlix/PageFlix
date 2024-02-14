package com.example.pageflix.addBook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pageflix.CallbackFlag;
import com.example.pageflix.R;
import com.example.pageflix.activities.mainLibrarian;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class screenAddBook extends AppCompatActivity{
    private EditText edAuthor, edTitle, edPublicationYear;
    private DatabaseReference LibrarianDB, bookDB;
    private String BOOKS = "Books"; // DataBase name for Librarians
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
        LibrarianDB = FirebaseDatabase.getInstance().getReference("Librarian").child(LibrarianID);
         bookDB = FirebaseDatabase.getInstance().getReference(BOOKS);
    }
    /*Here I take all book info and put in DataBase
    Check in Global DB : Books -> <BookID>  ->  author, title, year
    1: if (author, title, year  == found) add count ++
        check if book exist in Librarian Data base using book key
        if true : count++
        else :
        In Books DB(global) add LibrarianID under LibraryID
        add Librarian -> <LibrarianID> -> book key -> count =1
    2: else //not found
        go to screenAddBookINFO
        create Book object
        a) put bool object in Books data base
        b) get root key() of relevant book
        c) put this key with count=0 under Librarians-> <Librarian ID> -> Books
    }
    */
    public void addBook(View v) {
        String author = edAuthor.getText().toString();
        String title = edTitle.getText().toString();
        String year = edPublicationYear.getText().toString();
        if (!TextUtils.isEmpty(author) &&  !TextUtils.isEmpty(title) && !TextUtils.isEmpty(year)) {
            checkBookInGlobalDB(title, author, year, new CallbackFlag() {
                @Override
                public void checkBook(boolean bookFound,DatabaseReference db, int countbook1) {
                    // Use bookFound here to set your global flag or perform any other action
                    if (bookFound) {////////////////////////////////////////////////////////////////////////////////////////////////////////////---IN DATA BASE GLOBAL
                        checkBookInLocalDB(db.getKey(), new CallbackFlag(){
                            @Override
                            public void checkBook(boolean bookFound,DatabaseReference nul,int countbook2) {
                                if (bookFound) {////////////////////////////////////////////---IN DATA BASE LOCAL
                                    Toast.makeText(getApplicationContext(), "The book was added", Toast.LENGTH_SHORT).show();
                                    DatabaseReference data = db.child("LibraryID").child(LibrarianID);
                                    addBookCounnt(data,countbook2);
                                }
                                else {////////////////////////////////////////////---NOT IN DATA BASE LOCAL
                                    Toast.makeText(getApplicationContext(), "The book was added", Toast.LENGTH_SHORT).show();
                                    HashMap<String, Integer> bookCount = new HashMap<>();
                                    bookCount.put("count", 1);
                                    if(db.getKey()!=null){
                                        String bookID = db.getKey();
                                        Map<String, Object> BookUpdates = new HashMap<>();// to avoid unique key creation
                                        Map<String, Object> LibraryIDupdates = new HashMap<>();// to avoid unique key creation
                                        BookUpdates.put("LibraryID/"+LibrarianID ,bookCount);
                                        LibraryIDupdates.put("BooksID/" + bookID, bookCount);
                                        bookDB.child(bookID).updateChildren(BookUpdates);
                                        LibrarianDB.updateChildren(LibraryIDupdates);
                                    }
                                }
                            }
                        });
                    } else {////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////---NOT IN DATA BASE GLOBAL
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
        else{
            Toast.makeText(this, "Write Author, Year and Title", Toast.LENGTH_SHORT).show();
        }
    }
    private void checkBookInGlobalDB(String title,String author, String year,CallbackFlag callback) {
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference bookToUpdateRef = null;
                boolean flag = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String titleFromDatabase = ds.child("title").getValue(String.class); // Access 'title' directly
                    String authorFromDatabase = ds.child("author").getValue(String.class); // Access 'title' directly
                    String yearFromDatabase = ds.child("year").getValue(String.class); // Access 'title' directly
                    if (title.equalsIgnoreCase(titleFromDatabase) && author.equalsIgnoreCase(authorFromDatabase)
                            && year.equalsIgnoreCase(yearFromDatabase)) {
                        //book count ++
                        flag = true; // Set flag to true if title exists
                        bookToUpdateRef = ds.getRef(); // Reference to the book node
                        int currentCount = ds.child("count").getValue(Integer.class); // Get current count
                        addBookCounnt(bookToUpdateRef,currentCount);
                        break; // No need to continue checking once found
                    }
                }
                if (callback != null){
                    callback.checkBook(flag,bookToUpdateRef, 0);}
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        bookDB.addListenerForSingleValueEvent(vListener);
    }
    private void checkBookInLocalDB(String bookKey,CallbackFlag callback) {
        DatabaseReference booksRef = LibrarianDB.child("BooksID"); // Reference to the 'Books' node
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean flag = false;
                int currentCount = 0;
                DatabaseReference bookToUpdateRef;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String bookKeyFromDatabase = ds.getKey();
                    if (bookKey.equals(bookKeyFromDatabase)) {
                        //book count ++
                        flag = true; // Set flag to true if title exists
                        bookToUpdateRef = ds.getRef(); // Reference to the book node
                        currentCount = ds.child("count").getValue(Integer.class); // Get current count
                        addBookCounnt(bookToUpdateRef,currentCount);
                        break; // No need to continue checking once found
                    }
                }
                if (callback != null){ callback.checkBook(flag,null, currentCount);}
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
    public void backToPreviousScreen(View v){
        Intent intent = new Intent(this, mainLibrarian.class);// from Login Customer screen to First screen
        startActivity(intent);
    }

}
