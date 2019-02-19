package io.github.mehdiataei.photorush.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import io.github.mehdiataei.photorush.Models.Photo;
import io.github.mehdiataei.photorush.R;

import static android.support.constraint.Constraints.TAG;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List<Photo> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public MyRecyclerViewAdapter(Context context, List<Photo> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //holder.myImageView.setImageBitmap(mData.get(position));
        Log.d(TAG, "onBindViewHolder: Called.");

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.drawable.ic_error)
                .priority(Priority.HIGH);

        new GlideImageLoader(holder.myImageView,
                holder.myProgressBar).load(mData.get(position).getImage_path(), options);

    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView myImageView;
        ProgressBar myProgressBar;

        ViewHolder(View itemView) {
            super(itemView);
            myImageView = itemView.findViewById(R.id.images);
            myProgressBar = itemView.findViewById(R.id.gridImageProgressbar);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public Photo getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }


}