package com.makbe.weatherapp;

import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.makbe.weatherapp.data.Weather;
import com.makbe.weatherapp.data.WeatherData;
import com.makbe.weatherapp.utils.NetUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherDetailsActivity extends AppCompatActivity {
	private TextView textMain;
	private TextView textDescription;
	private TextView textTemperature;
	private TextView textHumidity;
	private TextView textWind;
	private ImageView conditionImage;
	private LottieAnimationView conditionAnimView;

	private ProgressBar progressBar;

	private String location;

	@Override
	protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

		MaterialToolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		ActionBar actionBar = getSupportActionBar();
		assert actionBar != null;

		actionBar.setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationIcon(R.drawable.ic_chevron_left);

		progressBar = findViewById(R.id.progressBar);

		conditionImage = findViewById(R.id.image_condition);
		conditionAnimView = findViewById(R.id.lottie_anim_condition);

		textMain = findViewById(R.id.text_main);
		textDescription = findViewById(R.id.text_description);

		textTemperature = findViewById(R.id.text_temp);
		textHumidity = findViewById(R.id.text_humidity);
		textWind = findViewById(R.id.text_wind);

		MaterialButton refreshBtn = findViewById(R.id.btn_refresh);
		refreshBtn.setOnClickListener(view -> {
			progressBar.setVisibility(View.VISIBLE);
			executeFetch();
		});

		WeatherData weatherData = getWeatherData();
		updateUI(weatherData);
	}

	private void executeFetch() {
		if (NetUtils.isNetworkAvailable(getApplicationContext())) {
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			Handler handler = new Handler(Looper.getMainLooper());

			executorService.execute(() -> Weather.fetchWeatherData(this, location, new Weather.WeatherCallback() {
				@Override
				public void onSuccess(WeatherData data) {
					handler.post(() -> progressBar.setVisibility(View.GONE));
					Log.d("DATA", "onSuccess: " + data);
					handler.post(() -> updateUI(data));
				}

				@Override
				public void onFailure(String errorMessage) {
					handler.post(() -> {
						progressBar.setVisibility(View.GONE);
						Toast.makeText(WeatherDetailsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
					});
				}
			}));
		} else {
			Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
		}
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

		switch (icon) {
			case "02n", "03n", "04n" -> handleAnimation("weather-cloudy-night.json");
			case "02d", "03d", "04d" -> handleAnimation("weather-cloudy-day.json");
			case "09n", "10n" -> handleAnimation("weather-rainy-night.json");
			case "09d", "10d" -> handleAnimation("weather-rainy-day.json");
			case "11n", "11d" -> handleAnimation("weather-storm.json");
			case "01n" -> handleAnimation("weather-night.json");
			case "01d" -> handleAnimation("weather-sunny.json");
			default -> displayDefaultIcon(icon);
		}

		conditionAnimView.playAnimation();

		textMain.setText(main);
		textDescription.setText(description);

		textTemperature.setText("Temperature: " + temperature);
		textHumidity.setText(String.valueOf(humidity));
		textWind.setText(String.valueOf(wind));

	}

	private void displayDefaultIcon(String icon) {
		conditionImage.setVisibility(View.VISIBLE);
		conditionAnimView.setVisibility(View.GONE);

		String BASE_IMG_URL = "https://openweathermap.org/img/wn/";
		String iconUrl = BASE_IMG_URL + icon + "@2x.png";

		Glide.with(this)
				.load(iconUrl)
				.into(conditionImage);
	}

	private void handleAnimation(String animationFile) {
		try {
			InputStream inputStream = getAssets().open(animationFile);
			int size = inputStream.available();
			byte[] buffer = new byte[size];
			inputStream.read(buffer);
			inputStream.close();

			String jsonContent = new String(buffer, StandardCharsets.UTF_8);
			conditionAnimView.setAnimationFromJson(jsonContent);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			getOnBackPressedDispatcher().onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
