package com.mobili.contextawareplayer;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class MyLocationListener implements LocationListener {
	
	Location currentBestLocation;
	
	@Override
	public void onLocationChanged(Location location) {
		if(isBetterLocation(location))
			this.currentBestLocation = location;

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
	
	public Location getCurrentBestLocation() {
		return this.currentBestLocation;
	}
	
	private boolean isBetterLocation(Location location) {
		if(this.currentBestLocation == null)
			// A new location is always better than no location
			return true;
		
		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - this.currentBestLocation.getTime();
		boolean isNewer = timeDelta > 0;
		
		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int)(location.getAccuracy() - this.currentBestLocation.getAccuracy());
		boolean isMoreAccurate = accuracyDelta < 0;
		
		if(isNewer && isMoreAccurate)
			return true;
		
		return false;
	}
	
	public boolean availableLocation() {
		return this.currentBestLocation != null;
	}

}
