package com.wisebison.leide.util;

import android.os.AsyncTask;

import lombok.Setter;

/**
 * Convenience class to call a method on a background thread and pass its return value to the
 * specified callback on the UI thread.
 *
 * @param <T> return type of the specified method
 */
public class RunInBackgroundTask<T> extends AsyncTask<Void, Void, T> {

  private final Action<T> action;

  @Setter
  private Callback<T> callback;

  RunInBackgroundTask(final Action<T> action) {
    this.action = action;
  }

  @Override
  protected T doInBackground(final Void... voids) {
    return action.run();
  }

  @Override
  protected void onPostExecute(final T result) {
    callback.resolve(result);
  }

  public interface Action<T> {
    T run();
  }

  public interface Callback<T> {
    void resolve(T result);
  }
}
