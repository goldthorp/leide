package com.wisebison.leide.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.wisebison.leide.util.CarouselClock;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

/**
 * A layout that takes multiple views and will cycle them like a slideshow every x milliseconds.
 */
public class CarouselView extends LinearLayout {

  private final List<View> views;
  private int idx;
  private boolean running;
  private CarouselClock carouselClock;
  private CarouselClock.Action action;
  private String indexId;

  @Setter
  private OnChangeListener onChangeListener;

  public CarouselView(final Context context, @Nullable final AttributeSet attrs) {
    super(context, attrs);
    views = new ArrayList<>();
    addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
      @Override
      public void onViewAttachedToWindow(final View v) { }

      @Override
      public void onViewDetachedFromWindow(final View v) {
        stop();
      }
    });
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

  public void start(final int timeoutMillis, final String indexId) {
    if (views.size() == 0) {
      throw new IllegalStateException("Must have at least one view to start carousel");
    }
    if (running) {
      return;
    }
    running = true;
    carouselClock = CarouselClock.getInstance(timeoutMillis);
    idx = carouselClock.getIndex(indexId);
    views.get(idx).setVisibility(VISIBLE);
    action = this::moveToNext;
    this.indexId = indexId;
    carouselClock.addAction(action, indexId);
  }

  public void start(final int timeoutMillis) {
    start(timeoutMillis, null);
  }

  public void stop() {
    if (running) {
      carouselClock.removeAction(action);
      running = false;
    }
  }

  public Pair<String, Integer> moveToNext() {
    views.get(idx).setVisibility(GONE);
    if (idx + 1 == views.size()) {
      idx = 0;
    } else {
      idx++;
    }
    views.get(idx).setVisibility(VISIBLE);
    if (onChangeListener != null) {
      onChangeListener.onChange();
    }
    return Pair.create(indexId, idx);
  }

  public interface OnChangeListener {
    void onChange();
  }
}
