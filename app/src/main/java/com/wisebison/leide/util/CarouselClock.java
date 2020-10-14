package com.wisebison.leide.util;

import android.os.Handler;
import android.util.Pair;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarouselClock {

  private static CarouselClock INSTANCE;

  private final List<Action> actions;
  private final int timeoutMillis;
  private final Handler handler;
  private boolean handlerHasCallbacks = false;
  private final Map<String, Integer> indexes;
  private CarouselClock(final int timeoutMillis) {
    actions = new ArrayList<>();
    this.timeoutMillis = timeoutMillis;
    indexes = new HashMap<>();
    handler = new Handler();
  }

  public static CarouselClock getInstance(final int timeoutMillis) {
    if (INSTANCE == null) {
      INSTANCE = new CarouselClock(timeoutMillis);
    }
    return INSTANCE;
  }

  private Runnable getHandlerCallback() {
    return new Runnable() {
      @Override
      public void run() {
        for (final Action action : actions) {
          final Pair<String, Integer> indexIdAndIndex = action.action();
          indexes.put(indexIdAndIndex.first, indexIdAndIndex.second);
        }
        handler.postDelayed(this, timeoutMillis);
      }
    };
  }

  public void addAction(final Action action, final String indexId) {
    actions.add(action);
    if (StringUtils.isNotBlank(indexId) && !indexes.containsKey(indexId)) {
      indexes.put(indexId, 0);
    }
    if (!handlerHasCallbacks) {
      handler.postDelayed(getHandlerCallback(), timeoutMillis);
      handlerHasCallbacks = true;
    }
  }

  public int getIndex(final String indexId) {
    final Integer index = indexes.get(indexId);
    return index != null ? index : 0;
  }

  public void removeAction(final Action action) {
    actions.remove(action);
    if (CollectionUtils.isEmpty(actions)) {
      handler.removeCallbacksAndMessages(null);
      handlerHasCallbacks = false;
    }
  }

  public interface Action {
    Pair<String, Integer> action();
  }
}
