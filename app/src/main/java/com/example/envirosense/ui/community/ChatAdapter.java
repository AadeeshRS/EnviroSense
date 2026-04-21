package com.example.envirosense.ui.community;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.envirosense.R;
import com.example.envirosense.data.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;

    private List<ChatMessage> messages;
    private OnMessageLongClickListener longClickListener;

    public interface OnMessageLongClickListener {
        void onMessageLongClick(ChatMessage message, int position);
    }

    public ChatAdapter(List<ChatMessage> messages, OnMessageLongClickListener longClickListener) {
        this.messages = messages;
        this.longClickListener = longClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isMe() ? VIEW_TYPE_ME : VIEW_TYPE_OTHER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ME) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_message_me, parent, false);
            return new MeViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_message_other, parent, false);
            return new OtherViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof MeViewHolder) {
            ((MeViewHolder) holder).bind(message);
            holder.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    int adapterPos = holder.getAdapterPosition();
                    if (adapterPos != RecyclerView.NO_POSITION) {
                        longClickListener.onMessageLongClick(message, adapterPos);
                    }
                    return true;
                }
                return false;
            });
        } else if (holder instanceof OtherViewHolder) {
            ((OtherViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MeViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTimestamp;

        MeViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getMessageText());
            tvTimestamp.setText(message.getTimestamp());
        }
    }

    static class OtherViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvMessage, tvTimestamp;

        OtherViewHolder(View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tv_sender);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }

        void bind(ChatMessage message) {
            tvSender.setText(message.getSenderName());
            tvMessage.setText(message.getMessageText());
            tvTimestamp.setText(message.getTimestamp());
        }
    }
}
