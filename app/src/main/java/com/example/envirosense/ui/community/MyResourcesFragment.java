package com.example.envirosense.ui.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.envirosense.R;

/**
 * Displays resources (notes, files) shared within the user's study groups.
 * Currently shows an empty state; wire up to a data source when ready.
 */
public class MyResourcesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Empty state is shown by default in the layout (rv_resources is GONE).
        // When resources are fetched, toggle visibility here.
        return inflater.inflate(R.layout.fragment_my_resources, container, false);
    }
}
