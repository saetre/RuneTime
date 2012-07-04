package org.ubicompforall;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

/******************************************************************
 * Private Class
 */
public class LocationPicker implements LocationListener, OnCancelListener{
	
	private RunetimeActivity ctx;
	private boolean stopFlag;
	private ProgressDialog dialog;
	private LocationManager locationManager;
	
	public LocationPicker(RunetimeActivity ctx) {
	    this.ctx = ctx;
	    initLocation( ctx );
	}
	
	public void debug(int level, String str){
		RunetimeActivity.debug(level,str);
	}
	
	public void disableProviders( ) {
		locationManager = (LocationManager) ctx.getSystemService( Context.LOCATION_SERVICE );
		locationManager.removeUpdates( this );
		debug(0, "Removed updates for "+locationManager.getProviders(true) );
	}//disableProviders

	/**
	 * Init the Network/GPS/WiFi Location Provider.
	 */
	public void initLocation( Context context ){
		String provider = getProvider();
		debug(0, "Init Location provider: "+provider );
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	
		Location lastKnownLocation = locationManager.getLastKnownLocation( provider );
		//debug(0, "context is "+context );
		//if ( context instanceof RunetimeActivity ){
			onLocationChanged( lastKnownLocation );
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestLocationUpdates( provider, 0, 0, this );
		//}
	}//initGPS

	public String getProvider(){
	    locationManager = (LocationManager) ctx.getSystemService( Context.LOCATION_SERVICE );
	    Criteria criteria = new Criteria();
	    //criteria.setAccuracy(Criteria.ACCURACY_FINE);
	    criteria.setAltitudeRequired(false);
	    criteria.setBearingRequired(false);
	    criteria.setCostAllowed(true);
	    criteria.setPowerRequirement(Criteria.POWER_LOW);
	    return locationManager.getBestProvider(criteria, true); //true: Enabled providers only
	}//getProvider
	
	public void retrieveLocation() {
	    Runnable showWaitDialog = new Runnable() {
	        @Override
	        public void run() {
	            while (!stopFlag) {
	                // Wait for first GPS Fix (do nothing until loc != null)
	            }
	            // After receiving first GPS Fix dismiss the Progress Dialog
	            dialog.dismiss();
	        }
	    };
	
	    String provider = getProvider();
	    if (provider==null){
	    	RunetimeActivity.debug(-1, "Ooops! Provider == null" );
	    }else{
	    	debug(2, "Using provided: "+provider );
	    	locationManager.requestLocationUpdates(provider, 0, 0, this);
		    dialog = ProgressDialog.show(ctx, "Please wait...", "Retrieving Location data...", true);
		    dialog.setCancelable(true);
		    dialog.setOnCancelListener(this);
		    Thread t = new Thread( showWaitDialog );
		    t.start();
	    }
	}//retrieveLocation
	
	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			stopFlag = true;
			//Toast.makeText(ctx, "Latitude : " + latitude + " Longitude : " + longitude , Toast.LENGTH_LONG).show();
			ctx.updateLocationScreens(latitude, longitude);
		}
		//locationManager.removeUpdates(this);
	}//onLocationChanged
	
	@Override
	public void onProviderDisabled(String provider) {
		debug(0, "Location_Provider DISABLED: "+provider );
		Toast.makeText(ctx, "Disabled: "+provider, Toast.LENGTH_LONG).show();
		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		ctx.startActivity(intent);
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(ctx, "Enabled: "+provider, Toast.LENGTH_SHORT).show(); //
		//ctx.startActivity(new Intent(ctx, LocationPickerActivity.class));
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Toast.makeText(ctx, "Provider "+provider+", Status="+status, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		debug(0, "Why?" );
	    stopFlag = true;
	    locationManager.removeUpdates(this);
	}
}//class LocationPicker

