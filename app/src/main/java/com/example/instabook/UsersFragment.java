package com.example.instabook;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {


    //firebase auth
    FirebaseAuth firebaseAuth;

    public UsersFragment() {
        // Required empty public constructor
    }


    RecyclerView recyclerView;
    AdapterUser adapterUsers;
    List<ModelUsers> userList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.users_recyclerView);

        recyclerView.setHasFixedSize(true);//avoid unnecesery layout passes
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));//layoutManager is responsible for measuring the positioning of an item in recyclerView

        //init user list
        userList=new ArrayList<>();

        getAllUsers();

        return view;
    }

    private void getAllUsers()
    {
        final FirebaseUser dbUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    ModelUsers modelUser = ds.getValue(ModelUsers.class);

                    //get all users except the currently signed in user
                    if(!modelUser.getUid().equals(dbUser.getUid()))
                    {
                        userList.add(modelUser);
                    }

                    //adapter for the userList
                    adapterUsers = new AdapterUser(getActivity(), userList);
                    //set recyclerView's adapter to adapterUsers
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchUsers(String namesToSearch)
    {
        final FirebaseUser dbUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    ModelUsers modelUser = ds.getValue(ModelUsers.class);

                    //get all users except the currently signed in user
                    if(!modelUser.getUid().equals(dbUser.getUid()))
                    {
                        if(modelUser.getName().toUpperCase().contains(namesToSearch.toUpperCase()))
                        {
                            userList.add(modelUser);
                        }
                    }
                    //adapter for the userList
                    adapterUsers = new AdapterUser(getActivity(), userList);
                    //refresh adapter
                    adapterUsers.notifyDataSetChanged();
                    //set recyclerView's adapter to adapterUsers
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

        //hide add post button from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);

        //search view
        MenuItem item= menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String namesToSearch) {
                //called when user presses enter after writing the name to search
                //if the name is not empty string then search
                if(!TextUtils.isEmpty(namesToSearch.trim()))
                {
                    searchUsers(namesToSearch);
                }
                else
                {
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String namesToSearch) {
                //called whenever a new letter was entered into the search bar
                //if the name is not empty string then search
                if(!TextUtils.isEmpty(namesToSearch.trim()))
                {
                    searchUsers(namesToSearch);
                }
                else
                {
                    getAllUsers();
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
            startActivity(new Intent(getActivity(),MainActivity.class));
            getActivity().finish();
        }
    }
}