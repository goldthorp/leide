package com.wisebison.leide.model;

import androidx.room.ColumnInfo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class NamedEntityForm {

  /**
   * The name of this entity.
   */
  @ColumnInfo(name = "name")
  private String name;

  /**
   * How many times an entity with this name has been found.
   */
  @ColumnInfo(name = "count(*)")
  private int count;
}
