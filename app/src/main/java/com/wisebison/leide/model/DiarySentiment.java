package com.wisebison.leide.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity(tableName = "diary-sentiment",
  foreignKeys = @ForeignKey(
    entity = DiaryEntry.class,
    parentColumns = "id",
    childColumns = "entry_fk",
    onDelete = ForeignKey.CASCADE),
  indices = @Index(
    name = "sentiment_entry_fk",
    value = "entry_fk"))
public class DiarySentiment {
  @PrimaryKey
  private Long id;

  private float score;

  private float magnitude;

  @ColumnInfo(name = "sentence_begin_offset")
  private Integer sentenceBeginOffset;

  @ColumnInfo(name = "sentence_end_offset")
  private Integer sentenceEndOffset;

  @ColumnInfo(name = "entry_fk")
  private Long entryId;
}
