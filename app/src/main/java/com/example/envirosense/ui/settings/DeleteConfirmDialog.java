package com.example.envirosense.ui.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.envirosense.R;
import com.example.envirosense.data.AppDatabase;

public class DeleteConfirmDialog extends DialogFragment {

    private OnDeleteListener listener;

    public interface OnDeleteListener {
        void onDataDeleted();
    }

    public DeleteConfirmDialog(OnDeleteListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_delete_confirm, null);
        builder.setView(view);

        Button btnDelete = view.findViewById(R.id.btn_delete);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnDelete.setOnClickListener(v -> {
            new Thread(() -> {
                AppDatabase.getInstance(requireContext()).focusSessionDao().deleteAllSessions();

                requireContext().getSharedPreferences("EnviroSenseAchieve", android.content.Context.MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply();

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Data permanently erased", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onDataDeleted();
                    dismiss();
                });
            }).start();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        return dialog;
    }
}