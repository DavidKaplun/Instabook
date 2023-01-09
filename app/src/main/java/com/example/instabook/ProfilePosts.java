package com.example.instabook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProfilePosts extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    RecyclerView postRecycleView;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    ImageView avatar;
    TextView name,email,phone;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_posts);

        postRecycleView = findViewById(R.id.recycleview_posts);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        avatar=findViewById(R.id.avatar);
        name=findViewById(R.id.nameTxt);
        email=findViewById(R.id.emailTxt);
        phone=findViewById(R.id.phoneTxt);

        firebaseAuth = FirebaseAuth.getInstance();




        //get uid of clicked user to retrieve his posts
        Intent intent = getIntent();
        uid = intent.getStringExtra("uidStr");

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    String nameStr = ""+ds.child("name").getValue();
                    String emailStr = ""+ds.child("email").getValue();
                    String phoneStr = ""+ds.child("phone").getValue();
                    String imageStr = ""+ds.child("image").getValue();

                    //set data
                    name.setText(nameStr);
                    email.setText(emailStr);
                    phone.setText(phoneStr);

                    //set pictures
                    try
                    {
                        Picasso.get().load(imageStr).into(avatar);
                    }
                    catch(Exception e)
                    {
                        Picasso.get().load(R.drawable.ic_add_image).into(avatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postList= new ArrayList<>();

        checkUserStatus();
        loadOtherUsersPosts();

    }

    private void loadOtherUsersPosts() {

        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postRecycleView.setLayoutManager(layoutManager);

        //query to get the data of the posts
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("uidStr").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    //add post to postList
                    postList.add(modelPost);

                    //adapter for the posts
                    adapterPosts = new AdapterPosts(ProfilePosts.this, postList);
                    //set recyclerView's adapter to adapterPosts
                    postRecycleView.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfilePosts.this,error.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchHisPosts(String searchQuery)
    {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postRecycleView.setLayoutManager(layoutManager);

        //query to get the data of the posts
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("uidStr").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    if(modelPost.getTitleStr().toUpperCase().contains(searchQuery.toUpperCase()))
                    {
                        //add to list
                        postList.add(modelPost);
                    }



                    //adapter
                    adapterPosts = new AdapterPosts(ProfilePosts.this, postList);
                    //set this adapter to recyclerView
                    postRecycleView.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfilePosts.this,error.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        MenuItem item = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query))
                {
                    searchHisPosts(query);
                }else
                {
                    loadOtherUsersPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if(!TextUtils.isEmpty(query))
                {
                    searchHisPosts(query);
                }else
                {
                    loadOtherUsersPosts();
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_logout)
        {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

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
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}