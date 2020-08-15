package com.wisebison.leide.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.wisebison.annotation.BackupEntity;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Entity(tableName = "sammy-item")
@BackupEntity(name = "sammy-item")
public class SammyItem implements Serializable {
  /**
   * Serial UID.
   */
  private static final long serialVersionUID = 6120578021300697972L;

  /**
   * SammyItem id.
   */
  @PrimaryKey(autoGenerate = true)
  private long id;

  /**
   * Which session this item is in. Can be null if item is not attached to a session.
   */
  @ColumnInfo(name = "session_fk")
  private Long sessionId;

  /**
   * The text that was analyzed.
   */
  private String text;

  /**
   * The score from the analysis.
   */
  @ColumnInfo(name = "sentiment_score")
  private float sentimentScore;

  /**
   * The magnitude from the analysis.
   */
  @ColumnInfo(name = "sentiment_magnitude")
  private float sentimentMagnitude;

  /**
   * For items that are not attached to a session, the time the item was saved.
   */
  private Long timestamp;

  @Ignore
  public SammyItem(final String text, final float sentimentScore, final float sentimentMagnitude,
                   final long sessionId) {
    this.text = text;
    this.sentimentScore = sentimentScore;
    this.sentimentMagnitude = sentimentMagnitude;
    this.sessionId = sessionId;
  }

  @Ignore
  public SammyItem(
    final String text, final float sentimentScore, final float sentimentMagnitude) {
    this.text = text;
    this.sentimentScore = sentimentScore;
    this.sentimentMagnitude = sentimentMagnitude;
  }
}
