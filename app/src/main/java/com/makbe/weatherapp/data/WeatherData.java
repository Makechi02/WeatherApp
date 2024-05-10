package com.makbe.weatherapp.data;

public record WeatherData(
		String main,
		String description,
		String icon,
		String name,
		double temperature,
		double humidity,
		double wind
) {}
