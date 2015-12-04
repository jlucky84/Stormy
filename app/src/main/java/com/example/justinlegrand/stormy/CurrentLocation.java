package com.example.justinlegrand.stormy;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by justin.legrand on 11/19/2015.
 */
public class CurrentLocation {

    private double mLatitude;
    private double mLongitude;
    private String mState;
    private String mCity;
    private String mCountry;
    private Formatter formatter;


    public String formatCityStateCountry(){
        StringBuilder sb = new StringBuilder();
        formatter = new Formatter(sb, Locale.US);
        return formatter.format("%1, %2 %3", getCity(), getState(), getCountry()).toString();
    }
    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String country) {
        this.mCountry = country;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        this.mLongitude = longitude;
    }

    public String getState() {
        return mState;
    }

    public void setState(String state) {
        this.mState = state;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String city) {
        this.mCity = city;
    }
}
