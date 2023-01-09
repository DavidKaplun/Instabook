package com.example.instabook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    DatabaseReference userDatabaseReference;

    //permission constants
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    //image pick constants
    private static final int IMAGE_CAMERA_CODE=300;
    private static final int IMAGE_STORAGE_CODE=400;


    //permissions array
    String[] cameraPermissions;
    String[] storagePermissions;


    EditText title, description;
    ImageView imageView;
    Button uploadBtn;

    //user info
    String name, email, uid, imageStr;

    Uri image_uri = null;

    //progress bar
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        progressDialog = new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();
        checkUserStatus();

        //get info of current user to include in post
        userDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDatabaseReference.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    name=ds.child("name").getValue().toString();
                    email=ds.child("email").getValue().toString();
                    imageStr=ds.child("image").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        actionBar.setSubtitle(email);

        //enable back button in action bar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        title=findViewById(R.id.postTitle);
        description=findViewById(R.id.postDescription);
        imageView=findViewById(R.id.postImg);
        uploadBtn=findViewById(R.id.postBtn);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });


        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get the data from the inputs which user provided
                String titleStr=title.getText().toString().trim();
                String descriptionStr=description.getText().toString().trim();

                if(TextUtils.isEmpty(titleStr))
                {
                    /*
                    if there is no title to the post then,tell the user to enter it,
                    stop the function and wait for the next click of upload button
                     */
                    Toast.makeText(AddPostActivity.this,"Enter title...",Toast.LENGTH_LONG).show();
                    return;
                }

                if(TextUtils.isEmpty(descriptionStr))
                {
                    /*
                    if there is no description to the post then,tell the user to enter it,
                    stop the function and wait for the next click of upload button
                    */
                    Toast.makeText(AddPostActivity.this,"enter description...", Toast.LENGTH_LONG).show();
                    return;
                }

                if(image_uri==null)
                {
                    //post without image
                    uploadData(title.getText().toString(), description.getText().toString(),"noImage");
                }
                else
                {
                    //post with image
                    uploadData(title.getText().toString(), description.getText().toString(), String.valueOf(image_uri));
                }
            }
        });

    }

    private void uploadData(String titleStr, String descriptionStr, String uriStr) {
        progressDialog.setMessage("Publishing post...");
        progressDialog.show();

        //the time for post-image name, post-id, post-publish-time
        String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName= "Posts/post_"+timeStamp;
        if(!uriStr.equals("noImage"))//check if post has image
        {
            //post with image
            StorageReference ref= FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putFile(Uri.parse(uriStr)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //image is uploaded to firebase storage, now get it's url
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("uidStr",uid);
                            hashMap.put("userNameStr",name);
                            hashMap.put("emailStr",email);
                            hashMap.put("imageStr", uriStr);

                            hashMap.put("titleStr",titleStr);
                            hashMap.put("descriptionStr",descriptionStr);
                            hashMap.put("timeStr",timeStamp);
                            hashMap.put("userPicStr",imageStr);
                            hashMap.put("likesStr","0");
                            hashMap.put("numOfCommentsStr","0");

                            //path to to store post data
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            //put the data in this reference
                            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //added in database
                                    progressDialog.dismiss();
                                    Toast.makeText(AddPostActivity.this,"Post published successfully",Toast.LENGTH_LONG).show();
                                    //reset editTexts
                                    title.setText("");
                                    description.setText("");
                                    imageView.setImageURI(null);
                                    image_uri=null;
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //failed adding post in database
                                    progressDialog.dismiss();
                                    Toast.makeText(AddPostActivity.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
                }
            });
        }
        else
        {
            //post without image
            HashMap<Object, String> hashMap = new HashMap<>();//create new hashmap
            //add values to the hashMap
            hashMap.put("uidStr",uid);
            hashMap.put("userNameStr",name);
            hashMap.put("emailStr",email);
            hashMap.put("imageStr", "noImage");

            hashMap.put("titleStr",titleStr);
            hashMap.put("descriptionStr",descriptionStr);
            hashMap.put("timeStr",timeStamp);
            hashMap.put("userPicStr",imageStr);
            hashMap.put("likesStr","0");
            hashMap.put("numOfCommentsStr","0");

            //path to to store post data
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            //put the data in this reference
            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    //added in database
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this,"Post published successfully",Toast.LENGTH_LONG).show();
                    //reset editTexts
                    title.setText("");
                    description.setText("");
                    imageView.setImageURI(null);
                    image_uri=null;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed adding post in database
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showImagePickDialog() {
        String options[] = {"Camera","Gallery"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose image from");
        //set options to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int option) {
                if(option==0)
                {
                    //chosen camera
                    if(checkCameraPermissions())
                    {
                        pickFromCamera();
                    }
                    else
                    {
                        requestCameraPermission();
                    }
                }
                else if(option==1)
                {
                    //chosen gallery
                    if(!checkStoragePermissions())
                    {
                        pickFromStorage();
                    }
                    else
                    {
                        requestStoragePermission();
                    }

                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void pickFromCamera()
    {
        //we will returns to this shit later
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_CAMERA_CODE);
    }

    private void pickFromStorage()
    {
        //same here
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_STORAGE_CODE);
    }

    private boolean checkStoragePermissions()
    {
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission()
    {
        ActivityCompat.requestPermissions(this, storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions()
    {
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) ==(PackageManager.PERMISSION_GRANTED);
        return result&&result1;
    }

    private void requestCameraPermission()
    {
        ActivityCompat.requestPermissions(this, cameraPermissions,CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//go to previus activity
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflates menu_main but without the search and add post options
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id =  item.getItemId();
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
            //user is signed in stay here and get his email and uid
            email=user.getEmail();
            uid=user.getUid();

        }
        else
        {
            //user isn't signed go to main activity
            startActivity(new Intent(this,MainActivity.class));
            finish();//destroys this activity
        }
    }

    /*
    this function checks wether user decided
    to upload from camera or gallery and if
    there is permission to use camera/gallery
    and if there is then call the neccessery
    function to get the picture
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode)
        {
            case CAMERA_REQUEST_CODE:
            {
                if(grantResults.length>0)
                {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted)
                    {
                        pickFromCamera();
                    }
                    else
                    {
                        Toast.makeText(this,"Please enable camera permission",Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:
            {
                if(grantResults.length>0)
                {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted)
                    {
                        pickFromStorage();
                    }
                    else
                    {
                        Toast.makeText(this,"Please enable storage permission",Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this method will be called after picking image from camera or gallery
        if(resultCode == RESULT_OK)
        {
            if(requestCode == IMAGE_STORAGE_CODE)
            {
                image_uri = data.getData();
                imageView.setImageURI(image_uri);
            }
            else if(requestCode == IMAGE_CAMERA_CODE)
            {
                imageView.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}