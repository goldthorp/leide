package com.wisebison.leide.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.wisebison.leide.model.Module;

import java.util.List;

@Dao
public abstract class ModuleDao {
  @Query("SELECT * FROM module")
  public abstract LiveData<List<Module>> getAll();

  @Insert
  public abstract void insert(Module module);

  @Update
  public abstract void update(Module module);

  @Delete
  public abstract void delete(Module module);
}
