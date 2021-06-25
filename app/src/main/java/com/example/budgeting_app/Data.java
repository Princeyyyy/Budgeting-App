package com.example.budgeting_app;

public class Data {

    String iem, date, id, notes;
    int amount, month;

    public Data() {
    }

    public Data(String iem, String date, String id, String notes, int amount, int month) {
        this.iem = iem;
        this.date = date;
        this.id = id;
        this.notes = notes;
        this.amount = amount;
        this.month = month;
    }

    public String getIem() {
        return iem;
    }

    public void setIem(String iem) {
        this.iem = iem;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }
}
