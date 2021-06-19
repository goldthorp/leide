package com.wisebison.leide.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.wisebison.leide.model.EntryComponent;
import com.wisebison.leide.model.EntryComponentForm;
import com.wisebison.leide.model.EntryComponentType;
import com.wisebison.leide.model.EntryComponentValue;
import com.wisebison.leide.util.BackgroundUtil;

import java.util.Collection;
import java.util.List;

@Dao
public abstract class EntryComponentDao {
  final EntryComponentValueDao entryComponentValueDao;
  public EntryComponentDao(final AppDatabase db) {
    entryComponentValueDao = db.getEntryComponentValueDao();
  }

  @Query("SELECT * FROM `entry_component` ORDER BY entry_fk ASC, list_seq ASC")
  abstract List<EntryComponent> _getAll();

  public BackgroundUtil.Background<List<EntryComponent>> getAll() {
    return BackgroundUtil.doInBackground(this::_getAll);
  }

  @Query("SELECT * FROM entry_component WHERE entry_fk = :entryId AND type = :type")
  public abstract List<EntryComponent> getComponentsByEntryId(
    long entryId, @TypeConverters(EntryComponentType.class) EntryComponentType type);

  @Query("SELECT * FROM entry_component WHERE type = 0 AND (entities_analyzed = 0 OR " +
    "sentiment_analyzed = 0)")
  public abstract LiveData<List<EntryComponentForm>> getAllUnanalyzed();

  @Query("SELECT * FROM entry_component WHERE type = 0 AND (entities_analyzed = 0 OR " +
    "sentiment_analyzed = 0)")
  abstract List<EntryComponentForm> _getAllUnanalyzedOnce();

  public BackgroundUtil.Background<List<EntryComponentForm>> getAllUnanalyzedOnce() {
    return BackgroundUtil.doInBackground(this::_getAllUnanalyzedOnce);
  }

  @Query("SELECT value FROM `entry_component` ec LEFT JOIN `entry_component_value` ecv ON " +
    "`entry_component_fk` = ec.id WHERE type = 2 AND  ecv.name = 'display' " +
    "GROUP BY value ORDER BY MAX(ec.id) DESC LIMIT 5")
  abstract List<String> _getRecentLocations();

  public BackgroundUtil.Background<List<String>> getRecentLocations() {
    return BackgroundUtil.doInBackground(this::_getRecentLocations);
  }

  @Update
  public abstract void update(EntryComponent... components);

  @Query("UPDATE entry_component SET entities_analyzed = 1 WHERE id IN (:componentIds)")
  public abstract void markAsEntitiesAnalyzed(Collection<Long> componentIds);

  @Query("UPDATE entry_component SET sentiment_analyzed = 1 WHERE id IN (:componentIds)")
  public abstract void markAsSentimentAnalyzed(Collection<Long> componentIds);

  public void insert(final EntryComponent component) {
    final long id = _insert(component);
    for (final EntryComponentValue value: component.getValues()) {
      value.setComponentId(id);
      entryComponentValueDao.insert(value);
    }
  }

  @Insert
  abstract long _insert(EntryComponent component);
}
