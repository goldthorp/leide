package com.wisebison.leide.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.wisebison.leide.model.EntryComponentSetting;
import com.wisebison.leide.model.EntryComponentTemplate;
import com.wisebison.leide.model.EntryComponentTemplateForm;

import java.util.List;

@Dao
public abstract class EntryComponentTemplateDao {
  final EntryComponentTemplateSettingDao entryComponentTemplateSettingDao;
  public EntryComponentTemplateDao(final AppDatabase db) {
    entryComponentTemplateSettingDao = db.getEntryComponentTemplateSettingDao();
  }

  @Query("SELECT * FROM entry_component_template")
  public abstract LiveData<List<EntryComponentTemplateForm>> getAll();

  @Insert
  abstract long _insert(EntryComponentTemplate template);

  public void insert(final EntryComponentTemplate template) {
    final long id = _insert(template);
    for (final EntryComponentSetting setting : template.getSettings()) {
      setting.setTemplateId(id);
      entryComponentTemplateSettingDao.insert(setting);
    }
  }
}
