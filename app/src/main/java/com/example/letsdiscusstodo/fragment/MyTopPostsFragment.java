package com.example.letsdiscusstodo.fragment;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.letsdiscusstodo.R;
import com.example.letsdiscusstodo.model.Post;
import com.example.letsdiscusstodo.viewholder.MyTopPostViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyTopPostsFragment extends Fragment {

    private static final String TAG = "PostListFragment";
    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<Post, MyTopPostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private ProgressDialog mProgressDialog;

    public MyTopPostsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_my_top_post, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecycler = rootView.findViewById(R.id.my_top_post_recycler_view);
        mRecycler.setHasFixedSize(true);
//        mProgressDialog = new ProgressDialog(getActivity());
//        mProgressDialog.setMessage("fetching...");
//        mProgressDialog.setCancelable(false);

        return rootView;

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mManager);

        String myUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query postsQuery = mDatabase.child("user-posts").child(myUserId).orderByChild("starCount");

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(postsQuery, Post.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Post, MyTopPostViewHolder>(options) {

            @Override
            public MyTopPostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

             //   mProgressDialog.dismiss();
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new MyTopPostViewHolder(inflater.inflate(R.layout.item_my_top_post, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(MyTopPostViewHolder viewHolder, int position, final Post model) {
                final DatabaseReference postRef = getRef(position);
               // mProgressDialog.dismiss();

                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(getContext(), "Hello", Toast.LENGTH_SHORT).show();

                    }
                });



                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View mMyTopPostStarsCount) {

                        Toast.makeText(getContext(), "Star Count", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        };
        mRecycler.setAdapter(mAdapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
           // mProgressDialog.show();
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
          //  mProgressDialog.dismiss();
            mAdapter.stopListening();
        }
    }

}




