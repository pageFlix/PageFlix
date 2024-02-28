package com.example.pageflix.activities.addBook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pageflix.entities.Book;
import com.example.pageflix.R;
import com.example.pageflix.activities.main.mainLibrarian;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class screenAddBookINFO extends AppCompatActivity {
    private EditText edDescription, edAge;
    private String title, author, year, libID,countstring;
    private String BOOKS = "Books"; // DataBase name for Librarians
    DatabaseReference bookDB, libDB;
    private Spinner spCategory;
    private AutoCompleteTextView edCategory;

    private String[] categories = {
            "Action/Adventure fiction",
            "Children’s fiction",
            "Classic fiction",
            "Contemporary fiction",
            "Comedy",
            "Fantasy",
            "Dark fantasy",
            "Drama",
            "Fairy tales",
            "Folktales",
            "Heroic fantasy",
            "High fantasy",
            "Historical fantasy",
            "Low fantasy",
            "Magical realism",
            "Mythic fantasy",
            "Urban fantasy",
            "Graphic novel",
            "Historical fiction",
            "Horror",
            "Body horror",
            "Comedy horror",
            "Gothic horror",
            "Lovecraftian/Cosmic horror",
            "Paranormal horror",
            "Post-apocalyptic horror",
            "Psychological horror",
            "Quiet horror",
            "Slasher",
            "LGBTQ+",
            "Literary fiction",
            "Mystery",
            "Caper",
            "Cozy mystery",
            "Gumshoe/Detective mystery",
            "Historical mystery",
            "Howdunnits",
            "Locked room mystery",
            "Noir",
            "Procedural/Hard-boiled mystery",
            "Supernatural mystery",
            "New adult",
            "Romance",
            "Contemporary romance",
            "Dark romance",
            "Erotic romance",
            "Fantasy romance (Romantasy)",
            "Gothic romance",
            "Historical romance",
            "Paranormal romance",
            "Regency",
            "Romantic comedy",
            "Romantic suspense",
            "Sci-fi romance",
            "Satire",
            "Science fiction",
            "Apocalyptic sci-fi",
            "Colonization sci-fi",
            "Hard sci-fi",
            "Military sci-fi",
            "Mind uploading sci-fi",
            "Parallel world sci-fi",
            "Soft sci-fi",
            "Space opera",
            "Space western",
            "Steampunk",
            "Short story",
            "Thriller",
            "Action thriller",
            "Conspiracy thriller",
            "Disaster thriller",
            "Espionage thriller",
            "Forensic thriller",
            "Historical thriller",
            "Legal thriller",
            "Paranormal thriller",
            "Psychological thriller",
            "Religious thriller",
            "Western",
            "Women’s fiction",
            "Young adult",
            "Art & photography",
            "Autobiography/Memoir",
            "Biography",
            "Essays",
            "Food & drink",
            "History",
            "How-To/Guides",
            "Humanities & social sciences",
            "Humor",
            "Parenting",
            "Philosophy",
            "Religion & spirituality",
            "Science & technology",
            "Self-help",
            "Travel",
            "True crime"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_add_book_info);
        init();
    }
    private void init(){
        edDescription = findViewById(R.id.edDescription);
        edCategory = findViewById(R.id.edCategory);
        edAge = findViewById(R.id.edAge);
        title = getIntent().getStringExtra("title");
        author = getIntent().getStringExtra("author");
        year = getIntent().getStringExtra("year");
        libID = getIntent().getStringExtra("libID");
        countstring = getIntent().getStringExtra("count_string");
        bookDB = FirebaseDatabase.getInstance().getReference(BOOKS);
        libDB = FirebaseDatabase.getInstance().getReference("Librarian").child(libID);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);

        // Apply the adapter to the AutoCompleteTextView
        edCategory.setAdapter(adapter);
    }
    //    add in Books -> author, title, year, description , publication year + LibraryID
    public void addBook(View v) {
        String ageS = edAge.getText().toString();
        int age = Integer.valueOf(ageS);
        int count = Integer.valueOf(countstring);
        String description = edDescription.getText().toString();
        String category = edCategory.getText().toString();
        Book newBook = new Book( title,author,  year, count, category, description,age);
        if (!TextUtils.isEmpty(description) &&  !TextUtils.isEmpty(category) &&  !TextUtils.isEmpty(ageS)) {
            DatabaseReference newBookRef = bookDB.push();// create newBookRef for add data after adding book info
            newBookRef.setValue(newBook); // add data
            String bookID = newBookRef.getKey(); // get relevant Book key
            HashMap<String, Integer> bookCount = new HashMap<>();// create count : 1
            bookCount.put("count", count);

            Map<String, Object> BookUpdates = new HashMap<>();// to avoid unique key creation
            Map<String, Object> LibraryIDupdates = new HashMap<>();// to avoid unique key creation
            BookUpdates.put("LibraryID/"  + libID,bookCount);
            LibraryIDupdates.put("BooksID/" + bookID, bookCount);

            newBookRef.updateChildren(BookUpdates);
            if (bookID != null){libDB.updateChildren(LibraryIDupdates);}
            Intent intent = new Intent(this, mainLibrarian.class);
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "Write Category and Description", Toast.LENGTH_SHORT).show();
        }
    }

}