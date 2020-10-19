package com.wisebison.leide.model;

import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.wisebison.leide.view.ModuleFragment;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(tableName = "module")
public class Module implements Serializable {
  @PrimaryKey(autoGenerate = true)
  private Long id;

  @ColumnInfo(name = "module_type")
  @TypeConverters(ModuleType.class)
  private ModuleType moduleType;

  public Module(final ModuleType moduleType) {
    this.moduleType = moduleType;
  }

  public ModuleFragment getFragment() {
    try {
      final Constructor<? extends ModuleFragment> constructor =
        getModuleType().getFragmentClass().getConstructor(getClass());
      return constructor.newInstance(this);
    } catch (final NoSuchMethodException | IllegalAccessException | InstantiationException |
      InvocationTargetException e) {
      Log.e("Module", "Failed to get instance of fragment for module type " + moduleType, e);
    }
    return null;
  }
}
