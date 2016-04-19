package com.example.justinlegrand.stormy;
//package com.google.android.gms.common.api.GoogleApiClient;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;

/**
 * Created by justin.legrand on 3/22/2016.
 */
public class LocationHelper {

    private final String TAG = LocationHelper.class.getSimpleName();
    private final Context context;
    private final Resources res;
    private String city;
    private String state;
    private String country;
    private double latitude;
    private double longitude;
    private String mapsUrl;
    private String jsonData;

    Intent intent = new Intent(Intent.ACTION_VIEW);
    //intent.set

    public LocationHelper(Context context, Resources res){
        this.context = context;
        this.res = res;
    }


    public void getMapsData(String address) {
        if(isNetworkAvailable()){
            OkHttpClient client = new OkHttpClient();

            String mapsUrl = context.getString(R.string.google_maps_url);
            mapsUrl = String.format(mapsUrl, address);

            Toast.makeText(context,"URL: " + mapsUrl,Toast.LENGTH_SHORT).show();

            Request request = new Request.Builder()
                    .url(mapsUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String jsonData = response.body().string();
                    if(response.isSuccessful()){
                        Log.d(TAG, jsonData);
                        try{
                            parseMapData(jsonData);
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public void parseMapData(String data)throws JSONException{
        JSONObject location = new JSONObject(data);
        JSONArray resultArray = location.getJSONArray("results");
        JSONObject results = resultArray.getJSONObject(0);
        JSONArray components = results.getJSONArray("address_components");

        //JSONArray components = results.getJSONArray("address_components");
        //parse json address_components to get City & State data
        for(int componentIndex=0; componentIndex<components.length(); componentIndex++){
            JSONObject addressComponent = components.getJSONObject(componentIndex);

            String componentType = addressComponent.getJSONArray("types").toString();
            if(componentType != null){
                if(componentType.contains("locality")) {
                    this.city = addressComponent.getString("short_name");
                }else if(componentType.contains("administrative_area_level_1")){
                    this.state = addressComponent.getString("short_name");
                }else if(componentType.contains("country")){
                    this.country = addressComponent.getString("short_name");
                }
            }
        }

        //Determine centered Geodetic coordinates by calculating average of boundaries
        JSONObject geodeticData = results.getJSONObject("geometry");
        //JSONObject bounds = geodeticData.getJSONObject("bounds");
        JSONObject coordinates = geodeticData.getJSONObject("location");

        double latitude = coordinates.getDouble("lat");
        double longitude = coordinates.getDouble("lng");
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public String getJsonData() {
        return jsonData;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityStateInfo(){
        return this.city + ", " + this.state + ", " + this.country;
    }
}
