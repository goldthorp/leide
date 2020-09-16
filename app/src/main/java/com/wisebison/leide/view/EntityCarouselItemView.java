package com.wisebison.leide.view;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wisebison.leide.R;

import lombok.Getter;

/**
 * A layout for a single timeframe in the entities carousel (i.e. a single 'slide' in the
 * 'slideshow')
 */
public class EntityCarouselItemView extends LinearLayout {

  @Getter
  private final TextView entitiesTextView;
  private final TextView timeFrameTextView;

  public EntityCarouselItemView(final Context context) {
    super(context);
    inflate(context, R.layout.view_entity_carousel_item, this);
    entitiesTextView = findViewById(R.id.entities_text_view);
    timeFrameTextView = findViewById(R.id.time_frame_text_view);
  }

  public EntityCarouselItemView(final Context context, final String timeFrame) {
    this(context);
    timeFrameTextView.setText(timeFrame);
  }

  public void setEntitiesText(final CharSequence entitiesText) {
    entitiesTextView.setText(entitiesText);
  }
}
