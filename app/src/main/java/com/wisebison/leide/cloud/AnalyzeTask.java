package com.wisebison.leide.cloud;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.language.v1.CloudNaturalLanguage;
import com.google.api.services.language.v1.model.AnalyzeEntitiesRequest;
import com.google.api.services.language.v1.model.AnalyzeEntitiesResponse;
import com.google.api.services.language.v1.model.AnalyzeSentimentRequest;
import com.google.api.services.language.v1.model.AnalyzeSentimentResponse;
import com.google.api.services.language.v1.model.Document;
import com.google.api.services.language.v1.model.Entity;
import com.google.api.services.language.v1.model.EntityMention;
import com.google.api.services.language.v1.model.Sentence;
import com.google.api.services.language.v1.model.Sentiment;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.DiaryEntryDao;
import com.wisebison.leide.data.DiaryNamedEntityDao;
import com.wisebison.leide.data.DiarySentimentDao;
import com.wisebison.leide.model.DiaryEntry;
import com.wisebison.leide.model.DiaryNamedEntity;
import com.wisebison.leide.model.DiarySentiment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.EqualsAndHashCode;

/**
 * Queries the GCNLAPI and saves the results in the database.
 */
class AnalyzeTask extends AsyncTask<DiaryEntry, Integer, Void> {

  private static final String TAG = "AnalyzeTask";

  /**
   * Call resolve() to inform the caller that analysis of the specified entries is complete
   * and the resulting NamedEntities have been saved in the database.
   * Call updateProgress(int) to inform the caller of how many entities have been analyzed.
   */
  private final Callbacks callbacks;

  /**
   * For marking entries as analyzed.
   */
  private final DiaryEntryDao entryDao;

  /**
   * For saving the results of the named entity queries.
   */
  private final DiaryNamedEntityDao namedEntityDao;

  /**
   * For saving the results of the sentiment queries.
   */
  private final DiarySentimentDao sentimentDao;

  private final boolean hasEntitiesModule;

  private final boolean hasSentimentModule;

  /**
   * The API.
   */
  private final CloudNaturalLanguage api;

  AnalyzeTask(final Callbacks callbacks, final AppDatabase db,
              final GoogleCredential credential, final boolean hasEntitiesModule,
              final boolean hasSentimentModule) {
    this.callbacks = callbacks;
    entryDao = db.getDiaryEntryDao();
    namedEntityDao = db.getDiaryNamedEntityDao();
    sentimentDao = db.getDiarySentimentDao();
    this.hasEntitiesModule = hasEntitiesModule;
    this.hasSentimentModule = hasSentimentModule;
    api = new CloudNaturalLanguage.Builder(
        new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential).build();
  }

  @Override
  protected Void doInBackground(final DiaryEntry... entries) {
    // AnalyzeUtil the specified entries, save the results, and mark the entries as analyzed.
    int analyzedCount = 0;
    for (final DiaryEntry entry : entries) {
      if (!entry.isEntitiesAnalyzed() && hasEntitiesModule) {
        try {
          // Perform the query.
          final Collection<DiaryNamedEntity> entryEntities = requestNamedEntities(entry);
          // Save the entities.
          namedEntityDao.insertAll(entryEntities);
          // Mark this entry as analyzed.
          entry.setEntitiesAnalyzed(true);
        } catch (final IOException e) {
          Log.e(TAG, "failed to analyze entities", e);
        }
      }
      if (!entry.isSentimentAnalyzed() && hasSentimentModule) {
        try {
          // Perform the query
          final Collection<DiarySentiment> sentiments = requestSentiment(entry);
          // Save the sentiment
          sentimentDao.insertAll(sentiments);
          // Mark this entry as analyzed
          entry.setSentimentAnalyzed(true);
        } catch (final IOException e) {
          Log.e(TAG, "failed to analyze sentiment", e);
        }
      }
      // Notify UI of number of entries analyzed
      publishProgress(++analyzedCount);
    }

    // Save all entries to mark them as analyzed
    Log.d(TAG, "updating entries");
    entryDao.update(entries);
    return null;
  }

