package com.example.instabook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfilePage extends AppCompatActivity {


    //firebase auth
    FirebaseAuth firebaseAuth;
    ActionBar actionBar;
    BottomNavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        //actionBar and its title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        //init auth
        firebaseAuth=FirebaseAuth.getInstance();

        //init
        navigationView=findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener= new BottomNavigationView.OnNavigationItemSelectedListener()
            {
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
                {
                    switch(menuItem.getItemId())
                    {
                        case R.id.nav_home:
                            //home fragment transaction
                            actionBar.setTitle("Home");
                            HomeFragment homeFragment= new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content, homeFragment,"");
                            ft1.commit();
                            return true;
                        case R.id.nav_profile:
                            //profile fragment transaction
                            actionBar.setTitle("Profile");
                            ProfileFragment profileFragment= new ProfileFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content, profileFragment,"");
                            ft2.commit();
                            return true;
                        case R.id.nav_users:
                            //users fragment transaction
                            actionBar.setTitle("Users");
                            UsersFragment usersFragment= new UsersFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content, usersFragment,"");
                            ft3.commit();
                            return true;

                    }
                    return false;
                }
            };

    private void checkUserStatus()
    {
        //get current user
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null)
        {
            //user is signed in stay here
        }
        else
        {
            //go to main activity
            startActivity(new Intent(ProfilePage.this,MainActivity.class));
            finish();//destroy this activity
        }
    }

    @Override
    protected void onStart()
    {
        checkUserStatus();
        super.onStart();
    }


}