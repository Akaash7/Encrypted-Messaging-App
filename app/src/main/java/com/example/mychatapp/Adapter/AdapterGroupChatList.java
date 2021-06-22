package com.example.mychatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mychatapp.GroupChatActivity;
import com.example.mychatapp.Model.ModelGroupChat;
import com.example.mychatapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChatList extends  RecyclerView.Adapter<AdapterGroupChatList.HolderGroupChatList> {
    private Context context;
    private ArrayList<ModelGroupChat> groupChatLists;

    public AdapterGroupChatList(Context context,ArrayList<ModelGroupChat> groupChatLists){
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

       View view = LayoutInflater.from(context).inflate(R.layout.row_groupchats_list,parent,false);
       return null;
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChatList holder, int position) {
//get data
        ModelGroupChat model = groupChatLists.get(position);
        final String groupID = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupTitle = model.getGroupTitle();

        holder.nameTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");

        //load last message and message-time
        loadLastMessage(model,holder);

        //set data
        holder.groupTitle.setText(groupTitle);
        try{
            //picasso
        }
        catch (Exception e){
            holder.groupIconIv.setImageResource(R.drawable.group_make_foreground);

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open group chat
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupID",groupID);
                context.startActivity(intent);
            }
        });
    }

    private void loadLastMessage(ModelGroupChat model, final HolderGroupChatList holder) {
        //gete last message from group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1)//get last item from that child
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){

                            String message = ""+ds.child("message").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String sender = ""+ds.child("sender").getValue();
                            String messageType = ""+ds.child("type").getValue();



                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

                            if(messageType.equals("image")){
                                holder.messageTv.setText("sent photo");
                            }
                            else {
                                holder.messageTv.setText(message);
                            }

                            holder.timeTv.setText(dateTime);


                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for(DataSnapshot ds: dataSnapshot.getChildren()){
                                                String name = ""+ds.child("name").getValue();
                                                holder.nameTv.setText(name);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }

    //view holder class
    class HolderGroupChatList extends RecyclerView.ViewHolder{
        //ui views
        private ImageView groupIconIv;
        private TextView groupTitle,nameTv,messageTv,timeTv;
        public HolderGroupChatList(View itemView) {
            super(itemView);

            groupIconIv = itemView.findViewById(R.id.groupIconIv);
            groupTitle = itemView.findViewById(R.id.groupTitleIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messagetv);
            timeTv = itemView.findViewById(R.id.timetv);

        }
    }
}
