package com.example.letsdiscusstodo.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.letsdiscusstodo.R;
import com.example.letsdiscusstodo.dialog.MyProgressDialog;
import com.example.letsdiscusstodo.model.UserInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserInfoActivity extends AppCompatActivity {

    private Button mUserProfileBtn;

    private TextInputLayout mUserName, mUserAbout;

    private CircleImageView mUserPic;

    private StorageReference mStorageRef;

    private FirebaseAuth mAuth;

    private FirebaseFirestore mFirestore;

    private DatabaseReference mDatabase;

    private String userId;

    private Uri uri = null;

    private String profileUri = "default", thumbUri = "default";

    private int galleryRequest = 1;

    private MyProgressDialog mMyProgressDialog;

    private boolean userDataStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserName = findViewById(R.id.user_name);
        mUserAbout = findViewById(R.id.user_about);
        mUserPic = findViewById(R.id.profile_image);
        mUserProfileBtn = findViewById(R.id.user_profile_btn);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirestore = FirebaseFirestore.getInstance();

        userId = mAuth.getUid();

        FirebaseUser user = mAuth.getCurrentUser();

        mUserName.getEditText().setText(user.getDisplayName());

        // Glide.with(getApplicationContext()).load(user.getPhotoUrl()).into(mUserPic);

        mStorageRef = FirebaseStorage.getInstance().getReference("profile_images/");


        mMyProgressDialog = new MyProgressDialog(this);

        mDatabase.child("users/" + userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {


                if (dataSnapshot.exists()) {

                    userDataStatus = false;

                    UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);
                    Log.d("Abhijeet", "onDataChange: " + userInformation.getProfileUri());

                    mUserName.getEditText().setText(userInformation.getUserName());

                    mUserAbout.getEditText().setText(userInformation.getAbout());


                    if (userInformation.getProfileUri().equals("default")) {
                        mUserPic.setImageResource(R.drawable.user);
                    } else {
                        Glide.with(getApplicationContext()).load(userInformation.getProfileUri()).into(mUserPic);
                    }

                } else {
                    userDataStatus = true;
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


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

    private String getFileExtension(Uri uri) {

        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadImages() {

        mUserProfileBtn.setEnabled(false);
        mUserAbout.setEnabled(false);
        mUserName.setEnabled(false);
        mUserPic.setEnabled(false);
        mUserProfileBtn.setText("Uploading Image....");


        if (uri != null) {


            final StorageReference picRef = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(uri));


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

        mUserProfileBtn.setText("Few more sec...");

        String userName = mUserName.getEditText().getText().toString().trim();

        String userAbout = mUserAbout.getEditText().getText().toString().trim();

        if (userAbout.isEmpty()) {
            userAbout = ".";
        }

        String userEmail = mAuth.getCurrentUser().getEmail();


        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {


                mDatabase.child("users").child(userId).child("device_token").setValue(instanceIdResult.getToken());


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        UserInformation userInformation = new UserInformation(userId, userName, userAbout, profileUri, userEmail);


        mDatabase.child("users").child(userId).setValue(userInformation).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                mMyProgressDialog.dismiss();

                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);
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


    @Override
    public void onBackPressed() {

        if (userDataStatus) {
            alertDialog();

        } else {


            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
            finish();
        }


    }

    private void alertDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false).setTitle("Are you sure you want to exit?").setMessage("If 'Yes' then you will not able to write a post.");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);
                finish();

            }
        });

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;

    }


}

