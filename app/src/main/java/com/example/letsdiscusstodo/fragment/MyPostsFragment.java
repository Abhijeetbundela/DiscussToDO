package com.example.letsdiscusstodo.fragment;



import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.letsdiscusstodo.R;
import com.example.letsdiscusstodo.activities.EntryChooseActivity;
import com.example.letsdiscusstodo.activities.UserInfoActivity;
import com.example.letsdiscusstodo.model.Post;
import com.example.letsdiscusstodo.model.UserInformation;
import com.example.letsdiscusstodo.viewholder.MyPostViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyPostsFragment extends Fragment {

    private static final String TAG = "PostListFragment";
    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<Post, MyPostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private ProgressDialog mProgressDialog;
    private String postkey, title, body, user;

    private FirebaseAuth mAuth;


    public MyPostsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_my_post, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        setHasOptionsMenu(true);

        mAuth = FirebaseAuth.getInstance();

        mRecycler = rootView.findViewById(R.id.my_post_recycler_view);
        mRecycler.setHasFixedSize(true);
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("fetching...");
        mProgressDialog.setCancelable(false);

        return rootView;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (isNetworkvailable(getActivity())) {

            fetch();

        } else {

            Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();

        }

    }

    private void fetch() {

        mManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mManager);

        Query postsQuery = mDatabase.child("user-posts").child(getUid());

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(postsQuery, Post.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Post, MyPostViewHolder>(options) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                mProgressDialog.dismiss();
            }

            @Override
            public void onError(@NonNull DatabaseError error) {
                super.onError(error);
                mProgressDialog.dismiss();
            }

            @Override
            public MyPostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new MyPostViewHolder(inflater.inflate(R.layout.item_my_post, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(MyPostViewHolder viewHolder, final int position, final Post model) {
                final DatabaseReference postRef = getRef(position);

                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        title = model.getTitle();
                        body = model.getBody();
                        postkey = getRef(position).getKey();

                        updateMyPost();

                    }
                });

                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        postkey = getRef(position).getKey();

                        deleteMyPost();
                        return true;
                    }
                });

                if (model.stars.containsKey(getUid())) {
                    viewHolder.mMyPoststar.setImageResource(R.drawable.star);
                } else {
                    viewHolder.mMyPoststar.setImageResource(R.drawable.star_border);
                }

                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {

                        DatabaseReference globalPostRef = mDatabase.child("posts").child(postRef.getKey());
                        DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());

                        onStarClicked(globalPostRef);
                        onStarClicked(userPostRef);
                    }
                });
            }
        };
        mRecycler.setAdapter(mAdapter);


    }

    private void deleteMyPost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setCancelable(false)
                .setTitle("Delete my Post").setMessage("Are you sure to Delete this Post?").
                setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mDatabase.child("posts/" + postkey).removeValue();
                        mDatabase.child("user-posts/" + getUid() + "/" + postkey).removeValue();
                        mDatabase.child("post-comments/" + postkey).removeValue();


                        Toast.makeText(getContext(), "Post Deleted", Toast.LENGTH_SHORT).show();


                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void updateMyPost() {

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View myView = layoutInflater.inflate(R.layout.new_post_input_layout, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setView(myView);

        final TextInputLayout mTitle = myView.findViewById(R.id.title);
        final TextInputLayout mBody = myView.findViewById(R.id.body);

        mTitle.getEditText().setText(title);
        mTitle.getEditText().setSelection(title.length());

        mBody.getEditText().setText(body);
        mBody.getEditText().setSelection(body.length());

        builder.setCancelable(false)
                .setTitle("My Post").setPositiveButton("Update", new DialogInterface.OnClickListener() {
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
                    Toast.makeText(getContext(), "Add title", Toast.LENGTH_SHORT).show();
                } else {
                    mTitle.setErrorEnabled(false);
                }

                if (note.isEmpty()) {
                    mBody.setError("Add Body");
                    status = false;
                    Toast.makeText(getContext(), "Add Body", Toast.LENGTH_SHORT).show();
                } else {
                    mBody.setErrorEnabled(false);
                }

                if (status) {

                    mProgressDialog.show();

                    mDatabase.child("users").child(getUid()).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    UserInformation user = dataSnapshot.getValue(UserInformation.class);

                                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM \n\t\thh:mm a");
                                    Calendar calendar = Calendar.getInstance();
                                    String currentDate = sdf.format(calendar.getTime());
                                    SimpleDateFormat sdf_ = new SimpleDateFormat("EEE");
                                    Date date = new Date();
                                    String dayName = sdf_.format(date);
                                    String mDate = dayName + ", " + currentDate;

                                    if (user != null) {
                                        updatePost(getUid(), user.getUserName(), title, note, mDate, postkey);
                                        Toast.makeText(getContext(), "Post Updated", Toast.LENGTH_SHORT).show();
                                    } else {

                                        mProgressDialog.dismiss();

                                        Toast.makeText(getContext(),
                                                "Complete the user profile details then you can post.",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                    alertDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();

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

    private void updatePost(String userId, String username, String title, String body, String mDate, String postkey) {


        Post post = new Post(userId, username, title, body, mDate);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + postkey, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + postkey, postValues);

        mDatabase.updateChildren(childUpdates);

        mProgressDialog.dismiss();
    }

    public static boolean isNetworkvailable(Context con) {
        try {

            ConnectivityManager cm = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public String getUid() {
        return mAuth.getCurrentUser().getUid();
    }

    private void onStarClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.stars.containsKey(getUid())) {
                    p.starCount = p.starCount - 1;
                    p.stars.remove(getUid());
                } else {
                    p.starCount = p.starCount + 1;
                    p.stars.put(getUid(), true);
                }

                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mProgressDialog.show();
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mProgressDialog.dismiss();
            mAdapter.stopListening();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.my_post_menu,menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out: {

                startActivity(new Intent(getContext(), EntryChooseActivity.class));
                mAuth.signOut();
                return true;
            }

            case R.id.user_info: {

                startActivity(new Intent(getContext(), UserInfoActivity.class));
                return true;
            }

            case R.id.app_info: {

              appInfo();
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void appInfo() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Post Info").setMessage("\n1. Tap post to Update Post. \n\n\n2. Hold post to Delete Post\n").setCancelable(false).
                setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}
