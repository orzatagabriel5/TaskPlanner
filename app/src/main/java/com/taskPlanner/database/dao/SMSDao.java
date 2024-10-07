package com.taskPlanner.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.taskPlanner.database.model.SMS;

@Dao
public interface SMSDao {

    @Insert
    public void insertSMS(SMS sms);

    @Transaction
    @Query("SELECT * FROM sms ORDER BY id DESC LIMIT 1")
    public abstract SMS findLastSMS();


}
