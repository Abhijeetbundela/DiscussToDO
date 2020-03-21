package com.example.letsdiscusstodo.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.letsdiscusstodo.R;
import com.example.letsdiscusstodo.model.Post;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyPostViewHolder extends RecyclerView.ViewHolder {

    public TextView mMyPostTitle, mMyPostStarsCount, mMyPostbody;
    public ImageView mMyPoststar;

    public MyPostViewHolder(@NonNull View itemView) {
        super(itemView);

        mMyPostTitle = itemView.findViewById(R.id.my_post_title);
        mMyPostbody = itemView.findViewById(R.id.my_post_body);
        mMyPoststar = itemView.findViewById(R.id.my_post_star);
        mMyPostStarsCount = itemView.findViewById(R.id.my_post_star_count);


    }

    public void bindToPost(Post post, View.OnClickListener starClickListener) {
        mMyPostTitle.setText(post.title);
        mMyPostbody.setText(post.body);
        mMyPostStarsCount.setText(String.valueOf(post.starCount));

        mMyPoststar.setOnClickListener(starClickListener);
    }
}
