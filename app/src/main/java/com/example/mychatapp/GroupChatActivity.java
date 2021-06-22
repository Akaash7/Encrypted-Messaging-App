package com.example.mychatapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mychatapp.Adapter.AdapterGroupChat;
import com.example.mychatapp.Model.ModelGroupChat2;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private String groupId,myGroupRole="";
    private Toolbar toolbar;
    private ImageView groupIconIv;
    private ImageButton attachBtn,sendBtn;
    private TextView groupTitleTv;
    private EditText messageEt;
    private RecyclerView chatRv;

    private ArrayList<ModelGroupChat2> groupChatList;
    private AdapterGroupChat adapterGroupChat;


    private static final int  CAMERA_REQUEST_CODE =200;
    private static final int  STORAGE_REQUEST_CODE =400;

    //iamgepik
    private static final int  IMAGE_PICK_GALLERY_CODE =1000;
    private static final int  IMAGE_PICK_CAMERA_CODE =1000;

    private String[] cameraPermission;
    private String[] storagePermission;

    private Uri image_uri = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        toolbar = findViewById(R.id.toolbar);
        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitleTv = findViewById(R.id.groupTitleTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        chatRv = findViewById(R.id.chatRv);
        setSupportActionBar(toolbar);

        //get id of the group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        //init permisions
        cameraPermission = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        storagePermission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadGroupMessages();
        loadMyGroupRole();


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //input
                String message = messageEt.getText().toString().trim();
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(GroupChatActivity.this,"Cant Send empty message",Toast.LENGTH_SHORT);
                }
                else{
                    //send
                    sendMessage(message);
                }
            }
        });

        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageImportDialog();

            }
        });
    }

    private void showImageImportDialog() {
        String[] options = {"Camera","Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0){
                            //camera clicked
                            if(!checkCameraPermission()){
                                requestCameraPermission();
                            } else {
                                pickCamera();
                            }
                        } else{
                            //gallery clicked
                            if(!checkStoragePermission()){
                                requestStoragePermission();
                            } else{
                                pickGallery();
                            }
                        }
                    }

                }).show();
    }
    private void pickGallery(){
        //itent to pick image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }
    private void pickCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"GroupImageTitle");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"GroupDescription");
         image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

         Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
         startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }
    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
                return result;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }
    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result&&result1;
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants")
                .orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        menu.findItem(R.id.createGroup).setVisible(false);
        if(myGroupRole.equals("creator")||myGroupRole.equals("admin")){
            menu.findItem(R.id.action_add_participant).setVisible(true);
        }
        else{
            menu.findItem(R.id.action_add_participant).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_add_participant){
            Intent intent = new Intent(this, GroupParticipantAddActivity.class);
            intent.putExtra("groupId",groupId);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadGroupMessages() {
        groupChatList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        groupChatList.clear();
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelGroupChat2 model = ds.getValue(ModelGroupChat2.class);
                            groupChatList.add(model);

                        }

                        adapterGroupChat = new AdapterGroupChat(GroupChatActivity.this,groupChatList);
                        chatRv.setAdapter(adapterGroupChat);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage(String message){
        //timestamp
        String timestamp = ""+System.currentTimeMillis();

        //setup message data
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender"," "+firebaseAuth.getUid());
        hashMap.put("message",""+message);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("type",""+"text");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //message sent
                        messageEt.setText("");

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            String groupTitle = ""+ds.child("groupTitle").getValue();
                            String groupDescription = ""+ds.child("groupDescription").getValue();
                            String groupIcon = ""+ds.child("groupIcon").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String createdBy = ""+ds.child("createdBy").getValue();

                            groupTitleTv.setText(groupTitle);
                            try{
                                //picasso
                            }
                            catch (Exception e){
                                groupIconIv.setImageResource(R.drawable.common_google_signin_btn_icon_light);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();
                sendImageMessage();

            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                sendImageMessage();
            }
        }
    }

    private void sendImageMessage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Please Wait");
        pd.setMessage("Sending Image");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        //filename
        String filenamePath = "ChatImages/" + System.currentTimeMillis();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filenamePath);

        storageReference.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!p_uriTask.isSuccessful());
                        Uri p_downloadUri = p_uriTask.getResult();

                        if(p_uriTask.isSuccessful()){
                            //image url received, save in db

                            //timestamp
                            String timestamp = ""+System.currentTimeMillis();

                            //setup message data
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("sender"," "+firebaseAuth.getUid());
                            hashMap.put("message",""+image_uri);
                            hashMap.put("timestamp",""+timestamp);
                            hashMap.put("type",""+"image");

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                            ref.child(groupId).child("Messages").child(timestamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //message sent
                                            messageEt.setText("");
                                            pd.dismiss();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(GroupChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                                }
                            });


                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1]== PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted&&writeStorageAccepted){
                        pickCamera();
                    }
                    else{
                        Toast.makeText(this, "Camera&Storage permissions are required....", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case  STORAGE_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean writeStorageAccepted = grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted){
                        pickGallery();
                    }
                    else{
                        Toast.makeText(this, "Storage Permission Required", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
}