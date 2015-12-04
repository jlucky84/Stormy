package com.example.justinlegrand.stormy;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.content.Context;


/**
 * Created by justin.legrand on 11/20/2015.
 */
public class ApiKeyReader {

    public static final String TAG = ApiKeyReader.class.getSimpleName();
    private String forecastApiKey;
    private String googleMapsApiKey;
    private String apiKeyFileName;
    private HashMap<String,String> keyMap;
    private AssetManager manager;
    private BufferedReader reader;



    public ApiKeyReader(Resources res){
        manager = res.getAssets();
        apiKeyFileName = res.getString(R.string.apiKeyFileName);
    }

    public void readKeyFile() throws IOException {
        this.reader = new BufferedReader(new InputStreamReader(manager.open(apiKeyFileName)));
        if(this.reader != null){
            buildHashMapFromFile(this.reader);
        }
        this.reader.close();
    }

    private void buildHashMapFromFile(BufferedReader reader) throws IOException{
        this.keyMap = new HashMap<String, String>();
        String line = reader.readLine();
        while(line != null){
            String[] keyValuePair = line.split(":");
            this.keyMap.put(keyValuePair[0],keyValuePair[1]);
            line = reader.readLine();
        }
    }

    public void setApiKeys(){
        setForecastApiKey(keyMap.get("forecastApiKey"));
        setGoogleMapsApiKey(keyMap.get("mapsApiKey"));
    }

    public String getForecastApiKey() {
        return forecastApiKey;
    }

    public void setForecastApiKey(String forecastApiKey) {
        this.forecastApiKey = forecastApiKey;
    }

    public String getGoogleMapsApiKey() {
        return googleMapsApiKey;
    }

    public void setGoogleMapsApiKey(String googleMapsApiKey) {
        this.googleMapsApiKey = googleMapsApiKey;
    }

}
