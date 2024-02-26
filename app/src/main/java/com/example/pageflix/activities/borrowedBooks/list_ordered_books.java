package com.example.pageflix.activities.borrowedBooks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.pageflix.R;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

public class list_ordered_books extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> listData,list_temp_user2,list_temp_user3,list_temp_user4;
    private List<User> list_temp_user;
    private List<String> filteredBooks ;

    private DatabaseReference rental_conectRef,rentals_Ref,customer_Ref,books_Ref;
    private String LibrarianID;
    private String RENTAL_CONNECTION = "rentalsConnection";
    private String RENTALS = "rentals";
    private String BOOKS = "Books";
    private String CUSTOMER = "Customer";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_ordered_books);
        init();
        getDataFromDB();
        setOnClickIten();
        search();
    }
    public void init() {
        listView = findViewById(R.id.listView);
        listData = new ArrayList<>();
        filteredBooks = new ArrayList<>();
        list_temp_user = new ArrayList<>();
        list_temp_user2 = new ArrayList<>();
        list_temp_user3 = new ArrayList<>();
        list_temp_user4 = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        listView.setAdapter(adapter);
        LibrarianID = FirebaseAuth.getInstance().getCurrentUser().getUid(); //find LibrarianID (unique key)
        rental_conectRef = FirebaseDatabase.getInstance().getReference(RENTAL_CONNECTION).child(LibrarianID);
        rentals_Ref = FirebaseDatabase.getInstance().getReference(RENTALS);
        customer_Ref = FirebaseDatabase.getInstance().getReference(CUSTOMER);
        books_Ref = FirebaseDatabase.getInstance().getReference(BOOKS);
    }
    public void search() {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Search..."); // Set hint text
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String searchText) {
        filteredBooks.clear();
        if (searchText.isEmpty()) {
            // If the search text is empty, add all items from the original list to listData
            getDataFromDB() ;
        } else {
            // If the search text is not empty, filter based on the search text
            for (String bookInfo : listData) {
                if (bookInfo.toLowerCase().contains(searchText.toLowerCase())) {
                    filteredBooks.add(bookInfo);
                }
            }
        }
        // Update the adapter with the new filtered list data
        adapter.clear();
        adapter.addAll(filteredBooks);
        adapter.notifyDataSetChanged();
    }
    private void getDataFromDB() {
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (listData.size() > 0) listData.clear();// check if list clean
                if (list_temp_user.size() > 0) list_temp_user.clear();// check if list clean
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String rentalKey = ds.getValue(String.class);
                    DatabaseReference rental_copy = rentals_Ref.child(rentalKey);
                    rental_copy.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Rental rental = dataSnapshot.getValue(Rental.class);
                            assert rental != null;
                            if (rental.isIfReturned() == false) {
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
                                                list_temp_user.add(user);
                                                list_temp_user2.add(idCustomer);
                                                list_temp_user3.add(idBook);
                                                list_temp_user4.add(rentalKey);
                                                Comparator<String> comparator = new Comparator<String>() {
                                                    @Override
                                                    public int compare(String item1, String item2) {
                                                        int diffInDays1 = Integer.parseInt(item1.split("Days past: ")[1]);
                                                        int diffInDays2 = Integer.parseInt(item2.split("Days past: ")[1]);
                                                        return Integer.compare(diffInDays2, diffInDays1);
                                                    }
                                                };
                                                Collections.sort(listData, comparator);
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

    private void setOnClickIten(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = list_temp_user.get(position);
                Intent intent = new Intent(getApplicationContext(), showUser_fromRentsList.class);
                intent.putExtra("customerID",user.getEmail());
                intent.putExtra("email",user.getEmail());
                intent.putExtra("fname",user.getFirstName());
                intent.putExtra("lname",user.getLastName());
                intent.putExtra("phone",user.getCellNumber());
                intent.putExtra("idCustomer",list_temp_user2.get(position));
                intent.putExtra("idBook",list_temp_user3.get(position));
                intent.putExtra("rentalKey",list_temp_user4.get(position));
                startActivity(intent);
            }
        });

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
    public void backToPreviousScreen(View v) {
        Intent intent = new Intent(this, mainLibrarian.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
}
