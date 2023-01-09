package com.example.instabook;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder>
{
    Context context;
    List<ModelUsers> userList;

    //constructor
    public AdapterUser(Context context, List<ModelUsers> userList)
    {
        this.context=context;
        this.userList=userList;
    }
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout(row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get the data
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String userEmail=userList.get(position).getEmail();
        String uidStr = userList.get(position).getUid();

        //set data
        holder.nameTxt.setText(userName);
        holder.emailTxt.setText(userEmail);
        try
        {
            //try to set the user profilePicture
            Picasso.get().load(userImage).placeholder(R.drawable.ic_person).into(holder.avatar);
        }
        catch(Exception e)
        {

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                click to go to profilePosts with the uid of a clicked user
                 */
                Intent intent = new Intent(context,ProfilePosts.class);
                intent.putExtra("uidStr",uidStr);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder class for user
    class MyHolder extends RecyclerView.ViewHolder
    {
        ImageView avatar;
        TextView nameTxt,emailTxt;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.avatar);
            nameTxt=itemView.findViewById(R.id.nameTxt);
            emailTxt=itemView.findViewById(R.id.emailTxt);
        }
    }
}
