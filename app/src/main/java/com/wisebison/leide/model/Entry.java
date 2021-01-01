package com.wisebison.leide.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.wisebison.annotation.BackupEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"components"})
@Entity(tableName = "entry")
@BackupEntity(name = "entry", index = 0)
public class Entry implements Comparable<Entry>, Serializable {
  @PrimaryKey(autoGenerate = true)
  private Long id;

  @ColumnInfo(name = "start_timestamp")
  private Long startTimestamp;

  @ColumnInfo(name = "save_timestamp")
  private Long saveTimestamp;

  @Ignore
  private List<EntryComponent> components = new ArrayList<>();

  public EntryComponent addComponent(final EntryComponentType type, final String value) {
    final EntryComponent component = new EntryComponent(type);
    component.setValue(value);
    getComponents().add(component);
    return component;
  }

  // Sort by newest to oldest
  @Override
  public int compareTo(final Entry e) {
    return e.getStartTimestamp().compareTo(startTimestamp);
  }
}
