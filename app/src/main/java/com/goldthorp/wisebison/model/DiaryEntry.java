package com.goldthorp.wisebison.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(tableName = "diary-entry")
@EqualsAndHashCode(exclude = {"entitiesAnalyzed", "sentimentAnalyzed"})
public class DiaryEntry implements Comparable<DiaryEntry>, Serializable {
  @PrimaryKey(autoGenerate = true)
  private Long id;

  private String text;

  private Long startTimestamp;

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