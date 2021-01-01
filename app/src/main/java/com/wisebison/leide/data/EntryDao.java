package com.wisebison.leide.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.wisebison.leide.model.Entry;
import com.wisebison.leide.model.EntryComponent;
import com.wisebison.leide.model.EntryForm;
import com.wisebison.leide.util.BackgroundUtil;

import java.util.List;

@Dao
public abstract class EntryDao {

  final EntryComponentDao entryComponentDao;
  public EntryDao(final AppDatabase db) {
    entryComponentDao = db.getEntryComponentDao();
  }

  @Transaction
  @Query("SELECT e.id, e.start_timestamp, s.score " +
    "FROM `entry` e LEFT JOIN `sentiment` s ON e.id = s.entry_fk " +
    "WHERE s.sentence_begin_offset IS NULL ORDER BY e.start_timestamp DESC")
  public abstract LiveData<List<EntryForm>> getList();

  @Query("SELECT COUNT(*) FROM `entry`")
  public abstract LiveData<Long> getCount();

  @Query("SELECT * FROM `entry` WHERE id = :entryId")
  abstract EntryForm _getById(long entryId);

  public BackgroundUtil.Background<EntryForm> getById(final long entryId) {
    return BackgroundUtil.doInBackground(() -> _getById(entryId));
  }

  @Query("SELECT * FROM `entry` WHERE start_timestamp IN " +
    "(SELECT MAX(start_timestamp) FROM `entry` WHERE start_timestamp < :startTimestamp)")
  abstract Entry _getPrevious(long startTimestamp);

  public BackgroundUtil.Background<Entry> getPrevious(final long startTimestamp) {
    return BackgroundUtil.doInBackground(() -> _getPrevious(startTimestamp));
  }

  @Query("SELECT * FROM `entry` WHERE start_timestamp IN " +
    "(SELECT MIN(start_timestamp) FROM `entry` WHERE start_timestamp > :startTimestamp)")
  abstract Entry _getNext(long startTimestamp);

  public BackgroundUtil.Background<Entry> getNext(final long startTimestamp) {
    return BackgroundUtil.doInBackground(() -> _getNext(startTimestamp));
  }

  @Query("SELECT MIN(start_timestamp) FROM `entry`")
  abstract Long _getEarliestTimestamp();

  public BackgroundUtil.Background<Long> getEarliestTimestamp() {
    return BackgroundUtil.doInBackground(this::_getEarliestTimestamp);
  }

  @Query("SELECT * FROM `entry`")
  abstract List<Entry> _getAll();

  public BackgroundUtil.Background<List<Entry>> getAll() {
    return BackgroundUtil.doInBackground(this::_getAll);
  }

  @Insert
  abstract long _insert(Entry entry);

  public void insert(final Entry entry) {
    final long id = _insert(entry);
    for (final EntryComponent component : entry.getComponents()) {
      component.setEntryId(id);
      entryComponentDao.insert(component);
    }
  }

  @Update
  public abstract void update(Entry... entries);
}
