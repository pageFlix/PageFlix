package com.example.pageflix.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.pageflix.R;
import com.example.pageflix.activities.history.historyCustomer;
import com.example.pageflix.entities.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class mainCustomer extends AppCompatActivity {
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_customer);
        showWelcomeText();
    }
    private void showWelcomeText(){
        textView = findViewById(R.id.textviewname);
        FirebaseAuth user = FirebaseAuth.getInstance();
        String userId = user.getCurrentUser().getUid();
        DatabaseReference DBref = FirebaseDatabase.getInstance().getReference("Customer").child(userId);
        DBref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                textView.setText("Welcome \n"+ "\t"+user.getFirstName());
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopup(user);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void showPopup(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Data:");
        builder.setMessage("First Name: "+ user.getFirstName() + "\nLast Name: "+user.getLastName()+"\nEmail: "+user.getEmail()
                +"\nAddress: "+user.getCity()+", "+user.getStreet()+", "+user.getNumber()
        +"\nBirth Day: "+user.getBirthDay()+"\nCell Number: "+user.getCellNumber());
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // You can do something when OK button is clicked
                dialog.dismiss();
            }
        });
        builder.show();
    }
    public void backToFirstScreen(View v) {
        Intent intent = new Intent(this, FirstScreen.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
    public void signOut(View v){
        Intent intent = new Intent(this, FirstScreen.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
    public void Update_Profile(View v) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        Intent intent = new Intent(getApplicationContext(), Update_Customer_Profile.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }
    public void Search_books(View v) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        Intent intent = new Intent(getApplicationContext(), SearchBooks.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    public void historyOrders(View v) {
        Intent intent = new Intent(this , historyCustomer.class);
        startActivity(intent);
    }

    public void myBooks(View v) {
        Intent intent = new Intent(this , customerBooks.class);
        startActivity(intent);
    }
}