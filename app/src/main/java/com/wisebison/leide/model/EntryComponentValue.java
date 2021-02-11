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
@Entity(tableName = "entry_component_value",
  foreignKeys = @ForeignKey(
    entity = EntryComponent.class,
    parentColumns = "id",
    childColumns = "entry_component_fk",
    onDelete = ForeignKey.CASCADE),
  indices = @Index(
    name = "component_value_component_fk",
    value = "entry_component_fk"))
@BackupEntity(name = "entry_component_value", index = 2)
public class EntryComponentValue {
  @PrimaryKey(autoGenerate = true)
  private Long id;

  @ColumnInfo(name = "entry_component_fk")
  private Long componentId;

  private String name;

  private String value;

  public EntryComponentValue(final String value) {
    this.value = value;
  }

  public EntryComponentValue(final String name, final String value) {
    this.name = name;
    this.value = value;
  }
}
