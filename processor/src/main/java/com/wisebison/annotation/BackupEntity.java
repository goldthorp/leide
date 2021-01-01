package com.wisebison.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an Entity should be backed up to the Firebase database.
 *
 * Classes annotated with this must also be annotated with `androidx.room.Entity`
 * and @BackupEntity.name must match @Entity.tableName exactly.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BackupEntity {
  /**
   * The @BackupEntity's name must match the tableName for the @Entity.
   */
  String name();

  /**
   * When restoring the backup from Firebase to the local database, parent tables need to be
   * restored first so that foreign keys in the child tables can reference them. The index
   * indicates the order that entity tables should be restored (e.g. @BackupEntity(index = 0)
   * will get restored before @BackupEntity(index = 1)). Multiple @BackupEntities can have the
   * same index if it does not matter what order they are restored in.
   */
  int index() default 0;
}
