package com.wisebison.leide.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.wisebison.annotation.BackupEntity;

import org.json.JSONException;
import org.json.JSONObject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity(tableName = "entry_component_template")
@BackupEntity(name = "entry_component_template")
public class EntryComponentTemplate {
  @PrimaryKey(autoGenerate = true)
  private Long id;

  @NonNull
  private String name;

  @TypeConverters(EntryComponentType.class)
  private EntryComponentType type;
  
  private String settings;

  public EntryComponentTemplate(@NonNull final String name, final EntryComponentType type) {
    this.name = name;
    this.type = type;
  }

  public String getSetting(final String settingName) {
    try {
      final JSONObject settingsObject = new JSONObject(settings);
      return settingsObject.getString(settingName);
    } catch (final JSONException e) {
      e.printStackTrace();
    }
    return "";
  }
}
