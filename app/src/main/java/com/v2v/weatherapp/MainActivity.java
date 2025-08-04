package com.v2v.weatherapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    Button searchBtn;
    EditText cityEt;
    TextView dataTv;
    FrameLayout progressOverlay;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        searchBtn = findViewById(R.id.search);
        cityEt = findViewById(R.id.cityName);
        dataTv = findViewById(R.id.data);
        progressOverlay = findViewById(R.id.progressOverlay);

        searchBtn.setOnClickListener(v -> {
            String city = cityEt.getText().toString().trim();
            if (!city.isEmpty()) {
                showProgress(true);
                searchWeatherData(city);
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress(boolean show) {
        runOnUiThread(() -> progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE));
    }

    private void searchWeatherData(String cityName) {
        new Thread(() -> {
            String apiKey = "4b2b722fc7d47a66166d1f746293155d";
            String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + apiKey + "&units=metric";

            try {
                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                parseWeatherData(result.toString());
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(MainActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void parseWeatherData(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject main = jsonObject.getJSONObject("main");
            final double temperature = main.getDouble("temp");

            JSONArray weatherArray = jsonObject.getJSONArray("weather");
            final String weatherDescription = weatherArray.getJSONObject(0).getString("description");

            runOnUiThread(() -> {
                dataTv.setText("Temperature: " + temperature + "°C\nDescription: " + weatherDescription);
                showProgress(false);
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> showProgress(false));
        }
    }
}