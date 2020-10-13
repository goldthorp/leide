package com.wisebison.leide.view;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wisebison.leide.R;
import com.wisebison.leide.util.Utils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.wisebison.leide.view.TimeFrameModuleFragment.TIME_FRAME_ITEM_24_HOURS_ID;
import static com.wisebison.leide.view.TimeFrameModuleFragment.TIME_FRAME_ITEM_30_DAYS_ID;
import static com.wisebison.leide.view.TimeFrameModuleFragment.TIME_FRAME_ITEM_7_DAYS_ID;

public class SentimentCarouselItemView extends LinearLayout {

  private int timeFrameId;
  private final TextView timeFrameTextView;
  private final TextView averageScoreTextView;
  private final TextView averageDeltaTextView;
  private final TextView sentenceTextView;

  private CarouselView carouselView;

  public SentimentCarouselItemView(final Context context) {
    super(context);
    inflate(context, R.layout.view_sentiment_carousel_item, this);
    timeFrameTextView = findViewById(R.id.time_frame_text_view);
    averageScoreTextView = findViewById(R.id.average_text_view);
    averageDeltaTextView = findViewById(R.id.average_delta_text_view);
    sentenceTextView = findViewById(R.id.sentence_text_view);
  }

  public SentimentCarouselItemView(
    final Context context, final int timeFrameId, final CarouselView carouselView) {
    this(context);
    this.timeFrameId = timeFrameId;
    timeFrameTextView.setText(getTimeFrameText());
    this.carouselView = carouselView;
  }

  public void setAverageScore(final Float average) {
    final String text;
    if (average != null) {
      text = getResources().getString(R.string.average_sentiment_label,
        Utils.formatFloat(average, false));
    } else {
      text = "No data";
    }
    averageScoreTextView.setText(text);
  }

  public void setAverageDelta(final Float averageDelta) {
    final String text;
    if (averageDelta != null) {
      text = getAverageDeltaText(averageDelta);
      if (averageDelta > 0) {
        averageDeltaTextView.setTextColor(getResources().getColor(R.color.sentiment_positive, null));
      } else if (averageDelta < 0) {
        averageDeltaTextView.setTextColor(getResources().getColor(R.color.sentiment_negative, null));
      }
    } else {
      text = "No data for previous";
    }
    averageDeltaTextView.setText(text);
  }

  public void setSentences(final List<String> sentences) {
    if (CollectionUtils.isEmpty(sentences)) {
      sentenceTextView.setVisibility(GONE);
      return;
    }
    Collections.shuffle(sentences);
    final AtomicInteger sentenceIndex = new AtomicInteger(0);
    sentenceTextView.setText(sentences.get(0));
    carouselView.setOnChangeListener(() -> {
      if (sentenceIndex.get() + 1 == sentences.size()) {
        sentenceTextView.setText(sentences.get(0));
        sentenceIndex.set(0);
      } else {
        sentenceTextView.setText(sentences.get(sentenceIndex.incrementAndGet()));
      }
    });
  }

  String getTimeFrameText() {
    switch (timeFrameId) {
      case TIME_FRAME_ITEM_24_HOURS_ID:
        return getResources().getString(R.string.timeframe_24_hours);
      case TIME_FRAME_ITEM_7_DAYS_ID:
        return getResources().getString(R.string.timeframe_7_days);
      case TIME_FRAME_ITEM_30_DAYS_ID:
        return getResources().getString(R.string.timeframe_30_days);
      default:
        throw new IllegalArgumentException("invalid time frame id: " + timeFrameId);
    }
  }

  String getAverageDeltaText(final float averageDelta) {
    final int stringResource;
    switch (timeFrameId) {
      case TIME_FRAME_ITEM_24_HOURS_ID:
        stringResource = R.string.average_sentiment_24_hours_delta;
        break;
      case TIME_FRAME_ITEM_7_DAYS_ID:
        stringResource = R.string.average_sentiment_7_days_delta;
        break;
      case TIME_FRAME_ITEM_30_DAYS_ID:
        stringResource = R.string.average_sentiment_30_days_delta;
        break;
      default:
        throw new IllegalArgumentException("invalid timeframe id: " + timeFrameId);
    }
    return getResources().getString(stringResource, Utils.formatFloat(averageDelta, true));
  }
}
