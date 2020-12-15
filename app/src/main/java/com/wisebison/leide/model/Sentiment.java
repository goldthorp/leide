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
@Entity(tableName = "sentiment",
  foreignKeys = @ForeignKey(
    entity = Entry.class,
    parentColumns = "id",
    childColumns = "entry_fk",
    onDelete = ForeignKey.CASCADE),
  indices = @Index(
    name = "sentiment_entry_fk",
    value = "entry_fk"))
public class Sentiment {
  @PrimaryKey
  private Long id;

  private float score;

  private float magnitude;

  @ColumnInfo(name = "sentence_begin_offset")
  private Integer sentenceBeginOffset;

  @ColumnInfo(name = "sentence_length")
  private Integer sentenceLength;

  @ColumnInfo(name = "entry_fk")
  private Long entryId;
}
