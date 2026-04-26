package com.example.envirosense.ui.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.envirosense.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Bottom sheet that lets the user choose the source when adding a resource:
 *   - From "My Resources" (existing app resources)
 *   - From device local storage (file picker)
 */
public class AddResourceBottomSheet extends BottomSheetDialogFragment {

    public interface OnSourceSelectedListener {
        void onMyResourcesSelected();
        void onDeviceStorageSelected();
    }

    private OnSourceSelectedListener listener;

    public void setOnSourceSelectedListener(OnSourceSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public int getTheme() {
        return com.google.android.material.R.style.Theme_Design_BottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_resource, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Make the bottom sheet background transparent so our custom drawable shows
        View bottomSheet = (View) view.getParent();
        if (bottomSheet != null) {
            bottomSheet.setBackgroundResource(android.R.color.transparent);
        }

        LinearLayout optionMyResources = view.findViewById(R.id.option_from_my_resources);
        LinearLayout optionDevice = view.findViewById(R.id.option_from_device);

        optionMyResources.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMyResourcesSelected();
            }
            dismiss();
        });

        optionDevice.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeviceStorageSelected();
            }
            dismiss();
        });
    }
}
