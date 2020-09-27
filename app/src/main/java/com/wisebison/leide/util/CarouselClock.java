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
  private final Handler handler;
  private final Map<String, Integer> indexes;
  private CarouselClock(final int timeoutMillis) {
    actions = new ArrayList<>();
    indexes = new HashMap<>();
    handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        for (final Action action : actions) {
          final Pair<String, Integer> indexIdAndIndex = action.action();
          indexes.put(indexIdAndIndex.first, indexIdAndIndex.second);
        }
        handler.postDelayed(this, timeoutMillis);
      }
    }, timeoutMillis);
  }

  public static CarouselClock getInstance(final int timeoutMillis) {
    if (INSTANCE == null) {
      INSTANCE = new CarouselClock(timeoutMillis);
    }
    return INSTANCE;
  }

  public void addAction(final Action action, final String indexId) {
    actions.add(action);
    if (StringUtils.isNotBlank(indexId) && !indexes.containsKey(indexId)) {
      indexes.put(indexId, 0);
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
    }
  }

  public interface Action {
    Pair<String, Integer> action();
  }
}
