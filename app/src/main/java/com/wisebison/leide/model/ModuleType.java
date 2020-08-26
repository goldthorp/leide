package com.wisebison.leide.model;

import androidx.room.TypeConverter;

import com.wisebison.leide.R;

import lombok.Getter;

@Getter
public enum ModuleType {
  NAMED_ENTITIES("named_entities", R.string.module_type_NAMED_ENTITIES),
  SENTIMENT("sentiment_analysis", R.string.module_type_SENTMENT);

  private final String sku;
  private final int titleId;
  ModuleType(final String sku, final int titleId) {
    this.sku = sku;
    this.titleId = titleId;
  }

  @TypeConverter
  public static ModuleType toModuleType(final int i) {
    return ModuleType.values()[i];
  }

  @TypeConverter
  public static int toInt(final ModuleType moduleType) {
    return moduleType.ordinal();
  }
}
