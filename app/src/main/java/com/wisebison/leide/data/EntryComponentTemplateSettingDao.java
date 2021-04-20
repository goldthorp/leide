package com.wisebison.leide.data;

import androidx.room.Dao;
import androidx.room.Insert;

import com.wisebison.leide.model.EntryComponentSetting;

@Dao
public interface EntryComponentTemplateSettingDao {
  @Insert
  long insert(EntryComponentSetting setting);
}
