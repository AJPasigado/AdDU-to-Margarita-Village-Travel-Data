package com.example.ajpasigado.traveldata;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Home extends AppCompatActivity {
    public static final String MY_PREFS_NAME = "MyPrefs";

    TextView tv_msec;
    TextView tv_h;
    TextView tv_min;
    TextView tv_sec;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        tv_msec = findViewById(R.id.tv_msec);
        tv_h = findViewById(R.id.tv_h);
        tv_min = findViewById(R.id.tv_min);
        tv_sec = findViewById(R.id.tv_sec);

        TextView datetv = findViewById(R.id.weather_date);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");
        datetv.setText(formatter.format(date));

        TextView weather_text = findViewById(R.id.weather_type);
        TextView weather_temp = findViewById(R.id.weather_temp);
        TextView weather_updated = findViewById(R.id.weather_updated);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean("timerIsRunning", false)) {
            seconds = System.currentTimeMillis() - prefs.getLong("timeElapsed", System.currentTimeMillis());
            pauseTimer = prefs.getBoolean("pasueTime", false);
            start_time = prefs.getLong("timeElapsed", System.currentTimeMillis());
            timerIsRunning = true;
            setStopwatch();
        }

        weather_text.setText(prefs.getString("weatherText", "Sunny"));
        weather_temp.setText(prefs.getString("weatherValue", "0") + "°");
        weather_updated.setText(prefs.getString("lastUpdate", "Never"));

        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0,0);
    }

    public void manualRefresh(View v){
        refreshWeather();
    }

    private void refreshWeather(){
        URL weatherUrl = Temperature.buildURLforWeather();
        new FetchWeatherDetails().execute(weatherUrl);
    }

    Long seconds = 0l;
    Timer stopwatch = new Timer();
    boolean pauseTimer = false;
    boolean timerIsRunning;
    Long start_time = 0l;
    Long time_paused = 0l;


    public void startStopwatch(View v) {
        setStopwatch();
    }

    private void setStopwatch(){
        if (!timerIsRunning) start_time = System.currentTimeMillis();
        stopwatch = new Timer();
        timerIsRunning = true;
        stopwatch.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!pauseTimer) {
                            tv_msec.setText(String.format("%02d", seconds % 100));
                            tv_sec.setText(String.format("%02d", (seconds / 1000) % 60));
                            tv_min.setText(String.format("%02d", ((seconds / 1000) / 60) % 60));
                            tv_h.setText(String.format("%02d", (seconds / 1000) / 3600));
                            seconds++;
                        }

                    }
                });
            }
        }, 0, 1);

        if (pauseTimer){
            tv_msec.setText(String.format("%02d", seconds % 100));
            tv_sec.setText(String.format("%02d", (seconds / 1000) % 60));
            tv_min.setText(String.format("%02d", ((seconds / 1000) / 60) % 60));
            tv_h.setText(String.format("%02d", (seconds / 1000) / 3600));
            Button pause = findViewById(R.id.button_pause);
            pause.setText("Resume");
        }
        findViewById(R.id.button_pause).setEnabled(true);
        findViewById(R.id.button_reset).setEnabled(true);
        findViewById(R.id.button_starttimer).setEnabled(false);
        findViewById(R.id.button_finish).setEnabled(false);
    }

    public void pauseStopWatch(View v){
        Button btn = findViewById(v.getId());
        btn.setText(!pauseTimer ? "Resume": "Pause");
        pauseTimer = !pauseTimer;
        if (pauseTimer) time_paused = System.currentTimeMillis();
        else {
            start_time += (System.currentTimeMillis() - time_paused);
            time_paused = 0l;
        }
        if (!pauseTimer)findViewById(R.id.button_finish).setEnabled(false);
        else findViewById(R.id.button_finish).setEnabled(true);
    }

    public void resetStopWatch(View v){
        stopwatch.cancel();
        seconds = 0l;
        pauseTimer = false;
        timerIsRunning = false;
        time_paused = 0l;
        findViewById(R.id.button_finish).setEnabled(false);

        Button pause = findViewById(R.id.button_pause);
        Button start = findViewById(R.id.button_starttimer);
        Button reset = findViewById(R.id.button_reset);

        pause.setText("Pause");
        pause.setEnabled(false);
        reset.setEnabled(false);
        start.setEnabled(true);

        tv_h.setText("00");
        tv_min.setText("00");
        tv_sec.setText("00");
        tv_msec.setText("00");
    }

    @Override
    protected void onStop() {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        if (pauseTimer) start_time += (System.currentTimeMillis() - time_paused);
        editor.putBoolean("pasueTime", pauseTimer);
        editor.putLong("timeElapsed", start_time);
        editor.putBoolean("timerIsRunning", timerIsRunning);
        editor.apply();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    private class FetchWeatherDetails extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL weatherUrl = urls[0];
            String weatherSearchResults = null;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.button_refresh_weather).setVisibility(View.INVISIBLE);
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                }
            });

            try {
                weatherSearchResults = Temperature.getResponsefromHTTP(weatherUrl);
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Home.this, "Failed to connect to server.", Toast.LENGTH_LONG).show();
                        findViewById(R.id.button_refresh_weather).setVisibility(View.VISIBLE);
                        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                    }
                });
            }
            return weatherSearchResults;
        }

        @Override
        protected void onPostExecute(String weatherSearchResults) {
            if(weatherSearchResults != null && !weatherSearchResults.equals("")) {
                try {
                    JSONArray results = new JSONArray(weatherSearchResults);
                    JSONObject resultsObj = results.getJSONObject(0);

                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();

                    TextView weather_text = findViewById(R.id.weather_type);
                    String weatherText = resultsObj.getString("WeatherText");
                    editor.putString("weatherText", weatherText);
                    weather_text.setText(weatherText);

                    String weatherValue = Integer.toString(resultsObj.getJSONObject("Temperature").getJSONObject("Metric").getInt("Value"));
                    editor.putString("weatherValue", weatherValue);
                    String minTemperature = weatherValue;

                    TextView weather_temp = findViewById(R.id.weather_temp);
                    weather_temp.setText(minTemperature + "°");

                    TextView weather_updated = findViewById(R.id.weather_updated);
                    Date date = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                    String dateFormat = formatter.format(date);
                    editor.putString("lastUpdate", dateFormat);
                    weather_updated.setText(formatter.format(date));

                    editor.apply();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.button_refresh_weather).setVisibility(View.VISIBLE);
                            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Home.this, "Failed to connect to server.", Toast.LENGTH_LONG).show();
                            findViewById(R.id.button_refresh_weather).setVisibility(View.VISIBLE);
                            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
            super.onPostExecute(weatherSearchResults);
        }
    }

    public void finish(View v){
        FinishDialog finishDialog = new FinishDialog();
        finishDialog.showDialog(this);
    }
}
