package com.example.mychatapp.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mychatapp.Model.ModelGroupChat;
import com.example.mychatapp.Model.ModelGroupChat2;
import com.example.mychatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<ModelGroupChat2> modelGroupChat2ArrayList;

    private FirebaseAuth firebaseAuth;

    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat2> modelGroupChat2ArrayList){
        this.context = context;
        this.modelGroupChat2ArrayList = modelGroupChat2ArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right,parent,false);
            return new HolderGroupChat(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left,parent,false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holderGroupChat, int position) {
        ModelGroupChat2 model = modelGroupChat2ArrayList.get(position);
        String message = model.getMessage();
        String timestamp = model.getTimestamp();
        String senderUid = model.getSender();
        String messageType = model.getType();


        //conversion
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

        if(messageType.equals("text")){
            //text message, hide imageview,showmessageTv
            holderGroupChat.messageIv.setVisibility(View.GONE);
            holderGroupChat.messageTv.setVisibility(View.VISIBLE);
            holderGroupChat.messageTv.setText(message);
        }
        else{
            //image message
            holderGroupChat.messageIv.setVisibility(View.VISIBLE);
            holderGroupChat.messageTv.setVisibility(View.GONE);
            try{
                //picasso
            }
            catch(Exception e){
                holderGroupChat.messageIv.setImageResource(R.drawable.drawable_red_gradient);

            }

        }

        //holderGroupChat.messageTv.setText(message);
        holderGroupChat.TimeTv.setText(dateTime);

        setUserName(model, holderGroupChat);
    }

    private void setUserName(ModelGroupChat2 model, final HolderGroupChat holderGroupChat) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       for(DataSnapshot ds: dataSnapshot.getChildren()){
                           String name = ""+ds.child("name").getValue();

                           holderGroupChat.nameTv.setText(name);
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelGroupChat2ArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(modelGroupChat2ArrayList.get(position).getSender().equals(firebaseAuth.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else{
            return MSG_TYPE_LEFT;
        }
    }

    class HolderGroupChat extends RecyclerView.ViewHolder{

        private TextView nameTv,messageTv,TimeTv;
        private ImageView messageIv;
        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv= itemView.findViewById(R.id.messageTv);
            TimeTv = itemView.findViewById(R.id.timeTv);
            messageIv = itemView.findViewById(R.id.messageIv);

        }
    }
}
