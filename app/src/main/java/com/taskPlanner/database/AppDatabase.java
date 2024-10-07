package com.taskPlanner.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;

import com.taskPlanner.database.dao.ClientDao;
import com.taskPlanner.database.dao.EventDao;
import com.taskPlanner.database.dao.SMSDao;
import com.taskPlanner.database.dao.ServiceDao;
import com.taskPlanner.database.model.Client;
import com.taskPlanner.database.model.Event;
import com.taskPlanner.database.model.SMS;
import com.taskPlanner.database.model.Service;

@Database(version = 3, entities = {Client.class, Event.class, Service.class, SMS.class}, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    abstract public ClientDao clientDao();

    abstract public EventDao eventDao();

    abstract public ServiceDao serviceDao();

    abstract public SMSDao smsDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE client "
                    + " ADD COLUMN lastEventDate INTEGER");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE event "
                    + " ADD COLUMN notes varchar(255)");
        }
    };

}
