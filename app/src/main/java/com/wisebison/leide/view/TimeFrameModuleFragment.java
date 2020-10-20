package com.wisebison.leide.view;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.DiaryEntryDao;
import com.wisebison.leide.model.Module;
import com.wisebison.leide.util.RunInBackgroundTask;

import lombok.NoArgsConstructor;

@NoArgsConstructor
abstract class TimeFrameModuleFragment extends ModuleFragment {

  CarouselView carouselView;

  final static String CAROUSEL_INDEX_ID = "TIME_FRAME";

  final static int TIME_FRAME_ITEM_24_HOURS_ID = 1001;
  final static int TIME_FRAME_ITEM_7_DAYS_ID = 1002;
  final static int TIME_FRAME_ITEM_30_DAYS_ID = 1003;

  private Long earliestTimestamp;
  private DiaryEntryDao entryDao;

  TimeFrameModuleFragment(final Module module) {
    super(module);
  }

  @Nullable
  @Override
  public View onCreateView(
    @NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
    @Nullable final Bundle savedInstanceState) {
    entryDao = AppDatabase.getInstance(requireContext()).getDiaryEntryDao();
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  /**
   * Get the millis for the start and end time based on the given time frame.
   *
   * @param timeFrameId time frame to get start/end millis for
   * @return pair of longs where first is start, second is end
   */
  static Pair<Long, Long> getTimeFrame(final int timeFrameId) {
    return getTimeFrame(timeFrameId, 0);
  }

  static Pair<Long, Long> getTimeFrame(final int timeFrameId, final int offset) {
    final long currentMillis = System.currentTimeMillis();
    final long oneDayInMillis = 24 * 60 * 60 * 1000;
    final long oneWeekInMillis = oneDayInMillis * 7;
    final long thirtyDaysInMillis = oneDayInMillis * 30;
    final long startMillis;
    switch (timeFrameId) {
      case TIME_FRAME_ITEM_24_HOURS_ID:
        startMillis = currentMillis - oneDayInMillis;
        break;
      case TIME_FRAME_ITEM_7_DAYS_ID:
        startMillis = currentMillis - oneWeekInMillis;
        break;
      case TIME_FRAME_ITEM_30_DAYS_ID:
        startMillis = currentMillis - thirtyDaysInMillis;
        break;
      default:
        throw new IllegalArgumentException("Illegal time frame id " + timeFrameId);
    }
    final long offsetMillis = (currentMillis - startMillis) * offset;
    return Pair.create(startMillis + offsetMillis, currentMillis + offsetMillis);
  }


  void getEarliestTimestamp(final RunInBackgroundTask.Callback<Long> callback) {
    if (earliestTimestamp != null) {
      callback.resolve(earliestTimestamp);
      return;
    }
    entryDao.getEarliestTimestamp().then(timestamp -> {
      earliestTimestamp = timestamp;
      callback.resolve(timestamp);
    });
  }
}
