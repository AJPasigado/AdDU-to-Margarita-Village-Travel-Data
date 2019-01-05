package com.example.ajpasigado.traveldata;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FinishDialog extends Activity {

    EditText tv_day;
    EditText tv_depart;
    EditText tv_time;
    EditText tv_route;
    EditText tv_type;
    EditText tv_weather;

    Date date;

    Activity activity;
    Dialog dialog;

    public void showDialog(final Activity activity, final Long start_time, final Long seconds, final String route, final String type, final String weather){
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_finish_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        this.activity = activity;

        date = new Date(start_time);
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE");
        String day = formatter.format(date);

        tv_day = dialog.findViewById(R.id.tv_dayOf);
        tv_day.setText(day);

        SimpleDateFormat formatter_depart = new SimpleDateFormat("HH:mm");
        String depart = formatter_depart.format(date);

        tv_depart = dialog.findViewById(R.id.tv_departure);
        tv_depart.setText(depart);

        tv_time = dialog.findViewById(R.id.tv_travelTime);
        tv_time.setText(Long.toString((seconds / 1000) / 60));

        tv_route = dialog.findViewById(R.id.tv_route);
        tv_route.setText(route);

        tv_type = dialog.findViewById(R.id.tv_type);
        tv_type.setText(type);

        tv_weather = dialog.findViewById(R.id.tv_weather);
        tv_weather.setText(weather);

        final Button confirm = dialog.findViewById(R.id.button_confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemToSheet(dialog, confirm, activity);
            }
        });
        dialog.show();
    }

    private void  addItemToSheet(final Dialog dialog, final Button confirm, final Activity activity) {
        dialog.findViewById(R.id.progressBar3).setVisibility(View.VISIBLE);
        confirm.setVisibility(View.GONE);
        dialog.setCancelable(false);

        SimpleDateFormat formatter_depart = new SimpleDateFormat("yyyy-MM-dd");

        final String month = formatter_depart.format(date);
        final String timeOfDeparture = tv_depart.getText().toString().trim();
        final String minutes = tv_time.getText().toString().trim();
        final String dayOfWeek = tv_day.getText().toString().trim();
        final String route = tv_route.getText().toString().trim();
        final String weather = tv_weather.getText().toString().trim();
        final String jeepType = tv_type.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://script.google.com/macros/s/AKfycbwNkRkA212jx6RyVe1hfSBwDXhQvw2DWI7U6Cmazoz_EsXFYiM/exec",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(activity, "Data added successfully.", Toast.LENGTH_LONG).show();
                        dialog.dismiss();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(activity, "There was an error in adding the data.", Toast.LENGTH_LONG).show();
                        dialog.findViewById(R.id.progressBar3).setVisibility(View.GONE);
                        confirm.setVisibility(View.VISIBLE);
                        dialog.setCancelable(true);
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                Map<String, String> parmas = new HashMap<>();

                parmas.put("action", "addItem");
                parmas.put("month", month);
                parmas.put("timeOfDeparture", timeOfDeparture);
                parmas.put("minutes", minutes);
                parmas.put("dayOfWeek", dayOfWeek);
                parmas.put("route", route);
                parmas.put("weather", weather);
                parmas.put("jeepType", jeepType);

                return parmas;
            }
        };

        int socketTimeOut = 30000;

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(activity);

        queue.add(stringRequest);
    }
}
