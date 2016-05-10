package com.mobili.contextawareplayer;

import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	MediaPlayer mediaPlayer;
	LocationManager locationManager;
	MyLocationListener myLocationListener;
	MockLocationProvider mockLocationProvider;
	ConnectivityManager connManager;
	
	final Location univLocation = new Location("");
	final Location homeLocation = new Location("");
	
	// TEST
	final double LatTest = 44.490;
	final double LonTest = 11.336;
	
	
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
		
		// Acquire a reference to the system Connectivity Manager
		connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		
		// ProgressBar initially hidden
		ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
		progressBar.setVisibility(View.INVISIBLE);
		
		Button button = (Button)findViewById(R.id.playButton);
		button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mediaPlayer.reset();
				
				TextView textView = (TextView)findViewById(R.id.locText);
//				Uri myUri = null;
				URL myURL = null;
				
				if(myLocationListener.availableLocation()) {
					Location location = myLocationListener.getCurrentBestLocation();
					
					if(location.distanceTo(univLocation) < 20) {
//						myUri = Uri.parse("android.resource://com.mobili.contextawareplayer/" + R.raw.alarm);
						try {
							myURL = new URL("http://soundbible.com/grab.php?id=2087&type=mp3");
						} catch (Exception e) {
							System.out.println("Malformed URL UNIVERSITY sound");
						}
						textView.setText("Current location: UNIVERSITY");
					}
					else if(location.distanceTo(homeLocation) < 20) {
//						myUri = Uri.parse("android.resource://com.mobili.contextawareplayer/" + R.raw.beeping);
						try {
							myURL = new URL("http://soundbible.com/grab.php?id=2084&type=mp3");
						} catch(Exception e) {
							System.out.println("Malformed URL HOME sound");
						}
						textView.setText("Current location: HOME");
					} else {
						textView.setText("Unknown location");
					}
					
					// Download file with AsyncTask
					DownloadFilesTask downloadTask = new DownloadFilesTask();
					downloadTask.setContext(MainActivity.this);
					downloadTask.setProgressBar((ProgressBar)findViewById(R.id.progressBar));
					downloadTask.setMediaPlayer(mediaPlayer);
					downloadTask.setConnectivityManager(connManager);
					downloadTask.execute(myURL);
					
					// NB: moved to asyncTask
//					try {
//						mediaPlayer.setDataSource(getApplicationContext(), myUri);
//						mediaPlayer.prepare();
//						mediaPlayer.start();
//					} catch(Exception e) {
//						System.err.println("Error in parsing audio file: onClick() method");
//					}
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


// --------------------- ASYNC TASK ---------------------

class DownloadFilesTask extends AsyncTask<URL, Integer, Boolean> {

	Context context;
	ProgressBar progressBar;
	MediaPlayer mediaPlayer;
	ConnectivityManager connManager;
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		// Make visible progressBar in UI
		this.progressBar.setVisibility(View.VISIBLE);
		this.progressBar.setProgress(0);
		
		// Show pop-up message: start download
		Toast.makeText(context, "Downloading file...", Toast.LENGTH_SHORT).show();
	};
	
	@Override
	protected Boolean doInBackground(URL... urls) {
		// Check the network connection
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		
		if(networkInfo != null && networkInfo.isConnected()) {
			// Prepare mediaPlayer downloading content from network
			try {
				mediaPlayer.setDataSource(urls[0].toString());
				publishProgress(50);
				mediaPlayer.prepare();
				publishProgress(100);
				mediaPlayer.start();
				return true;
			} catch(Exception e) {
				System.err.println("Error in parsing audio file: doInBackground(...) method");
			}
		} else
			// Show pop-up message: connection error
			Toast.makeText(context, "No connection available!", Toast.LENGTH_LONG).show();;
		
		return false;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		
		if(this.progressBar != null)
			progressBar.setProgress(values[0]);
	};
	
	@Override
	protected void onPostExecute(Boolean result) {
		this.progressBar.setVisibility(View.INVISIBLE);
		
		// Show pop-up message: end download
		if(result)
			Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
	};
	
	protected void setContext(Context context) {
		this.context = context;
	}
	
	protected void setProgressBar(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}
	
	protected void setMediaPlayer(MediaPlayer mediaPlayer) {
		this.mediaPlayer = mediaPlayer;
	}
	
	protected void setConnectivityManager(ConnectivityManager connManager) {
		this.connManager = connManager;
	}
}
