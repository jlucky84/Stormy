package com.example.justinlegrand.stormy;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;

/**
 * Created by justin.legrand on 11/24/2015.
 */
public class LocationGeocoder {
    private double latitude;
    private double longitude;
    private List<Address> addressList;
    private String cityAndState;
    private Geocoder mGeocoder;
    private final int MAX_RESULTS = 1;


    public LocationGeocoder(Context context){
        mGeocoder = new Geocoder(context);
        //these coordinates are for Alcatraz Island, CA, USA
        //this.latitude = 37.8267;
        //this.longitude = -122.423;

        //PENDLETON COORDS
        //this.latitude = 45.669722;
        //this.longitude = -118.791389;
    }

    public void parseLocation(String location) throws NumberFormatException, IOException{

        addressList = mGeocoder.getFromLocationName(location, MAX_RESULTS);
        if(!addressList.isEmpty()){
            Address address = addressList.get(0);
            setLatitude(address.getLatitude());
            setLongitude(address.getLongitude());
        }
    }

    public boolean inputFitsForecastInput(String input){
        return input.matches("(-?\\d|\\.)+");
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCityAndState() {
        return cityAndState;
    }

    public void setCityAndState(String cityAndState) {
        this.cityAndState = cityAndState;
    }

    public List<Address> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<Address> addressList) {
        this.addressList = addressList;
    }

}
