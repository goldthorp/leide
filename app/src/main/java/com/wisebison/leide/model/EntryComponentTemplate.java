package com.wisebison.leide.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.wisebison.annotation.BackupEntity;

import java.util.ArrayList;
import java.util.List;

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

  @Ignore
  private List<EntryComponentSetting> settings = new ArrayList<>();

  public EntryComponentTemplate(@NonNull final String name, final EntryComponentType type) {
    this.name = name;
    this.type = type;
  }
}
