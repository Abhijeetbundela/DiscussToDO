package com.example.letsdiscusstodo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout mForgotPasswordEmail;
    private Button mForgotPasswordBtn;

    private FirebaseAuth mAuth;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Forgot Password");

        mForgotPasswordEmail = findViewById(R.id.forgot_password_email);
        mForgotPasswordBtn = findViewById(R.id.forgot_password_btn);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle("Sending mail.....");

        mAuth = FirebaseAuth.getInstance();

        mForgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressDialog.show();


                String resetemail = mForgotPasswordEmail.getEditText().getText().toString().trim();
                boolean status = true;

                if (resetemail.isEmpty()) {

                    mForgotPasswordEmail.setError("Enter Email");
                    mProgressDialog.dismiss();
                    status = false;
                } else {
                    mForgotPasswordEmail.setErrorEnabled(false);
                }

                if (status) {

                    mAuth.sendPasswordResetEmail(resetemail).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Check your email to reset password", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                }

            }
        });


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
