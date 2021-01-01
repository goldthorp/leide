package com.wisebison.leide.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.wisebison.leide.model.EntryComponent;
import com.wisebison.leide.model.EntryComponentType;
import com.wisebison.leide.util.BackgroundUtil;

import java.util.List;

@Dao
public abstract class EntryComponentDao {
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
  public abstract LiveData<List<EntryComponent>> getAllUnanalyzed();

  @Query("SELECT * FROM entry_component WHERE type = 0 AND entities_analyzed = 0 OR " +
    "sentiment_analyzed = 0")
  abstract List<EntryComponent> _getAllUnanalyzedOnce();

  public BackgroundUtil.Background<List<EntryComponent>> getAllUnanalyzedOnce() {
    return BackgroundUtil.doInBackground(this::_getAllUnanalyzedOnce);
  }

  @Query("SELECT value FROM `entry_component` WHERE type = 3 " +
    "GROUP BY value ORDER BY MAX(id) DESC LIMIT 5")
  abstract List<String> _getRecentLocations();

  public BackgroundUtil.Background<List<String>> getRecentLocations() {
    return BackgroundUtil.doInBackground(this::_getRecentLocations);
  }

  @Update
  public abstract void update(EntryComponent... components);

  @Insert
  public abstract long insert(EntryComponent component);
}
