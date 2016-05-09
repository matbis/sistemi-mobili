package com.mobili.contextawareplayer;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	MediaPlayer mediaPlayer;
	LocationManager locationManager;
	MyLocationListener myLocationListener;
	MockLocationProvider mockLocationProvider;
	final Location univLocation = new Location("");
	final Location homeLocation = new Location("");
	
	// TEST
	final double LatTest = 44.448;
	final double LonTest = 11.328;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// University: lat = 44.448, lon = 11.328
		univLocation.setLatitude(44.448);
		univLocation.setLongitude(11.328);
		univLocation.setAltitude(0);
		// Home: lat = 44.490, lon = 11.336
		homeLocation.setLatitude(44.490);
		homeLocation.setLongitude(11.336);
		homeLocation.setAltitude(0);		
		
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);		
		// Define a listener that responds to location updates
		myLocationListener = new MyLocationListener();
		
		Button button = (Button)findViewById(R.id.playButton);
		button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mediaPlayer.reset();
				
				TextView textView = (TextView)findViewById(R.id.locText);
				Uri myUri = null;
				
				if(myLocationListener.availableLocation()) {
					Location location = myLocationListener.getCurrentBestLocation();
					
					if(location.distanceTo(univLocation) < 20) {
						myUri = Uri.parse("android.resource://com.mobili.contextawareplayer/" + R.raw.alarm);
						textView.setText("Current location: UNIVERSITY");
					}
					else if(location.distanceTo(homeLocation) < 20) {
						myUri = Uri.parse("android.resource://com.mobili.contextawareplayer/" + R.raw.beeping);
						textView.setText("Current location: HOME");
					} else {
						textView.setText("Unknown location");
					}
					
					try {
						mediaPlayer.setDataSource(getApplicationContext(), myUri);
						mediaPlayer.prepare();
						mediaPlayer.start();
					} catch(Exception e) {
						System.err.println("Error in parsing audio file: onClick() method");
					}
				} else
					textView.setText("Current position not yet detected. Hold on please...");
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		mediaPlayer = new MediaPlayer();
		
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
		
		// Create a mock location provider
		mockLocationProvider = new MockLocationProvider(LocationManager.GPS_PROVIDER, this);
		// Set test location
		mockLocationProvider.pushLocation(LatTest, LonTest);
	};
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mediaPlayer.release();
		mediaPlayer = null;
		// Remove the listener you previously added
		locationManager.removeUpdates(myLocationListener);
		// Remove mock location provider
		mockLocationProvider.shutdown();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