  /**
   * Query the GCNLAPI for named entities and create DiaryNamedEntity objects from the results.
   *
   * @param entry to analyze the text of
   * @return the named entities from the entry's text
   * @throws IOException querying the GCNLAPI
   */
  private Collection<DiaryNamedEntity> requestNamedEntities(final DiaryEntry entry) throws IOException {
    // Query the API.
    final AnalyzeEntitiesResponse entitiesResponse =
        api.documents().analyzeEntities(new AnalyzeEntitiesRequest()
            .setDocument(new Document()
                .setContent(entry.getText())
                .setType("PLAIN_TEXT"))
            .setEncodingType("Utf16")).execute();
    // Convert the results to DiaryNamedEntity objects.
    final List<Entity> entities = entitiesResponse.getEntities();
    // Use a map for the entities to save to prevent duplicates. The API can count the same entity
    // more than once if it has multiple types (e.g. 1975 is both NUMBER and DATE)
    final Map<DiaryNamedEntityKey, DiaryNamedEntity> namedEntities = new HashMap<>();
    for (final Entity entity : entities) {
      // Iterate over all mentions of this entity in the entry
      for (final EntityMention mention : entity.getMentions()) {
        final DiaryNamedEntity namedEntity = new DiaryNamedEntity();
        namedEntity.setEntryId(entry.getId());
        namedEntity.setName(entity.getName());
        namedEntity.setType(entity.getType());
        namedEntity.setSalience(entity.getSalience());
        namedEntity.setBeginOffset(mention.getText().getBeginOffset());
        namedEntity.setContent(mention.getText().getContent());
        // Before adding, see if this entity already exists. If so, just append the type onto the
        // existing entity's type, separated by a comma
        final DiaryNamedEntityKey key = new DiaryNamedEntityKey(namedEntity, mention);
        final DiaryNamedEntity existingEntity = namedEntities.get(key);
        if (existingEntity == null) {
          namedEntities.put(key, namedEntity);
        } else {
          existingEntity.setType(existingEntity.getType() + "," + namedEntity.getType());
        }
      }
    }
    return namedEntities.values();
  }

  @EqualsAndHashCode
  private class DiaryNamedEntityKey {
    Long entryId;
    String name;
    int beginOffset;
    private DiaryNamedEntityKey(final DiaryNamedEntity entity, final EntityMention mention) {
      entryId = entity.getEntryId();
      name = entity.getName();
      beginOffset = mention.getText().getBeginOffset();
    }
  }

  /**
   * Query the GCNLAPI for sentiment and create a Sentiment object from the results.
   *
   * @param entry to analyze the text of
   * @return the sentiment of the text for the entry
   * @throws IOException querying the GCNLAPI
   */
  private List<DiarySentiment> requestSentiment(final DiaryEntry entry) throws IOException {
    final AnalyzeSentimentResponse sentimentResponse =
        api.documents().analyzeSentiment(new AnalyzeSentimentRequest()
          .setDocument(new Document()
            .setContent(entry.getText())
            .setType("PLAIN_TEXT"))
          .setEncodingType("Utf16")).execute();
    // Convert the results to Sentiment objects
    final List<DiarySentiment> results = new ArrayList<>();
    final Sentiment documentSentiment = sentimentResponse.getDocumentSentiment();
    final DiarySentiment sentiment = new DiarySentiment();
    sentiment.setEntryId(entry.getId());
    sentiment.setScore(documentSentiment.getScore());
    sentiment.setMagnitude(documentSentiment.getMagnitude());
    results.add(sentiment);
    for (final Sentence sentence : sentimentResponse.getSentences()) {
      final DiarySentiment sentenceSentiment = new DiarySentiment();
      sentenceSentiment.setEntryId(entry.getId());
      sentenceSentiment.setScore(sentence.getSentiment().getScore());
      sentenceSentiment.setMagnitude(sentence.getSentiment().getMagnitude());
      final Integer beginOffset = sentence.getText().getBeginOffset();
      sentenceSentiment.setSentenceBeginOffset(beginOffset);
      sentenceSentiment.setSentenceEndOffset(
        beginOffset + sentence.getText().getContent().length());
      results.add(sentenceSentiment);
    }
    return results;
  }

  @Override
  protected void onProgressUpdate(final Integer... values) {
    callbacks.updateProgress(values[0]);
  }

  @Override
  protected void onPostExecute(final Void aVoid) {
    // Inform the caller that the task is complete
    Log.d(TAG, "resolving");
    callbacks.resolve();
  }

  public interface Callbacks {
    void resolve();
    void updateProgress(int analyzedCount);
  }
}
