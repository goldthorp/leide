package com.wisebison.leide.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wisebison.leide.R;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.EntryDao;
import com.wisebison.leide.model.Module;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ViewEntriesModuleFragment extends ModuleFragment {
  // Invoked using reflection in Module
  public ViewEntriesModuleFragment(final Module module) {
    super(module);
  }

  @Nullable
  @Override
  public View onCreateView(
    @NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
    @Nullable final Bundle savedInstanceState) {
    final View rootView = super.onCreateView(inflater, container, savedInstanceState);
    final TextView entryCountTextView = rootView.findViewById(R.id.entry_count_text_view);
    final EntryDao diaryEntryDao = AppDatabase.getInstance(requireContext()).getDiaryEntryDao();
    diaryEntryDao.getCount().observe(getViewLifecycleOwner(), count -> {
      entryCountTextView.setText(getString(R.string.entry_count, count));
    });
    entryCountTextView.setOnClickListener(v -> {
      final Intent intent = new Intent(requireContext(), ViewEntriesActivity.class);
      startActivity(intent);
    });

    return rootView;
  }

  @Override
  int getLayoutResource() {
    return R.layout.fragment_view_entries_module;
  }
}
