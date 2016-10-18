package com.example.justinlegrand.stormy;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by justin.legrand on 11/17/2015.
 */
public class CurrentWeather {
    private String mIcon;
    private long mTime;
    private double mTemperature;
    private double mHumidity;
    private double mPrecipChance;
    private String mSummary;
    private String mTimeZone;
    private double windSpeed;
    private double windBearing;
    private double barometricPressure;
    private final double MBAR_TO_INHG_RATIO = 0.02953;

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public int getIconId(){
        int iconId = R.drawable.clear_day;

        if (mIcon.equals("clear-day")) {
            iconId = R.drawable.clear_day;
        }
        else if (mIcon.equals("clear-night")) {
            iconId = R.drawable.clear_night;
        }
        else if (mIcon.equals("rain")) {
            iconId = R.drawable.rain;
        }
        else if (mIcon.equals("snow")) {
            iconId = R.drawable.snow;
        }
        else if (mIcon.equals("sleet")) {
            iconId = R.drawable.sleet;
        }
        else if (mIcon.equals("wind")) {
            iconId = R.drawable.wind;
        }
        else if (mIcon.equals("fog")) {
            iconId = R.drawable.fog;
        }
        else if (mIcon.equals("cloudy")) {
            iconId = R.drawable.cloudy;
        }
        else if (mIcon.equals("partly-cloudy-day")) {
            iconId = R.drawable.partly_cloudy;
        }
        else if (mIcon.equals("partly-cloudy-night")) {
            iconId = R.drawable.cloudy_night;
        }

        return iconId;
    }

    public long getTime() {
        return mTime;
    }

    public String getFormattedTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        //get default timezone based on the location in which the app is being run
        formatter.setTimeZone(TimeZone.getDefault());
        String timeString = formatter.format(new Date(getTime() * 1000));

        return timeString;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public String getTimeZone() {
        return mTimeZone;
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;
    }

    public int getTemperature() {
        return (int) Math.round(mTemperature);
    }

    public void setTemperature(double temperature) {
        mTemperature = temperature;
    }

    public int getHumidity() {
        return (int) Math.round(mHumidity* 100);
    }

    public void setHumidity(double humidity) {
        mHumidity = humidity;
    }

    public int getPrecipChance() {
        return (int) Math.round(mPrecipChance * 100);
    }

    public void setPrecipChance(double precipChance) {
        mPrecipChance = precipChance;
    }

    public int getWindSpeed() {
        return (int) windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindDirection() {
        //Forecast api returns integer value representing the degrees from 0 (North) the wind is blowing.
        //If wind speed is 0, wind bearing is not defined
       String direction = "-";
       if(this.windBearing > 348.75 || this.windBearing <= 11.25)
           direction = "N";
       else if(this.windBearing < 33.75)
           direction = "NNE";
       else if(this.windBearing < 56.25)
           direction = "NE";
       else if(this.windBearing < 78.75)
           direction = "ENE";
       else if(this.windBearing < 101.25)
           direction = "E";
       else if(this.windBearing < 123.75)
           direction = "ESE";
       else if(this.windBearing < 146.25)
           direction = "SE";
       else if(this.windBearing < 168.75)
           direction = "SSE";
       else if(this.windBearing < 191.25)
           direction = "S";
       else if(this.windBearing < 213.75)
           direction = "SSW";
       else if(this.windBearing < 236.25)
           direction = "SW";
       else if(this.windBearing < 258.75)
           direction = "WSW";
       else if(this.windBearing < 281.25)
           direction = "W";
       else if(this.windBearing < 303.75)
           direction = "WNW";
       else if(this.windBearing < 326.25)
           direction = "NW";
       else if(this.windBearing < 348.75)
           direction = "NNW";

        return direction;
    }

    public void setWindBearing(double windBearing) {
        this.windBearing = windBearing;
    }

    public int getBarometricPressure() {
        //ForecastAPI returns pressure in millibars, convert to inches of Mercury for this app
        return (int) Math.round(barometricPressure * MBAR_TO_INHG_RATIO);
    }

    public void setBarometricPressure(double barometricPressure) {
        this.barometricPressure = barometricPressure;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

}
