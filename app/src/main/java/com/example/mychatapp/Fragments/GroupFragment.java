package com.example.mychatapp.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mychatapp.Adapter.AdapterGroupChatList;
import com.example.mychatapp.Model.ModelGroupChat;
import com.example.mychatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class GroupFragment extends Fragment {

    private RecyclerView groupsRv;
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelGroupChat> groupChatsLists;
    private AdapterGroupChatList adapterGroupChatList;

    public GroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group,container,false);

        groupsRv = view.findViewById(R.id.groupsRv);
        firebaseAuth = FirebaseAuth.getInstance();

        loadGroupChatsList();

        return view;
    }

    private void loadGroupChatsList() {
        groupChatsLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupChatsLists.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    //if current users uid exists in participants list the group then show that group
                    if(ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        ModelGroupChat model = ds.getValue(ModelGroupChat.class);
                        groupChatsLists.add(model);
                    }
                }
                adapterGroupChatList = new AdapterGroupChatList(getActivity(), groupChatsLists);
                groupsRv.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void searchGroupChatsList(final String query) {
        groupChatsLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupChatsLists.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    //if current users uid exists in participants list the group then show that group
                    if(ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        if(ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){
                            ModelGroupChat model = ds.getValue(ModelGroupChat.class);
                            groupChatsLists.add(model);
                        }
                    }
                }
                adapterGroupChatList = new AdapterGroupChatList(getActivity(), groupChatsLists);
                groupsRv.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}