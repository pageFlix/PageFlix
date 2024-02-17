package com.example.pageflix.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.pageflix.R;
import com.example.pageflix.entities.Book;
import com.example.pageflix.entities.Rental;
import com.example.pageflix.entities.User;
import com.example.pageflix.interfaces.BookInfoCallback;
import com.example.pageflix.interfaces.LibraryInfoCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class historyLibrary extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listData;
    private DatabaseReference db_rentalsConnection, db_rentals;
    private String LibrarianID;
    private String RENTALS_CONNECT = "rentalsConnection";
    private  String RENTALS = "rentals";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_library);
        init();
        getDataFromDB();
    }
    public void init(){
        listView = findViewById(R.id.listView);
        listData = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        listView.setAdapter(adapter);
        LibrarianID = FirebaseAuth.getInstance().getCurrentUser().getUid(); //find LibrarianID (unique key)
        db_rentalsConnection = FirebaseDatabase.getInstance().getReference(RENTALS_CONNECT).child(LibrarianID);
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
// Create a list to hold the rental data
                List<String> rentalDataList = new ArrayList<>();

                for (Long key : keys) {
                    String order = snapshot.child(String.valueOf(key)).getValue(String.class);
                    assert order != null;
                    db_rentals.child(order).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Rental rental = snapshot.getValue(Rental.class);
                            assert rental != null;
                            String idBook = rental.getBookID();
                            String idCustomer = rental.getCustomerID();
                            Date currentDate = new Date(rental.getTimestamp());
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            String formattedDate = dateFormat.format(currentDate);

                            // Fetch book information
                            bookInfo(idBook, new BookInfoCallback() {
                                @Override
                                public void onBookInfoReceived(Book book) {
                                    // Fetch library information
                                    libraryInfo(idCustomer, new LibraryInfoCallback() {
                                        @Override
                                        public void onLibraryInfoReceived(User library) {
                                            // Construct the string with book title, library name, and date
                                            String data = "Book: " + book.getTitle() + "\nRented by Customer : "+ library.getFirstName() +" "+library.getLastName() + "\nDate: " + formattedDate;
                                            // Add the string to rentalDataList
                                            rentalDataList.add(data);

                                            // Check if all rental data has been collected
                                            if (rentalDataList.size() == keys.size()) {
                                                // Sort rentalDataList based on dates
                                                Collections.sort(rentalDataList, new Comparator<String>() {
                                                    DateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                    @Override
                                                    public int compare(String o1, String o2) {
                                                        try {
                                                            return f.parse(o2.split("\nDate: ")[1]).compareTo(f.parse(o1.split("\nDate: ")[1]));
                                                        } catch (ParseException e) {
                                                            throw new IllegalArgumentException(e);
                                                        }
                                                    }
                                                });

                                                // Update adapter with sorted data
                                                listData.addAll(rentalDataList);
                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors in reading the book data
            }
        };
        bookRef.addListenerForSingleValueEvent(bookListener);
    }

    private void libraryInfo(String idCustomer,  LibraryInfoCallback callback) {
        DatabaseReference libraryRef = FirebaseDatabase.getInstance().getReference().child("Customer").child(idCustomer);
        ValueEventListener libraryListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User library = snapshot.getValue(User.class);
                    // Invoke the callback with the retrieved library
                    callback.onLibraryInfoReceived(library);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors in reading the library data
            }
        };
        libraryRef.addListenerForSingleValueEvent(libraryListener);
    }

    public void backToPreviousScreen(View v) {
        Intent intent = new Intent(this, mainLibrarian.class);
        startActivity(intent);
    }
}