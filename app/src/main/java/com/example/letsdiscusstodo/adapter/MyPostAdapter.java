package com.example.letsdiscusstodo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.letsdiscusstodo.R;
import com.example.letsdiscusstodo.model.Post;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyPostAdapter extends RecyclerView.Adapter<MyPostAdapter.PostHolder> {

    private ArrayList<Post> post;
    Context context;

    public MyPostAdapter(ArrayList<Post> post, Context context) {
        this.post = post;
        this.context = context;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewTyp) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_post, parent, false);
        return new PostHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {

        holder.mMyPostTitle.setText(post.get(position).title);
        holder.mMyPostBody.setText(post.get(position).body);


    }

    @Override
    public int getItemCount() {
        return post.size();
    }

    class PostHolder extends RecyclerView.ViewHolder {

        private TextView mMyPostTitle, mMyPostBody;

        public PostHolder(View itemView) {
            super(itemView);

            mMyPostTitle = itemView.findViewById(R.id.my_post_title);
            mMyPostBody = itemView.findViewById(R.id.my_post_body);


        }


    }


}
