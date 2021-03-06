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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Home extends AppCompatActivity {
    public static final String MY_PREFS_NAME = "MyPrefs";

    TextView tv_msec;
    TextView tv_h;
    TextView tv_min;
    TextView tv_sec;

    String weather_type;
    String route;
    String jeepType;

    ListView listView;
    ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        tv_msec = findViewById(R.id.tv_msec);
        tv_h = findViewById(R.id.tv_h);
        tv_min = findViewById(R.id.tv_min);
        tv_sec = findViewById(R.id.tv_sec);

        listView = findViewById(R.id.listView_history);

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
        } else {
            seconds = prefs.getLong("seconds", 0);
            start_time = prefs.getLong("timeElapsed", System.currentTimeMillis());

            tv_msec.setText(String.format("%02d", (seconds % 1000) / 10));
            tv_sec.setText(String.format("%02d", (seconds / 1000) % 60));
            tv_min.setText(String.format("%02d", ((seconds / 1000) / 60) % 60));
            tv_h.setText(String.format("%02d", (seconds / 1000) / 3600));
        }

        weather_type = prefs.getString("weatherText", "Sunny");
        weather_text.setText(weather_type);
        weather_temp.setText(prefs.getString("weatherValue", "0") + "°");
        weather_updated.setText(prefs.getString("lastUpdate", "Never"));

        route = prefs.getString("route", "Sasa");
        jeepType = prefs.getString("type", "Multicab");

        RadioButton jeep = findViewById(R.id.radioButton_type_jeep);
        jeep.setChecked(jeepType.equals("Jeep"));

        RadioButton sasa = findViewById(R.id.radioButton_route_sasa);
        sasa.setChecked(route.equals("Sasa"));

        RadioButton don = findViewById(R.id.radioButton_route_pilar);
        don.setChecked(route.equals("Doña Pilar"));

        RadioButton sasavc = findViewById(R.id.radioButton_route_sasavc);
        sasavc.setChecked(route.equals("Sasa via Cabaguio"));

        RadioButton tib = findViewById(R.id.radioButton_route_tib);
        tib.setChecked(route.equals("Tibungco - Roxas"));

        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0,0);

        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<HashMap<String, String>>>() {
        }.getType();
        list = gson.fromJson(prefs.getString("list", list.toString()), type);

        adapter = new SimpleAdapter(this, list, R.layout.listview_row,
                new String[]{"dayOfWeek", "month", "minutes", "route"}, new int[]{R.id.lv_tv_day, R.id.lv_tv_month, R.id.lv_tv_time, R.id.lv_tv_route});

        listView.setAdapter(adapter);

        final SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();

        RadioGroup rgs = findViewById(R.id.radioGroup_route);
        rgs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch(checkedId)
                {
                    case R.id.radioButton_route_sasa:
                        route = "Sasa";
                        editor.putString("route", route);
                        editor.apply();
                        break;
                    case R.id.radioButton_route_pilar:
                        route = "Doña Pilar";
                        editor.putString("route", route);
                        editor.apply();
                        break;
                    case R.id.radioButton_route_sasavc:
                        route = "Sasa via Cabaguio";
                        editor.putString("route", route);
                        editor.apply();
                        break;
                    case R.id.radioButton_route_tib:
                        route = "Tibungco - Roxas";
                        editor.putString("route", route);
                        editor.apply();
                        break;
                }
            }
        });

        RadioGroup rgt = findViewById(R.id.radioGroup_type);
        rgt.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch(checkedId)
                {
                    case R.id.radioButton_type_jeep:
                        jeepType = "Jeep";
                        editor.putString("type", jeepType);
                        editor.apply();
                        break;
                    case R.id.radioButton_type_mult:
                        jeepType = "Multicab";
                        editor.putString("type", jeepType);
                        editor.apply();
                        break;
                }
            }
        });
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


    public void startStopwatch(View v) {
        resetTime();
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
                    tv_msec.setText(String.format("%02d", (seconds % 1000) / 10));
                    tv_sec.setText(String.format("%02d", (seconds / 1000) % 60));
                    tv_min.setText(String.format("%02d", ((seconds / 1000) / 60) % 60));
                    tv_h.setText(String.format("%02d", (seconds / 1000) / 3600));
                    seconds++;
                }

                }
            });
            }
        }, 0, 1);

        findViewById(R.id.button_pause).setEnabled(true);
        findViewById(R.id.button_starttimer).setEnabled(false);
        findViewById(R.id.button_finish).setEnabled(false);
    }

    public void pauseStopWatch(View v){
        pauseTimer = true;
        timerIsRunning = false;
        findViewById(R.id.button_pause).setEnabled(false);
        findViewById(R.id.button_finish).setEnabled(true);
        findViewById(R.id.button_starttimer).setEnabled(true);
    }

    private void resetTime(){
        stopwatch.cancel();
        seconds = 0l;
        pauseTimer = false;
        timerIsRunning = false;
        findViewById(R.id.button_finish).setEnabled(false);

        Button pause = findViewById(R.id.button_pause);
        Button start = findViewById(R.id.button_starttimer);

        pause.setEnabled(false);
        start.setEnabled(true);

        tv_h.setText("00");
        tv_min.setText("00");
        tv_sec.setText("00");
        tv_msec.setText("00");
    }

    @Override
    protected void onStop() {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();

        editor.putBoolean("timerIsRunning", timerIsRunning);
        editor.putBoolean("pasueTime", pauseTimer);
        editor.putLong("timeElapsed", start_time);
        editor.putLong("seconds", seconds);
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
                    weather_type = weatherText;
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

    private void getItems() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://script.google.com/macros/s/AKfycbwNkRkA212jx6RyVe1hfSBwDXhQvw2DWI7U6Cmazoz_EsXFYiM/exec?action=getItems",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseItems(response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );

        int socketTimeOut = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        stringRequest.setRetryPolicy(policy);

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);

    }


    private void parseItems(String jsonResposnce) {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        try {
            JSONObject jobj = new JSONObject(jsonResposnce);
            JSONArray jarray = jobj.getJSONArray("items");

            Log.i("Here", jarray.toString());
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jo = jarray.getJSONObject(i);

                HashMap<String, String> item = new HashMap<>();
                item.put("month", jo.getString("month"));
                item.put("timeOfDeparture", jo.getString("timeOfDeparture"));
                item.put("minutes", jo.getString("minutes"));
                item.put("dayOfWeek", jo.getString("dayOfWeek"));
                item.put("route", jo.getString("route"));
                item.put("weather", jo.getString("weather"));
                item.put("jeepType", jo.getString("jeepType"));

                list.add(item);


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        try {
            Gson gson = new Gson();
            editor.putString("list", gson.toJson(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.apply();

        adapter = new SimpleAdapter(this,list,R.layout.listview_row,
                new String[]{"dayOfWeek","month","minutes", "route"},new int[]{R.id.lv_tv_day,R.id.lv_tv_month,R.id.lv_tv_time,R.id.lv_tv_route});

        listView.setAdapter(adapter);

       findViewById(R.id.button_refresh_history).setVisibility(View.VISIBLE);
       findViewById(R.id.progressBar_history).setVisibility(View.INVISIBLE);
    }

    public void finish(View v){
        FinishDialog finishDialog = new FinishDialog();
        finishDialog.showDialog(this, start_time, seconds, route, jeepType, weather_type);
    }

    public void refreshHistory(View v){
        findViewById(R.id.button_refresh_history).setVisibility(View.INVISIBLE);
        findViewById(R.id.progressBar_history).setVisibility(View.VISIBLE);

        getItems();
    }
}
