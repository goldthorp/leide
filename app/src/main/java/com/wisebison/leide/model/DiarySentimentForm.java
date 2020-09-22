package com.wisebison.leide.model;

import androidx.room.ColumnInfo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DiarySentimentForm {
  @ColumnInfo(name = "score")
  private float score;

  @ColumnInfo(name = "start_timestamp")
  private Long timestamp;
}
