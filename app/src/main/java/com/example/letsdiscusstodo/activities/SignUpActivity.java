package com.example.letsdiscusstodo.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.letsdiscusstodo.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private Button mSignBtn;
    private TextInputLayout mInputSignUpEmail, mInputSignUpPassword, mInputSignUpConfrimPassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("SignUp");

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle("SignUp.....");

        mSignBtn = findViewById(R.id.sign_up_btn);
        mInputSignUpEmail = findViewById(R.id.sign_up_input_email);
        mInputSignUpPassword = findViewById(R.id.sign_up_input_password);
        mInputSignUpConfrimPassword = findViewById(R.id.sign_up_input_confirm_password);


        mSignBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String inputEmail = mInputSignUpEmail.getEditText().getText().toString().trim();
                String inputPassword = mInputSignUpPassword.getEditText().getText().toString().trim();
                String inputConfirmPassword = mInputSignUpConfrimPassword.getEditText().getText().toString().trim();

                Boolean status = true;

                if (inputEmail.isEmpty()) {

                    mInputSignUpEmail.setError("Enter Email");
                    status = false;

                } else {
                    mInputSignUpEmail.setErrorEnabled(false);
                }

                if (inputPassword.isEmpty()) {

                    mInputSignUpPassword.setError("Enter Password");
                    status = false;

                } else {
                    mInputSignUpPassword.setErrorEnabled(false);

                }

                if (inputConfirmPassword.isEmpty()) {

                    mInputSignUpConfrimPassword.setError("Enter Email");
                    status = false;

                } else {
                    mInputSignUpConfrimPassword.setErrorEnabled(false);
                }

                if (!inputPassword.equals(inputConfirmPassword)) {
                    Toast.makeText(getApplicationContext(), "Password does not match", Toast.LENGTH_SHORT).show();
                    status = false;
                }

                if (status) {

                    mProgressDialog.show();
                    signUp(inputEmail, inputPassword);


                }


            }
        });


    }

    private void signUp(String inputEmail, String inputPassword) {

        mAuth.createUserWithEmailAndPassword(inputEmail, inputPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                mProgressDialog.dismiss();

                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(getApplicationContext(), "Registration Complete", Toast.LENGTH_SHORT).show();

                    mInputSignUpEmail.setEnabled(false);
                    mInputSignUpPassword.setEnabled(false);
                    mInputSignUpConfrimPassword.setEnabled(false);
                    mSignBtn.setEnabled(false);
                    mSignBtn.setText("Few second more...");

                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //  Toast.makeText(getApplicationContext(), "Please check your email & verify.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(new Intent(getApplicationContext(), UserInfoActivity.class));
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                        }
                    });

                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
            }
        });


    }


    @Override
    protected void onStop() {
        super.onStop();
        mProgressDialog.dismiss();
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
}
