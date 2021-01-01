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
import com.wisebison.leide.model.EntryComponent;
import com.wisebison.leide.model.EntryForm;
import com.wisebison.leide.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewEntryFragment extends Fragment {

  private TextView dateTextView;
  private TextView locationTextView;
  private TextView textTextView;
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

    dateTextView = root.findViewById(R.id.entry_date_text_view);
    locationTextView = root.findViewById(R.id.entry_location_text_view);
    textTextView = root.findViewById(R.id.entry_text_text_view);
    previousButton = root.findViewById(R.id.previous_button);
    nextButton = root.findViewById(R.id.next_button);

    final ConstraintLayout layout = root.findViewById(R.id.view_entry_layout);

    final ViewEntryFragmentArgs args = ViewEntryFragmentArgs.fromBundle(requireArguments());
    final long entryId = args.getEntryId();

    diaryEntryDao = AppDatabase.getInstance(requireContext()).getEntryDao();
    diaryEntryDao.getById(entryId).then(entry -> {
      try {
        setEntry(entry);
        layout.setVisibility(View.VISIBLE);
      } catch (final JSONException e) {
        e.printStackTrace();
      }
    });

    // FIXME
//    // Go to the previous entry
//    previousButton.setOnClickListener(v -> setEntry(previousEntry));
//
//    // Go to the next entry
//    nextButton.setOnClickListener(v -> setEntry(nextEntry));

    return root;
  }

  private void setEntry(final EntryForm entry) throws JSONException {
    //FIXME
//    dateTextView.setText(Utils.formatDate(entry.getStartTimestamp()));
//    if (StringUtils.isNotBlank(entry.getLocation())) {
//      locationTextView.setText(entry.getLocation());
//      locationTextView.setVisibility(View.VISIBLE);
//    } else {
      locationTextView.setVisibility(View.GONE);
//    }
//    textTextView.setText(entry.getText());
    for (final EntryComponent component : entry.getComponents()) {
      switch (component.getType()) {
        case DATE:
          final JSONObject dateValue = new JSONObject(component.getValue());
          dateTextView.setText(
            Utils.formatDate(dateValue.getLong("millis"), dateValue.getString("timeZone")));
          break;
        case LOCATION:
          final JSONObject locationValue = new JSONObject(component.getValue());
          locationTextView.setText(locationValue.getString("display"));
          locationTextView.setVisibility(View.VISIBLE);
          break;
        case TEXT:
          textTextView.setText(component.getValue());
      }
    }
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
