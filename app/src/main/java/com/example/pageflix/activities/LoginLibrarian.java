package com.example.pageflix.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pageflix.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class
LoginLibrarian extends AppCompatActivity {
    private EditText edEmail ,edPassword;
    private FirebaseAuth fbAuth; // for email+password connection
    private String CUSTOMER_KEY = "Customer";//DataBase name for Customers
    private String LIB_KEY = "Librarian";//DataBase name for Librarians
    private DatabaseReference dbRef;
    private TextView textView;
    private GoogleSignInClient client;
    private GoogleSignInOptions options;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_librarian);
        init();
        initGoogle();
    }
    public void init(){
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        fbAuth = FirebaseAuth.getInstance();
    }
    //google auth==========================================================================================================================================================================
    //create connection
    public void initGoogle(){
        textView = findViewById(R.id.signInWithGoogle);
        options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(this,options);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = client.getSignInIntent();
                startActivityForResult(i,1234);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1234){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    FirebaseUser user = fbAuth.getCurrentUser();
                                    checkGoogleUser(user);
                                }else {
                                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            } catch (ApiException e) {
                e.printStackTrace();
            }
        }

    }
    //check if Google user is Client or Librarian
    private void checkGoogleUser(FirebaseUser user) {
        assert user != null;
        String email = user.getEmail();
        dbRef = FirebaseDatabase.getInstance().getReference(CUSTOMER_KEY);
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean emailFound = false; // Flag to indicate if email is found
                for(DataSnapshot ds : snapshot.getChildren()){
                    String emailFromDatabase = ds.child("email").getValue(String.class);
                    assert email != null;
                    if (email.equalsIgnoreCase(emailFromDatabase)) {
                        Toast.makeText(getApplicationContext(), "This email not related to Librarian account ", Toast.LENGTH_SHORT).show();
                        emailFound = true;
                        break;
                    }
                }
                // If email is found, return from the function
                if (!emailFound) {
                    DatabaseReference dbRef2 = FirebaseDatabase.getInstance().getReference(LIB_KEY);
                    ValueEventListener vListener2 = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean flag = true;
                            for(DataSnapshot ds : snapshot.getChildren()){
                                String emailFromDatabase = ds.child("email").getValue(String.class);
                                // if finds that user with 'x' email is customer open his main screen
                                if (email.equalsIgnoreCase(emailFromDatabase)) {
                                    flag = false;
                                    break;
                                }
                            }
                            if (flag){
                                HashMap<String, Object> userMap = new HashMap<>();
                                userMap.put("email", email);
                                dbRef2.child(user.getUid()).setValue(userMap);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    };dbRef2.addValueEventListener(vListener2);
                    Toast.makeText(getApplicationContext(), "User Sign In Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), mainLibrarian.class);
                    startActivity(intent);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };dbRef.addValueEventListener(vListener);
        client.signOut();
    }
    //<>google auth==========================================================================================================================================================================
    public void loginButton(View v){
        String email = edEmail.getText().toString();
        String password = edPassword.getText().toString();
        //check if .user fill all the lines
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            // check email+password
            fbAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        //find all Librarians
                        dbRef = FirebaseDatabase.getInstance().getReference(LIB_KEY);
                        checkUserTypeAndRedirect(email);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Email or Password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            Toast.makeText(this, "Write name and Email", Toast.LENGTH_SHORT).show();
        }
    }
    // check type user email
    private void checkUserTypeAndRedirect(String email) {
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean flag = true;
                for(DataSnapshot ds : snapshot.getChildren()){
                    String emailFromDatabase = ds.child("email").getValue(String.class);
                    // if finds that user with 'x' email is Librarian open his main screen
                    if (email.equalsIgnoreCase(emailFromDatabase)) {
                         flag = false;
                        Intent intent = new Intent(getApplicationContext(), mainLibrarian.class);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), "User Sign In Successful", Toast.LENGTH_SHORT).show();
                    }
                }
                if (flag){
                    Toast.makeText(getApplicationContext(), "This email not related to Librarian account ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        dbRef.addValueEventListener(vListener);
    }

    public void registerLibrarian(View v){
        Intent intent = new Intent(this, registerLibrarian.class);// from Login Customer screen to registerLibrarian screen
        startActivity(intent);
    }
    public void backToFirstScreen(View v){
        Intent intent = new Intent(this, FirstScreen.class);// from Login com.example.pageflix.activities.LoginLibrarian.Librarian screen to First screen
        startActivity(intent);
    }

}