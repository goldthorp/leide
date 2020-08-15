package com.wisebison.leide.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.wisebison.leide.model.DiaryEntry;
import com.wisebison.leide.util.BackgroundUtil;

import java.util.List;

@Dao
public abstract class DiaryEntryDao {
  @Query("SELECT * FROM `diary-entry` ORDER BY start_timestamp DESC")
  public abstract LiveData<List<DiaryEntry>> getList();

  @Query("SELECT COUNT(*) FROM `diary-entry`")
  public abstract LiveData<Long> getCount();

  @Query("SELECT location FROM `diary-entry` GROUP BY location ORDER BY id DESC LIMIT 5")
  public abstract LiveData<List<String>> getRecentLocations();

  @Query("SELECT * FROM `diary-entry` WHERE id = :entryId")
  abstract DiaryEntry _getById(long entryId);

  public BackgroundUtil.Background<DiaryEntry> getById(final long entryId) {
    return BackgroundUtil.doInBackground(() -> _getById(entryId));
  }

  @Query("SELECT * FROM `diary-entry` WHERE start_timestamp IN " +
    "(SELECT MAX(start_timestamp) FROM `diary-entry` WHERE start_timestamp < :startTimestamp)")
  abstract DiaryEntry _getPrevious(long startTimestamp);

  public BackgroundUtil.Background<DiaryEntry> getPrevious(final long startTimestamp) {
    return BackgroundUtil.doInBackground(() -> _getPrevious(startTimestamp));
  }

  @Query("SELECT * FROM `diary-entry` WHERE start_timestamp IN " +
    "(SELECT MIN(start_timestamp) FROM `diary-entry` WHERE start_timestamp > :startTimestamp)")
  abstract DiaryEntry _getNext(long startTimestamp);

  public BackgroundUtil.Background<DiaryEntry> getNext(final long startTimestamp) {
    return BackgroundUtil.doInBackground(() -> _getNext(startTimestamp));
  }

  @Insert
  public abstract void insert(DiaryEntry entry);
}
