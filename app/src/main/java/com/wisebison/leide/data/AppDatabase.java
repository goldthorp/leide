package com.wisebison.leide.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.wisebison.leide.model.DiaryEntry;
import com.wisebison.leide.model.DiaryNamedEntity;
import com.wisebison.leide.model.DiarySentiment;
import com.wisebison.leide.model.Module;
import com.wisebison.leide.model.SammyItem;
import com.wisebison.leide.model.SammySession;

@Database(entities = {DiaryEntry.class, DiaryNamedEntity.class, DiarySentiment.class,
  SammySession.class, SammyItem.class, Module.class}, version = 1,
  exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

  private static AppDatabase INSTANCE;

  public abstract DiaryEntryDao getDiaryEntryDao();
  public abstract DiaryNamedEntityDao getDiaryNamedEntityDao();
  public abstract DiarySentimentDao getDiarySentimentDao();

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
