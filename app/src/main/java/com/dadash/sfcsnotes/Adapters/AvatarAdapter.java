package com.dadash.sfcsnotes.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.dadash.sfcsnotes.R;

import java.util.List;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder> {

    private final Context context;
    private final List<String> avatarList;
    private String selectedAvatar = null;
    private final OnAvatarSelectedListener onAvatarSelectedListener;

    public interface OnAvatarSelectedListener {
        void onAvatarSelected(String avatarName);
    }

    public AvatarAdapter(Context context, List<String> avatarList, OnAvatarSelectedListener onAvatarSelectedListener) {
        this.context = context;
        this.avatarList = avatarList;
        this.onAvatarSelectedListener = onAvatarSelectedListener;
    }

    @NonNull
    @Override
    public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_avatar, parent, false);
        return new AvatarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvatarViewHolder holder, int position) {
        String avatarName = avatarList.get(position);

        // Load avatar from res/drawable
        int avatarResId = context.getResources().getIdentifier(avatarName, "drawable", context.getPackageName());
        if (avatarResId != 0) {
            holder.avatarImage.setImageResource(avatarResId);
        }

        // Set elevation and corner radius
        holder.avatarCard.setCardElevation(8f);
        holder.avatarCard.setRadius(16f);

        // Handle selection
        if (avatarName.equals(selectedAvatar)) {
            holder.avatarCard.setCardBackgroundColor(Color.RED);
        } else {
            holder.avatarCard.setCardBackgroundColor(Color.WHITE);
        }

        // Handle click
        holder.itemView.setOnClickListener(v -> {
            selectedAvatar = avatarName;
            notifyDataSetChanged();
            onAvatarSelectedListener.onAvatarSelected(avatarName);
        });
    }

    @Override
    public int getItemCount() {
        return avatarList.size();
    }

    public static class AvatarViewHolder extends RecyclerView.ViewHolder {
        public final ImageView avatarImage;
        public final CardView avatarCard;

        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.ivAvatar);
            avatarCard = itemView.findViewById(R.id.cardAvatar);
        }
    }
}
