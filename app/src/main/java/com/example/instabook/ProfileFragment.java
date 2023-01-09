package com.example.instabook;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.ICancelToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    //storage
    StorageReference storageReference;
    //path where images of user profile will be stored
    String storagePath = "Users_Profile_Imgs/";


    ImageView profilePic;
    TextView name,email,phone;

    FloatingActionButton floatingActionButton;
    RecyclerView postRecycleView;

    ProgressDialog pd;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    //arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    //uri of picked image
    Uri image_uri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile, container, false);;

        //init
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();


        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        profilePic=view.findViewById(R.id.profilePic);
        name=view.findViewById(R.id.nameTxt);
        email=view.findViewById(R.id.emailTxt);
        phone=view.findViewById(R.id.phoneTxt);

        floatingActionButton=view.findViewById((R.id.floatingBtn));
        postRecycleView = view.findViewById(R.id.recycleview_posts);

        pd = new ProgressDialog(getActivity());

        String emailStr=firebaseUser.getEmail();
        //query for reading the data of the user with a specific email
        Query query = databaseReference.orderByChild("email").equalTo(emailStr);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    String nameStr = ""+ds.child("name").getValue();
                    String phoneStr = ""+ds.child("phone").getValue();
                    String imageStr = ""+ds.child("image").getValue();

                    name.setText(nameStr);
                    email.setText(emailStr);
                    phone.setText(phoneStr);

                    try
                    {
                        Picasso.get().load(imageStr).into(profilePic);
                    }
                    catch(Exception e)
                    {
                        Picasso.get().load(R.drawable.ic_add_image).into(profilePic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });

        postList= new ArrayList<>();

        checkUserStatus();
        loadMyPosts();
        return view;
    }

    private void loadMyPosts() {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postRecycleView.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query to read the data of the posts
        Query query = ref.orderByChild("uidStr").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    //add to list
                    postList.add(modelPost);

                    //adapter for the posts
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set recyclerView's adapter to adapterPosts
                    postRecycleView.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(),error.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSearchedPosts(String searchQuery) {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postRecycleView.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query to read the data of the posts
        Query query = ref.orderByChild("uidStr").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    if(modelPost.getTitleStr().toUpperCase().contains(searchQuery.toUpperCase()))//checks if post title contains the letters that the user typed
                    {
                        //add to list
                        postList.add(modelPost);
                    }



                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set this adapter to recyclerView
                    postRecycleView.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(),error.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkStoragePermission()
    {
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission()
    {
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }


    private boolean checkCameraPermission()
    {
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission()
    {
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog()
    {
        String options[] = {"Edit profile Picture","Edit name","Edit Phone"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(options,new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int option)
            {
                if(option==0)//Edit profile picture clicked
                {
                    pd.setMessage("Updating Profile Picture");
                    showImagePicDialog();//calls the function that updates the profile pic
                }
                else if (option==1)//Edit name clicked
                {
                    pd.setMessage("Updating name");
                    showNamePhoneUpdateDialog("name");//calls the function that updates the name
                }
                else if(option==2)
                {
                    //Edit phone clicked
                    pd.setMessage("Updating Phone num");
                    showNamePhoneUpdateDialog("phone");//calls the function that updates the phone number
                }
            }
        });
        builder.create().show();
    }
    private void showNamePhoneUpdateDialog(String key)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update"+key);
        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add edit text
        EditText editText = new EditText(getActivity());
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);
        //add button in dialog to update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //input text from editText
                String value = editText.getText().toString().trim();
                //validate if user entered something or not
                if(!TextUtils.isEmpty(value))
                {
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();//creates a hashmap for the row in the database which will be updated
                    result.put(key, value);

                    databaseReference.child(firebaseUser.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            pd.dismiss();
                            Toast.makeText(getActivity(),"Updated...",Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(),""+e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else
                {
                    //in case the user clicked the update button without writing any text into the editText
                    Toast.makeText(getActivity(),"Please Enter "+key,Toast.LENGTH_LONG).show();
                }
            }
        });
        //add button for canceling the update
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void showImagePicDialog()
    {
        String options[] = {"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");
        builder.setItems(options,new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int option)
            {
                if(option==0)//Camera
                {
                    if(!checkCameraPermission())
                    {
                        requestCameraPermission();
                    }
                    else
                    {
                        pickFromCamera();
                    }
                }
                else if (option==1)//gallery
                {
                    if(!checkStoragePermission())
                    {
                        requestStoragePermission();
                    }
                    else
                    {
                        pickFromStorage();
                    }
                }

            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case CAMERA_REQUEST_CODE:
            {
                //picking from camera and checking if camera permission is allowed
                if(grantResults.length>0)
                {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted)
                    {
                        pickFromCamera();
                    }
                    else//permission denied
                    {
                        Toast.makeText(getActivity(),"Please enable camera permission", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            }

            case STORAGE_REQUEST_CODE:
            {
                if(grantResults.length>0)
                {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted)
                    {
                        pickFromStorage();
                    }
                    else//permission denied
                    {
                        Toast.makeText(getActivity(),"Please enable storage permission", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            }
        }


        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this method will be called after picking image from camera or gallery
        if(resultCode==RESULT_OK)//could be errors here check it later
        {
            if(requestCode == IMAGE_PICK_GALLERY_CODE)//image is picked from gallery
            {
                image_uri = data.getData();
                uploadProfilePhoto(image_uri);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE)//image is picked from camera
            {
                uploadProfilePhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfilePhoto(Uri image_uri)
    {
        pd.show();
        String filePathAndName = storagePath+"image_"+firebaseUser.getUid();
        storageReference.child(filePathAndName).putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        HashMap<String, Object> results = new HashMap<>();//hashmap for the image
                        results.put("image", uri.toString());
                        databaseReference.child(firebaseUser.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                pd.dismiss();
                                Toast.makeText(getActivity(),"Updated image successfully",Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(getActivity(),e.getMessage().toString(),Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(),e.getMessage().toString(),Toast.LENGTH_LONG).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getActivity(),"Couldnt upload image because:"+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void pickFromStorage()
    {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera()
    {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        //put image uri
        image_uri=getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//to show menu option in fragment
        super.onCreate(savedInstanceState);

    }

    //inflate options menu
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query))//check if user entered something in searchView
                {
                    loadSearchedPosts(query);
                }else
                {
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if(!TextUtils.isEmpty(query))//check if user entered something in searchView
                {
                    loadSearchedPosts(query);
                }else
                {
                    loadMyPosts();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }


    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if(id==R.id.action_logout)
        {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if(id==R.id.action_add_post)
        {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
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
            uid = user.getUid();
        }
        else
        {
            //go to main activity
            startActivity(new Intent(getActivity(),MainActivity.class));
            getActivity().finish();
        }
    }
}