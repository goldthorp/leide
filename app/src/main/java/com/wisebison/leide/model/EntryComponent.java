package com.wisebison.leide.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.firebase.database.Exclude;
import com.wisebison.annotation.BackupEntity;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"entitiesAnalyzed", "sentimentAnalyzed", "id"})
@NoArgsConstructor
@Entity(tableName = "entry_component",
  foreignKeys = @ForeignKey(
    entity = Entry.class,
    parentColumns = "id",
    childColumns = "entry_fk",
    onDelete = ForeignKey.CASCADE),
  indices = @Index(
    name = "component_entry_fk",
    value = "entry_fk"))
@BackupEntity(name = "entry_component", index = 1)
public class EntryComponent implements Comparable<EntryComponent> {
  @PrimaryKey(autoGenerate = true)
  private Long id;

  @ColumnInfo(name = "entry_fk")
  private Long entryId;

  @TypeConverters(EntryComponentType.class)
  private EntryComponentType type;

  private String name;

  @ColumnInfo(name = "list_seq")
  private int listSeq;

  @Getter(onMethod=@__({@Exclude}))
  @ColumnInfo(name = "entities_analyzed")
  private boolean entitiesAnalyzed;

  @Getter(onMethod=@__({@Exclude}))
  @ColumnInfo(name = "sentiment_analyzed")
  private boolean sentimentAnalyzed;

  @Ignore
  private List<EntryComponentValue> values = new ArrayList<>();

  public String getValue(final String name) {
    for (final EntryComponentValue value : values) {
      if (StringUtils.equals(name, value.getName())) {
        return value.getValue();
      }
    }
    return null;
  }

  public EntryComponent(final EntryComponentType type) {
    this.type = type;
  }

  @Override
  public int compareTo(final EntryComponent o) {
    return Integer.compare(listSeq, o.listSeq);
  }
}
