package com.example.letsdiscusstodo.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.letsdiscusstodo.R;
import com.example.letsdiscusstodo.model.Post;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;


public class AllUserPostViewHolder extends RecyclerView.ViewHolder {


    public TextView titleView, allUserPostdateView;
    public TextView userView, likecount;
    public ImageView starView, likeImage;
    public CircleImageView authorpic;
    public TextView numStarsView;
    public TextView bodyView;
    public ProgressBar progressBar;
    public ConstraintLayout postCommentLayout, likeLayout;


    public AllUserPostViewHolder(View itemView) {
        super(itemView);

        titleView = itemView.findViewById(R.id.postTitle);
        userView = itemView.findViewById(R.id.postAuthor);
        starView = itemView.findViewById(R.id.star);
        numStarsView = itemView.findViewById(R.id.postNumStars);
        bodyView = itemView.findViewById(R.id.postBody);
        authorpic = itemView.findViewById(R.id.postAuthorPhoto);
        progressBar = itemView.findViewById(R.id.image_progressBar);
        allUserPostdateView = itemView.findViewById(R.id.all_user_post_date);
        postCommentLayout = itemView.findViewById(R.id.all_user_post_comment);
        likeImage = itemView.findViewById(R.id.all_user_post_like_image);
        likecount = itemView.findViewById(R.id.all_user_post_like_count);

        likeLayout = itemView.findViewById(R.id.like_layout);


    }

    public void bindToPost(Post post, View.OnClickListener starClickListener) {


        titleView.setText(post.getTitle());

        numStarsView.setText(String.valueOf(post.getStarCount()));
        likecount.setText(String.valueOf(post.getPostlikeCount()));

        bodyView.setText(post.getBody());
        allUserPostdateView.setText(post.getDate());

        starView.setOnClickListener(starClickListener);


    }
}