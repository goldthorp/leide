package com.wisebison.leide.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.wisebison.leide.model.DiarySentiment;
import com.wisebison.leide.model.DiarySentimentForm;

import java.util.Collection;
import java.util.List;

@Dao
public interface DiarySentimentDao {
  @Query("SELECT s.score, e.start_timestamp FROM `diary-sentiment` s LEFT JOIN `diary-entry` e " +
      "ON e.id = s.entry_fk WHERE s.sentence_begin_offset IS NULL ORDER BY e.start_timestamp")
  LiveData<List<DiarySentimentForm>> findAll();

  @Insert
  void insert(DiarySentiment sentiment);

  @Insert
  void insertAll(Collection<DiarySentiment> sentiments);

  @Query("DELETE FROM `diary-sentiment`")
  void deleteAll();

  @Query("DELETE FROM `diary-sentiment` WHERE entry_fk = :entryId")
  void deleteByEntryId(Long entryId);
}
