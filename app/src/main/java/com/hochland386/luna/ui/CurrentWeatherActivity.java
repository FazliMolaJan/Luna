package com.hochland386.luna.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hochland386.luna.R;
import com.hochland386.luna.bus.CurrentWeatherFailureEvent;
import com.hochland386.luna.bus.CurrentWeatherResponseEvent;
import com.hochland386.luna.bus.LocationChangedEvent;
import com.hochland386.luna.bus.LocationFailureEvent;
import com.hochland386.luna.model.CurrentWeather;
import com.hochland386.luna.utils.GeocoderUtils;
import com.hochland386.luna.utils.ProvidersChecker;
import com.hochland386.luna.utils.ResponseParser;
import com.hochland386.luna.utils.UrlBuilder;
import com.hochland386.luna.worker.LocationWorker;
import com.hochland386.luna.worker.NetworkWorker;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;

public class CurrentWeatherActivity extends AppCompatActivity {

//    Constants
    private final int LOCATION_PERMISSIONS_REQUEST_CODE = 0;

//    View's declaration
    private ImageButton refreshIb;
    private ProgressBar refreshPb;
    private TextView placeTv, minusSymbolTv, temperatureTv,
            humidityValueTv, pressureValueTv, weatherSummaryTv;

//    Members
    private CurrentWeather mCurrentWeather;
    private String[] mLocationPermissionsArray = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_weather);

//        View's init
        refreshIb = (ImageButton) findViewById(R.id.refreshIb);
        refreshPb = (ProgressBar) findViewById(R.id.refreshPb);
        placeTv = (TextView) findViewById(R.id.placeTv);
        minusSymbolTv = (TextView) findViewById(R.id.minusSymbolTv);
        temperatureTv = (TextView) findViewById(R.id.temperatureTv);
        humidityValueTv = (TextView) findViewById(R.id.humidityValueTv);
        pressureValueTv = (TextView) findViewById(R.id.pressureValueTv);
        weatherSummaryTv = (TextView) findViewById(R.id.weatherSummaryTv);

//        Set onClickListener to refreshIb
        refreshIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshWeather();
            }
        });

//        Set onClickListener to temperatureTv
        temperatureTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showForecastActivityIntent = new Intent(
                        CurrentWeatherActivity.this,
                        DailyForecastActivity.class
                );
                startActivity(showForecastActivityIntent);
            }
        });

//        Register for events
        EventBus.getDefault().register(this);

//        Refresh weather data when activity first created
        refreshWeather();
    }

//    Private interface
    /**
     * Checks runtime permissions, location and network availability and get weather data
     * if everything is ok
     */
    private void refreshWeather() {
        /* Checks runtime permissions. Request permissions if it's not already granted */
        if (ContextCompat.checkSelfPermission(
                CurrentWeatherActivity.this,
                mLocationPermissionsArray[0])
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                CurrentWeatherActivity.this,
                mLocationPermissionsArray[1])
                != PackageManager.PERMISSION_GRANTED) {
                /* Warning! No permissions at this point of the time. Requesting ... */
            ActivityCompat.requestPermissions(
                    CurrentWeatherActivity.this,
                    mLocationPermissionsArray,
                    LOCATION_PERMISSIONS_REQUEST_CODE
            );
        } else {
                /* Yay! Required Permissions granted. Now we can proceed */
            getCurrentWeather();
        }
    }

    /**
     * Updates UI with data from mCurrentWeather object
     */
    private void updateUi() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleRefreshAnimationOff();
                placeTv.setText(mCurrentWeather.getPlace());
                if (mCurrentWeather.getTemperature() < 0) {
                    int reversedTemperature = mCurrentWeather.getTemperature() * -1;
                    minusSymbolTv.setVisibility(View.VISIBLE);
                    temperatureTv.setText(String.valueOf(reversedTemperature));
                } else {
                    minusSymbolTv.setVisibility(View.INVISIBLE);
                    temperatureTv.setText(String.valueOf(mCurrentWeather.getTemperature()));
                }
                String formattedHumidity = String.format("%s", mCurrentWeather.getHumidity() + "%");
                humidityValueTv.setText(formattedHumidity);
                pressureValueTv.setText(String.valueOf(mCurrentWeather.getPressure() * 0.75));
                String uppercaseSummary = mCurrentWeather.getSummary()
                        .substring(0, 1)
                        .toUpperCase()
                        + mCurrentWeather.getSummary()
                        .substring(1);
                weatherSummaryTv.setText(uppercaseSummary);
            }
        });
    }

    /**
     * Hide refresh ImageButton and shows ProgressBar instead
     */
    private void toggleRefreshAnimationOn() {
        refreshIb.setVisibility(View.INVISIBLE);
        refreshPb.setVisibility(View.VISIBLE);
    }

    /**
     * Hide refresh ProgressBar and shows ImageButton instead
     */
    private void toggleRefreshAnimationOff() {
        refreshPb.setVisibility(View.INVISIBLE);
        refreshIb.setVisibility(View.VISIBLE);
    }

