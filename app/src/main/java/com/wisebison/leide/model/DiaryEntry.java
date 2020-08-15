package com.wisebison.leide.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.wisebison.annotation.BackupEntity;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"entitiesAnalyzed", "sentimentAnalyzed"})
@Entity(tableName = "diary-entry")
@BackupEntity(name = "diary-entry")
public class DiaryEntry implements Comparable<DiaryEntry>, Serializable {
  @PrimaryKey(autoGenerate = true)
  private Long id;

  private String text;

  @ColumnInfo(name = "start_timestamp")
  private Long startTimestamp;

  @ColumnInfo(name = "save_timestamp")
  private Long saveTimestamp;

  private String location;

  private String timeZone;

  @ColumnInfo(name = "entities_analyzed")
  private boolean entitiesAnalyzed;

  @ColumnInfo(name = "sentiment_analyzed")
  private boolean sentimentAnalyzed;

  // Sort by newest to oldest
  @Override
  public int compareTo(final DiaryEntry e) {
    return e.getStartTimestamp().compareTo(startTimestamp);
  }
}
