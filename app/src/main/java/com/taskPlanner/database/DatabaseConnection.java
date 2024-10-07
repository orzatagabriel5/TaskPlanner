package com.taskPlanner.database;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.taskPlanner.AppContext;

public class DatabaseConnection {

    private static AppDatabase instance = null;

    public static AppDatabase getInstance(){
            if(instance == null) {
                instance = Room.databaseBuilder(AppContext.getInstance(), AppDatabase.class, "appointment-db").addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3).allowMainThreadQueries().build();
            }
            return instance;
    }
}
