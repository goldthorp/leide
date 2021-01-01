package com.wisebison.leide.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.wisebison.leide.model.Entry;
import com.wisebison.leide.model.EntryComponent;
import com.wisebison.leide.model.Module;
import com.wisebison.leide.model.NamedEntity;
import com.wisebison.leide.model.Sentiment;

@Database(entities = {Entry.class, EntryComponent.class, NamedEntity.class, Sentiment.class,
  Module.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

  private static AppDatabase INSTANCE;

  public abstract EntryDao getEntryDao();
  public abstract EntryComponentDao getEntryComponentDao();
  public abstract NamedEntityDao getNamedEntityDao();
  public abstract SentimentDao getSentimentDao();

  public abstract ModuleDao getModuleDao();

  public abstract BackupEntityGeneratedDao getBackupEntityGeneratedDao();

  public static synchronized AppDatabase getInstance(final Context context) {
    final String dbName = "leide.db";
    if (INSTANCE == null) {
      INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, dbName)
        .build();
    }
    return INSTANCE;
  }
}
