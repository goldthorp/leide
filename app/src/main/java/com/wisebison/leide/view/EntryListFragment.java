package com.wisebison.leide.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.wisebison.leide.R;
import com.wisebison.leide.billing.BillingUtil;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.DiaryEntryDao;
import com.wisebison.leide.model.DiaryEntryForm;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EntryListFragment extends Fragment {

  // Used to restore the scroll position of the ListView
  // when navigating back from viewing an entry.
  private int topItemIndex = 0;
  private int topItemPadding = 0;

  @Inject
  BillingUtil billingUtil;

  @Nullable
  @Override
  public View onCreateView(
    @NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
    @Nullable final Bundle savedInstanceState) {
    final View root = inflater.inflate(R.layout.fragment_diary_entry_list, container, false);
    billingUtil.hasPremium(hasPremium -> {
      // Set up list ui
      final ArrayList<DiaryEntryForm> entriesList = new ArrayList<>();
      final DiaryEntryAdapter entryArrayAdapter =
        new DiaryEntryAdapter(requireContext(), entriesList, hasPremium);
      final ListView listView = root.findViewById(R.id.entries_list_view);
      listView.setAdapter(entryArrayAdapter);
      // Set scroll position after list is drawn
      listView.addOnLayoutChangeListener(
        (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
          listView.setSelectionFromTop(topItemIndex, topItemPadding);
        });

      listView.setOnItemClickListener((parent, view, position, id) -> {
        // Save the current scroll position so it can be restored on back press
        topItemIndex = listView.getFirstVisiblePosition();
        final View firstItem = listView.getChildAt(0);
        topItemPadding = (firstItem == null) ? 0 : (firstItem.getTop() - listView.getPaddingTop());
        final EntryListFragmentDirections.ActionEntryListFragmentToViewDiaryEntryFragment action =
          EntryListFragmentDirections
            .actionEntryListFragmentToViewDiaryEntryFragment(entriesList.get(position).getId());
        Navigation.findNavController(root).navigate(action);
      });


      // Load all entries and subscribe to changes
      final DiaryEntryDao entryDao = AppDatabase.getInstance(requireContext()).getDiaryEntryDao();
      entryDao.getList().observe(getViewLifecycleOwner(), entryArrayAdapter::update);

      final FloatingActionButton fab = root.findViewById(R.id.fab);
      fab.setOnClickListener(view -> {
        final Intent intent = new Intent(requireContext(), CreateDiaryEntryActivity.class);
        startActivity(intent);
      });
    });

    return root;
  }
}
