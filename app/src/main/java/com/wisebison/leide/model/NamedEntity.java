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
@EqualsAndHashCode(of = {"name", "entryComponentId", "beginOffset"})
@Entity(tableName = "named_entity",
  foreignKeys = @ForeignKey(
    entity = EntryComponent.class,
    parentColumns = "id",
    childColumns = "entry_component_fk",
    onDelete = ForeignKey.CASCADE),
  indices = @Index(
    name = "named_entity_entry_component_fk",
    value = "entry_component_fk"))
public class NamedEntity {
  @PrimaryKey(autoGenerate = true)
  private Long id;

  @ColumnInfo(name = "entry_fk")
  private Long entryId;

  @ColumnInfo(name = "entry_component_fk")
  private Long entryComponentId;

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
