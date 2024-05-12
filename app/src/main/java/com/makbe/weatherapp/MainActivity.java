package com.makbe.weatherapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
	private ProgressBar progressBar;

	private final int PERM_CODE = 1;

	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final Handler handler = new Handler(Looper.getMainLooper());

	@Override
	protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		MaterialButton submitBtn = findViewById(R.id.btn_submit);
		MaterialButton useDeviceBtn = findViewById(R.id.btn_use_device);
		TextInputLayout locationInputLayout = findViewById(R.id.layout_input_location);
		TextInputEditText locationInput = findViewById(R.id.input_location);
		progressBar = findViewById(R.id.progressBar);

		submitBtn.setOnClickListener(view -> {
			if (Objects.requireNonNull(locationInput.getText()).toString().isBlank()) {
				locationInputLayout.setError("Location can't be blank!");
				locationInputLayout.requestFocus();
				return;
			}

			progressBar.setVisibility(View.VISIBLE);
			location = locationInput.getText().toString().trim();
			FetchWeatherDetails();
		});

		useDeviceBtn.setOnClickListener(view -> {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERM_CODE);
			} else {
				FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
				client.getLastLocation()
						.addOnSuccessListener(this, location -> {
							if (location != null) {
								double latitude = location.getLatitude();
								double longitude = location.getLongitude();

								progressBar.setVisibility(View.VISIBLE);
								fetchWeatherByCoordinates(latitude, longitude);
							}
						});
			}
		});

		locationInput.setOnKeyListener((v, keyCode, event) -> {
			locationInputLayout.setErrorEnabled(false);
			return false;
		});
	}

	private void fetchWeatherByCoordinates(double latitude, double longitude) {
		if (NetUtils.isNetworkAvailable(getApplicationContext())) {
			executorService.execute(() -> Weather.fetchWeatherDataByCoordinates(this, latitude, longitude, new WeatherFetchCallBack()));
		} else {
			Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
		}
	}

	private void FetchWeatherDetails() {
		if (NetUtils.isNetworkAvailable(getApplicationContext())) {
			executorService.execute(() -> Weather.fetchWeatherData(this, location, new WeatherFetchCallBack()));
		} else {
			Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
		}
	}

	private class WeatherFetchCallBack implements Weather.WeatherCallback {

		@Override
		public void onSuccess(WeatherData data) {
			handler.post(() -> progressBar.setVisibility(View.GONE));

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
			handler.post(() -> {
				progressBar.setVisibility(View.GONE);
				Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
			});
		}
	}
}
