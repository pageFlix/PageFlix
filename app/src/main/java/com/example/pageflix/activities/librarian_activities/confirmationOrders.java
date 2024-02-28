package com.example.pageflix.activities.librarian_activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.pageflix.R;
import com.example.pageflix.activities.borrowedBooks.showUser_fromRentsList;
import com.example.pageflix.activities.main.mainLibrarian;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class confirmationOrders extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> listData;
    private List<String> filteredBooks ;
    private List<String> list_temp;
    private List<Rental> list_temp2;
    private DatabaseReference rental_conectRef,rentals_Ref;
    private String LibrarianID;
    private String RENTAL_CONNECTION = "rentalsConnection";
    private String RENTALS = "rentals";
    private String BOOKS = "Books";
    private String CUSTOMER = "Customer";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation_orders);
        init();
        getDataFromDB();
//        search();
        setOnClickIten();
    }
    public void init() {
        listView = findViewById(R.id.listView);
        listData = new ArrayList<>();
        list_temp = new ArrayList<>();
        list_temp2 = new ArrayList<>();
        filteredBooks = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        listView.setAdapter(adapter);
        LibrarianID = FirebaseAuth.getInstance().getCurrentUser().getUid(); //find LibrarianID (unique key)
        rental_conectRef = FirebaseDatabase.getInstance().getReference(RENTAL_CONNECTION).child(LibrarianID);
        rentals_Ref = FirebaseDatabase.getInstance().getReference(RENTALS);
    }
//    public void search() {
//        SearchView searchView = findViewById(R.id.searchView);
//        searchView.setQueryHint("Search..."); // Set hint text
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                filter(newText);
//                return true;
//            }
//        });
//    }
//
//    private void filter(String searchText) {
//        filteredBooks.clear();
//        if (searchText.isEmpty()) {
//            // If the search text is empty, add all items from the original list to listData
//            getDataFromDB() ;
//        } else {
//            // If the search text is not empty, filter based on the search text
//            for (String bookInfo : listData) {
//                if (bookInfo.toLowerCase().contains(searchText.toLowerCase())) {
//                    filteredBooks.add(bookInfo);
//                }
//            }
//        }
//        // Update the adapter with the new filtered list data
//        adapter.clear();
//        adapter.addAll(filteredBooks);
//        adapter.notifyDataSetChanged();
//    }
    private void getDataFromDB() {
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (listData.size() > 0) listData.clear();// check if list clean
                if (list_temp.size() > 0) list_temp.clear();// check if list clean
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String rentalKey = ds.getValue(String.class);
                    DatabaseReference rental_copy = rentals_Ref.child(rentalKey);
                    rental_copy.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Rental rental = dataSnapshot.getValue(Rental.class);
                            assert rental != null;
                            if (rental.isIfAccept() == false && rental.isIfReturned() == false) {
                                String idBook = rental.getBookID();
                                String idCustomer = rental.getCustomerID();
                                Date currentDate = new Date(rental.getTimestamp());
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                String formattedDate = dateFormat.format(currentDate);
                                Date nowTime = new Date();
                                long diffInMillies = nowTime.getTime() - currentDate.getTime();
                                long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillies);

                                bookInfo(idBook, new BookInfoCallback() {
                                    @Override
                                    public void onBookInfoReceived(Book book) {
                                        userInfo(idCustomer, new LibraryInfoCallback() {
                                            @Override
                                            public void onLibraryInfoReceived(User user) {
                                                listData.add("Book: " + book.getTitle() + "\nCustomer: " + user.getFirstName() + " " + user.getLastName() + "\nDate: " + formattedDate+"\nDays past: "+diffInDays);
                                                list_temp.add(rentalKey);
                                                list_temp2.add(rental);
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        rental_conectRef.addValueEventListener(vListener);
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
    private void userInfo(String idCustomer,  LibraryInfoCallback callback) {
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
    private void setOnClickIten(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(confirmationOrders.this);
                builder.setMessage("Are you sure you want confirm this order?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String reantal_k = list_temp.get(position);
                                Rental rental_r = list_temp2.get(position);
                                rental_r.setIfAccept(true);
                                rentals_Ref.child(reantal_k).setValue(rental_r);
                                finish();
                                Toast.makeText(getApplicationContext(), "The order was confirmed", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Do nothing
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

    }
    public void backToPreviousScreen(View v){
        finish();
    }
}