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
  String name();
}
