package com.wisebison.leide.cloud;

import android.util.Log;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.language.v1.CloudNaturalLanguageScopes;
import com.wisebison.leide.R;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.DiaryEntryDao;
import com.wisebison.leide.model.DiaryEntry;
import com.wisebison.leide.view.MainActivity;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class AnalyzeUtil {

  private static final String TAG = "AnalyzeUtil";

  private final MainActivity mainActivity;
  private final AppDatabase db;
  private final DiaryEntryDao entryDao;

  private boolean running;
  private boolean analysisInProgress;
  private boolean analysisQueued;

  private static AnalyzeUtil INSTANCE;

  public static AnalyzeUtil getInstance(final MainActivity mainActivity) {
    if (INSTANCE == null) {
      INSTANCE = new AnalyzeUtil(mainActivity);
    }
    return INSTANCE;
  }

  private AnalyzeUtil(final MainActivity mainActivity) {
    this.mainActivity = mainActivity;
    db = AppDatabase.getInstance(mainActivity);
    entryDao = db.getDiaryEntryDao();
  }

  /**
   * Subscribe to the entries table and query the Google Cloud Natural Language API to analyze any
   * entries that haven't been analyzed yet. Query is made on a background thread and results are
   * saved to the local database.
   */
  public void start() {
    if (running) {
      analyze();
      return;
    }
    running = true;

    Log.d(TAG, "subscribing");
    // Subscribe to entries that haven't been analyzed yet
    entryDao.getAllUnanalyzed().observe(mainActivity, entries -> {
      Log.d(TAG, "entries changed");
        analyze(entries);
    });
  }

  private void analyze() {
    entryDao.getAllUnanalyzedOnce().then(this::analyze);
  }

  private void analyze(final List<DiaryEntry> entries) {
    if (!mainActivity.billingUtil.hasPremium()) {
      Log.d(TAG, "user is not subscribed to premium");
      return;
    }
    if (analysisInProgress) {
      Log.e(TAG, "attempting concurrent analysis");
      analysisQueued = true;
      return;
    }
    if (CollectionUtils.isNotEmpty(entries)) {
      // Snackbar shows progress at the bottom of screen while analysis is in progress
      final ConstraintLayout constraintLayout = mainActivity.findViewById(R.id.constraint_layout);
      final Snackbar snackbar  = Snackbar.make(constraintLayout,
        mainActivity.getString(R.string.analysis_progress, 0), Snackbar.LENGTH_INDEFINITE);
      // The text of the snackbar to update when percentage changes
      final TextView snackbarTextView =
        snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);

      snackbar.show();
      // Prevent concurrent analysis
      analysisInProgress = true;

      final AnalyzeTask.Callbacks taskCallbacks = new AnalyzeTask.Callbacks() {
        @Override
        public void resolve() {
          analysisInProgress = false;
          if (analysisQueued) {
            analyze();
            analysisQueued = false;
          }
          snackbar.dismiss();
        }

        @Override
        public void updateProgress(final int analyzedCount) {
          final int progress = analyzedCount * 100 / entries.size();
          snackbarTextView.setText(mainActivity.getString(R.string.analysis_progress, progress));
        }
      };
      // Get access token to query the api
      ApiUtils.getAccessToken(mainActivity, token -> {
        // Get the credential using the access token.
        final GoogleCredential credential = new GoogleCredential()
          .setAccessToken(token)
          .createScoped(CloudNaturalLanguageScopes.all());
        new AnalyzeTask(taskCallbacks, db, credential, mainActivity.billingUtil)
          .execute(entries.toArray(new DiaryEntry[0]));
      });
    }
  }
}
