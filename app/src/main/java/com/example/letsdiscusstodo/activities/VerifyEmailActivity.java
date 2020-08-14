package com.example.letsdiscusstodo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.letsdiscusstodo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import androidx.appcompat.app.AppCompatActivity;

public class VerifyEmailActivity extends AppCompatActivity {

    private FirebaseUser user;
    private DatabaseReference userDatabaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        mAuth = FirebaseAuth.getInstance();

        getSupportActionBar().setTitle("VerifyEmail");

        final Button verifyEmailBtn = findViewById(R.id.verify_btn);

        verifyEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                    verifyEmailBtn.setText("Please verify Email to setup account, check your email & verify  ");

                    Toast.makeText(getApplicationContext(), "Email is not verified. Please verify first", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                }


            }
        });

    }
}
