package com.example.justinlegrand.stormy;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends Activity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather mCurrentWeather;
    private LocationGeocoder mGeocoder;
    @Bind(R.id.timeLabel) TextView mTimeLabel;
    @Bind(R.id.tempLabel) TextView mTempLabel;
    @Bind(R.id.humidityValue) TextView mHumidityValue;
    @Bind(R.id.precipChanceValue) TextView mPrecipChanceValue;
    @Bind(R.id.summaryLabel) TextView mSummaryLabel;
    @Bind(R.id.weatherIcon) ImageView mWeatherIcon;
    @Bind(R.id.locationLabel) TextView mLocationLabel;
    @Bind(R.id.barPressureValue) TextView mBarPressureValue;
    @Bind(R.id.windValue) TextView mWindValue;
    @Bind(R.id.locationEditText) EditText mLocationEditText;
    @Bind(R.id.refreshImageView) ImageView mRefreshImageView;
    @Bind(R.id.refreshProgressBar) ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mGeocoder = new LocationGeocoder(getApplicationContext());

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userLocation = mLocationEditText.getText().toString();
                if(inputIsNotNull(userLocation)){
                    try {
                        mGeocoder.parseLocation(userLocation);
                        getForecast(mGeocoder.getLatitude(), mGeocoder.getLongitude());
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            }
        });

        getForecast(mGeocoder.getLatitude(), mGeocoder.getLongitude());
    }

    private void getForecast(double latitude, double longitude) {
        Resources res = getResources();
        final Context context = getApplicationContext();

        ApiKeyReader apiKeyReader = new ApiKeyReader(res);
        generateApiKeys(apiKeyReader);

        String forecastURL = res.getString(R.string.forecast_url);
        String forecastApiKey = apiKeyReader.getForecastApiKey();
        String googleMapsApiKey = apiKeyReader.getGoogleMapsApiKey();

        forecastURL = String.format(forecastURL,
                forecastApiKey,
                latitude,
                longitude);

        if(isNetworkAvailable()) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toggleRefresh();
                }
            });
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastURL)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);

                            //only main UI thread is allowed to update user interface
                            //update the display of the app based on revised weather info
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });

                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e){
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        }else{
            Toast.makeText(this, getString(R.string.network_unavailable_message),
                    Toast.LENGTH_LONG).show();
            Log.d(TAG,getString(R.string.network_unavailable_message));
        }
    }

    private void toggleRefresh() {
        //data request done, replace ProgressBar with refresh ImageView
        if(mRefreshImageView.getVisibility() == View.VISIBLE) {
            mRefreshImageView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        }
        else {
            mRefreshImageView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void generateApiKeys(ApiKeyReader apiKeyReader) {
        try {
            apiKeyReader.readKeyFile();
            apiKeyReader.setApiKeys();
        }
        catch (IOException e) {
            Log.e(TAG,getString(R.string.apiKeyFileName));
            Toast.makeText(this,getString(R.string.api_file_read_error_messaage),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void updateDisplay() {
        mTempLabel.setText(mCurrentWeather.getTemperature() + "");
        mHumidityValue.setText(mCurrentWeather.getHumidity() + "%");
        mTimeLabel.setText("As of " + mCurrentWeather.getFormattedTime() + ", conditions are:");
        mSummaryLabel.setText(mCurrentWeather.getSummary());
        mPrecipChanceValue.setText(mCurrentWeather.getPrecipChance() + "%");
        mWindValue.setText(mCurrentWeather.getWindDirection() + " " +
                mCurrentWeather.getWindSpeed());
        mBarPressureValue.setText(mCurrentWeather.getBarometricPressure() + "");
        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());
        mWeatherIcon.setImageDrawable(drawable);

        //TODO create class/methods to revise location of forecast, and set location labels

        //mLocationLabel.setText("");
    }

    private CurrentWeather getCurrentDetails (String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        CurrentWeather currentWeather = new CurrentWeather();
        JSONObject currently = forecast.getJSONObject("currently");

        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setTimeZone(timezone);
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setWindSpeed(currently.getDouble("windSpeed"));
        currentWeather.setBarometricPressure(currently.getDouble("pressure"));
        currentWeather.setWindBearing(currently.getDouble("windBearing"));

        return currentWeather;
    }

    private boolean inputIsNotNull(String input){
        return input != null &&
                input.length() > 0;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(),"error_dialog");

    }
}
