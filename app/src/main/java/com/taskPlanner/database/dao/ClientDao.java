package com.taskPlanner.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.taskPlanner.database.model.Client;

import java.util.Calendar;
import java.util.List;

@Dao
public interface ClientDao {

    @Insert
    public void insertClient(Client client);

    @Update
    public void updateClient(Client client);

    @Query("SELECT * FROM client")
    public abstract List<Client> findAllClients();

    @Query("SELECT * FROM client WHERE name=:name LIMIT 1")
    public abstract Client findClientByName(String name);

    @Query("SELECT * FROM client WHERE lastEventDate <= :date")
    public abstract List<Client> findClientsOlderThan(Calendar date);

    @Query("SELECT * FROM client WHERE lastEventDate > :date")
    public abstract  List<Client> findClientsNotOlderThan(Calendar date);
}
