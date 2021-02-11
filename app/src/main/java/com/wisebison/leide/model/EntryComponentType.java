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
    return EntryComponentType.values()[i];
  }

  @TypeConverter
  public static int toInt(final EntryComponentType entryComponentType) {
    return entryComponentType.ordinal();
  }
}
