package com.wisebison.leide.cloud;

import android.os.AsyncTask;
import android.util.Log;
import androidx.room.Transaction;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
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
import com.google.common.collect.Lists;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.EntryComponentDao;
import com.wisebison.leide.data.NamedEntityDao;
import com.wisebison.leide.data.SentimentDao;
import com.wisebison.leide.model.EntryComponentForm;
import com.wisebison.leide.model.NamedEntity;
import com.wisebison.leide.model.Sentiment;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Queries the GCNLAPI and saves the results in the database.
 */
class AnalyzeTask extends AsyncTask<EntryComponentForm, Integer, Void> {

  private static final String TAG = "AnalyzeTask";

  /**
   * Call resolve() to inform the caller that analysis of the specified entries is complete
   * and the resulting DiaryNamedEntities/DiarySentiments have been saved in the database.
   * Call updateProgress(int) to inform the caller of how many entities have been analyzed.
   */
  private final Callbacks callbacks;

  /**
   * For marking entries as analyzed.
   */
  private final EntryComponentDao entryComponentDao;

  /**
   * For saving the results of the named entity queries.
   */
  private final NamedEntityDao namedEntityDao;

  /**
   * For saving the results of the sentiment queries.
   */
  private final SentimentDao sentimentDao;

  /**
   * The API.
   */
  private final CloudNaturalLanguage api;

  AnalyzeTask(final Callbacks callbacks, final AppDatabase db,
              final GoogleCredential credential) {
    this.callbacks = callbacks;
    entryComponentDao = db.getEntryComponentDao();
    namedEntityDao = db.getNamedEntityDao();
    sentimentDao = db.getSentimentDao();
    api = new CloudNaturalLanguage.Builder(
        new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential).build();
  }

  @Transaction
  @Override
  protected Void doInBackground(final EntryComponentForm... components) {
    // Analyze the specified components, save the results, and mark the components as analyzed.
    int analyzedCount = 0;
    List<List<EntryComponentForm>> componentPartitions = Lists.partition(Arrays.asList(components), 100);
    for (List<EntryComponentForm> partition : componentPartitions) {
      final Collection<NamedEntity> entities = new ArrayList<>();
      final Collection<Sentiment> sentiments = new ArrayList<>();
      final Set<Long> entitiesAnalyzedIds = new HashSet<>();
      final Set<Long> sentimentAnalyzedIds = new HashSet<>();
      for (final EntryComponentForm component : partition) {
        if (!component.isEntitiesAnalyzed()) {
          try {
            if (CollectionUtils.isNotEmpty(component.getValues())) {
              // Perform the query.
              entities.addAll(requestNamedEntities(component));
              // Mark this entry as analyzed.
              entitiesAnalyzedIds.add(component.getId());
            }
          } catch (final IOException e) {
            if (e instanceof GoogleJsonResponseException) {
              entitiesAnalyzedIds.add(component.getId());
            }
            Log.e(TAG, "failed to analyze entities", e);
          }
        }
        if (!component.isSentimentAnalyzed()) {
          try {
            if (CollectionUtils.isNotEmpty(component.getValues())) {
              // Perform the query
              sentiments.addAll(requestSentiment(component));
              // Mark this entry as analyzed
              sentimentAnalyzedIds.add(component.getId());
            }
          } catch (final IOException e) {
            if (e instanceof GoogleJsonResponseException) {
              sentimentAnalyzedIds.add(component.getId());
            }
            Log.e(TAG, "failed to analyze sentiment", e);
          }
        }
        // Notify UI of number of components analyzed
        publishProgress(++analyzedCount);
      }

      if (CollectionUtils.isNotEmpty(entities)) {
        namedEntityDao.insertAll(entities);
      }
      if (CollectionUtils.isNotEmpty(sentiments)) {
        sentimentDao.insertAll(sentiments);
      }

      // Save all components to mark them as analyzed
      Log.d(TAG, "updating components");
      entryComponentDao.markAsEntitiesAnalyzed(entitiesAnalyzedIds);
      entryComponentDao.markAsSentimentAnalyzed(sentimentAnalyzedIds);
    }
    return null;
  }

