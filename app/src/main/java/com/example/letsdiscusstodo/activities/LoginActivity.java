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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mLoginBtn;
    private TextView mForgotPasswordTextView, mSignUpTextView;
    private TextInputLayout mInputLogInEmail, mInputLogInPassword;

    private FirebaseAuth mAuth;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("LogIn");

        mAuth = FirebaseAuth.getInstance();

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
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                break;
            }

            case R.id.forgot_password_text_view: {


                Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

            }


        }

    }

    private void logIn(String inputEmail, String inputPassword) {


        mAuth.signInWithEmailAndPassword(inputEmail, inputPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {


                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
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
