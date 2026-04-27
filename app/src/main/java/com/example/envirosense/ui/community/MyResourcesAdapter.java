package com.example.envirosense.ui.community;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.envirosense.R;
import com.example.envirosense.data.models.MyResource;

import java.util.List;

/**
 * RecyclerView adapter for displaying personal resources stored locally.
 * Reuses the item_group_resource layout for visual consistency.
 */
public class MyResourcesAdapter extends RecyclerView.Adapter<MyResourcesAdapter.ResourceViewHolder> {

    public interface OnResourceClickListener {
        void onResourceClick(MyResource resource, int position);
    }

    public interface OnResourceLongClickListener {
        void onResourceLongClick(MyResource resource, int position);
    }

    private final List<MyResource> resources;
    private OnResourceClickListener clickListener;
    private OnResourceLongClickListener longClickListener;

    public MyResourcesAdapter(List<MyResource> resources) {
        this.resources = resources;
    }

    public void setOnResourceClickListener(OnResourceClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnResourceLongClickListener(OnResourceLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public ResourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_resource, parent, false);
        return new ResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceViewHolder holder, int position) {
        MyResource resource = resources.get(position);

        holder.tvIcon.setText(resource.getTypeEmoji());
        holder.tvName.setText(resource.fileName != null ? resource.fileName : "Unknown file");
        holder.tvSize.setText(resource.getFormattedSize());
        // Show "Local file" instead of uploader name for personal resources
        holder.tvUploader.setText("Local file");

        String typeLabel = resource.fileType != null ? resource.fileType.toUpperCase() : "FILE";
        holder.tvTypeBadge.setText(typeLabel);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onResourceClick(resource, position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onResourceLongClick(resource, position);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return resources.size();
    }

    static class ResourceViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvName, tvSize, tvUploader, tvTypeBadge;

        ResourceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tv_resource_icon);
            tvName = itemView.findViewById(R.id.tv_resource_name);
            tvSize = itemView.findViewById(R.id.tv_resource_size);
            tvUploader = itemView.findViewById(R.id.tv_resource_uploader);
            tvTypeBadge = itemView.findViewById(R.id.tv_resource_type_badge);
        }
    }
}
