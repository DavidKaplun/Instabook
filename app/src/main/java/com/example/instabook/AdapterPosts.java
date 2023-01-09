package com.example.instabook;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.widget.PopupMenu;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder>{

    Context context;
    List<ModelPost> postList;

    String myUid;

    private DatabaseReference likesRef;
    private DatabaseReference postsRef;

    boolean mProccessLike=false;

    //the constructor
    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        this.myUid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        this.postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the row_posts to the layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);
        return new MyHolder(view);
    }

    //a function that puts all the data from the post list into the row_posts layout
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder,int position) {
        //get data
        String uidStr = postList.get(position).getUidStr();
        String titleStr = postList.get(position).getTitleStr();
        String descriptionStr = postList.get(position).getDescriptionStr();
        String imageStr = postList.get(position).getImageStr();
        String userNameStr = postList.get(position).getUserNameStr();
        String userPicStr = postList.get(position).getUserPicStr();
        String timeStr = postList.get(position).getTimeStr();
        String likesStr = postList.get(position).getLikesStr();
        String numOfCommentsStr = postList.get(position).getNumOfCommentsStr();

        //convert timeStr to dd/mm/yyyy hh:mm am/pm format
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        if(timeStr!=null)
        {
            calendar.setTimeInMillis(Long.parseLong(timeStr));
            String timeStr2 = DateFormat.format("dd/mm/yyyy hh:mm am/pm",calendar).toString();
            holder.timeTxt.setText(timeStr2);
        }



        //set data
        holder.userNameTxt.setText(userNameStr);
        holder.titleTxt.setText(titleStr);
        holder.descriptionTxt.setText(descriptionStr);
        holder.likesTxt.setText(likesStr + "Likes");
        holder.commentsTxt.setText(numOfCommentsStr + " Comments");


        //set likes for each post
        setLikes(holder, timeStr);

        //set the profilePic
        try
        {
            Picasso.get().load(userPicStr).into(holder.userPicture);
        }
        catch(Exception e)
        {

        }

        //if there is an image to the post then load the image
        if(!imageStr.equals("noImage")) {
            try {
                Picasso.get().load(imageStr).into(holder.postPicture);
            } catch (Exception e) {

            }
        }

        //handle button clicks
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions(holder.moreBtn, uidStr,myUid,imageStr,timeStr);
            }
        });

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get totall number of likes
                int numOfLikes = Integer.parseInt(postList.get(holder.getAdapterPosition()).getLikesStr());
                mProccessLike=true;
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(mProccessLike)//to make sure it doesn't change the num of likes every second
                        {
                            if(snapshot.child(timeStr).hasChild(myUid))
                            {
                                //the user already liked post then remove his like
                                postsRef.child(timeStr).child("likesStr").setValue(""+(numOfLikes-1));
                                likesRef.child(timeStr).child(myUid).removeValue();
                                mProccessLike=false;
                            }
                            else
                            {
                                //if the signed user didnt like the post then increase the num by 1
                                postsRef.child(timeStr).child("likesStr").setValue(""+(numOfLikes+1));
                                likesRef.child(timeStr).child(myUid).setValue("Liked");
                                mProccessLike=false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start post details activity
                Intent intent = new Intent(context, PostDetailsActivity.class);
                intent.putExtra("timeStr",timeStr);
                context.startActivity(intent);
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                click to go to profilePosts with the uid of a clicked user
                 */
                Intent intent = new Intent(context,ProfilePosts.class);
                intent.putExtra("uidStr", uidStr);
                context.startActivity(intent);
            }
        });


    }

    private void setLikes(MyHolder holder, String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postKey).hasChild(myUid))//check if user liked the post before
                {
                    //user liked post before
                    /*to indicate that the user liked it
                    the color of the icon of the like button will be changed,
                    the text from like to liked
                    and there will be a sound*/
                    context.startService(new Intent( context, SoundService.class));
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_red,0,0,0);//changes the color of the like to red
                    holder.likeBtn.setText("Liked");

                }
                else
                {
                    //user didnt like post before
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like,0,0,0);//changes the color of the like to black
                    holder.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showMoreOptions(ImageButton moreBtn, String uidStr, String myUid, String imageStr,String timeStr) {
        //creating popup menu
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //show delete option only to posts that the signed user owns
        if(uidStr.equals(myUid))
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
                if(id==0)//delete option is clicked
                {
                    beginDelete(timeStr,imageStr);
                }
                else if(id==1)//view details option is clicked
                {
                    //start post details activity
                    Intent intent = new Intent(context, PostDetailsActivity.class);
                    intent.putExtra("timeStr",timeStr);
                    context.startActivity(intent);
                }
                return false;//stop the function
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(String timeStr, String imageStr) {
        if(imageStr.equals("noImage"))
        {
            //delete post with no image
            deleteWithoutImage(timeStr);
        }
        else
        {
            //delete post with image
            deleteWithImage(timeStr,imageStr);
        }
    }

    private void deleteWithImage(String timeStr, String imageStr) {
        //progress dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");

        //1) deleting image using url
        StorageReference picRef = FirebaseStorage.getInstance().getReference("Posts").child("post_"+timeStr);//get the reference of the post we want to delete
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //image delete from storage, now delete it from database
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("timeStr").equalTo(timeStr);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren())
                        {
                            ds.getRef().removeValue();//remove posts with matching times to this
                        }
                        //deleted
                        Toast.makeText(context,"Deleted Post Successfully",Toast.LENGTH_LONG).show();
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
                //failed to delete the post. show the error message.
                progressDialog.dismiss();
                Toast.makeText(context,e.getMessage().toString(),Toast.LENGTH_LONG).show();
            }
        });


    }

    private void deleteWithoutImage(String timeStr) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("timeStr").equalTo(timeStr);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    ds.getRef().removeValue();//remove posts with matching times to this
                }
                //deleted
                Toast.makeText(context,"Deleted Post Successfully",Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder class for post
    class MyHolder extends RecyclerView.ViewHolder
    {
        //views from row_post.xml
        ImageView postPicture, userPicture;
        TextView userNameTxt, timeTxt, titleTxt, descriptionTxt, likesTxt, commentsTxt;
        ImageButton moreBtn;
        Button likeBtn, commentBtn;

        LinearLayout profileLayout;



        public MyHolder(@NonNull View itemView)
        {
            super(itemView);

            postPicture = itemView.findViewById(R.id.imageTxt);
            userPicture = itemView.findViewById(R.id.userPic);
            userNameTxt = itemView.findViewById(R.id.userNameTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            descriptionTxt = itemView.findViewById(R.id.descriptionTxt);
            likesTxt = itemView.findViewById(R.id.likesTxt);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);
            commentsTxt = itemView.findViewById(R.id.postCommentsTxt);
        }
    }

}
