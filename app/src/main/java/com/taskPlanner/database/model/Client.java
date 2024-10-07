package com.taskPlanner.database.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Arrays;
import java.util.Calendar;

@Entity
public class Client {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String phone;
    private Calendar lastEventDate;

    public Client(){

    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getLastEventDate() {
        return lastEventDate;
    }

    public void setLastEventDate(Calendar lastEventDate) {
        this.lastEventDate = lastEventDate;
    }

    @Override
    public boolean equals(Object o){
        Client clientToCompare = (Client) o;
        if (this.getName().equals(clientToCompare.getName())
        && this.getPhone().equals(clientToCompare.getPhone())){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return 31;
    }
}
