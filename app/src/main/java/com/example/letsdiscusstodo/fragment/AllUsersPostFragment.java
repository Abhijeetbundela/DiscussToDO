package com.example.letsdiscusstodo.fragment;


import android.content.Context;
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

import com.bumptech.glide.Glide;
import com.example.letsdiscusstodo.R;
import com.example.letsdiscusstodo.activities.EntryChooseActivity;
import com.example.letsdiscusstodo.activities.PostCommentActivity;
import com.example.letsdiscusstodo.activities.UserInfoActivity;
import com.example.letsdiscusstodo.model.Post;
import com.example.letsdiscusstodo.model.UserInformation;
import com.example.letsdiscusstodo.viewholder.AllUserPostViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class AllUsersPostFragment extends Fragment {

    private static final String TAG = "RecentPostsFragment";

    private DatabaseReference mDatabase;

    private FirebaseAuth mAuth;

    private FirebaseFirestore mFirestore;


    private FirebaseRecyclerAdapter<Post, AllUserPostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public AllUsersPostFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_user_posts, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        setHasOptionsMenu(true);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        mRecycler = rootView.findViewById(R.id.all_user_post_recycler_view);
        mRecycler.setHasFixedSize(true);

        if (isNetworkvailable(getActivity())) {

            fetch();

        } else {

        }

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    private void fetch() {

        mManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mManager);
        mRecycler.setHasFixedSize(true);

        Query postsQuery = mDatabase.child("posts").limitToFirst(100);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(postsQuery, Post.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Post, AllUserPostViewHolder>(options) {

            @NonNull
            @Override
            public AllUserPostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new AllUserPostViewHolder(inflater.inflate(R.layout.item_all_user_post, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final AllUserPostViewHolder viewHolder, int position, @NonNull final Post model) {

                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.authorpic.setVisibility(View.VISIBLE);

                final DatabaseReference postRef = getRef(position);

                final String postKey = postRef.getKey();

                viewHolder.postCommentLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(getActivity(), PostCommentActivity.class);
                        intent.putExtra("post_key", postKey);
                        startActivity(intent);

                    }
                });

                if (model.stars.containsKey(getUid())) {
                    viewHolder.starView.setImageResource(R.drawable.star);
                } else {
                    viewHolder.starView.setImageResource(R.drawable.star_border);
                }


                if (model.postlike.containsKey(getUid())) {

                    viewHolder.likeImage.setImageResource(R.drawable.thumb_up_blue);
                } else {
                    viewHolder.likeImage.setImageResource(R.drawable.thumb_up_like);
                }

                viewHolder.likeImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //     DatabaseReference likeref = mDatabase.child("user-posts").child(getUid()).child(postRef.getKey());
                        DatabaseReference globalPostRef = mDatabase.child("posts").child(Objects.requireNonNull(postRef.getKey()));

//                        likeref.runTransaction(new Transaction.Handler() {
//                            @NonNull
//                            @Override
//                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
//                                Post p = mutableData.getValue(Post.class);
//                                if (p == null) {
//                                    return Transaction.success(mutableData);
//                                }
//
//                                if (p.postlike.containsKey(getUid())) {
//                                    p.postlikeCount = p.postlikeCount - 1;
//                                    p.postlike.remove(getUid());
//                                } else {
//                                    p.postlikeCount = p.postlikeCount + 1;
//                                    p.postlike.put(getUid(), true);
//                                }
//
//                                mutableData.setValue(p);
//                                return Transaction.success(mutableData);
//                            }
//
//                            @Override
//                            public void onComplete(DatabaseError databaseError, boolean b,
//                                                   DataSnapshot dataSnapshot) {
//                                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
//                            }
//                        });
                        globalPostRef.runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                Post p = mutableData.getValue(Post.class);
                                if (p == null) {
                                    return Transaction.success(mutableData);
                                }

                                if (p.postlike.containsKey(getUid())) {
                                    p.postlikeCount = p.postlikeCount - 1;
                                    p.postlike.remove(getUid());
                                } else {
                                    p.postlikeCount = p.postlikeCount + 1;
                                    p.postlike.put(getUid(), true);
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
                });


//
//                mDatabase.child("posts").child(postKey).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                        final String timeStamp = dataSnapshot.child("timeStamp").getValue().toString();
//
//
//                      //  viewHolder.allUserPostdateView.setText(getTimeAgo(Long.parseLong(timeStamp)));
//
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });

                mDatabase.child("users/" + model.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);


                        viewHolder.userView.setText(userInformation.getUserName());


                        Log.d(TAG, "onDataChange: " + userInformation.getProfileUri());

                        if (userInformation.getProfileUri().equals("default")) {
                            Glide.with(requireContext()).load(R.drawable.user).into(viewHolder.authorpic);
                        } else {
                            Glide.with(requireContext()).load(userInformation.getProfileUri()).into(viewHolder.authorpic);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {

                        DatabaseReference globalPostRef = mDatabase.child("posts").child(Objects.requireNonNull(postRef.getKey()));
                        DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());

                        onStarClicked(globalPostRef);
                        onStarClicked(userPostRef);
                    }
                });
            }
        };
        mRecycler.setAdapter(mAdapter);

    }

    private void onStarClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
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
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    private String getUid() {
        return Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    }

    private static boolean isNetworkvailable(Context con) {
        try {

            ConnectivityManager cm = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert cm != null;
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

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

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private String getTimeAgo(long time)
    {
        final long diff = System.currentTimeMillis() - time;

        if(diff < 1)
        {
            return " just now";
        }
        if(diff < 60 * 1000)
        {
            if(diff / 1000 < 2)
            {
                return diff / 1000 + " second ago";
            }
            else
            {
                return diff / 1000 + " seconds ago";
            }
        }
        else if(diff < 60 * (60 * 1000))
        {
            if(diff / (60 * 1000) < 2)
            {
                return diff / (60 * 1000) + " minute ago";
            }
            else
            {
                return diff / (60 * 1000) + " minutes ago";
            }
        }
        else if(diff < 24 * (60 * (60 * 1000)))
        {
            if(diff / (60 * (60 * 1000)) < 2)
            {
                return diff / (60 * (60 * 1000)) + " hour ago";
            }
            else
            {
                return diff / (60 * (60 * 1000)) + " hours ago";
            }
        }
        else
        {
            if(diff / (24 * (60 * (60 * 1000))) < 2)
            {
                return diff / (24 * (60 * (60 * 1000))) + " day ago";
            }
            else
            {
                return diff / (24 * (60 * (60 * 1000))) + " days ago";
            }
        }
    }
}
