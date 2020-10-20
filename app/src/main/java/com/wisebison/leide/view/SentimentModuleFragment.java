package com.wisebison.leide.view;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.wisebison.leide.R;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.DiarySentimentDao;
import com.wisebison.leide.model.Module;
import com.wisebison.leide.util.BackgroundUtil;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SentimentModuleFragment extends TimeFrameModuleFragment {
  // Invoked using reflection in Module
  public SentimentModuleFragment(final Module module) {
    super(module);
  }

  private DiarySentimentDao sentimentDao;

  private LiveData<Long> sentimentCountLiveData;
  private Long sentimentCount;

  @Override
  int getLayoutResource() {
    return R.layout.fragment_sentiment_module;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
    final View root = super.onCreateView(inflater, container, savedInstanceState);
    Objects.requireNonNull(root);
    carouselView = root.findViewById(R.id.sentiment_carousel_view);

    final AppDatabase db = AppDatabase.getInstance(requireContext());

    sentimentDao = db.getDiarySentimentDao();

    sentimentCountLiveData = sentimentDao.getCount();

    return root;
  }

  @Override
  public void onStart() {
    super.onStart();
    sentimentCountLiveData.observe(getViewLifecycleOwner(), count -> {
      if (!count.equals(sentimentCount) && count > 0) {
        sentimentCount = count;
        getEarliestTimestamp(earliestTimestamp -> {
          carouselView.stop();
          carouselView.removeAllViews();
          if (earliestTimestamp == null) {
            final SentimentCarouselItemView noSentimentView =
              new SentimentCarouselItemView(requireContext());
//            noSentimentView.set TODO: handle no entries
          } else {
            BackgroundUtil.doInBackground(() -> {
              final DateTime earliest = new DateTime(earliestTimestamp);

              List<SentimentSummary> summaries = new ArrayList<>();
              SentimentSummary summary24Hours = new SentimentSummary();
              summary24Hours.timeFrameId = TIME_FRAME_ITEM_24_HOURS_ID;
              final Pair<Long, Long> timeFrame24Hours = getTimeFrame(TIME_FRAME_ITEM_24_HOURS_ID);
              summary24Hours.average =
                sentimentDao.getAverageSentiment(timeFrame24Hours.first, timeFrame24Hours.second);

              if (summary24Hours.average != null) {
                final Pair<Long, Long> timeFramePrevious24Hours =
                  getTimeFrame(TIME_FRAME_ITEM_24_HOURS_ID, -1);
                Float averagePrevious24Hours =
                  sentimentDao.getAverageSentiment(timeFramePrevious24Hours.first,
                    timeFramePrevious24Hours.second);
                if (averagePrevious24Hours != null) {
                  summary24Hours.averageDelta = summary24Hours.average - averagePrevious24Hours;
                }
              }

              summary24Hours.sentences =
                sentimentDao.getMostPositiveSentences(timeFrame24Hours.first,
                  timeFrame24Hours.second);

              summaries.add(summary24Hours);

              final Pair<Long, Long> timeFrame7Days = getTimeFrame(TIME_FRAME_ITEM_7_DAYS_ID);
              if (earliest.isBefore(DateTime.now().minusHours(24))) {
                SentimentSummary summary7Days = new SentimentSummary();
                summary7Days.timeFrameId = TIME_FRAME_ITEM_7_DAYS_ID;
                summary7Days.average =
                  sentimentDao.getAverageSentiment(timeFrame7Days.first, timeFrame7Days.second);

                if (summary7Days.average != null) {
                  final Pair<Long, Long> timeFramePrevious7Days =
                    getTimeFrame(TIME_FRAME_ITEM_7_DAYS_ID, -1);
                  Float averagePrevious7Days =
                    sentimentDao.getAverageSentiment(timeFramePrevious7Days.first,
                      timeFramePrevious7Days.second);
                  if (averagePrevious7Days != null) {
                    summary7Days.averageDelta = summary7Days.average - averagePrevious7Days;
                  }
                }

                summary7Days.sentences = sentimentDao.getMostPositiveSentences(timeFrame7Days.first,
                  timeFrame24Hours.first);

                summaries.add(summary7Days);
              }

              if (earliest.isBefore(DateTime.now().minusDays(7))) {
                SentimentSummary summary30Days = new SentimentSummary();
                summary30Days.timeFrameId = TIME_FRAME_ITEM_30_DAYS_ID;
                final Pair<Long, Long> timeFrame30Days = getTimeFrame(TIME_FRAME_ITEM_30_DAYS_ID);
                summary30Days.average =
                  sentimentDao.getAverageSentiment(timeFrame30Days.first, timeFrame30Days.second);

                if (summary30Days.average != null) {
                  final Pair<Long, Long> timeFramePrevious30Days =
                    getTimeFrame(TIME_FRAME_ITEM_30_DAYS_ID, -1);
                  Float averagePrevious30Days =
                    sentimentDao.getAverageSentiment(timeFramePrevious30Days.first,
                      timeFramePrevious30Days.second);
                  if (averagePrevious30Days != null) {
                    summary30Days.averageDelta = summary30Days.average - averagePrevious30Days;
                  }
                }

                summary30Days.sentences =
                  sentimentDao.getMostPositiveSentences(timeFrame30Days.first,
                    timeFrame7Days.first);

                summaries.add(summary30Days);
              }

              return summaries;
            }).then(summaries -> {
              for (final SentimentSummary summary : summaries) {
                final SentimentCarouselItemView carouselItemView =
                  new SentimentCarouselItemView(requireContext(), summary.timeFrameId, carouselView);
                carouselItemView.setAverageScore(summary.average);
                carouselItemView.setAverageDelta(summary.averageDelta);
                carouselItemView.setSentences(summary.sentences);
                carouselView.addView(carouselItemView);
              }
              carouselView.start(5000, CAROUSEL_INDEX_ID);
            });
          }
        });
      }
    });
  }

  private class SentimentSummary {
    private int timeFrameId;
    private Float average;
    private Float averageDelta;
    private List<String> sentences;
  }

  @Override
  public void onStop() {
    super.onStop();
    sentimentCountLiveData.removeObservers(getViewLifecycleOwner());
  }
}
