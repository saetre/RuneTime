package org.ubicompforall.runtime;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

//public class LocationPicker implements LocationListener, OnCancelListener{
public class LocationPicker extends AsyncTask<String, String, String> implements LocationListener, OnCancelListener{

	private RunetimeActivity context;
	private boolean stopFlag;
	private ProgressDialog dialog;
	private LocationManager locationManager;
	private double latitude, longitude;
	
	public LocationPicker(RunetimeActivity ctx) {
	    this.context = ctx;
	}
	
	public void debug(int level, String str){
		RunetimeActivity.debug(level,str);
	}

	@Override
    protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
    protected String doInBackground(String... params) {
	    initLocation( );
		return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        debug(-1, "Update the progress!");
		Toast.makeText(context, "Latitude : " + latitude + " Longitude : " + longitude , Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostExecute( String result ) {
        super.onPostExecute( result );
        Toast.makeText( context, "Done: "+result, Toast.LENGTH_LONG ).show();
    }

    public void disableProviders( ) {
		locationManager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
		locationManager.removeUpdates( this );
		debug(0, "Removed updates for "+locationManager.getProviders(true) );
        dialog.dismiss();
	}//disableProviders

	/**
	 * Init the Network/GPS/WiFi Location Provider.
	 */
	public void initLocation(){ // Context context ){
		String provider = getProvider();
		debug(0, "provider: "+provider );
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	
		Location lastKnownLocation = locationManager.getLastKnownLocation( provider );
		debug(0, "context is "+context );
		//if ( context instanceof RunetimeActivity ){
			onLocationChanged( lastKnownLocation );
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestLocationUpdates( provider, 0, 0, this );
		//}
	}//initLocation

	public String getProvider(){
	    locationManager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
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
	            while ( ! stopFlag) {
	                debug(0, "Wait for first position fix (do nothing until loc != null)" );
	                try {
						Thread.sleep( 1000 ); //One sec
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	            }
	            // After receiving first GPS Fix dismiss the Progress Dialog
	            //dialog.dismiss();
	        }
	    };
	
	    String provider = getProvider();
	    if (provider==null){
	    	RunetimeActivity.debug(-1, "Ooops! Provider == null" );
	    }else{
	    	debug(2, "Using provided: "+provider );
	    	locationManager.requestLocationUpdates(provider, 0, 0, this);
		    dialog = ProgressDialog.show(context, "Please wait...", "Retrieving Location data...", true);
		    dialog.setCancelable(true);
		    dialog.setOnCancelListener(this);
		    Thread t = new Thread( showWaitDialog );
		    t.start();
	    }
	}//retrieveLocation
	
	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			float[] results = new float[] { 0, 0, 0 }; // Meter[0], start-heading[1], stop-heading[2]
			Location.distanceBetween(latitude, longitude, location.getLatitude(), location.getLongitude(), results );
			if ( results[0] >100 ){
				latitude = location.getLatitude();
				longitude = location.getLongitude();

				debug(-1, "Moved "+results[0]+" meters in direction "+results[1] );
				context.updateLocationScreens(latitude, longitude);
				RunetimeActivity.showNotification( context.getBaseContext() );
			}
			stopFlag = true;
		}
		//locationManager.removeUpdates(this);
	}//onLocationChanged
	
	@Override
	public void onProviderDisabled(String provider) {
		debug(0, "Location_Provider DISABLED: "+provider );
		Toast.makeText(context, "Disabled: "+provider, Toast.LENGTH_LONG).show();
		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		context.startActivity(intent);
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(context, "Enabled: "+provider, Toast.LENGTH_SHORT).show(); //
		//ctx.startActivity(new Intent(ctx, LocationPickerActivity.class));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Toast.makeText(context, "Provider "+provider+", Status="+status, Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		debug(-1, "Why?" );
	    stopFlag = true;
	    locationManager.removeUpdates(this);
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
}//class LocationPicker

