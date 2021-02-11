package com.wisebison.leide.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.wisebison.leide.R;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.EntryDao;
import com.wisebison.leide.model.Entry;
import com.wisebison.leide.model.EntryForm;

public class ViewEntryFragment extends Fragment {

  private TextView textView;
  private Button previousButton;
  private Button nextButton;

  private Entry previousEntry;
  private Entry nextEntry;

  private EntryDao diaryEntryDao;

  @Nullable
  @Override
  public View onCreateView(
    @NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
    @Nullable final Bundle savedInstanceState) {
    final View root = inflater.inflate(R.layout.fragment_view_entry, container, false);

    textView = root.findViewById(R.id.entry_text_view);
    previousButton = root.findViewById(R.id.previous_button);
    nextButton = root.findViewById(R.id.next_button);

    final ConstraintLayout layout = root.findViewById(R.id.view_entry_layout);

    final ViewEntryFragmentArgs args = ViewEntryFragmentArgs.fromBundle(requireArguments());
    final long entryId = args.getEntryId();

    diaryEntryDao = AppDatabase.getInstance(requireContext()).getEntryDao();
    diaryEntryDao.getById(entryId).then(entry -> {
      setEntry(entry);
      layout.setVisibility(View.VISIBLE);
    });

    // FIXME
//    // Go to the previous entry
//    previousButton.setOnClickListener(v -> setEntry(previousEntry));
//
//    // Go to the next entry
//    nextButton.setOnClickListener(v -> setEntry(nextEntry));

    return root;
  }

  private void setEntry(final EntryForm entry) {
    textView.setText(entry.getEntryForDisplay(getContext(), false));
    // FIXME
//    // Pre-load the previous and next entries to show if the previous/next entry buttons are
//    // clicked
//    diaryEntryDao.getPrevious(entry.getStartTimestamp()).then(previous -> {
//      previousEntry = previous;
//      if (previousEntry == null) {
//        previousButton.setVisibility(View.GONE);
//      } else {
//        previousButton.setVisibility(View.VISIBLE);
//      }
//    });
//    diaryEntryDao.getNext(entry.getStartTimestamp()).then(next -> {
//      nextEntry = next;
//      if (nextEntry == null) {
//        nextButton.setVisibility(View.GONE);
//      } else {
//        nextButton.setVisibility(View.VISIBLE);
//      }
//    });
  }
}
