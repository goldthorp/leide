package com.wisebison.leide.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.wisebison.leide.model.EntryComponentTemplate;

import java.util.List;

@Dao
public interface EntryComponentTemplateDao {
  @Query("SELECT * FROM entry_component_template")
  LiveData<List<EntryComponentTemplate>> getAll();
}
