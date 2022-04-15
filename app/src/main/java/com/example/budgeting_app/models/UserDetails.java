package com.example.budgeting_app.models;

public class UserDetails {

    String fname;
    String lname;
    String email;
    String password;
    String passcode;
    String enablePasscode;

    public UserDetails() {
    }

    public UserDetails(String fname, String lname, String email, String password, String passcode) {
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.password = password;
        this.passcode = passcode;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }
}
