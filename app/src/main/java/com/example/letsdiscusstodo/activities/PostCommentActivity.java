package com.example.letsdiscusstodo.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.letsdiscusstodo.R;
import com.example.letsdiscusstodo.model.Comment;
import com.example.letsdiscusstodo.model.Post;
import com.example.letsdiscusstodo.model.UserInformation;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class PostCommentActivity extends AppCompatActivity {

    private static final String TAG = "PostDetailActivity";

    private FirebaseAuth mAuth;
    private String userId;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgressDialog;

    public static final String EXTRA_POST_KEY = "post_key";

    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;

    private ValueEventListener mPostListener;
    private String mPostKey, mPostUserUid;

    private TextView mAuthorView, mTitleView, mBodyView, mLoadingTextView;

    private CircleImageView mPostUserPhoto;
    private EditText mCommentField;
    private Button mCommentButton;
    private RecyclerView mCommentsRecycler;

    private FirebaseRecyclerAdapter<Comment, CommentViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        getSupportActionBar().setTitle("PostComments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getUid();

        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        mAuthorView = findViewById(R.id.postAuthor);
        mTitleView = findViewById(R.id.postTitle);
        mBodyView = findViewById(R.id.postBody);
        mCommentField = findViewById(R.id.fieldCommentText);
        mCommentButton = findViewById(R.id.buttonPostComment);
        mCommentsRecycler = findViewById(R.id.recyclerPostComments);
        mPostUserPhoto = findViewById(R.id.postUserPhoto);
        mLoadingTextView = findViewById(R.id.comments_loading_textView);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("loading comments....");
        mProgressDialog.setCancelable(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mCommentsRecycler.setHasFixedSize(true);
        mCommentsRecycler.setLayoutManager(layoutManager);


        mPostReference = FirebaseDatabase.getInstance().getReference()
                .child("posts").child(mPostKey);

        mCommentsReference = FirebaseDatabase.getInstance().getReference()
                .child("post-comments").child(mPostKey);

        mPostReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Post post = dataSnapshot.getValue(Post.class);

            try{
                mPostUserUid = post.getUid();
            }catch (Exception e){
                Log.d(TAG, "Exception : " + e.getLocalizedMessage() );
            }


//                Log.d(TAG, "User Post ref :  " + mPostUserUid);

                mDatabase = FirebaseDatabase.getInstance().getReference().child("users/" + mPostUserUid);

                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);


                        if (userInformation.getProfileUri().equals("default")) {
                            mPostUserPhoto.setImageResource(R.drawable.user);

                        } else {
                            Glide.with(getApplicationContext()).load(userInformation.getProfileUri()).into(mPostUserPhoto);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        fetch();

        mCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                postComment();


            }
        });


    }


    @Override
    public void onStart() {
        super.onStart();

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Post post = dataSnapshot.getValue(Post.class);

                mAuthorView.setText(post.author);
                mTitleView.setText(post.title);
                mBodyView.setText(post.body);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getApplicationContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();

            }
        };
        mPostReference.addValueEventListener(postListener);

        mPostListener = postListener;
        mAdapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();

        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }

        mLoadingTextView.setVisibility(View.INVISIBLE);

        mAdapter.stopListening();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        private TextView authorView;
        private TextView bodyView, likeView, replyView, likeCount;
        private CircleImageView commentUserPhoto;
        private View v;


        private CommentViewHolder(View itemView) {
            super(itemView);

            authorView = itemView.findViewById(R.id.commentAuthor);
            bodyView = itemView.findViewById(R.id.commentBody);
            commentUserPhoto = itemView.findViewById(R.id.commentPhoto);
            likeView = itemView.findViewById(R.id.like_text_view);
            replyView = itemView.findViewById(R.id.reply_text_view);
            likeCount = itemView.findViewById(R.id.like_count);
            v =itemView.findViewById(R.id.like_include);



        }

    }

    private void fetch() {

         mLoadingTextView.setVisibility(View.VISIBLE);

        //mProgressDialog.show();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Comment>().setQuery(mCommentsReference, Comment.class).build();

        mAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(options) {

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);

                return new CommentViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final CommentViewHolder holder, final int position, @NonNull final Comment model) {

                holder.authorView.setText(model.getAuthor());
                holder.bodyView.setText(model.getText());
                mLoadingTextView.setVisibility(View.GONE);
                holder.likeCount.setText(String.valueOf(model.likeCount));





                if(model.likeCount == 0){
                    holder.v.setVisibility(View.INVISIBLE);
                }else{
                    holder.v.setVisibility(View.VISIBLE);
                }

                final DatabaseReference postRef = getRef(position);

                final String postKey = postRef.getKey();


                if (model.likes.containsKey(userId)) {
                    holder.likeView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                } else {
                    holder.likeView.setTextColor(getResources().getColor(R.color.material_gray_600));
                }
                
                holder.replyView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(PostCommentActivity.this, "Coming Soon", Toast.LENGTH_SHORT).show();
                    }
                });


                holder.likeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference()
                                .child("post-comments").child(mPostKey).child(postKey);

                        likeRef.runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                Comment p = mutableData.getValue(Comment.class);
                                if (p == null) {
                                    return Transaction.success(mutableData);
                                }

                                if (p.likes.containsKey(userId)) {
                                    p.likeCount = p.likeCount - 1;
                                    p.likes.remove(userId);
                                } else {
                                    p.likeCount = p.likeCount + 1;
                                    p.likes.put(userId, true);
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


                final DatabaseReference data = FirebaseDatabase.getInstance().getReference()
                        .child("post-comments").child(mPostKey).child(postKey);

                 Log.d(TAG,"Data : " + data);

                data.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Comment comment = dataSnapshot.getValue(Comment.class);

                      //  Log.d(TAG, "Comment : " + comment.getUid());

                        try {
                            DatabaseReference userdata = FirebaseDatabase.getInstance().getReference("users/" + comment.getUid());
                            userdata.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);

                                    // Log.d(TAG, "onChildAdded " + userInformation.getProfileUri());

                                    if (userInformation.getProfileUri().equals("default")) {

                                        holder.commentUserPhoto.setImageResource(R.drawable.user);

                                    } else {
                                        Glide.with(getApplicationContext()).load(userInformation.getProfileUri()).into(holder.commentUserPhoto);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }catch (Exception e){
                            Log.d(TAG,"Exception 1 : " + e.getLocalizedMessage());

                        }






                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        };


        mCommentsRecycler.setAdapter(mAdapter);

    }


    private void postComment() {

        FirebaseDatabase.getInstance().getReference().child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        UserInformation user = dataSnapshot.getValue(UserInformation.class);

                        String commentText = mCommentField.getText().toString();

                        if (commentText.isEmpty()) {

                            Toast.makeText(getApplicationContext(), "Comment can't be empty.", Toast.LENGTH_SHORT).show();

                        } else {


                            String key = mCommentsReference.push().getKey();
                            Comment comment = new Comment(userId, user.getUserName(), commentText);
                            Map<String, Object> commentValues = comment.toMap();

                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put(key, commentValues);

                            mCommentsReference.updateChildren(childUpdates);

//                            Comment comment = new Comment(userId, user.getUserName(), commentText, null, 0);
//                            mCommentsReference.push().setValue(comment);
                            mCommentField.setText(null);
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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
}
