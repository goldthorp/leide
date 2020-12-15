package com.wisebison.leide.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.database.Exclude;
import com.wisebison.annotation.BackupEntity;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"entitiesAnalyzed", "sentimentAnalyzed"})
@Entity(tableName = "entry")
@BackupEntity(name = "entry")
public class Entry implements Comparable<Entry>, Serializable {
  @PrimaryKey(autoGenerate = true)
  private Long id;

  private String text;

  @ColumnInfo(name = "start_timestamp")
  private Long startTimestamp;

  @ColumnInfo(name = "save_timestamp")
  private Long saveTimestamp;

  private String location;

  @ColumnInfo(name = "time_zone")
  private String timeZone;

  @Getter(onMethod=@__({@Exclude}))
  @ColumnInfo(name = "entities_analyzed")
  private boolean entitiesAnalyzed;

  @Getter(onMethod=@__({@Exclude}))
  @Exclude
  @ColumnInfo(name = "sentiment_analyzed")
  private boolean sentimentAnalyzed;

  // Sort by newest to oldest
  @Override
  public int compareTo(final Entry e) {
    return e.getStartTimestamp().compareTo(startTimestamp);
  }
}
