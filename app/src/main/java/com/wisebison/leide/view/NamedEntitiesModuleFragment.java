package com.wisebison.leide.view;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.text.ParcelableSpan;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.util.CollectionUtils;
import com.wisebison.leide.R;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.DiaryEntryDao;
import com.wisebison.leide.data.NamedEntityDao;
import com.wisebison.leide.model.Module;
import com.wisebison.leide.model.NamedEntityForm;

import org.joda.time.DateTime;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class NamedEntitiesModuleFragment extends ModuleFragment {
  public NamedEntitiesModuleFragment(final Module module) {
    super(module);
  }

  private ProgressBar entitiesProgressBar;
  private CarouselView carouselView;

  private NamedEntityDao namedEntityDao;

  private final int TIME_FRAME_ITEM_24_HOURS_ID = 1004;
  private final int TIME_FRAME_ITEM_WEEK_ID = 1003;
  private final int TIME_FRAME_ITEM_30_DAYS_ID = 1002;
  private final int TIME_FRAME_ITEM_YEAR_ID = 1001;

  @Override
  int getLayoutResource() {
    return R.layout.fragment_named_entities_module;
  }

  @Nullable
  @Override
  public View onCreateView(
    @NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
    @Nullable final Bundle savedInstanceState) {
    final View root = super.onCreateView(inflater, container, savedInstanceState);
    Objects.requireNonNull(root);
    entitiesProgressBar = root.findViewById(R.id.entities_progress_bar);
    carouselView = root.findViewById(R.id.entities_carousel_view);

    final Context context = requireContext();

    final AppDatabase db = AppDatabase.getInstance(context);

    namedEntityDao = db.getNamedEntityDao();

    final DiaryEntryDao diaryEntryDao = db.getDiaryEntryDao();
    namedEntityDao.getAll().observe(getViewLifecycleOwner(), namedEntities ->
      // Get timestamp of earliest entry to determine what the earliest timeframe to display
      // in the carousel is.
      diaryEntryDao.getEarliestTimestamp().then(earliestTimeStamp -> {
        carouselView.removeAllViews();
        if (earliestTimeStamp == null) {
          // No entries have been added yet
          entitiesProgressBar.setVisibility(View.GONE);
          final EntityCarouselItemView noEntitiesView = new EntityCarouselItemView(context);
          noEntitiesView.setEntitiesText(getResources().getString(R.string.no_entities));
          carouselView.addView(noEntitiesView);
          carouselView.setVisibility(View.VISIBLE);
        } else {
          new ProcessNamedEntitiesTask(this).execute(new DateTime(earliestTimeStamp));
        }
      }));

    return root;
  }

  /**
   * Task to display the entities on the screen with the most commonly used entities appearing at
   * the top in the largest font and the least commonly used entities appearing that the bottom
   * in the smallest font.
   */
  private static class ProcessNamedEntitiesTask
    extends AsyncTask<DateTime, Void, Map<String, SpannableString>> {

    private final WeakReference<NamedEntitiesModuleFragment> fragmentReference;
    ProcessNamedEntitiesTask(final NamedEntitiesModuleFragment fragment) {
      fragmentReference = new WeakReference<>(fragment);
    }

    @Override
    protected Map<String, SpannableString> doInBackground(final DateTime... earliestDate) {
      final NamedEntitiesModuleFragment fragment = fragmentReference.get();

      final DateTime earliest = earliestDate[0];

      final NamedEntityDao namedEntityDao = fragment.namedEntityDao;

      final Map<String, SpannableString> results = new LinkedHashMap<>();

      // add 24 hrs
      final Pair<Long, Long> timeFrame24Hours = getTimeFrame(fragment.TIME_FRAME_ITEM_24_HOURS_ID);
      final List<NamedEntityForm> namedEntityForms24Hours =
        namedEntityDao.countEntitiesByName(timeFrame24Hours.first, timeFrame24Hours.second);
      results.put(fragment.getString(R.string.timeframe_24_hours),
        processEntities(namedEntityForms24Hours));

      if (earliest.isBefore(DateTime.now().minusHours(24))) {
        // add 7 days
        final Pair<Long, Long> timeFrame7Days = getTimeFrame(fragment.TIME_FRAME_ITEM_WEEK_ID);
        final List<NamedEntityForm> namedEntityForms7Days =
          namedEntityDao.countEntitiesByName(timeFrame7Days.first, timeFrame7Days.second);
        results.put(fragment.getString(R.string.timeframe_7_days),
          processEntities(namedEntityForms7Days));
      }
      if (earliest.isBefore(DateTime.now().minusDays(7))) {
        // add 30 days
        final Pair<Long, Long> timeFrame30Days = getTimeFrame(fragment.TIME_FRAME_ITEM_30_DAYS_ID);
        final List<NamedEntityForm> namedEntityForms30Days =
          namedEntityDao.countEntitiesByName(timeFrame30Days.first, timeFrame30Days.second);
        results.put(fragment.getString(R.string.timeframe_30_days), 
          processEntities(namedEntityForms30Days));
      }
      if (earliest.isBefore(DateTime.now().minusDays(30))) {
        // add year
        final Pair<Long, Long> timeFrameYear = getTimeFrame(fragment.TIME_FRAME_ITEM_YEAR_ID);
        final List<NamedEntityForm> namedEntityFormsYear =
          namedEntityDao.countEntitiesByName(timeFrameYear.first, timeFrameYear.second);
        results.put(fragment.getString(R.string.timeframe_year),
          processEntities(namedEntityFormsYear));
      }
      if (earliest.isBefore(DateTime.now().minusYears(1))) {
        // add all time
        final List<NamedEntityForm> namedEntityFormsAllTime = namedEntityDao.countEntitiesByName();
        results.put(fragment.getString(R.string.timeframe_all_time),
          processEntities(namedEntityFormsAllTime));
      }
      return results;
    }

    /**
     * Get the millis for the start and end time based on the given time frame.
     *
     * @param timeFrameItemId time frame to get start/end millis for (ID of menu item for time frame)
     * @return pair of longs where first is start, second is end
     */
    private Pair<Long, Long> getTimeFrame(final int timeFrameItemId) {
      final NamedEntitiesModuleFragment fragment = fragmentReference.get();
      final long currentMillis = System.currentTimeMillis();
      final long oneDayInMillis = 24 * 60 * 60 * 1000;
      final long oneWeekInMillis = oneDayInMillis * 7;
      final long thirtyDaysInMillis = oneDayInMillis * 30;
      final long oneYearInMillis = oneDayInMillis * 365;
      final long startMillis;
      if (timeFrameItemId == fragment.TIME_FRAME_ITEM_24_HOURS_ID) {
        startMillis = currentMillis - oneDayInMillis;
      } else if (timeFrameItemId == fragment.TIME_FRAME_ITEM_WEEK_ID) {
        startMillis = currentMillis - oneWeekInMillis;
      } else if (timeFrameItemId == fragment.TIME_FRAME_ITEM_30_DAYS_ID) {
        startMillis = currentMillis - thirtyDaysInMillis;
      } else if (timeFrameItemId == fragment.TIME_FRAME_ITEM_YEAR_ID) {
        startMillis = currentMillis - oneYearInMillis;
      } else {
        throw new IllegalArgumentException("Illegal time frame id " + timeFrameItemId);
      }
      return Pair.create(startMillis, currentMillis);
    }

    /**
     * Process the given entities to create the SpannableString with entities sized appropriately
     * to reflect the frequency of that entity.
     *
     * @param namedEntityForms entities to process (text + count)
     * @return SpannableString with spans set for font size and color
     */
    private SpannableString processEntities(final List<NamedEntityForm> namedEntityForms) {
      final NamedEntitiesModuleFragment fragment = fragmentReference.get();
      if (CollectionUtils.isEmpty(namedEntityForms)) {
        return null;
      }
      // The count for the most frequently used entity
      final int largest = namedEntityForms.get(0).getCount();
      // The spans that will be used to style the text. Key is a pair of ints where first is
      // the start index for the span and second is the end index. Value is the span to apply
      // at those indexes in the SpannableString
      final Map<Pair<Integer, Integer>, Set<ParcelableSpan>> spans = new HashMap<>();
      // Build the string and create the spans to apply to the SpannableString
      final StringBuilder sb = new StringBuilder();
      for (int i = 0; i < namedEntityForms.size(); i++) {
        final NamedEntityForm entity = namedEntityForms.get(i);
        final int start = sb.length();
        final int end = start + entity.getName().length();
        sb.append(entity.getName());
        sb.append(" ");
        final Set<ParcelableSpan> spanData = new HashSet<>();
        // Use ((<count> / <largest>) * 3) + 1 to achieve a consistent font size regardless
        // of how big <largest> is (most common word will always be scaled up by a factor of 4,
        // least common will approach 1)
        spanData.add(new RelativeSizeSpan((((float) entity.getCount() / largest) * 3) + 1));
        // Alternate colors so that entities can be differentiated since some entities are
        // more than one word
        final int color = i % 2 == 0 ? R.color.entity_text_dark : R.color.entity_text_light;
        spanData.add(new ForegroundColorSpan(fragment.getResources().getColor(color, null)));
        spans.put(Pair.create(start, end), spanData);
      }
      // Create a SpannableString and apply the spans in the map
      final SpannableString ss = new SpannableString(sb);
      for (final Map.Entry<Pair<Integer, Integer>, Set<ParcelableSpan>> spanData :
        spans.entrySet()) {
        final int start = spanData.getKey().first;
        final int end = spanData.getKey().second;
        for (final ParcelableSpan span : spanData.getValue()) {
          ss.setSpan(span, start, end, 0);
        }
      }
      return ss;
    }

    // Display the processed data in the carousel view
    @Override
    protected void onPostExecute(final Map<String, SpannableString> entitiesTexts) {
      final NamedEntitiesModuleFragment fragment = fragmentReference.get();
      // For each timeframe
      for (final Map.Entry<String, SpannableString> entitiesTextEntry : entitiesTexts.entrySet()) {
        // Create view and add to carousel
        final EntityCarouselItemView carouselItemView =
          new EntityCarouselItemView(fragment.requireContext(), entitiesTextEntry.getKey());
        fragment.carouselView.addView(carouselItemView);
        // If there are entities to display for this timeframe
        if (entitiesTextEntry.getValue() != null) {
          carouselItemView.setEntitiesText(entitiesTextEntry.getValue());
          // Once the entities text is set, we need to calculate the max line count to avoid
          // overflow
          carouselItemView.getEntitiesTextView().setMaxLines(0);
          final int heightPx =
            fragment.getResources().getDimensionPixelSize(R.dimen.carousel_item_content_height);
          // We must wait for the TextView's layout to be set to read the height of each line of
          // text - use ViewTreeObserver.OnGlobalLayoutListener
          final ViewTreeObserver viewTreeObserver =
            carouselItemView.getEntitiesTextView().getViewTreeObserver();
          viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
              final Layout layout = carouselItemView.getEntitiesTextView().getLayout();
              if (layout != null) {
                int maxLines = 0;
                // Find which line (if any) would spill out of the view by comparing each line's
                // bottom px to the height of the container
                for (int i = 0; i < layout.getLineCount(); i++) {
                  if (layout.getLineBottom(i) > heightPx) {
                    maxLines = i;
                    break;
                  }
                }
                // If no lines extend below the bottom of the container, max lines is simply the
                // total number of lines
                if (maxLines == 0) {
                  maxLines = layout.getLineCount();
                }
                carouselItemView.getEntitiesTextView().setMaxLines(maxLines);
                // Don't forget to remove the global layout listener once finished
                viewTreeObserver.removeOnGlobalLayoutListener(this);
              }
            }
          });
        } else {
          carouselItemView.setEntitiesText(fragment.getString(R.string.no_entities));
        }
      }

      fragment.carouselView.setVisibility(View.VISIBLE);
      fragment.entitiesProgressBar.setVisibility(View.GONE);
      fragment.carouselView.start(5000);
    }
  }
}
