package com.wisebison.leide.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wisebison.leide.R;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.DiaryEntryDao;

public class DiaryModuleFragment extends Fragment {
  @Nullable
  @Override
  public View onCreateView(
    @NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
    @Nullable final Bundle savedInstanceState) {
    final View root = inflater.inflate(R.layout.fragment_diary_module, container, false);
    final TextView entryCountTextView = root.findViewById(R.id.entry_count_text_view);
    final DiaryEntryDao diaryEntryDao = AppDatabase.getInstance(requireContext()).getDiaryEntryDao();
    diaryEntryDao.getCount().observe(getViewLifecycleOwner(), count -> {
      entryCountTextView.setText(getString(R.string.entry_count, count));
    });
    entryCountTextView.setOnClickListener(v -> {
      final Intent intent = new Intent(requireContext(), ViewDiaryActivity.class);
      startActivity(intent);
    });

    final LinearLayout newEntryLayout = root.findViewById(R.id.new_entry_layout);
    newEntryLayout.setOnClickListener(v -> {
      final Intent intent = new Intent(requireContext(), CreateDiaryEntryActivity.class);
      startActivity(intent);
    });
    return root;
  }
}
