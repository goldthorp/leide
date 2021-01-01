package com.wisebison.leide.model;

import androidx.room.TypeConverter;

public enum EntryComponentType {
  TEXT,
  NUMBER,
  DATE,
  LOCATION;

  @TypeConverter
  public static EntryComponentType toEntryComponentType(final int i) {
    return EntryComponentType.values()[i];
  }

  @TypeConverter
  public static int toInt(final EntryComponentType entryComponentType) {
    return entryComponentType.ordinal();
  }
}
