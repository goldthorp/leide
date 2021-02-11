package com.wisebison.leide.data;

import androidx.room.Dao;
import androidx.room.Insert;

import com.wisebison.leide.model.EntryComponentValue;

@Dao
public interface EntryComponentValueDao {
  @Insert
  long insert(EntryComponentValue value);
}
