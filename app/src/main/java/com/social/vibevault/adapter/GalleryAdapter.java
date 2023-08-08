package com.social.vibevault.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;


import com.bumptech.glide.Glide;
import com.social.vibevault.R;
import com.social.vibevault.model.GalleryImages;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryHolder> {

    SendImage onSendImage;
    List<GalleryImages> list;

    public GalleryAdapter(List<GalleryImages> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public GalleryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_items, parent, false);
        return new GalleryHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull GalleryHolder holder, int position) {

        Glide.with(holder.itemView.getContext().getApplicationContext())
                .load(list.get(position).getPicUri())
                .into(holder.imageView);

        holder.imageView.setOnClickListener(v -> chooseImage(list.get(holder.getAdapterPosition()).getPicUri()));

    }

    private void chooseImage(Uri picUri) {

        onSendImage.onSend(picUri);

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public void SendImage(SendImage sendImage) {
        this.onSendImage = sendImage;
    }

    public interface SendImage {     //establish communication between the GalleryAdapter and the class that instantiates it
        void onSend(Uri picUri);    //purpose - callers gets Uri of the selected image without knowing the implementation details of the adapter
    }

    static class GalleryHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public GalleryHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);

        }
    }

}
