package com.wisebison.leide.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.wisebison.leide.model.NamedEntity;
import com.wisebison.leide.model.NamedEntityForm;

import java.util.Collection;
import java.util.List;

@Dao
public interface NamedEntityDao {
  @Query("SELECT * FROM `named_entity`")
  LiveData<List<NamedEntity>> getAll();

  @Query("SELECT COUNT(*) FROM `named_entity`")
  LiveData<Long> getCount();

  @Query("SELECT name, count(*) FROM `named_entity` GROUP BY name ORDER BY count(*) DESC, name ASC")
  List<NamedEntityForm> countEntitiesByName();

  @Query("SELECT ne.name, count(*) FROM `named_entity` ne " +
      "LEFT JOIN `entry` e ON ne.entry_fk = e.id " +
      "WHERE e.start_timestamp >= :startMillis AND e.start_timestamp < :endMillis " +
      "GROUP BY name ORDER BY count(*) DESC, name ASC")
  List<NamedEntityForm> countEntitiesByName(long startMillis, long endMillis);

  @Insert
  void insertAll(Collection<NamedEntity> namedEntities);

  @Query("DELETE FROM `named_entity`")
  void deleteAll();

  @Query("DELETE FROM `named_entity` WHERE entry_fk = :entryId")
  void deleteByEntryId(Long entryId);
}
