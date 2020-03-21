package com.example.letsdiscusstodo.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.letsdiscusstodo.R;
import com.example.letsdiscusstodo.model.Comment;
import com.example.letsdiscusstodo.model.Post;
import com.example.letsdiscusstodo.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PostDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private String userId;

    public static final String EXTRA_POST_KEY = "post_key";

    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;
    private ValueEventListener mPostListener;
    private String mPostKey;
    private CommentAdapter mAdapter;

    private TextView mAuthorView;
    private TextView mTitleView;
    private TextView mBodyView;
    private EditText mCommentField;
    private Button mCommentButton;
    private RecyclerView mCommentsRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        getSupportActionBar().setTitle("PostDetails");

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getUid();


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

        mCommentButton.setOnClickListener(this);
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));

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

                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.username;


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

        public TextView authorView;
        public TextView bodyView;

        public CommentViewHolder(View itemView) {
            super(itemView);

            authorView = itemView.findViewById(R.id.commentAuthor);
            bodyView = itemView.findViewById(R.id.commentBody);
        }
    }

    private static class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

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
        public void onBindViewHolder(CommentViewHolder holder, int position) {
            Comment comment = mComments.get(position);
            holder.authorView.setText(comment.author);
            holder.bodyView.setText(comment.text);
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
