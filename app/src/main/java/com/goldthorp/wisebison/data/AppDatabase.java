package com.goldthorp.wisebison.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.goldthorp.wisebison.model.DiaryEntry;
import com.goldthorp.wisebison.model.DiaryNamedEntity;
import com.goldthorp.wisebison.model.DiarySentiment;
import com.goldthorp.wisebison.model.SammyItem;
import com.goldthorp.wisebison.model.SammySession;

@Database(entities = {DiaryEntry.class, DiaryNamedEntity.class, DiarySentiment.class,
  SammySession.class, SammyItem.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

  private static AppDatabase INSTANCE;

  public abstract BackupEntityGeneratedDao getBackupEntityGeneratedDao();

  public static synchronized AppDatabase getInstance(final Context context) {
    final String dbName = "wisebison.db";
    if (INSTANCE == null) {
      INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, dbName)
        .build();
    }
    return INSTANCE;
  }
}
