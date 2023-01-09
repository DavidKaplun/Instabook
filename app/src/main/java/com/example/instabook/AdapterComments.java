package com.example.instabook;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder>{
    /*
    this is a class of the adapter of the comment
    from which the comments will be shown in the
    recyclerView based on where user scrolled.
     */

    //the context of the adapter and the commentList declaration.
    Context context;
    List<ModelComment> modelCommentList;

    public AdapterComments(Context applicationContext, List<ModelComment> commentList) {
        this.context=applicationContext;
        this.modelCommentList=commentList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the row_comments.xml layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent,false);
        return new MyHolder(view);
    }

    /*
    this is a function which is called by recyclerView to display the
    data at a specified position.
    this method is used to update the contents of the itemView
    to show the item at the given position.
     */
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String comment = modelCommentList.get(position).getComment();
        String time = modelCommentList.get(position).getTimeStamp();
        String name = modelCommentList.get(position).getUserName();
        String userImage = modelCommentList.get(position).getUserImage();
        //convert time to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(time));
        String timeStr2 = DateFormat.format("dd/mm/yyyy hh:mm am/pm",calendar).toString();

        //set the comment data to the holder
        holder.nameTxt.setText(name);
        holder.commentTxt.setText(comment);
        holder.timeTxt.setText(time);

        //set user profile picture
        try
        {
            Picasso.get().load(userImage).into(holder.avatarImg);
        }catch(Exception e)
        {

        }
    }

    @Override
    public int getItemCount() {
        return modelCommentList.size();
    }

    /*
    this is a class of the holder
    which will be generated into the
    recycler view when the user scrolls
     */
    class MyHolder extends RecyclerView.ViewHolder
    {
        //declare views from row comments.xml
        ImageView avatarImg;
        TextView nameTxt,commentTxt,timeTxt;

        public MyHolder(@NonNull View itemView)
        {
            super(itemView);
            avatarImg= itemView.findViewById(R.id.avatarImg);
            nameTxt=itemView.findViewById(R.id.nameTxt);
            commentTxt = itemView.findViewById(R.id.commentTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
        }
    }
}
