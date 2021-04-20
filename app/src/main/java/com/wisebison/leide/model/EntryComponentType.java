package com.wisebison.leide.model;

import androidx.room.TypeConverter;

import lombok.Getter;

public enum EntryComponentType {
  TEXT(false),
  NUMBER(false),
  DATE(true),
  LOCATION(true);

  @Getter
  boolean onlyOnePerEntry;
  EntryComponentType(final boolean onlyOnePerEntry) {
    this.onlyOnePerEntry = onlyOnePerEntry;
  }

  @TypeConverter
  public static EntryComponentType toEntryComponentType(final int i) {
    if (i == -1) {
      return null;
    }
    return EntryComponentType.values()[i];
  }

  @TypeConverter
  public static Integer toInt(final EntryComponentType entryComponentType) {
    if (entryComponentType == null) {
      return -1;
    }
    return entryComponentType.ordinal();
  }
}
