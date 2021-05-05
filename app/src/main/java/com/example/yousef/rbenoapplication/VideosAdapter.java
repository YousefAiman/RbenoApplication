package com.example.yousef.rbenoapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {
  private final ArrayList<Promotion> promos;
  private final VideoViewClickListener videoViewClickListener;
  private final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

  VideosAdapter(ArrayList<Promotion> promos, VideosAdapter.VideoViewClickListener itemListener) {
    videoViewClickListener = itemListener;
    this.promos = promos;
  }

  @NonNull
  @Override
  public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new VideoViewHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.video_item_horizontal_layout, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull VideoViewHolder holder, final int position) {
    holder.bind(promos.get(position));
  }

  @Override
  public long getItemId(int position) {
    return promos.get(position).hashCode();
  }

  @Override
  public int getItemCount() {
    return promos.size();
  }

  public interface VideoViewClickListener {
    void videoViewClickListener(int position);
  }

  class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final ImageView videoItemIv;
    private final TextView videoItemTv;


    public VideoViewHolder(@NonNull View itemView) {
      super(itemView);
      videoItemIv = itemView.findViewById(R.id.videoItemIv);
      videoItemTv = itemView.findViewById(R.id.videoItemTv);
    }

    void bind(Promotion promotion) {

      Picasso.get().load(promotion.getVideoThumbnail())
              .fit().centerCrop().into(videoItemIv);

      itemView.setOnClickListener(this);

      videoItemTv.setText(promotion.getTitle());
    }

    @Override
    public void onClick(View view) {

      if (Promotion.printPromoStatus(
              itemView.getContext(), promos.get(getAdapterPosition()), currentUid)) {

        videoItemIv.setImageResource(R.drawable.ic_delete_grey);
        videoItemTv.setVisibility(View.GONE);

        return;
      }

      videoViewClickListener.videoViewClickListener(getAdapterPosition());
    }
  }
}