//    Private interface
    /**
     * Checks availability of location and network and request location from LocationWorker.
     * Shows Toast with error if location or network are unavailable.
     */
    private void getCurrentWeather() {
        /* Checks availability of location and network and request location from LocationWorker */
        ProvidersChecker providersChecker = ProvidersChecker.getInstance();
        if (providersChecker.isLocationAndNetworkAvailable(this)) {
            toggleRefreshAnimationOn();
            LocationWorker locationWorker = LocationWorker.getInstance();
            locationWorker.determineUserLocation(this);
        } else {
            /* Shows error Toast if location or network are unavailable */
            Toast.makeText(
                    this,
                    R.string.noLocationOrNetworkErrorMessage,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

//    Handle requestPermissions results
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                /* If request is cancelled, the result arrays are empty. */
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /* Permissions was granted, yay! */
                    getCurrentWeather();
                } else {
                    /* Permissions denied, boo! Shows toast with error */
                    Toast.makeText(
                            this,
                            R.string.permissionsDeniedErrorMessage,
                            Toast.LENGTH_LONG
                    ).show();
                }
        }
    }

    //    Implements Location events
    public void onEvent(LocationChangedEvent ev) {
        Location location = ev.getLocation();
        /* Build currentWeatherUrl and download data from server */
        LocationWorker locationWorker = LocationWorker.getInstance();
        UrlBuilder urlBuilder = UrlBuilder.getInstance();
        String currentWeatherUrl = urlBuilder.buildCurrentWeatherUrl(
                String.valueOf(locationWorker.getLatitude()),
                String.valueOf(location.getLongitude())
        );
        NetworkWorker networkWorker = NetworkWorker.getInstance();
        networkWorker.downloadCurrentWeatherData(currentWeatherUrl);
    }

    public void onEvent(LocationFailureEvent ev) {
    /* Shows Toast with error */
        Toast.makeText(
                this,
                ev.getFailureMessage(),
                Toast.LENGTH_LONG
        ).show();
        /* Build currentWeatherUrl and download data from server */
        LocationWorker locationWorker = LocationWorker.getInstance();
        UrlBuilder urlBuilder = UrlBuilder.getInstance();
        String currentWeatherUrl = urlBuilder.buildCurrentWeatherUrl(
                String.valueOf(locationWorker.getLatitude()),
                String.valueOf(locationWorker.getLongitude())
        );
        NetworkWorker networkWorker = NetworkWorker.getInstance();
        networkWorker.downloadCurrentWeatherData(currentWeatherUrl);
    }

//    Implements Network events
    public void onEvent(CurrentWeatherResponseEvent ev) {
        String response = ev.getResponse();
        /* Trying to parse response and build CurrentWeather object */
        try {
            ResponseParser responseParser = ResponseParser.getInstance();
            mCurrentWeather = responseParser.parseCurrentWeatherResponse(response);
        } catch (JSONException e) {
            e.printStackTrace();
            /* This will create a mock CurrentWeather object */
            mCurrentWeather = new CurrentWeather();
        }
        /* Trying to reverse LAT & LONG into a place and set it as CurrentWeather place */
        LocationWorker locationWorker = LocationWorker.getInstance();
        double latitude = locationWorker.getLatitude();
        double longitude = locationWorker.getLongitude();
        try {
            GeocoderUtils geocoderUtils = GeocoderUtils.getInstance();
            List<Address> addresses = geocoderUtils.getPlaceFromLocation(
                    this, latitude, longitude);
            if (addresses.size() > 0) {
                if (addresses.get(0).getLocality() != null) {
                    mCurrentWeather.setPlace(addresses.get(0).getLocality());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        /* Hooray! CurrentWeather object is ready. Call updateUi(). */
        updateUi();
    }

    public void onEvent(CurrentWeatherFailureEvent ev) {
        /* Create a mock CurrentWeather object and call updateUi() */
        mCurrentWeather = new CurrentWeather();
        updateUi();
    }
}
