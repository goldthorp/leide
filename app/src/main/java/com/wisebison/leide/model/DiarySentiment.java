package com.wisebison.leide.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity(tableName = "diary-sentiment")
public class DiarySentiment {
  @PrimaryKey
  private Long id;

  private float score;

  private float magnitude;

  @ColumnInfo(name = "entry_fk")
  private Long entryId;
}
