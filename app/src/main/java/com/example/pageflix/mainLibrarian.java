package com.example.pageflix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.gson.internal.bind.ArrayTypeAdapter;

import java.util.List;

public class mainLibrarian extends AppCompatActivity {
    private ListView listView;
    private ArrayTypeAdapter<String> adapter;
    private List<String> listData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_librarian);
    }
    public void addBook(View v){
        Intent intent = new Intent(this, screenAddBook.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
    public void inStock(View v){
        Intent intent = new Intent(this, inStockScreen.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
    public void signOut(View v){
        Intent intent = new Intent(this, FirstScreen.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
}