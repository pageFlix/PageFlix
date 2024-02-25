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
import com.example.pageflix.activities.main.mainLibrarian;
import com.example.pageflix.entities.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class blocked_list extends AppCompatActivity {
    private ListView listView;
    private String idLibrarian;
    private  String CUSTOMER = "Customer";
    private  String BLOCK_LIST = "Blocklist";
    private ArrayAdapter<String> adapter;
    private List<String> listData;
    private List<String> list_unblock;
    private List<String> filteredBooks ;

    private DatabaseReference dbBlockedList,dbCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_list);
        init();
        getDataFromDB();
        setOnClickIten();
        search();
    }
    private void init(){
        listView = findViewById(R.id.listView);
        list_unblock = new ArrayList<>();
        listData = new ArrayList<>();
        filteredBooks = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        listView.setAdapter(adapter);

        idLibrarian =  FirebaseAuth.getInstance().getCurrentUser().getUid(); //find LibrarianID (unique key)
        dbBlockedList = FirebaseDatabase.getInstance().getReference(BLOCK_LIST);
        dbCustomer = FirebaseDatabase.getInstance().getReference(CUSTOMER);
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
        dbBlockedList.child(idLibrarian).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot child: snapshot.getChildren()) {
                        String idCustomer = child.getKey();
                        Boolean block = child.child("block").getValue(Boolean.class);
                        if (block == true) {
                            dbCustomer.child(idCustomer).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    User user = snapshot.getValue(User.class);
                                    if (user != null) {
                                        listData.add("Customer name: "+user.getFirstName()+" "+user.getLastName()+
                                                "\nEmail: "+user.getEmail());
                                        list_unblock.add(idCustomer);
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void setOnClickIten(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(blocked_list.this);
                builder.setMessage("Do you want to unblock the user?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String customer = list_unblock.get(position);
                                Map<String, Object> map = new HashMap<>();// to avoid unique key creation
                                map.put("block",false);
                                dbBlockedList.child(idLibrarian).child(customer).updateChildren(map);
                                Toast.makeText(getApplicationContext(), "User unblocked!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(blocked_list.this, mainLibrarian.class);// from Login Customer screen to First screen
                                startActivity(intent);
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

        public void backToPreviousScreen(View v) {
        Intent intent = new Intent(this, mainLibrarian.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
}