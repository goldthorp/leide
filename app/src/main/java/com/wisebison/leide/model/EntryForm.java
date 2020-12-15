package com.wisebison.leide.model;

import androidx.room.ColumnInfo;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EntryForm implements Serializable {

  @ColumnInfo(name = "id")
  private Long id;

  @ColumnInfo(name = "text")
  private String text;

  @ColumnInfo(name = "time_zone")
  private String timeZone;

  @ColumnInfo(name = "start_timestamp")
  private Long timestamp;

  @ColumnInfo(name = "location")
  private String location;

  @ColumnInfo(name = "score")
  private Float sentiment;
}
