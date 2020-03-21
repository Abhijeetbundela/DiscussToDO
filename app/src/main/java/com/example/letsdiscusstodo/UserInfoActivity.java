package com.example.letsdiscusstodo;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.example.letsdiscusstodo.dialog.MyProgressDialog;
import com.example.letsdiscusstodo.model.UserInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class UserInfoActivity extends AppCompatActivity {

    private Button mUserProfileBtn;

    private TextInputLayout mUserName, mUserAbout;

    private CircleImageView mUserPic;

    private StorageReference mStorageRef;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;


    private String userId;

    private FirebaseFirestore database;

    private Uri uri = null;

    private String profileUri = "default", thumbUri = "default";

    private byte[] thumbByte;

    private int galleryRequest = 1;

    private MyProgressDialog mMyProgressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        getSupportActionBar().setTitle("Profile");

        mUserName = findViewById(R.id.user_name);
        mUserAbout = findViewById(R.id.user_about);
        mUserPic = findViewById(R.id.profile_image);
        mUserProfileBtn = findViewById(R.id.user_profile_btn);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        userId = mAuth.getUid();

        database = FirebaseFirestore.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference("images/profileImages/");
       // mDatabaseReference = FirebaseDatabase.getInstance().getReference("uploads");

        mMyProgressDialog = new MyProgressDialog(this);

        mMyProgressDialog.setTitle("Uploading.....");
        mMyProgressDialog.setMessage("");
        mMyProgressDialog.setCancelable(false);

        mUserPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), galleryRequest);

            }
        });

        mUserProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMyProgressDialog.show();

                if (checkInputs()) {

                    uploadImages();

                }


            }
        });


    }

    private String getFileExtension(Uri uri){

        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

//    private void uploadFile() {
//
//        final String userName = mUserName.getEditText().getText().toString().trim();
//
//        final String userAbout = mUserAbout.getEditText().getText().toString().trim();
//
//        if(uri != null){
//            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(uri));
//
//            fileReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                    String userEmail = mAuth.getCurrentUser().getEmail();
//
//                    UserInformation userInformation = new UserInformation(userId,userName,userAbout,taskSnapshot.getUploadSessionUri().toString(),userEmail);
//                    String uploadId = mDatabaseReference.push().getKey();
//                    mDatabaseReference.child(uploadId).setValue(userInformation);
//                    mMyProgressDialog.dismiss();
//                    Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_SHORT).show();
//
//
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                }
//            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
//
//                }
//            });
//
//        }else{
//
//
//        }
//
//    }

    private void uploadImages() {

        mUserProfileBtn.setEnabled(false);
        mUserAbout.setEnabled(false);
        mUserName.setEnabled(false);
        mUserPic.setEnabled(false);
        mUserProfileBtn.setText("Uploading Image....");


        if (uri != null) {




            final StorageReference picRef = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(uri));
            //final StorageReference thumbRef = mStorageRef.child("images/profileImages/thumbs/$userId.jpg");

            picRef.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        picRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {

                                    profileUri = task.getResult().toString();

                                    upLoadUserData();

                                }
                            }
                        });

                    }
                }
            });


        } else {

            upLoadUserData();

        }

    }


    private void upLoadUserData() {

        mUserProfileBtn.setText("Uploading Data...");

        String userName = mUserName.getEditText().getText().toString().trim();

        String userAbout = mUserAbout.getEditText().getText().toString().trim();

        if (userAbout == "") {
            userAbout = ".";
        }

        String userEmail = mAuth.getCurrentUser().getEmail();

        UserInformation userInformationData = new UserInformation(userId, userName, userAbout, profileUri, userEmail);

        mDatabase.child("users").child(userId).setValue(userInformationData);

        database.collection("users_information_data").
                document(userId).set(userInformationData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                mMyProgressDialog.dismiss();

                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                mMyProgressDialog.dismiss();
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        mMyProgressDialog.dismiss();
    }

    private boolean checkInputs() {

        String userName = mUserName.getEditText().getText().toString().trim();

        if (userName.length() < 3) {
            mUserName.setError("Enter valid name");
            return false;
        } else {
            mUserName.setErrorEnabled(false);
            return true;
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryRequest && resultCode == Activity.RESULT_OK) {
            if (data != null) {

                uri = data.getData();
                mUserPic.setImageURI(uri);

//                CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON)
//                        .setCropShape(CropImageView.CropShape.OVAL).setAspectRatio(1, 1).start(this);

            }

//            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//
//                CropImage.ActivityResult result = CropImage.getActivityResult(data);
//                if (resultCode == RESULT_OK) {
//
//                    uri = result.getUri();
//                    mUserPic.setImageURI(uri);
//
//
//                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                    Exception error = result.getError();
//                    Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//
//
//                }
//
//
//            }
        }


    }


}

