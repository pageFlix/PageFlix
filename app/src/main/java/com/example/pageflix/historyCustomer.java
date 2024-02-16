package com.example.pageflix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.pageflix.activities.mainCustomer;
import com.example.pageflix.activities.registerLibrarian;
import com.example.pageflix.entities.Book;
import com.example.pageflix.entities.Rental;
import com.example.pageflix.entities.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class historyCustomer extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listData;
    private List<Long> keys;
    private DatabaseReference db_rentalsConnection, db_rentals;
    private String CustomerID;
    private String RENTALS_CONNECT = "rentalsConnection";
    private  String RENTALS = "rentals";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_customer);
        init();
        getDataFromDB();
    }
    public void init(){
        listView = findViewById(R.id.listView);
        listData = new ArrayList<>();
         keys = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        listView.setAdapter(adapter);
        CustomerID = FirebaseAuth.getInstance().getCurrentUser().getUid(); //find LibrarianID (unique key)
        db_rentalsConnection = FirebaseDatabase.getInstance().getReference(RENTALS_CONNECT).child(CustomerID);
        db_rentals = FirebaseDatabase.getInstance().getReference(RENTALS);
    }
    private void getDataFromDB() {
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (listData.size() > 0) listData.clear(); // Clear the list before adding new data

                // Create a list to hold the keys
                List<Long> keys = new ArrayList<>();

                // Add keys to the list
                for (DataSnapshot ds : snapshot.getChildren()) {
                    keys.add(Long.parseLong(ds.getKey()));
                }
                // Sort the keys in ascending order
                Collections.sort(keys);
                Collections.reverse(keys);

                // Iterate over the sorted keys and add corresponding data to listData
                for (Long key : keys) {
                    String order = snapshot.child(String.valueOf(key)).getValue(String.class);
                    assert order != null;
                    db_rentals.child(order).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Rental rental = snapshot.getValue(Rental.class);
                            assert rental != null;
                            final String idBook = rental.getBookID();
                            final String idLibrary = rental.getLibraryID();
                            Date currentDate = new Date(rental.getTimestamp());
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            String formattedDate = dateFormat.format(currentDate);

                            bookInfo(idBook, new BookInfoCallback() {
                                @Override
                                public void onBookInfoReceived(Book book) {
                                    libraryInfo(idLibrary, new LibraryInfoCallback() {
                                        @Override
                                        public void onLibraryInfoReceived(User library) {
                                            listData.add("Title: " + book.getTitle() + "\nLibraryName: " + library.getLibraryName()+"\nDate: "+formattedDate);
                                            adapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onLibraryInfoNotFound() {
                                            // Handle the case where the library info is not found
                                        }

                                        @Override
                                        public void onLibraryInfoError(String errorMessage) {
                                            // Handle error retrieving library info
                                        }
                                    });
                                }

                                @Override
                                public void onBookInfoNotFound() {
                                    // Handle the case where the book info is not found
                                }

                                @Override
                                public void onBookInfoError(String errorMessage) {
                                    // Handle error retrieving book info
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle onCancelled event
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled event
            }
        };
        db_rentalsConnection.addValueEventListener(vListener);
    }

    private void bookInfo(String idBook,  BookInfoCallback callback) {
        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference().child("Books").child(idBook);
        ValueEventListener bookListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Book book = snapshot.getValue(Book.class);
                    // Invoke the callback with the retrieved book
                    callback.onBookInfoReceived(book);
                } else {
                    // Handle the case where the book with the given ID does not exist
                    callback.onBookInfoNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors in reading the book data
                callback.onBookInfoError(error.getMessage());
            }
        };
        bookRef.addListenerForSingleValueEvent(bookListener);
    }

    private void libraryInfo(String idLibrary,  LibraryInfoCallback callback) {
        DatabaseReference libraryRef = FirebaseDatabase.getInstance().getReference().child("Librarian").child(idLibrary);
        ValueEventListener libraryListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User library = snapshot.getValue(User.class);
                    // Invoke the callback with the retrieved library
                    callback.onLibraryInfoReceived(library);
                } else {
                    // Handle the case where the library with the given ID does not exist
                    callback.onLibraryInfoNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors in reading the library data
                callback.onLibraryInfoError(error.getMessage());
            }
        };
        libraryRef.addListenerForSingleValueEvent(libraryListener);
    }

    public void backToPreviousScreen(View v) {
        Intent intent = new Intent(this, mainCustomer.class);
        startActivity(intent);
    }
}