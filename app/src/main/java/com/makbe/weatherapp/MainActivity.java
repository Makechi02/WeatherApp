package com.makbe.weatherapp;

import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.makbe.weatherapp.data.Weather;
import com.makbe.weatherapp.data.WeatherData;
import com.makbe.weatherapp.utils.NetUtils;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

	private String location;

	@Override
	protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		MaterialButton submitBtn = findViewById(R.id.btn_submit);
		TextInputLayout locationInputLayout = findViewById(R.id.layout_input_location);
		TextInputEditText locationInput = findViewById(R.id.input_location);

		submitBtn.setOnClickListener(view -> {
			if (Objects.requireNonNull(locationInput.getText()).toString().isBlank()) {
				locationInputLayout.setError("Location can't be blank!");
				locationInputLayout.requestFocus();
				return;
			}

			location = locationInput.getText().toString().trim();
			executeFetch();
		});

		locationInput.setOnKeyListener((v, keyCode, event) -> {
			locationInputLayout.setErrorEnabled(false);
			return false;
		});
	}

	private void executeFetch() {
		if (NetUtils.isNetworkAvailable(getApplicationContext())) {
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			Handler handler = new Handler(Looper.getMainLooper());

			executorService.execute(() -> Weather.fetchWeatherData(this, location, new Weather.WeatherCallback() {
				@Override
				public void onSuccess(WeatherData data) {

					Log.d("DATA", "onSuccess: " + data);

					Intent intent = new Intent(MainActivity.this, WeatherDetailsActivity.class);
					intent.putExtra("main", data.main());
					intent.putExtra("description", data.description());
					intent.putExtra("icon", data.icon());
					intent.putExtra("name", data.name());
					intent.putExtra("temperature", data.temperature());
					intent.putExtra("humidity", data.humidity());
					intent.putExtra("wind", data.wind());
					startActivity(intent);
				}

				@Override
				public void onFailure(String errorMessage) {
					handler.post(() -> Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show());
				}
			}));
		} else {
			Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
		}
	}
}
