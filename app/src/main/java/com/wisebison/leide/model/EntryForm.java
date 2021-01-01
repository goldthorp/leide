package com.wisebison.leide.model;

import androidx.room.ColumnInfo;
import androidx.room.Relation;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EntryForm implements Serializable {

  @ColumnInfo(name = "id")
  private Long id;

  @ColumnInfo(name = "start_timestamp")
  private Long timestamp;

  @ColumnInfo(name = "score")
  private Float sentiment;

  @Relation(parentColumn = "id", entityColumn = "entry_fk")
  private List<EntryComponent> components;
}
