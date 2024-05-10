package com.makbe.weatherapp;

import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.makbe.weatherapp.data.Weather;
import com.makbe.weatherapp.data.WeatherData;
import com.makbe.weatherapp.utils.NetUtils;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherDetailsActivity extends AppCompatActivity {
	private TextView textMain;
	private TextView textDescription;
	private TextView textTemperature;
	private TextView textHumidity;
	private TextView textWind;
	private ImageView conditionImage;

	private String location;

	@Override
	protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

		MaterialToolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		conditionImage = findViewById(R.id.image_condition);

		textMain = findViewById(R.id.text_main);
		textDescription = findViewById(R.id.text_description);

		textTemperature = findViewById(R.id.text_temp);
		textHumidity = findViewById(R.id.text_humidity);
		textWind = findViewById(R.id.text_wind);

		MaterialButton refreshBtn = findViewById(R.id.btn_refresh);
		refreshBtn.setOnClickListener(view -> executeFetch());

		WeatherData weatherData = getWeatherData();
		updateUI(weatherData);
	}

	private @NotNull WeatherData getWeatherData() {
		Intent intent = getIntent();
		String main = intent.getStringExtra("main");
		String description = intent.getStringExtra("description");
		String icon = intent.getStringExtra("icon");
		String name = intent.getStringExtra("name");
		double temperature = intent.getDoubleExtra("temperature", 0);
		double humidity = intent.getDoubleExtra("humidity", 0);
		double wind = intent.getDoubleExtra("wind", 0);

		return new WeatherData(main, description, icon, name, temperature, humidity, wind);
	}

	private void executeFetch() {
		if (NetUtils.isNetworkAvailable(getApplicationContext())) {
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			Handler handler = new Handler(Looper.getMainLooper());

			executorService.execute(() -> Weather.fetchWeatherData(this, location, new Weather.WeatherCallback() {
				@Override
				public void onSuccess(WeatherData data) {
					Log.d("DATA", "onSuccess: " + data);
					handler.post(() -> updateUI(data));
				}

				@Override
				public void onFailure(String errorMessage) {
					handler.post(() -> Toast.makeText(WeatherDetailsActivity.this, errorMessage, Toast.LENGTH_LONG).show());
				}
			}));
		} else {
			Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
		}
	}

	private void updateUI(WeatherData data) {
			double temperature = data.temperature();
			double humidity = data.humidity();
			double wind = data.wind();

			String main = data.main();
			String description = data.description();
			String icon = data.icon();

			String name = data.name();
			location = name;
			setTitle(name);

		String BASE_IMG_URL = "https://openweathermap.org/img/wn/";
		String iconUrl = BASE_IMG_URL + icon + "@2x.png";
			Glide.with(this)
					.load(iconUrl)
					.into(conditionImage);

			textMain.setText(main);
			textDescription.setText(description);

			textTemperature.setText("Temperature: " + temperature);
			textHumidity.setText("Humidity: " + humidity);
			textWind.setText("Wind: " + wind);

	}
}
