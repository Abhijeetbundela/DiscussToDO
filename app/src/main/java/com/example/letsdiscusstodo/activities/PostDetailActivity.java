package com.example.letsdiscusstodo.activities;

import android.content.Context;
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
import com.example.letsdiscusstodo.model.User;
import com.example.letsdiscusstodo.model.UserInformation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PostDetailActivity";

    private FirebaseAuth mAuth;
    private String userId;
    private DatabaseReference mDatabase;

    private UserInformation userInformation;

    public static final String EXTRA_POST_KEY = "post_key";

    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;

    private ValueEventListener mPostListener;
    private String mPostKey, mPostUserUid;
    private CommentAdapter mAdapter;

    private TextView mAuthorView;
    private TextView mTitleView;
    private TextView mBodyView;
    private CircleImageView mPostUserPhoto;
    private EditText mCommentField;
    private Button mCommentButton;
    private RecyclerView mCommentsRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post_detail);

        getSupportActionBar().setTitle("PostDetails");


        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }


        mPostReference = FirebaseDatabase.getInstance().getReference()
                .child("posts").child(mPostKey);
        mCommentsReference = FirebaseDatabase.getInstance().getReference()
                .child("post-comments").child(mPostKey);


        mAuthorView = findViewById(R.id.postAuthor);
        mTitleView = findViewById(R.id.postTitle);
        mBodyView = findViewById(R.id.postBody);
        mCommentField = findViewById(R.id.fieldCommentText);
        mCommentButton = findViewById(R.id.buttonPostComment);
        mCommentsRecycler = findViewById(R.id.recyclerPostComments);
        mPostUserPhoto = findViewById(R.id.postUserPhoto);

        mCommentButton.setOnClickListener(this);
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));

        //DatabaseReference postUserUid =  mDatabase.child("posts/" + mPostKey);

//        mDatabase.child("posts/" + mPostKey).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                Post post = dataSnapshot.getValue(Post.class);
//
//                mPostUserUid = post.getUid();
//
//                Log.d(TAG, "Abhi Post ref:  " + mPostUserUid);
//
//
//
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });




//        mDatabase.child("users/" + mPostUserUid).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                userInformation = dataSnapshot.getValue(UserInformation.class);
//
//                Log.d(TAG, "User Post ref:  " + mPostUserUid);
//               // Log.d(TAG, "getProfileUri()  " + userInformation.getUserName());
//
//
////                if (userInformation.getProfileUri().equals("default") ) {
////                    mPostUserPhoto.setImageResource(R.drawable.user);
////
////                } else {
////                    Glide.with(getApplicationContext()).load(userInformation.getProfileUri()).into(mPostUserPhoto);
////                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });


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

                Toast.makeText(PostDetailActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();

            }
        };
        mPostReference.addValueEventListener(postListener);

        mPostListener = postListener;

        mAdapter = new CommentAdapter(this, mCommentsReference);
        mCommentsRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }

        mAdapter.cleanupListener();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.buttonPostComment) {
            postComment();
        }
    }

    private void postComment() {

        FirebaseDatabase.getInstance().getReference().child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        UserInformation user = dataSnapshot.getValue(UserInformation.class);
                        String authorName = user.getUserName();


                        String commentText = mCommentField.getText().toString();
                        Comment comment = new Comment(userId, authorName, commentText);


                        mCommentsReference.push().setValue(comment);


                        mCommentField.setText(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private static class CommentViewHolder extends RecyclerView.ViewHolder {

        private TextView authorView;
        private TextView bodyView;
        private CircleImageView commentUserPhoto;


        private CommentViewHolder(View itemView) {
            super(itemView);

            authorView = itemView.findViewById(R.id.commentAuthor);
            bodyView = itemView.findViewById(R.id.commentBody);
            commentUserPhoto = itemView.findViewById(R.id.commentPhoto);
        }

    }

    private static class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;
        private FirebaseAuth mAuth;
        private String userId;
        private DatabaseReference mDatabase;

        private List<String> mCommentIds = new ArrayList<>();
        private List<Comment> mComments = new ArrayList<>();

        public CommentAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;


            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {


                    Comment comment = dataSnapshot.getValue(Comment.class);

                    mCommentIds.add(dataSnapshot.getKey());
                    mComments.add(comment);
                    notifyItemInserted(mComments.size() - 1);

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {


                    Comment newComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();


                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {

                        mComments.set(commentIndex, newComment);


                        notifyItemChanged(commentIndex);
                    } else {

                    }

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {


                    String commentKey = dataSnapshot.getKey();


                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {

                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);


                        notifyItemRemoved(commentIndex);
                    } else {

                    }

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

                    Comment movedComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    Toast.makeText(mContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);

            mChildEventListener = childEventListener;
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final CommentViewHolder holder, int position) {

            mAuth = FirebaseAuth.getInstance();
            userId = mAuth.getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference();

            Comment comment = mComments.get(position);
            holder.authorView.setText(comment.author);
            holder.bodyView.setText(comment.text);

//            mDatabase.child("users/" + userId).addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                    UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);
//
//                    if (userInformation.getProfileUri().equals("default")) {
//                        Glide.with(mContext).load(R.drawable.user).into(holder.commentUserPhoto);
//                    } else {
//                        Glide.with(mContext).load(userInformation.getProfileUri()).into(holder.commentUserPhoto);
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });


        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

    }
}
