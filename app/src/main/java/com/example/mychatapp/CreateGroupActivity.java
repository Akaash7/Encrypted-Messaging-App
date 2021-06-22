package com.example.mychatapp;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.drm.DrmStore;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mychatapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class CreateGroupActivity extends AppCompatActivity {
    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;
    //picked image uri
    private Uri image_uri = null;
    //actionbar
    private ActionBar actionBar;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    //UI
    private ImageView groupIconIv;
    private EditText groupTitle,groupDescription;
    private FloatingActionButton createBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //initiliaze UI views
        groupIconIv = findViewById(R.id.group_make_foreground);
        groupTitle = findViewById(R.id.groupname);
        groupDescription = findViewById(R.id.groupdesc);
        createBtn =  findViewById(R.id.createGroupBtn);

        //init permisson arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};



        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        //pick image
        groupIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //handle click event
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreatingGroup();

            }
        });

    }

    private void startCreatingGroup() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Group");
        
        //input title,description
        String groupTitleet = groupTitle.getText().toString().trim();
        String groupDescri = groupDescription.getText().toString().trim();
        //validation
        if(TextUtils.isEmpty(groupTitleet)){
            Toast.makeText(this,"Please Enter Group Title...",Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressDialog.show();

        final String g_timestamp = ""+System.currentTimeMillis();

        if(image_uri==null){
            
            CreateGroup(""+g_timestamp,""+groupTitle,""+groupDescri,"");
        }
        else{
            String fileNameAndPath = "Group_Imgs/"+g_timestamp;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded get url
                            Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while(!p_uriTask.isSuccessful());
                            Uri p_downloaderUri = p_uriTask.getResult();
                            if(p_uriTask.isSuccessful()){
                                CreateGroup(
                                        ""+g_timestamp,
                                        ""+groupTitle,
                                        ""+groupDescription,
                                        ""+p_downloaderUri
                                );
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(CreateGroupActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT);
                }
            });
        }
    }

    private void CreateGroup(final String g_timestamp, String groupTitle, String groupDescription, String groupIcon) {
        final HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("groupID",""+g_timestamp);
        hashMap.put("groupTitle",""+groupTitle);
        hashMap.put("groupDescription",""+groupDescription);
        hashMap.put("groupIcon",""+groupIcon);
        hashMap.put("timestamp",""+g_timestamp);
        hashMap.put("createdBy",""+firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(g_timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //created Success

                        //setup memberinf(add current user in groups participants list)
                        HashMap<String,String> hashMap1 = new HashMap<>();
                        hashMap1.put("uid",firebaseAuth.getUid());
                        hashMap1.put("role","creator");
                        hashMap.put("timestamp",g_timestamp);

                        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
                        ref1.child(g_timestamp).child("Participants").child(firebaseAuth.getUid())
                                .setValue(hashMap1)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //participant added
                                        progressDialog.dismiss();
                                        Toast.makeText(CreateGroupActivity.this,"Group created...",Toast.LENGTH_SHORT);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(CreateGroupActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed
                progressDialog.dismiss();
                Toast.makeText(CreateGroupActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showImagePickDialog(){
        //options to pick omage
        String[] options = {"Camera","Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image:")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //handle clicks
                        if(i==0){
                            //camera clicked
                            if(!checkCameraPermissions()){
                                requestCameraPermissions();
                            }
                            else{
                                pickFromCamera();
                            }
                        }
                        else{
                                if(!checkStoragePermissions()){
                                    requestStoragePermissions();
                                }
                                else{
                                    pickFromGallery();
                                }
                        }
                    }
                }).show();

    }

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);

    }
    private void pickFromCamera(){
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Group Image Icon");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Group Image Icon Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }


    private boolean checkStoragePermissions(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermissions(){
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermissions(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result&&result1;
    }
    private void requestCameraPermissions(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }


    private void checkUser(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){
            actionBar.setSubtitle(user.getEmail());
        }



    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //handle permission result
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if(cameraAccepted && storageAccepted){
                    //permission allowed
                    pickFromCamera();
                }
                else{
                    Toast.makeText(this,"Camera Storage Permissions are required", Toast.LENGTH_SHORT).show();
                }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:
                {
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        pickFromGallery();
                    }
                    else{
                        Toast.makeText(this,"Storage Permission Required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();
                groupIconIv.setImageURI(image_uri);
            }
        }
        else if(requestCode == IMAGE_PICK_CAMERA_CODE){
            //picked from camera
            //set to imageview
            groupIconIv.setImageURI(image_uri);

        }


        super.onActivityResult(requestCode, resultCode, data);
    }
}