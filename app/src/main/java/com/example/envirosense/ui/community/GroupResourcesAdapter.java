package com.example.envirosense.ui.community;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.envirosense.R;
import com.example.envirosense.data.models.GroupResource;

import java.util.List;

/**
 * RecyclerView adapter for displaying group resources (shared files).
 */
public class GroupResourcesAdapter extends RecyclerView.Adapter<GroupResourcesAdapter.ResourceViewHolder> {

    public interface OnResourceClickListener {
        void onResourceClick(GroupResource resource, int position);
    }

    public interface OnResourceLongClickListener {
        void onResourceLongClick(GroupResource resource, int position);
    }

    private final List<GroupResource> resources;
    private OnResourceClickListener clickListener;
    private OnResourceLongClickListener longClickListener;

    public GroupResourcesAdapter(List<GroupResource> resources) {
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
        GroupResource resource = resources.get(position);

        holder.tvIcon.setText(resource.getTypeEmoji());
        holder.tvName.setText(resource.fileName != null ? resource.fileName : "Unknown file");
        holder.tvSize.setText(resource.getFormattedSize());
        holder.tvUploader.setText(resource.uploadedBy != null ? "by " + resource.uploadedBy : "");
        
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
