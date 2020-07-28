package com.example.letsdiscusstodo.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.letsdiscusstodo.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mLoginBtn;
    private TextView mForgotPasswordTextView, mSignUpTextView;
    private TextInputLayout mInputLogInEmail, mInputLogInPassword;

    private FirebaseAuth mAuth;




    private FirebaseUser user;

    private ProgressDialog mProgressDialog;

    private DatabaseReference userDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("LogIn");

        mAuth = FirebaseAuth.getInstance();

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle("SignUp.....");

        mLoginBtn = findViewById(R.id.login_btn);
        mForgotPasswordTextView = findViewById(R.id.forgot_password_text_view);
        mSignUpTextView = findViewById(R.id.sign_up_text_view);
        mInputLogInEmail = findViewById(R.id.login_input_email);
        mInputLogInPassword = findViewById(R.id.login_input_password);

        mLoginBtn.setOnClickListener(this);
        mForgotPasswordTextView.setOnClickListener(this);
        mSignUpTextView.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.login_btn: {

                final String inputEmail = mInputLogInEmail.getEditText().getText().toString().trim();
                final String inputPassword = mInputLogInPassword.getEditText().getText().toString().trim();

                boolean status = true;

                if (inputEmail.isEmpty()) {
                    mInputLogInEmail.setError("Enter Email");
                    status = false;
                } else {
                    mInputLogInEmail.setErrorEnabled(false);
                }

                if (inputPassword.isEmpty()) {
                    mInputLogInPassword.setError("Enter Password");
                    status = false;
                } else {
                    mInputLogInPassword.setErrorEnabled(false);
                }

                if (status) {
                    mProgressDialog.show();
                    logIn(inputEmail, inputPassword);

                }
                break;


            }

            case R.id.sign_up_text_view: {

                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                break;
            }

            case R.id.forgot_password_text_view: {


                Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;

            }


        }

    }

    private void logIn(String inputEmail, String inputPassword) {


        mAuth.signInWithEmailAndPassword(inputEmail, inputPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {


                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {

                        String userUID = mAuth.getCurrentUser().getUid();


                        userDatabaseReference.child(userUID).child("device_token").setValue(instanceIdResult.getToken())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        checkVerifiedEmail();
                                    }
                                });

                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                mProgressDialog.dismiss();
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void checkVerifiedEmail() {
        user = mAuth.getCurrentUser();
        boolean isVerified = false;
        if (user != null) {
            isVerified = user.isEmailVerified();
        }
        if (isVerified) {
            String UID = mAuth.getCurrentUser().getUid();
            userDatabaseReference.child(UID).child("verified").setValue("true");

            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);


        } else {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Email is not verified. Please verify first", Toast.LENGTH_LONG).show();
            mAuth.signOut();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;

    }

    @Override
    protected void onStop() {
        super.onStop();
        mProgressDialog.dismiss();
    }
}
