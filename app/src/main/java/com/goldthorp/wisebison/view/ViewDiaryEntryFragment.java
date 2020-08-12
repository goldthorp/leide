package com.goldthorp.wisebison.view;

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

import com.goldthorp.wisebison.R;
import com.goldthorp.wisebison.data.AppDatabase;
import com.goldthorp.wisebison.data.DiaryEntryDao;
import com.goldthorp.wisebison.model.DiaryEntry;
import com.goldthorp.wisebison.util.Utils;

import org.apache.commons.lang3.StringUtils;

public class ViewDiaryEntryFragment extends Fragment {

  private TextView dateTextView;
  private TextView locationTextView;
  private TextView textTextView;
  private Button previousButton;
  private Button nextButton;

  private DiaryEntry previousEntry;
  private DiaryEntry nextEntry;

  private DiaryEntryDao diaryEntryDao;

  @Nullable
  @Override
  public View onCreateView(
    @NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
    @Nullable final Bundle savedInstanceState) {
    final View root = inflater.inflate(R.layout.fragment_view_diary_entry, container, false);

    dateTextView = root.findViewById(R.id.entry_date_text_view);
    locationTextView = root.findViewById(R.id.entry_location_text_view);
    textTextView = root.findViewById(R.id.entry_text_text_view);
    previousButton = root.findViewById(R.id.previous_button);
    nextButton = root.findViewById(R.id.next_button);

    final ConstraintLayout layout = root.findViewById(R.id.view_entry_layout);

    final ViewDiaryEntryFragmentArgs args =
      ViewDiaryEntryFragmentArgs.fromBundle(requireArguments());
    final long entryId = args.getDiaryEntryId();

    diaryEntryDao = AppDatabase.getInstance(requireContext()).getDiaryEntryDao();
    diaryEntryDao.getById(entryId).then(entry -> {
      setEntry(entry);
      layout.setVisibility(View.VISIBLE);
    });

    // Go to the previous entry
    previousButton.setOnClickListener(v -> setEntry(previousEntry));

    // Go to the next entry
    nextButton.setOnClickListener(v -> setEntry(nextEntry));

    return root;
  }

  private void setEntry(final DiaryEntry entry) {
    dateTextView.setText(Utils.formatDate(entry.getStartTimestamp()));
    if (StringUtils.isNotBlank(entry.getLocation())) {
      locationTextView.setText(entry.getLocation());
      locationTextView.setVisibility(View.VISIBLE);
    } else {
      locationTextView.setVisibility(View.GONE);
    }
    textTextView.setText(entry.getText());
    // Pre-load the previous and next entries to show if the previous/next entry buttons are
    // clicked
    diaryEntryDao.getPrevious(entry.getStartTimestamp()).then(previous -> {
      previousEntry = previous;
      if (previousEntry == null) {
        previousButton.setVisibility(View.GONE);
      } else {
        previousButton.setVisibility(View.VISIBLE);
      }
    });
    diaryEntryDao.getNext(entry.getStartTimestamp()).then(next -> {
      nextEntry = next;
      if (nextEntry == null) {
        nextButton.setVisibility(View.GONE);
      } else {
        nextButton.setVisibility(View.VISIBLE);
      }
    });
  }
}