  /**
   * Query the GCNLAPI for named entities and create DiaryNamedEntity objects from the results.
   *
   * @param entryComponent to analyze the text of
   * @return the named entities from the entry's text
   * @throws IOException querying the GCNLAPI
   */
  private Collection<NamedEntity> requestNamedEntities(final EntryComponentForm entryComponent) throws IOException {
    // Query the API.
    final AnalyzeEntitiesResponse entitiesResponse =
        api.documents().analyzeEntities(new AnalyzeEntitiesRequest()
            .setDocument(new Document()
                .setContent(entryComponent.getValues().get(0).getValue())
                .setType("PLAIN_TEXT"))
            .setEncodingType("Utf16")).execute();
    // Convert the results to DiaryNamedEntity objects.
    final List<Entity> entities = entitiesResponse.getEntities();
    // Use a map for the entities to save to prevent duplicates. The API can count the same entity
    // more than once if it has multiple types (e.g. 1975 is both NUMBER and DATE)
    final Map<NamedEntityKey, NamedEntity> namedEntities = new HashMap<>();
    for (final Entity entity : entities) {
      // Iterate over all mentions of this entity in the entry
      for (final EntityMention mention : entity.getMentions()) {
        final NamedEntity namedEntity = new NamedEntity();
        namedEntity.setEntryId(entryComponent.getEntryId());
        namedEntity.setEntryComponentId(entryComponent.getId());
        namedEntity.setName(entity.getName());
        namedEntity.setType(entity.getType());
        namedEntity.setSalience(entity.getSalience());
        namedEntity.setBeginOffset(mention.getText().getBeginOffset());
        namedEntity.setContent(mention.getText().getContent());
        // Before adding, see if this entity already exists. If so, just append the type onto the
        // existing entity's type, separated by a comma
        final NamedEntityKey key = new NamedEntityKey(namedEntity, mention);
        final NamedEntity existingEntity = namedEntities.get(key);
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
  private class NamedEntityKey {
    Long entryComponentId;
    String name;
    int beginOffset;
    private NamedEntityKey(final NamedEntity entity, final EntityMention mention) {
      entryComponentId = entity.getEntryComponentId();
      name = entity.getName();
      beginOffset = mention.getText().getBeginOffset();
    }
  }

  /**
   * Query the GCNLAPI for sentiment and create DiarySentiment objects from the results.
   *
   * @param entryComponent to analyze the text of
   * @return the sentiment of the text for the entry
   * @throws IOException querying the GCNLAPI
   */
  private List<Sentiment> requestSentiment(
    final EntryComponentForm entryComponent) throws IOException {
    final AnalyzeSentimentResponse sentimentResponse =
        api.documents().analyzeSentiment(new AnalyzeSentimentRequest()
          .setDocument(new Document()
            .setContent(entryComponent.getValues().get(0).getValue())
            .setType("PLAIN_TEXT"))
          .setEncodingType("Utf16")).execute();
    // Convert the results to Sentiment objects
    final List<Sentiment> results = new ArrayList<>();
    final com.google.api.services.language.v1.model.Sentiment documentSentiment = sentimentResponse.getDocumentSentiment();
    final Sentiment sentiment = new Sentiment();
    sentiment.setEntryId(entryComponent.getEntryId());
    sentiment.setEntryComponentId(entryComponent.getId());
    sentiment.setScore(documentSentiment.getScore());
    sentiment.setMagnitude(documentSentiment.getMagnitude());
    results.add(sentiment);
    for (final Sentence sentence : sentimentResponse.getSentences()) {
      final Sentiment sentenceSentiment = new Sentiment();
      sentenceSentiment.setEntryId(entryComponent.getEntryId());
      sentenceSentiment.setEntryComponentId(entryComponent.getId());
      sentenceSentiment.setScore(sentence.getSentiment().getScore());
      sentenceSentiment.setMagnitude(sentence.getSentiment().getMagnitude());
      final Integer beginOffset = sentence.getText().getBeginOffset();
      sentenceSentiment.setSentenceBeginOffset(beginOffset);
      sentenceSentiment.setSentenceLength(sentence.getText().getContent().length());
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
