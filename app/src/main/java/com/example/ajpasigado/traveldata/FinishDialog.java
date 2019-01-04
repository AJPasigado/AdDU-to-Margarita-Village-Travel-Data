package com.example.ajpasigado.traveldata;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FinishDialog extends Activity {
    public void showDialog(final Activity activity, final Long start_time, final Long seconds, final String route, final String type, final String weather){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_finish_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Date date = new Date(start_time);
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE");
        String day = formatter.format(date);

        TextView tv_day = dialog.findViewById(R.id.tv_dayOf);
        tv_day.setText(day);

        SimpleDateFormat formatter_depart = new SimpleDateFormat("HH:mm");
        String depart = formatter_depart.format(date);

        TextView tv_depart = dialog.findViewById(R.id.tv_departure);
        tv_depart.setText(depart);

        TextView tv_time = dialog.findViewById(R.id.tv_travelTime);
        tv_time.setText(Long.toString((seconds / 1000) / 60));

        TextView tv_route = dialog.findViewById(R.id.tv_route);
        tv_route.setText(route);

        TextView tv_type = dialog.findViewById(R.id.tv_type);
        tv_type.setText(type);

        TextView tv_weather = dialog.findViewById(R.id.tv_weather);
        tv_weather.setText(weather);

        final Button confirm = dialog.findViewById(R.id.button_confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.findViewById(R.id.progressBar3).setVisibility(View.VISIBLE);
                confirm.setVisibility(View.GONE);
                dialog.setCancelable(false);
            }
        });
        dialog.show();
    }
}
