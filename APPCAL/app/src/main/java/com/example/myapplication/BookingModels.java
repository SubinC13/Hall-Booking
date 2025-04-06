package com.example.myapplication;

public class BookingModels {

    String email,company_name,bookin_date,fromtime,totime;

    public BookingModels() {

    }
    public BookingModels(String email, String company_name, String bookin_date, String fromtime, String totime) {
        this.email = email;
        this.company_name = company_name;
        this.bookin_date = bookin_date;
        this.fromtime = fromtime;
        this.totime = totime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public String getBookin_date() {
        return bookin_date;
    }

    public void setBookin_date(String bookin_date) {
        this.bookin_date = bookin_date;
    }

    public String getFromtime() {
        return fromtime;
    }

    public void setFromtime(String fromtime) {
        this.fromtime = fromtime;
    }

    public String getTotime() {
        return totime;
    }

    public void setTotime(String totime) {
        this.totime = totime;
    }
}
