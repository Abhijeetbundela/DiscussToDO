package com.example.letsdiscusstodo.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.letsdiscusstodo.R;
import com.example.letsdiscusstodo.model.Post;
import com.example.letsdiscusstodo.viewholder.MyPostViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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




    public MyPostsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_my_post, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

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

        if(isNetworkvailable(getActivity())){

            fetch();

        }else {

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
            protected void onBindViewHolder(MyPostViewHolder viewHolder, int position, final Post model) {
                final DatabaseReference postRef = getRef(position);


                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

//                        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
//                        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
//                        startActivity(intent);

                        Toast.makeText(getContext(), "Hello", Toast.LENGTH_SHORT).show();
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

    public static boolean isNetworkvailable(Context con){
        try{

            ConnectivityManager cm = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
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
}
