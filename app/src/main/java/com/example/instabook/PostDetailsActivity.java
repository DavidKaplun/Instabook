package com.example.instabook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailsActivity extends AppCompatActivity {

    //to get details of user and post
    String myUid, myEmail, myName, myProfilePic, postTime, postLikes, hisProfilePic, hisName, hisUid, postImgStr;

    boolean mProcessComment=false;
    boolean mProcessLike=false;


    //progress bar
    ProgressDialog pd;

    String timeStr;

    //views
    ImageView profilePicImg,postImg;
    TextView nameTxt, timeTxt, titleTxt, descriptionTxt, likesTxt, postCommentsTxt;
    Button likeBtn;

    //the layouts
    LinearLayout profileLayout;
    RecyclerView recyclerView;

    List<ModelComment> commentList;
    AdapterComments adapterComments;

    EditText commentTxt;
    ImageButton sendBtn,moreBtn;
    ImageView commentAvatar;

    String timeStr2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post details");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //get id of post using intent
        Intent intent =getIntent();
        timeStr = intent.getStringExtra("timeStr");


        profilePicImg=findViewById(R.id.userPic);
        postImg = findViewById(R.id.imageTxt);

        nameTxt = findViewById(R.id.userNameTxt);
        timeTxt = findViewById(R.id.titleTxt);
        titleTxt = findViewById(R.id.titleTxt);
        descriptionTxt = findViewById(R.id.descriptionTxt);
        likesTxt = findViewById(R.id.likesTxt);
        postCommentsTxt = findViewById(R.id.postCommentsTxt);

        likeBtn = findViewById(R.id.likeBtn);
        commentTxt = findViewById(R.id.commentTxt);
        commentAvatar = findViewById(R.id.commentAvatar);
        sendBtn = findViewById(R.id.sendBtn);
        moreBtn = findViewById(R.id.moreBtn);
        profileLayout = findViewById(R.id.profileLayout);
        recyclerView = findViewById(R.id.recyclerView_comments);


        loadPostInfo();

        loadComments();

        checkUserStatus();

        loadUserInfo();

        setLikes();

        //set the subtitle in the actionBar
        actionBar.setSubtitle("Signed in as: "+myEmail);

        //send comment button click
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postComment();
            }
        });

        //like button click handling
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likePost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions();
            }
        });
    }

    private void loadComments()
    {
        //layout() for recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        //set layout about recyclerview
        recyclerView.setLayoutManager(linearLayoutManager);

        //init commentList
        commentList = new ArrayList<>();

        //path of the post to get its comments
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(timeStr).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentList.add(modelComment);

                    adapterComments = new AdapterComments(getApplicationContext(),commentList);

                    recyclerView.setAdapter(adapterComments);//adds the comments to the recyclerView
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions()
    {
        //creating popup menu
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        //show delete option only to posts that the signed user owns
        if(hisUid.equals(myUid))
        {
            //add item in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
        }
        popupMenu.getMenu().add(Menu.NONE,1,0,"View Details");

        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id= item.getItemId();
                if(id==0)
                {
                    //delete option is clicked
                    beginDelete();
                }
                else if(id==1)
                {
                    //start post details activity
                    Intent intent = new Intent(PostDetailsActivity.this, PostDetailsActivity.class);
                    intent.putExtra("timeStr",timeStr);
                    startActivity(intent);
                }
                return false;
            }
        });

        //show menu
        popupMenu.show();
    }

    private void beginDelete()
    {
        if(postImgStr.equals("noImage"))
        {
            //post is without image
            deleteWithoutImage();
        }
        else
        {
            //post is with image
            deleteWithImage();
        }
    }

    private void deleteWithImage()
    {
        //progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");

        //deleting image using url
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(postImgStr);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //image deleted from storage, now delete it from database
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("timeStr").equalTo(timeStr);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren())
                        {
                            ds.getRef().removeValue();//remove posts with matching times to this
                        }
                        //deleted
                        Toast.makeText(PostDetailsActivity.this,"Deleted Post Successfully",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //something went wrong while deleting print the error message
                progressDialog.dismiss();
                Toast.makeText(PostDetailsActivity.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteWithoutImage()
    {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");

        //deletes the post with a specific time
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("timeStr").equalTo(timeStr);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    ds.getRef().removeValue();//remove posts with matching times to this
                }
                //deleted
                Toast.makeText(PostDetailsActivity.this,"Deleted Post Successfully",Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes()
    {
        //when the details of the post are loading, also check if the user like it or not
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(timeStr).hasChild(myUid))
                {
                    //user liked post before
                    /*to indicate that the user liked it
                    the color of the icon of the like button will be changed
                    and the text from like to liked*/
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_red,0,0,0);
                    likeBtn.setText("Liked");
                }
                else
                {
                    //user didnt like post before
                    /*to indicate that the user unliked it
                    the color of the icon of the like button will be changed
                    and the text from liked to like*/
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
                    likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void likePost()
    {
        //get totall number of likes
        //if the signed user didnt like the post then increase the num by 1
        mProcessLike=true;

        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("Likes");
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessLike)
                {
                    if(snapshot.child(timeStr).hasChild(myUid))
                    {
                        postsRef.child(timeStr).child("likesStr").setValue(""+(Integer.parseInt(postLikes)-1));
                        likesRef.child(timeStr).child(myUid).removeValue();
                        mProcessLike=false;

                        likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
                        likeBtn.setText("Like");
                    }
                    else
                    {
                        //It wasnt liked before by this user, now it is
                        postsRef.child(timeStr).child("likesStr").setValue(""+(Integer.parseInt(postLikes)+1));
                        likesRef.child(timeStr).child(myUid).setValue("Liked");//set any value
                        mProcessLike=false;

                        likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_red,0,0,0);
                        likeBtn.setText("Liked");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void postComment()
    {
        pd = new ProgressDialog(this);
        pd.setMessage("Adding comment...");

        //get data from comment edit text
        String comment = commentTxt.getText().toString().trim();
        //validate
        if(TextUtils.isEmpty(comment))
        {
            Toast.makeText(this,"comment is empty...", Toast.LENGTH_LONG).show();
            return;
        }


        String timeStamp = String.valueOf(System.currentTimeMillis());

        //each post will have a child "Comments" that will contains comments of that post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(timeStr).child("Comments");

        //creating hashMap for the comments
        HashMap<String, Object> hashMap = new HashMap<>();
        //put info in hashMap
        hashMap.put("cId",timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("uid",myUid);
        hashMap.put("userEmail",myEmail);
        hashMap.put("userName", myName);
        hashMap.put("userImage", myProfilePic);

        //put this data in the database
        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //added comments successfully tell that to the user and update the comment count
                pd.dismiss();
                Toast.makeText(PostDetailsActivity.this,"Comment added ",Toast.LENGTH_LONG).show();
                commentTxt.setText("");
                updateCommentCount();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed, didnt add comment print the error message
                pd.dismiss();
                Toast.makeText(PostDetailsActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();

            }
        });
    }


    private void updateCommentCount()
    {
        mProcessComment=true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(timeStr);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //onDataChange is taking and proccessing snapshots every second
                //but we want to change he number of comments only once
                //so after we change the number of comments the function should
                //change anything else afterwards
                if(mProcessComment)
                {
                    String comments = snapshot.child("numOfCommentsStr").getValue().toString();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    ref.child("numOfCommentsStr").setValue(""+newCommentVal);
                    mProcessComment=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUserInfo()
    {
        //get current user info
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");//query for reading user data
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    //set data
                    myName=ds.child("name").getValue().toString();
                    myProfilePic=ds.child("image").getValue().toString();

                    //set picture
                    try {
                        Picasso.get().load(myProfilePic).into(commentAvatar);
                    }
                    catch(Exception e)
                    {
                        Picasso.get().load(R.drawable.ic_profile).into(commentAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfo()
    {
        //query for reading post info
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("timeStr").equalTo(timeStr);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //keep checking the posts until get the required post
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    String titleStr=""+ds.child("titleStr").getValue();
                    String descStr=""+ds.child("descriptionStr").getValue();
                    postLikes = ""+ds.child("likesStr").getValue();
                    postImgStr = ""+ds.child("imageStr").getValue();
                    hisProfilePic = ""+ds.child("userPicStr").getValue();
                    hisUid = ""+ds.child("uidStr").getValue();
                    hisName = ""+ds.child("userNameStr").getValue();

                    //convert timeStr to dd/mm/yyyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    if(timeStr!=null)
                    {
                        calendar.setTimeInMillis(Long.parseLong(timeStr));
                        timeStr2= DateFormat.format("dd/mm/yyyy hh:mm am/pm",calendar).toString();
                    }

                    //set the data
                    titleTxt.setText(titleStr);
                    descriptionTxt.setText(descStr);
                    likesTxt.setText(postLikes);
                    timeTxt.setText(timeStr);
                    nameTxt.setText(hisName);
                    postCommentsTxt.setText(ds.child("numOfCommentsStr").getValue().toString()+" Comments");

                    //set post image
                    //if there is no image then hide imageView
                    if(postImgStr!=null)
                    {
                        if(!postImgStr.equals("noImage")) {
                            try {
                                Picasso.get().load(postImgStr).into(postImg);
                            } catch (Exception e) {

                            }
                        }
                    }

                    //set user image in comments
                    try {
                        Picasso.get().load(hisProfilePic).placeholder(R.drawable.ic_profile).into(profilePicImg);
                    }catch(Exception e)
                    {
                        Picasso.get().load(R.drawable.ic_profile).into(profilePicImg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
        {
            //user is signed in get his email and uid
            myEmail=user.getEmail();
            myUid = user.getUid();
        }
        else
        {
            //user is not signed in go back to main activity and destory this activity
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        //hide add post button and search button from menu main
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_logout)
        {
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}