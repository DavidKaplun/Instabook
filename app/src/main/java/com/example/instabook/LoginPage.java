package com.example.instabook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginPage extends AppCompatActivity {

    EditText emailInp,passwordInp;
    Button loginBtn;
    TextView dontHaveAccountTxt;

    //declare an instane of FirebaseAuth
    private FirebaseAuth auth;

    //progress dialog
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        //actionBar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        auth=FirebaseAuth.getInstance();

        emailInp=findViewById(R.id.emailInp);
        passwordInp=findViewById(R.id.passInp);
        loginBtn=findViewById(R.id.loginBtn);
        dontHaveAccountTxt=findViewById(R.id.dontHaveAccount);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=emailInp.getText().toString();
                String password=passwordInp.getText().toString().trim();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    //invalid email pattern
                    emailInp.setError("Invalid email!");
                    emailInp.setFocusable(true);
                }
                else
                {
                    //valid email
                    loginUser(email,password);
                }
            }
        });
        dontHaveAccountTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginPage.this, Register.class));
            }
        });

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
    }


    private void loginUser(String email, String password)
    {
        progressDialog.show();
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(LoginPage.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    FirebaseUser user=auth.getCurrentUser();
                    if(task.getResult().getAdditionalUserInfo().isNewUser())
                    {
                        //if user registered then put his info in the database
                        //and set name,phone and profile pic to "", so he can chose it later
                        String email = user.getEmail();
                        String uid = user.getUid();

                        //creating hashmap for the user info
                        HashMap<Object, String> hashMap= new HashMap<>();
                        hashMap.put("email",email);
                        hashMap.put("uid",uid);
                        hashMap.put("name","");//just adding the name row to the database and the user will change his name later if he wants to
                        hashMap.put("phone","");//the same like the name row but for the phone
                        hashMap.put("image","");//the same like the name row but for the profile pic
                        //Firebase database instance
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference dataBaseReference = database.getReference();

                        dataBaseReference.child(uid).setValue(hashMap);
                    }

                    startActivity(new Intent(LoginPage.this,ProfilePage.class));//goes to profile page
                    finish();//destroys this activity
                }
                else
                {
                    //failed to login because email or password is wrong
                    Toast.makeText(LoginPage.this,"Incorrect email or password", Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //some error while login in print it
                Toast.makeText(LoginPage.this, ""+e.getMessage(),Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();//go to previous activity
        return super.onSupportNavigateUp();
    }
}