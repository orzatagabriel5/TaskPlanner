package com.taskPlanner.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;


import com.taskPlanner.database.model.Service;

import java.util.List;

@Dao
public interface ServiceDao {

    @Insert
    public long insertService(Service service);

    @Query("SELECT * FROM service")
    public abstract List<Service> findAllServices();

    @Query("SELECT * FROM service WHERE name = :name LIMIT 1")
    public abstract Service findServiceWithName(String name);


    @Update
    public void updateService(Service service);

    @Delete
    public void deleteService(Service service);

}
