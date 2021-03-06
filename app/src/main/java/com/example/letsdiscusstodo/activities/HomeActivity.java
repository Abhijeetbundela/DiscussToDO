package com.example.letsdiscusstodo.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.letsdiscusstodo.R;
import com.example.letsdiscusstodo.fragment.AllUsersPostFragment;
import com.example.letsdiscusstodo.fragment.MyPostsFragment;
import com.example.letsdiscusstodo.fragment.MyTopPostsFragment;
import com.example.letsdiscusstodo.model.Post;
import com.example.letsdiscusstodo.model.UserInformation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class HomeActivity extends AppCompatActivity {

    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    private FloatingActionButton mFabBtn;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private String userId;

    private ProgressDialog mProgressDialog;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        mToolbar = findViewById(R.id.home_toolbar);
//        setSupportActionBar(mToolbar);

        mFabBtn = findViewById(R.id.fabNewPost);

        mAuth = FirebaseAuth.getInstance();

        userId = mAuth.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Posting....");
        mProgressDialog.setCancelable(false);

        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            private final Fragment[] mFragments = new Fragment[]{
                    new AllUsersPostFragment(),
                    new MyPostsFragment(),
                    new MyTopPostsFragment(),
            };
            private final String[] mFragmentNames = new String[]{
                    "All User Posts",
                    "My Posts",
                    "My Top Posts"

            };

            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public int getCount() {
                return mFragments.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };


        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case 0: {
                        mFabBtn.hide();
                        break;
                    }

                    case 1: {
                        mFabBtn.show();
                        break;
                    }

                    case 2: {
                        mFabBtn.hide();

                    }


                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mFabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  startActivity(new Intent(getApplicationContext(), NewPostActivity.class));


                addPost();


            }
        });


    }

    private void addPost() {

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View myview = layoutInflater.inflate(R.layout.new_post_input_layout, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(myview);

        final TextInputLayout mTitle = myview.findViewById(R.id.title);
        final TextInputLayout mBody = myview.findViewById(R.id.body);

        builder.setCancelable(false).setTitle("Add Post");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = mTitle.getEditText().getText().toString().trim();
                final String note = mBody.getEditText().getText().toString().trim();

                boolean status = true;

                if (title.isEmpty()) {
                    mTitle.setError("Add Title");
                    status = false;
                    Toast.makeText(getApplicationContext(), "Add title", Toast.LENGTH_SHORT).show();
                } else {
                    mTitle.setErrorEnabled(false);
                }

                if (note.isEmpty()) {
                    mBody.setError("Add Body");
                    status = false;
                    Toast.makeText(getApplicationContext(), "Add Body", Toast.LENGTH_SHORT).show();
                } else {
                    mBody.setErrorEnabled(false);
                }

                if (status) {

                    mProgressDialog.show();

                    mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    UserInformation user = dataSnapshot.getValue(UserInformation.class);


                                    SimpleDateFormat sdf1 = new SimpleDateFormat(" dd MMM ");
                                    SimpleDateFormat sdf2 = new SimpleDateFormat(" hh:mm a");
                                    Calendar calendar2 = Calendar.getInstance();
                                    String currentDate2 = sdf2.format(calendar2.getTime());
                                    Calendar calendar1 = Calendar.getInstance();
                                    String currentDate1 = sdf1.format(calendar1.getTime());
                                    SimpleDateFormat sdf_1 = new SimpleDateFormat("EEE");
                                    Date date1 = new Date();
                                    String dayName1 = sdf_1.format(date1);
                                    String mDate1 = dayName1 + "," + currentDate1 + "at" + currentDate2;

                                    Log.d("MYYT", mDate1);
//                                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM\n\t\thh:mm a\t");
//                                    Calendar calendar = Calendar.getInstance();
//                                    String currentDate = sdf.format(calendar.getTime());
//                                    SimpleDateFormat sdf_ = new SimpleDateFormat("EEE");
//                                    Date date = new Date();
//                                    String dayName = sdf_.format(date);
//                                    String mDate = dayName + ", " + currentDate;


//                                    UserLastSeenTime lastSeenTime = new UserLastSeenTime();
//                                    long last_seen = System.currentTimeMillis();
//
//
//                                    String lastSeenOnScreenTime = lastSeenTime.getTimeAgo(last_seen, getApplicationContext());
//                                    Log.e("lastSeenTime", lastSeenOnScreenTime);

                                    if (user != null) {


                                        writeNewPost(userId, user.getUserName(), title, note, mDate1);


                                    } else {

                                        mProgressDialog.dismiss();

                                        Toast.makeText(getApplicationContext(),
                                                "Complete the user profile details then you can post.",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                    alertDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });


                }

            }
        });


        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


    }


    private void writeNewPost(String userId, String username, String title, String body, String mDate) {

        String key = mDatabase.child("posts").push().getKey();
        Post post = new Post(userId, username, title, body, mDate);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);


        mDatabase.updateChildren(childUpdates);
        //mDatabase.child("posts").child(key).child("timeStamp").setValue(ServerValue.TIMESTAMP);

        mProgressDialog.dismiss();
    }


}
