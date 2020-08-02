package com.goldthorp.wisebison.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.goldthorp.annotation.BackupEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(tableName = "sammy-session")
@BackupEntity(name = "sammy-session")
public class SammySession implements Serializable {
  /**
   * Serial UID.
   */
  private static final long serialVersionUID = -4169391057351695L;

  /**
   * Session id.
   */
  @PrimaryKey(autoGenerate = true)
  private long id;

  /**
   * When the session began.
   */
  @ColumnInfo(name = "start-timestamp")
  private long startTimestamp;

  /**
   * When the session was saved.
   */
  @ColumnInfo(name = "end-timestamp")
  private long endTimestamp;

  /**
   * Mood rating at start of session (1-9)
   */
  @ColumnInfo(name = "start-mood")
  private int startMood;

  /**
   * Mood rating at end of session (1-9)
   */
  @ColumnInfo(name = "end-mood")
  private int endMood;

  @Ignore
  private List<SammyItem> items = new ArrayList<>();

  public SammyItem addSessionItem(final String text, final float score, final float magnitude) {
    final SammyItem item = new SammyItem(text, score, magnitude);
    items.add(item);
    return item;
  }

  @Ignore
  public SammySession(final long startTimestamp, final int startMood) {
    this.startTimestamp = startTimestamp;
    this.startMood = startMood;
  }
}
