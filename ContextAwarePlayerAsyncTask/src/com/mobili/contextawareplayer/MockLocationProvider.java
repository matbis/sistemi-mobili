package com.mobili.contextawareplayer;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

public class MockLocationProvider {
	
	String providerName;
	Context ctx;
	
	public MockLocationProvider(String providerName, Context ctx) {
		this.providerName = providerName;
		this.ctx = ctx;
		
		LocationManager locationManager = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
		locationManager.addTestProvider(providerName, false, false, false, false, false, true, true, 0, 5);
		locationManager.setTestProviderEnabled(providerName, true);
	}
	
	public void pushLocation(double lat, double lon) {
		LocationManager locationManager = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
		
		Location mockLocation = new Location(providerName);
		mockLocation.setLatitude(lat);
		mockLocation.setLongitude(lon);
		mockLocation.setAltitude(0);
		mockLocation.setTime(System.currentTimeMillis());
		locationManager.setTestProviderLocation(providerName, mockLocation);
	}
	
	public void shutdown() {
		LocationManager locationManager = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeTestProvider(providerName);
	}
	
	

}
