package com.taskPlanner.database.model;

public class IgnoreClient {

    private String name;
    private String phone;

    public IgnoreClient(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }
    public IgnoreClient() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof IgnoreClient){
            if(this.phone != null && ((IgnoreClient) obj).phone != null && this.phone.equals(((IgnoreClient) obj).phone)){
                return true;
            }
        }
        return false;
    }
}
