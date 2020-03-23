package com.example.letsdiscusstodo.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.letsdiscusstodo.R;
import com.example.letsdiscusstodo.model.Comment;
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
import com.google.firebase.database.ValueEventListener;

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
    private String mPostKey, mPostUserUid, mCommentUserUid;

    private TextView mAuthorView, mTitleView, mBodyView;

    private CircleImageView mPostUserPhoto;
    private EditText mCommentField;
    private Button mCommentButton;
    private RecyclerView mCommentsRecycler;

    private FirebaseRecyclerAdapter<Comment, CommentViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        getSupportActionBar().setTitle("PostDetails");

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

                mPostUserUid = post.getUid();

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

        mAdapter.stopListening();
    }

    public  static  class CommentViewHolder extends RecyclerView.ViewHolder {

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

    private void fetch() {

        //mProgressDialog.show();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Comment>().setQuery(mCommentsReference,Comment.class).build();

        mAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(options){

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment,parent,false);

                return new CommentViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Comment model) {

                holder.authorView.setText(model.getAuthor());
                holder.bodyView.setText(model.getText());



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
                        String authorName = user.getUserName();

                        String commentText = mCommentField.getText().toString();

                        if (commentText.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Comment can't be empty.", Toast.LENGTH_SHORT).show();

                        } else {
                            Comment comment = new Comment(userId, authorName, commentText);
                            mCommentsReference.push().setValue(comment);
                            mCommentField.setText(null);
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
