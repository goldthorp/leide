package com.wisebison.leide.model;

import androidx.room.Relation;
import androidx.room.TypeConverters;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntryComponentTemplateForm {
  private Long id;

  private String name;

  @TypeConverters(EntryComponentType.class)
  private EntryComponentType type;

  @Relation(parentColumn = "id", entityColumn = "entry_component_template_fk")
  List<EntryComponentSetting> settings;

  public String getSetting(final String settingName) {
    for (final EntryComponentSetting setting : settings) {
      if (StringUtils.equals(settingName, setting.getName())) {
        return setting.getValue();
      }
    }
    return null;
  }

  public Map<String, String> getSettingsAsMap() {
    final Map<String, String> settingsMap = new HashMap<>();
    for (final EntryComponentSetting setting : settings) {
      settingsMap.put(setting.getName(), setting.getValue());
    }
    return settingsMap;
  }
}
