package com.wisebison.leide.model;

import androidx.room.TypeConverter;

import com.wisebison.leide.R;
import com.wisebison.leide.view.ModuleFragment;
import com.wisebison.leide.view.NamedEntitiesModuleFragment;
import com.wisebison.leide.view.SentimentModuleFragment;

import lombok.Getter;

@Getter
public enum ModuleType {
  NAMED_ENTITIES("named_entities", R.string.module_type_NAMED_ENTITIES, NamedEntitiesModuleFragment.class),
  SENTIMENT("sentiment_analysis", R.string.module_type_SENTMENT, SentimentModuleFragment.class);

  private final String sku;
  private final int titleId;
  private final Class<? extends ModuleFragment> fragmentClass;
  ModuleType(final String sku, final int titleId,
             final Class<? extends ModuleFragment> fragmentClass) {
    this.sku = sku;
    this.titleId = titleId;
    this.fragmentClass = fragmentClass;
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
