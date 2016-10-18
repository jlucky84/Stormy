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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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

import java.io.IOException;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends Activity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private String googleMapsApiKey, forecastApiKey, forecastURL;
    private final int SNOW_TEMP_FH = 45;
    private CurrentWeather mCurrentWeather;
    private LocationHelper mLocationHelper;
    private LocationGeocoder mGeocoder;
    @Bind(R.id.timeLabel) TextView mTimeLabel;
    @Bind(R.id.tempLabel) TextView mTempLabel;
    @Bind(R.id.humidityValue) TextView mHumidityValue;
    @Bind(R.id.precipChanceValue) TextView mPrecipChanceValue;
    @Bind(R.id.summaryLabel) TextView mSummaryLabel;
    @Bind(R.id.weatherIcon) ImageView mWeatherIcon;
    //@Bind(R.id.locationLabel) TextView mLocationLabel;
    @Bind(R.id.barPressureValue) TextView mBarPressureValue;
    @Bind(R.id.windValue) TextView mWindValue;
    @Bind(R.id.locationInput) EditText mLocationInput;
    @Bind(R.id.refreshImageView) ImageView mRefreshImageView;
    @Bind(R.id.pinpointImageView) ImageView mPinpointImageView;
    //@Bind(R.id.loadingImageView) ImageView mLoadingImageView;
    @Bind(R.id.precipChanceLabel) TextView mPrecipLabel;
    @Bind(R.id.refreshProgressBar) ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Resources res = getResources();
        Context context = getApplicationContext();

        generateApiKeys(res);

        mLocationInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (hasWindowFocus()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        });

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //verify API keys are accessible
                if (apiKeysVerified()) {
                    String inputLocation = mLocationInput.getText().toString();
                    if (inputIsNotNull(inputLocation)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toggleRefresh();
                            }
                        });

                        mLocationHelper = new LocationHelper(getApplicationContext(), getResources());
                        mLocationHelper.getMapsData(inputLocation);
                        getForecast(mLocationHelper);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toggleRefresh();
                            }
                        });
                        //showMessage(getString(R.string.json_location_error), Toast.LENGTH_LONG);

                    } else {
                        showMessage(getString(R.string.empty_location_field_message),
                                Toast.LENGTH_SHORT);
                    }
                } else {
                    showMessage(getString(R.string.error_api_key), Toast.LENGTH_SHORT);
                }
            }
        });



    }

    private void getForecast(LocationHelper locationHelper) {
        Resources res = getResources();
        String forecastURL = res.getString(R.string.forecast_url);

        if (isNetworkAvailable()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toggleRefresh();
                }
            });
            OkHttpClient client = new OkHttpClient();
            forecastURL = String.format(forecastURL,
                    forecastApiKey,
                    locationHelper.getLatitude(),
                    locationHelper.getLongitude());

            Log.d(TAG,"Forecast URL: " + forecastURL);
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
                    showErrorDialog();
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
                            WeatherHelper weatherHelper = new WeatherHelper(getApplicationContext());
                            mCurrentWeather = weatherHelper.getCurrentDetails(jsonData);

                            //only main UI thread is allowed to update user interface
                            //update the display of the app based on revised weather info
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });

                        } else {
                            showErrorDialog();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        } else {
            showMessage(getString(R.string.error_network_unavailable),
                    Toast.LENGTH_LONG);
            Log.d(TAG, getString(R.string.error_network_unavailable));
        }
    }

    private void toggleRefresh() {
        //data request done, replace ProgressBar with refresh ImageView
        if (mRefreshImageView.getVisibility() == View.VISIBLE) {
            mRefreshImageView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mRefreshImageView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void generateApiKeys(Resources res) {
        ApiKeyReader apiKeyReader = new ApiKeyReader(res);
        try {
            apiKeyReader.readKeyFile();
            apiKeyReader.setApiKeys();

            //assign keys to local variables
            forecastApiKey = apiKeyReader.getForecastApiKey();
            googleMapsApiKey = apiKeyReader.getGoogleMapsApiKey();
        } catch (IOException e) {
            Log.e(TAG, getString(R.string.apiKeyFileName));
            showMessage(getString(R.string.error_api_file_read),
                    Toast.LENGTH_LONG);
        }
    }

    private boolean apiKeysVerified() {
        return forecastApiKey != null && googleMapsApiKey != null;
    }

    private void updateDisplay() {
        int currTemp = mCurrentWeather.getTemperature();
        mTempLabel.setText(currTemp + "");
        if(currTemp <= SNOW_TEMP_FH)
            mPrecipLabel.setText("RAIN/SNOW?");
        else
            mPrecipLabel.setText("RAIN?");
        mHumidityValue.setText(mCurrentWeather.getHumidity() + "%");
        mTimeLabel.setText(
                String.format(Locale.getDefault(), getString(R.string.time_label_text),
                        mLocationHelper.getCityStateInfo(),
                        mCurrentWeather.getFormattedTime()));
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


    private boolean inputIsNotNull(String input) {
        return input != null &&
                input.length() > 0;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private void showErrorDialog() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    private void showMessage(String message, int duration) {
        Toast.makeText(this, message,
                duration).show();
    }
}
