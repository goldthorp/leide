package com.goldthorp.wisebison.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundUtil {
  /**
   * Perform an action on a background thread immediately with no callback.
   *
   * @param action to perform on background thread
   */
  public static void doInBackgroundNow(final Runnable action) {
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.execute(action);
  }

  /**
   * Perform an action on a background thread with a callback that will run on the UI thread.
   *
   * The action will not be performed until the callback is provided
   * (i.e. `BackgroundUtil.doInBackground(<action>);` does not do anything, you must do
   * `BackgroundUtil.doInBackground(<action>).then(<callback>);` for the action to be performed.
   * For actions that don't require a callback, use doInBackgroundNow).
   *
   * The return value of the action will be passed to the callback.
   *
   * @param action to perform on background thread
   * @param <T>    return type of action
   * @return Background object to provide callback via `then` method
   */
  public static <T> Background<T> doInBackground(final RunInBackgroundTask.Action<T> action) {
    return new Background<>(action);
  }

  /**
   * This is a holder for a RunInBackgroundTask that allows it to be used with JS Promise-like
   * syntax via the `then` method. The difference is that the async action is not actually
   * performed until `then` is called.
   *
   * @param <T>
   */
  public static class Background<T> {
    RunInBackgroundTask<T> task;

    public Background(final RunInBackgroundTask.Action<T> action) {
      task = new RunInBackgroundTask<>(action);
    }

    public void then(final RunInBackgroundTask.Callback<T> callback) {
      task.setCallback(callback);
      task.execute();
    }
  }
}
