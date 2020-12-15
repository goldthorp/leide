package com.wisebison.leide.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"name", "entryId", "beginOffset"})
@Entity(tableName = "named_entity",
  foreignKeys = @ForeignKey(
    entity = Entry.class,
    parentColumns = "id",
    childColumns = "entry_fk",
    onDelete = ForeignKey.CASCADE),
  indices = @Index(
    name = "named_entity_entry_fk",
    value = "entry_fk"))
public class NamedEntity {
  @PrimaryKey(autoGenerate = true)
  private Long id;

  @ColumnInfo(name = "entry_fk")
  private Long entryId;

  /**
   * @see com.google.api.services.language.v1.model.Entity#getName()
   */
  private String name;

  /**
   * @see com.google.api.services.language.v1.model.Entity#getType()
   */
  private String type;

  /**
   * @see com.google.api.services.language.v1.model.Entity#getSalience()
   */
  private float salience;

  /**
   * @see com.google.api.services.language.v1.model.EntityMention#getText()
   * @see com.google.api.services.language.v1.model.TextSpan#getBeginOffset()
   */
  @ColumnInfo(name = "begin_offset")
  private Integer beginOffset;

  /**
   * @see com.google.api.services.language.v1.model.EntityMention#getText()
   * @see com.google.api.services.language.v1.model.TextSpan#getContent()
   */
  private String content;
}
