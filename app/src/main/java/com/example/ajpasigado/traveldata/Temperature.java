package com.example.ajpasigado.traveldata;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Temperature {

    public static String WEATHER_API_URL = "http://dataservice.accuweather.com/currentconditions/v1/2-262966_1_AL";
    private static String api_key = "";

    public static URL buildURLforWeather(){
        Uri builtUri = Uri.parse(WEATHER_API_URL).buildUpon().appendQueryParameter("apikey", api_key).build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (Exception e){
            e.printStackTrace();
        }

        return url;
    }

    public static String getResponsefromHTTP(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {
            InputStream inputStream =  urlConnection.getInputStream();

            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else return  null;
        } finally {
            urlConnection.disconnect();
        }
    }
}
