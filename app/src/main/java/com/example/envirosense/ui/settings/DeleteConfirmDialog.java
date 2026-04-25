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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

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
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String uid = user != null ? user.getUid() : "guest";
                AppDatabase.getInstance(requireContext()).focusSessionDao().deleteAllSessions(uid);

                requireContext().getSharedPreferences("EnviroSenseAchieve", android.content.Context.MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply();

                if (user != null) {
                    Map<String, Object> resetStats = new HashMap<>();
                    resetStats.put("totalHours", 0.0);
                    resetStats.put("averageScore", 0);
                    resetStats.put("sessionCount", 0);
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user.getUid())
                            .set(resetStats, SetOptions.merge());
                }

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Data permanently erased", Toast.LENGTH_SHORT).show();
                    androidx.fragment.app.Fragment homeFrag = getParentFragmentManager().findFragmentByTag("home");
                    if (homeFrag instanceof com.example.envirosense.ui.home.HomeFragment) {
                        ((com.example.envirosense.ui.home.HomeFragment) homeFrag).updateUi(com.example.envirosense.ui.home.HomeFragment.HomeState.FIRST_LAUNCH);
                    }
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
