package com.wisebison.leide.cloud;

import android.util.Log;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.language.v1.CloudNaturalLanguageScopes;
import com.wisebison.leide.R;
import com.wisebison.leide.billing.BillingUtil;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.EntryComponentDao;
import com.wisebison.leide.model.EntryComponent;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AnalyzeFragment extends Fragment {

  public static final String TAG = AnalyzeFragment.class.getSimpleName();

  @Inject
  BillingUtil billingUtil;

  private AppDatabase db;
  private EntryComponentDao entryComponentDao;

  private boolean running;
  private boolean analysisInProgress;
  private boolean analysisQueued;

  /**
   * Subscribe to the entries table and query the Google Cloud Natural Language API to analyze any
   * entries that haven't been analyzed yet. Query is made on a background thread and results are
   * saved to the local database.
   */
  public void start() {
    db = AppDatabase.getInstance(getActivity());
    entryComponentDao = db.getEntryComponentDao();
    if (running) {
      analyze();
      return;
    }
    running = true;

    Log.d(TAG, "subscribing");
    // Subscribe to entries that haven't been analyzed yet
    entryComponentDao.getAllUnanalyzed().observe(requireActivity(), entries -> {
      Log.d(TAG, "unanalyzed components updated " + entries.size() + " " + entries);
      analyze(entries);
    });
  }

  private void analyze() {
    entryComponentDao.getAllUnanalyzedOnce().then(this::analyze);
  }

  private void analyze(final List<EntryComponent> entries) {
    billingUtil.hasPremium(hasPremium -> {
      if (!hasPremium) {
        Log.d(TAG, "user is not subscribed to premium");
        return;
      }
      if (analysisInProgress) {
        Log.e(TAG, "attempting concurrent analysis");
        analysisQueued = true;
        return;
      }
      if (CollectionUtils.isNotEmpty(entries)) {
        // TODO: move responsibility for displaying progress to the activity
        // Snackbar shows progress at the bottom of screen while analysis is in progress
        final ConstraintLayout constraintLayout = requireActivity().findViewById(R.id.constraint_layout);
        final Snackbar snackbar  = Snackbar.make(constraintLayout,
          requireActivity().getString(R.string.analysis_progress, 0), Snackbar.LENGTH_INDEFINITE);
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
            final FragmentActivity activity = getActivity();
            if (activity != null) {
              snackbarTextView.setText(activity.getString(R.string.analysis_progress, progress));
            }
          }
        };
        // Get access token to query the api
        ApiUtils.getAccessToken(requireContext(), token -> {
          // Get the credential using the access token.
          final GoogleCredential credential = new GoogleCredential()
            .setAccessToken(token)
            .createScoped(CloudNaturalLanguageScopes.all());
          new AnalyzeTask(taskCallbacks, db, credential).execute(entries.toArray(new EntryComponent[0]));
        });
      }
    });
  }
}
