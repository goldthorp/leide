package com.wisebison.leide.model;

import androidx.room.ColumnInfo;
import androidx.room.Relation;
import androidx.room.TypeConverters;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import javax.annotation.Nullable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntryComponentForm {
  private Long id;

  @ColumnInfo(name = "entry_fk")
  private Long entryId;

  @TypeConverters(EntryComponentType.class)
  @Nullable
  private EntryComponentType type;

  private String name;

  @ColumnInfo(name = "entities_analyzed")
  private boolean entitiesAnalyzed;

  @ColumnInfo(name = "sentiment_analyzed")
  private boolean sentimentAnalyzed;

  @Relation(parentColumn = "id", entityColumn = "entry_component_fk")
  private List<EntryComponentValue> values;

  public String getValue(final String name) {
    for (final EntryComponentValue value : values) {
      if (StringUtils.equals(value.getName(), name)) {
        return value.getValue();
      }
    }
    return null;
  }
}
