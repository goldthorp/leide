package com.wisebison.leide.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.wisebison.leide.model.DiaryNamedEntity;
import com.wisebison.leide.model.DiaryNamedEntityForm;

import java.util.Collection;
import java.util.List;

@Dao
public interface DiaryNamedEntityDao {
  @Query("SELECT * FROM `diary-named-entity`")
  LiveData<List<DiaryNamedEntity>> getAll();

  @Query("SELECT COUNT(*) FROM `diary-named-entity`")
  LiveData<Long> getCount();

  @Query("SELECT name, count(*) FROM `diary-named-entity` GROUP BY name ORDER BY count(*) DESC, name ASC")
  List<DiaryNamedEntityForm> countEntitiesByName();

  @Query("SELECT ne.name, count(*) FROM `diary-named-entity` ne " +
      "LEFT JOIN `diary-entry` e ON ne.entry_fk = e.id " +
      "WHERE e.start_timestamp >= :startMillis AND e.start_timestamp < :endMillis " +
      "GROUP BY name ORDER BY count(*) DESC, name ASC")
  List<DiaryNamedEntityForm> countEntitiesByName(long startMillis, long endMillis);

  @Insert
  void insertAll(Collection<DiaryNamedEntity> namedEntities);

  @Query("DELETE FROM `diary-named-entity`")
  void deleteAll();

  @Query("DELETE FROM `diary-named-entity` WHERE entry_fk = :entryId")
  void deleteByEntryId(Long entryId);
}
