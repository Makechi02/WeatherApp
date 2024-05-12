package com.makbe.weatherapp.data;

import android.content.Context;
import com.makbe.weatherapp.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Weather {
	private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

	public interface WeatherCallback {
		void onSuccess(WeatherData data);
		void onFailure(String errorMessage);
	}

	public static void fetchWeatherData(Context context, String location, WeatherCallback callback) {
		String API_KEY = context.getResources().getString(R.string.api_key);

		try {
			URL url = new URL(BASE_URL + "?q=" + location + "&appid=" + API_KEY);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			int responseCode = connection.getResponseCode();

			if (responseCode == 200) {
				InputStream inputStream = connection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				StringBuilder response = new StringBuilder();
				String line;

				while ((line = reader.readLine()) != null) {
					response.append(line);
				}

				reader.close();
				inputStream.close();
				connection.disconnect();

				WeatherData weatherData = parseWeatherData(response.toString());
				callback.onSuccess(weatherData);
			} else if (responseCode == 404) {
				callback.onFailure("Unable to find location");
			}
		} catch (IOException e) {
			callback.onFailure(e.getMessage());
		}
	}

	private static WeatherData parseWeatherData(String JsonResponse) {
		try {
			JSONObject response = new JSONObject(JsonResponse);
			JSONObject mainResponse = response.getJSONObject("main");
			double temperature = Math.floor(mainResponse.getDouble("temp") - 273);
			double humidity = mainResponse.getDouble("humidity");
			double wind = response.getJSONObject("wind").getDouble("speed");

			JSONObject mainObject = response.getJSONArray("weather").getJSONObject(0);
			String main = mainObject.getString("main");
			String description = mainObject.getString("description");
			String icon = mainObject.getString("icon");

			String name = response.getString("name");

			return new WeatherData(main, description, icon, name, temperature, humidity, wind);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
