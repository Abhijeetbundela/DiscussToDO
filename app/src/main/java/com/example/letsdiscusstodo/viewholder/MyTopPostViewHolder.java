package com.example.letsdiscusstodo.viewholder;

import android.view.View;
import android.widget.TextView;

import com.example.letsdiscusstodo.R;
import com.example.letsdiscusstodo.model.Post;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyTopPostViewHolder extends RecyclerView.ViewHolder {

    public TextView mMyTopPostTitle, mMyTopPostStarsCount, mMyTopPostbody;

    public MyTopPostViewHolder(@NonNull View itemView) {
        super(itemView);

        mMyTopPostTitle = itemView.findViewById(R.id.my_top_post_title);
        mMyTopPostbody = itemView.findViewById(R.id.my_top_post_body);
        mMyTopPostStarsCount = itemView.findViewById(R.id.my_top_post_star_count);


    }

    public void bindToPost(Post post, View.OnClickListener starCountClickListener) {
        mMyTopPostTitle.setText(post.title);
        mMyTopPostbody.setText(post.body);
        mMyTopPostStarsCount.setText(String.valueOf(post.starCount));

        mMyTopPostStarsCount.setOnClickListener(starCountClickListener);
    }


}
