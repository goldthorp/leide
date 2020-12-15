package com.wisebison.leide.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.wisebison.leide.model.Sentiment;
import com.wisebison.leide.model.SentimentForm;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Dao
public abstract class SentimentDao {
  @Query("SELECT s.score, e.start_timestamp FROM `sentiment` s LEFT JOIN `entry` e " +
      "ON e.id = s.entry_fk WHERE s.sentence_begin_offset IS NULL ORDER BY e.start_timestamp")
  public abstract LiveData<List<SentimentForm>> findAll();

  @Insert
  public abstract void insert(Sentiment sentiment);

  @Insert
  public abstract void insertAll(Collection<Sentiment> sentiments);

  @Query("DELETE FROM `sentiment`")
  public abstract void deleteAll();

  @Query("DELETE FROM `sentiment` WHERE entry_fk = :entryId")
  public abstract void deleteByEntryId(Long entryId);

  @Query("SELECT COUNT(id) FROM `sentiment`")
  public abstract LiveData<Long> getCount();

  @Query("SELECT AVG(score) FROM `sentiment` s LEFT JOIN `entry` e " +
    "ON e.id = s.entry_fk WHERE s.sentence_begin_offset IS NULL " +
    "AND e.start_timestamp >= :startMillis AND e.start_timestamp < :endMillis")
  public abstract Float getAverageSentiment(long startMillis, long endMillis);

  @Query("SELECT MAX(s.score) FROM `sentiment` s LEFT JOIN `entry` e " +
    "ON e.id = s.entry_fk WHERE s.sentence_begin_offset IS NOT NULL " +
    "AND e.start_timestamp >= :startMillis AND e.start_timestamp < :endMillis")
  abstract Float getMaxScore(long startMillis, long endMillis);

  @Query("SELECT SUBSTR(e.text, s.sentence_begin_offset + 1, s.sentence_length) " +
    "FROM `sentiment` s LEFT JOIN `entry` e ON e.id = s.entry_fk " +
    "WHERE s.sentence_begin_offset IS NOT NULL AND e.start_timestamp >= :startMillis " +
    "AND e.start_timestamp < :endMillis AND s.score = :score")
  abstract List<String> getSentencesWithScore(long startMillis, long endMillis, float score);

  public List<String> getMostPositiveSentences(
    final long startMillis, final long endMillis) {
    final Float maxScore = getMaxScore(startMillis, endMillis);
    if (maxScore == null) {
      return Collections.emptyList();
    }
    return getSentencesWithScore(startMillis, endMillis, maxScore);
  }
}
