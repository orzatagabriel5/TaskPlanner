package com.taskPlanner.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.taskPlanner.database.model.ClientTotal;
import com.taskPlanner.database.model.Event;

import java.util.Calendar;
import java.util.List;

@Dao
public interface EventDao {

    @Insert
    public long insertEvent(Event event);

    @Query("SELECT * FROM event")
    public abstract List<Event> findAllEvents();

    @Query("SELECT * FROM event WHERE event.startDate >= :startMonthDate")
    public abstract List<Event> findAllEventsStartingWith(Calendar startMonthDate);

    @Query("SELECT * FROM event WHERE :startMonthDate <= startDate AND  :endMonthDate >= endDate")
    public List<Event> findEventsBetween(Calendar startMonthDate, Calendar endMonthDate);

    @Query("SELECT * FROM event WHERE id = :id")
    public Event findEventId(int id);

    @Delete
    public void removeEvent(Event event);

    @Update
    public void updateEvent(Event event);

    @Query("SELECT clientName as name, SUM(price) as totalPaid FROM event WHERE endDate <= :currentDate GROUP BY clientName ORDER BY SUM(price) DESC")
    public List<ClientTotal> getTotalForEveryClient(Calendar currentDate);

    @Query("SELECT * FROM event WHERE clientName = :clientName AND clientPhoneNumber = :clientPhoneNumber AND startDate = :startDate AND endDate = :endDate AND services = :services AND reminders = :reminders AND price = :price")
    public List<Event> findEventsByAllFields(String clientName, String clientPhoneNumber, Calendar startDate, Calendar endDate, String services, String reminders, int price);

}
