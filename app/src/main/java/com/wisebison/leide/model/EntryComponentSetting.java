package com.wisebison.leide.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.wisebison.annotation.BackupEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity(tableName = "entry_component_setting",
  foreignKeys = {
    @ForeignKey(entity = EntryComponent.class,
      parentColumns = "id",
      childColumns = "entry_component_fk",
      onDelete = ForeignKey.CASCADE),
    @ForeignKey(entity = EntryComponentTemplate.class,
      parentColumns = "id",
      childColumns = "entry_component_template_fk",
      onDelete = ForeignKey.CASCADE)
  },
  indices = {
    @Index(name = "component_setting_component_fk", value = "entry_component_fk"),
    @Index(name = "component_setting_template_fk", value = "entry_component_template_fk")
  })
@BackupEntity(name = "entry_component_setting", index = 3)
public class EntryComponentSetting {
  @PrimaryKey(autoGenerate = true)
  private Long id;

  @ColumnInfo(name = "entry_component_fk")
  private Long componentId;

  @ColumnInfo(name = "entry_component_template_fk")
  private Long templateId;

  private String name;

  private String value;

  public EntryComponentSetting(final String value) {
    this.value = value;
  }

  public EntryComponentSetting(final String name, final String value) {
    this.name = name;
    this.value = value;
  }
}
