package com.example.letsdiscusstodo.viewholder;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.letsdiscusstodo.R;
import com.example.letsdiscusstodo.model.Post;
import com.example.letsdiscusstodo.model.UserInformation;
import com.squareup.picasso.Picasso;

import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;


public class AllUserPostViewHolder extends RecyclerView.ViewHolder {

    public TextView titleView;
    public TextView userView;
    public ImageView starView;
    public CircleImageView authorpic;
    public TextView numStarsView;
    public TextView bodyView;





    public AllUserPostViewHolder(View itemView) {
        super(itemView);

        titleView = itemView.findViewById(R.id.postTitle);
        userView = itemView.findViewById(R.id.postAuthor);
        starView = itemView.findViewById(R.id.star);
        numStarsView = itemView.findViewById(R.id.postNumStars);
        bodyView = itemView.findViewById(R.id.postBody);
        authorpic = itemView.findViewById(R.id.postAuthorPhoto);

    }

    public void bindToPost(Post post, View.OnClickListener starClickListener) {
        titleView.setText(post.title);
        userView.setText(post.author);
        numStarsView.setText(String.valueOf(post.starCount));
        bodyView.setText(post.body);
        //authorpic.setImageResource(Integer.parseInt(post.profile));
        starView.setOnClickListener(starClickListener);


    }
}