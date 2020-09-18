package com.wisebison.leide.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.wisebison.leide.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A layout that takes multiple views and will cycle them like a slideshow every x milliseconds.
 */
public class CarouselView extends LinearLayout {

  private final List<View> views;
  private int idx;
  private boolean running;
  private final Handler handler;

  public CarouselView(final Context context, @Nullable final AttributeSet attrs) {
    super(context, attrs);
    inflate(context, R.layout.view_carousel, this);
    views = new ArrayList<>();
    handler = new Handler();
  }

  @Override
  public void addView(final View view) {
    views.add(view);
    view.setVisibility(GONE);
    super.addView(view);
  }

  @Override
  public void removeAllViews() {
    super.removeAllViews();
    views.clear();
  }

  public void start(final int timeoutMillis) {
    if (views.size() == 0) {
      throw new IllegalStateException("Must have at least one view to start carousel");
    }
    if (running) {
      return;
    }
    running = true;
    idx = 0;
    views.get(idx).setVisibility(VISIBLE);
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        CarouselView.this.moveToNext();
        handler.postDelayed(this, timeoutMillis);
      }
    }, timeoutMillis);
  }

  public void stop() {
    handler.removeCallbacksAndMessages(null);
    running = false;
  }

  public void moveToNext() {
    views.get(idx).setVisibility(GONE);
    if (++idx == views.size()) {
      idx = 0;
    }
    views.get(idx).setVisibility(VISIBLE);
  }
}
