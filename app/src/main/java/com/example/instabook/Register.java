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

public class Register extends AppCompatActivity {

    EditText emailInp,passInp;
    Button register;
    TextView haveAccount;
    //progress display while registering user
    ProgressDialog progressDialog;

    //declare an instance of fireAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Register");
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        emailInp=findViewById(R.id.emailInp);
        passInp=findViewById(R.id.passInp);
        register=findViewById(R.id.registerBtn);
        haveAccount=findViewById(R.id.haveAccount);


        mAuth= FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("registering user...");

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //input email,password
                String email=emailInp.getText().toString().trim();
                String password=passInp.getText().toString().trim();

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    emailInp.setError("This email is already registered");
                    emailInp.setFocusable(true);
                }
                else if(password.length()<6)
                {
                    passInp.setError("Too short. enter again but longer");
                    passInp.setFocusable(true);
                }
                else
                {
                    registerUser(email, password);//register the user
                }

            }
        });
        haveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this,LoginPage.class));//goes to login page
                finish();//destroys this activity
            }
        });
    }

    private void registerUser(String email, String password)
    {
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    FirebaseUser user=mAuth.getCurrentUser();

                    String email = user.getEmail();
                    String uid = user.getUid();

                    //user hashMap
                    HashMap<Object, String> hashMap= new HashMap<>();
                    hashMap.put("email",email);
                    hashMap.put("uid",uid);
                    hashMap.put("name","");//just adding the name row to the database and the user will change his name later if he wants to
                    hashMap.put("phone","");//the same like the name row but for the phone
                    hashMap.put("image","");//the same like the name row but for the profile pic
                    //Firebase database instance
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference dataBaseReference = database.getReference("Users");

                    dataBaseReference.child(uid).setValue(hashMap);

                    progressDialog.dismiss();
                    Toast.makeText(Register.this,"Registered "+user.getEmail(),Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Register.this,ProfilePage.class));//goes to profile page
                    finish();//destroys this activity
                }
                else
                {
                    progressDialog.dismiss();
                    Toast.makeText(Register.this,"authentication failed",Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(Register.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
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