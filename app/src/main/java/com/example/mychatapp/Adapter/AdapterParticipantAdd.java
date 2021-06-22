package com.example.mychatapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mychatapp.Model.User;
import com.example.mychatapp.Notifications.Data;
import com.example.mychatapp.R;
import com.google.android.gms.common.data.DataBuffer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;

public class AdapterParticipantAdd extends RecyclerView.Adapter<AdapterParticipantAdd.HolderParticipantAdd>  {

    private Context context;
    private ArrayList<User> userList;
    private String groupID,myGroupRole;

    public AdapterParticipantAdd(Context context, ArrayList<User> userList, String groupID, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupID = groupID;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantAdd onCreateViewHolder(@NonNull ViewGroup parent, int postition) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_participant_add,parent,false);
        return new HolderParticipantAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantAdd holderParticipantAdd, int position) {
        final User modelUser = userList.get(position);
        String name = modelUser.getUsername();
        //String email = modelUser.getEmail();
        String email = "nm123@gmail.com";
        String image = modelUser.getImageURL();
        final String uid = modelUser.getId();

        //set data
        holderParticipantAdd.nameTv.setText(name);
        holderParticipantAdd.emailTv.setText(email);
        try{
            //picasso
        }
        catch (Exception e){
            holderParticipantAdd.avatarIv.setImageResource(R.drawable.lock);
        }

        checkIfAlreadyExists(modelUser,holderParticipantAdd);
        holderParticipantAdd.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               //check if user already added or not
               //if added show remove participant/make-admin/remove-admin option/admin wont be able to change the role of the creator
               //if not added, show add participant options
               DatabaseReference ref =  FirebaseDatabase.getInstance().getReference("Groups");
               ref.child(groupID).child("Participants").child(uid)
                       .addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                               if (dataSnapshot.exists()){
                                   //user exists
                                   String hisPreviousRole = ""+dataSnapshot.child("role").getValue();
                                   //options to display in dialog
                                   String[] options;
                                   AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                   builder.setTitle("Choose Option");
                                   if(myGroupRole.equals("creator")){
                                       if(hisPreviousRole.equals("admin")){
                                           //im creator, he is admin
                                           options = new String[]{"Remove Admin","Remove User"};
                                           builder.setItems(options, new DialogInterface.OnClickListener() {
                                               @Override
                                               public void onClick(DialogInterface dialog, int which) {
                                                   //handle item clicks
                                                   if(which==0){
                                                        //Remove Admin clicked
                                                       removeAdmin(modelUser);
                                                   }
                                                   else{
                                                       removeParticipant(modelUser);
                                                   }
                                               }
                                           }).show();
                                       }
                                       else if(hisPreviousRole.equals("participants")){
                                           //im creator, he is participant
                                           options = new String[]{"Make Admin","Remove User"};
                                           builder.setItems(options, new DialogInterface.OnClickListener() {
                                               @Override
                                               public void onClick(DialogInterface dialog, int which) {
                                                   if(which==0){
                                                       //make adming clicked
                                                       makeAdmin(modelUser);
                                                   }
                                                   else{
                                                       //Remove User clicked
                                                       removeParticipant(modelUser);
                                                   }
                                               }
                                           });
                                       }
                                   }
                                   else if(myGroupRole.equals("admin")){
                                       if(hisPreviousRole.equals("creator")){
                                        //im admin hi is creator
                                           Toast.makeText(context,"Creator of Group...",Toast.LENGTH_SHORT);

                                       }
                                       else if(hisPreviousRole.equals("admin")){
                                           //im adming he is admin too
                                           options = new String[]{"Remove Admin","Remove User"};
                                           builder.setItems(options, new DialogInterface.OnClickListener() {
                                               @Override
                                               public void onClick(DialogInterface dialog, int which) {
                                                   //handle item clicks
                                                   if(which==0){
                                                       //Remove Admin clicked
                                                       removeAdmin(modelUser);
                                                   }
                                                   else{
                                                       removeParticipant(modelUser);
                                                   }
                                               }
                                           }).show();                                       }
                                       else if(hisPreviousRole.equals("participant")){
                                           options = new String[]{"Remove Admin","Remove User"};
                                           builder.setItems(options, new DialogInterface.OnClickListener() {
                                               @Override
                                               public void onClick(DialogInterface dialog, int which) {
                                                   //handle item clicks
                                                   if(which==0){
                                                       //Remove Admin clicked
                                                       removeAdmin(modelUser);
                                                   }
                                                   else{
                                                       removeParticipant(modelUser);
                                                   }
                                               }
                                           }).show();                                   }

                                   }
                               }
                               else{
                                   //user doesnt exists/not-participant:add
                                   AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                   builder.setTitle("Add Participant")
                                           .setMessage("Add this user in this group?")
                                           .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                               @Override
                                               public void onClick(DialogInterface dialogInterface, int i) {
                                                   addParticipant(modelUser);
                                               }
                                           })
                                           .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                               @Override
                                               public void onClick(DialogInterface dialog, int which) {
                                                   dialog.dismiss();

                                               }
                                           }).show();

                               }
                           }

                           @Override
                           public void onCancelled(@NonNull DatabaseError databaseError) {

                           }
                       });
            }
        });

    }

    private void addParticipant(User modelUser) {
        //setup user data - add user in group
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("uid",modelUser.getId());
        hashMap.put("role","participant");
        hashMap.put("timestamp",""+timestamp);
        //add that user in Groups>groupId>Participants
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupID).child("Participants").child(modelUser.getId()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,"",Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void makeAdmin(User modelUser) {
        //setup data - changerole
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("role","admin");
        //update role in db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupID).child("Participants").child(modelUser.getId()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //make admin
                        Toast.makeText(context, "The user is now admin...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeParticipant(User modelUser) {
        //remove participant from group
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupID).child("Participants").child(modelUser.getId()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //removed successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    //failed making admin
                    }
                });
    }

    private void removeAdmin(User modelUser) {
        //setup data = remove admin - just change role
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("role","participant");//participant/admin/creator
        //update role in db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupID).child("Participants").child(modelUser.getId()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //make admin
                        Toast.makeText(context, "The user is no longer admin...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfAlreadyExists(User modelUser, final HolderParticipantAdd holderParticipantAdd) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupID).child("Participants").child(modelUser.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       if(dataSnapshot.exists()){
                           //already exysts
                           String hisRole = ""+dataSnapshot.child("role").getValue();
                           holderParticipantAdd.statusTv.setText(hisRole);
                       }
                       else {
                            //doesnt
                           holderParticipantAdd.statusTv.setText("");
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return 0;
    }


    class HolderParticipantAdd extends RecyclerView.ViewHolder{

        private ImageView avatarIv;
        private TextView nameTv,emailTv,statusTv;

        public HolderParticipantAdd(@NonNull View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            statusTv = itemView.findViewById(R.id.statusTv);

        }
    }
}
