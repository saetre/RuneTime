package org.ubicompforall.runtime;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ubicompforall.cityexplorer.CityExplorer;
import org.ubicompforall.runtime.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.Time;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class RunetimeActivity extends Activity {
	// private static final String TAG = "RuneTime";
	private static final String TAG = "cityTime";
	public static final int DEBUG = 1;

	public static final int REQUEST_KILL_BROWSER = 11;

	LocationPicker lp;
	CityContentConsumer cp;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Just call the below method and it will return the latitude and
		// longitude:
		lp = new LocationPicker(this);
		lp.retrieveLocation(); // And print it in the EditText
		showNotification();
	}// onCreate

	private void showNotification() {
		debug(-1, "Show notification" );
		Context context = this;
		//String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );

		int icon = R.drawable.ic_launcher;
		CharSequence tickerText = "Hello";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		CharSequence contentTitle = "My notification";
		CharSequence contentText = "Hello World!";

		//To make an Intent with no action, use this instead of Intent:
		notification.setLatestEventInfo(context, contentTitle, contentText, PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0) );
	
		//Intent showIntent = new Intent(context, org.ubicompforall.cityexplorer.CityExplorer.class);
		Intent showIntent = new Intent();
		Time now = new Time();
		now.setToNow();
		showIntent.putExtra( "time", now.toString() );
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, showIntent, 0); 

	    notification.setLatestEventInfo(context, "Arriving at POI:", "Poi-ID", contentIntent);

		mNotificationManager.notify(999, notification);
	
	}//showNotification

	@Override
	public void onPause() {
		super.onPause();
		lp.disableProviders();
	}// onPause

	/*******************************************************************
	 * STATIC METHODS
	 */

	/***
	 * Debug method to include the filename, line-number and method of the
	 * caller
	 */
	public static void debug(int d, String msg) {
		if (DEBUG >= d) {
			StackTraceElement[] st = Thread.currentThread().getStackTrace();
			int stackLevel = 2;
			while (stackLevel < st.length - 1
					&& (st[stackLevel].getMethodName().equals("debug") || st[stackLevel]
							.getMethodName().matches("access\\$\\d+"))) {
				// || st[stackLevel].getMethodName().matches("run")
				stackLevel++;
			}
			StackTraceElement e = st[stackLevel];
			if (d < 0) { // error
				Log.e(TAG,
						e.getMethodName() + ": " + msg + " at ("
								+ e.getFileName() + ":" + e.getLineNumber()
								+ ")");
			} else { // debug
				Log.d(TAG,
						e.getMethodName() + ": " + msg + " at ("
								+ e.getFileName() + ":" + e.getLineNumber()
								+ ")");
			}// if debug, else error
		} // if verbose enough
	} // debug

	
	public static String connect(String url) {
		String result = "";
		
		HttpClient httpclient = new DefaultHttpClient();
		// Prepare a request object
		HttpGet httpget = new HttpGet(url);
		// Execute the request
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			// Examine the response status
			debug(0, response.getStatusLine().toString() );

			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release

			if (entity != null) {
				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				result = convertStreamToString(instream);
				// now you have the string representation of the HTML request
				instream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}//connect
	
	private static String convertStreamToString(InputStream is) {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();

	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append(line + "\n");
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return sb.toString();
	}//convertStreamToString


	// Information Screen Methods

	private void updateCityBusStop(String address) {
		EditText edit = (EditText) findViewById(R.id.nearBus_edit);
		String text;

		//address = replaceSeries( address );
		address = address.replaceAll( "-\\d+", "" );
		debug(-1, "Address: "+address );
		String httpGet = "http://busstjener.idi.ntnu.no/busstuc/oracle?q=";
		if ( ! ensureConnected( this ) ){ //For downloading Address and buses
			showNoConnectionDialog( this, "", "", null );
		}
		try {
			text = connect(httpGet + URLEncoder.encode("fra "+address, "utf-8") );
		} catch (UnsupportedEncodingException e) {
			text = "No Bus Found";
		}
		edit.setText(text);
	}// updateCityBusStop

	public void updatePoiList(Double[] myPos, TreeMap<String, Double[]> pois) {
		TreeMap<Integer, String> distances = new TreeMap<Integer, String>();
		EditText edit = (EditText) findViewById(R.id.poi_edit);
		for (Entry<String, Double[]> name_pos : pois.entrySet()) {
			float[] results = new float[] { 0, 0, 0 };
			Location.distanceBetween(myPos[0], myPos[1],
					name_pos.getValue()[0], name_pos.getValue()[1], results);
			distances.put(Math.round(results[0]), name_pos.getKey());
		}
		// Print Sorted
		String text = "", SEPARATOR = "";
		int count = 0, limit = 5;
		for (Entry<Integer, String> dist_name : distances.entrySet()) {
			if (count++ > limit)
				break;
			text += SEPARATOR + dist_name.getValue() + ": "
					+ dist_name.getKey();
			SEPARATOR = "\n";
		}
		edit.setText(text);
	}//updatePoiList(latitude, longitude)

	public void updateLocationScreens(double latitude, double longitude) {
		EditText edit = (EditText) findViewById(R.id.location_edit);
		String text = latitude + " " + longitude;
		String address = "";
		try {
			address = new Geocoder(this)
					.getFromLocation(latitude, longitude, 1).get(0)
					.getAddressLine(0);
			text = address;
		} catch (IOException e) {
			text += "No Address Found!!!";
		}
		edit.setText(text);

		cp = new CityContentConsumer(this);
		TreeMap<String, Double[]> pois = cp.getAllPois();
		updatePoiList(new Double[] { latitude, longitude }, pois);
		updateCityBusStop(address);
	}// updateLocationScreens(latitude, longitude)

	
	//HELPER METHODS
	
	public static boolean ensureConnected( Context myContext ) {
		ConnectivityManager connectivityManager	= (ConnectivityManager) myContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = null;
		if (connectivityManager != null) {
		    networkInfo = connectivityManager.getActiveNetworkInfo();
		}
		if ( networkInfo == null ){
			return false; //Network is not enabled
		}else{

			boolean activated = networkInfo.getState() == NetworkInfo.State.CONNECTED ? true : false ;
/**			if ( activated ){
				//Ping Google
				activated = verifyGoogleConnection ( context );
			}
*/
			//Toast.makeText( context, "Network state is "+networkInfo.getState(), Toast.LENGTH_LONG).show();
			return activated;
		}
	} // isConnected


	public static void showProgressDialog( Context context, String... msg ){
		String status = "Loading";
		if ( ! (msg == null || msg.length==0 || msg[0].equals("") ) ){
			status = msg[0];
		}
		ProgressDialog pd = ProgressDialog.show( context, "", status+"...", true, false);
		timerDelayRemoveDialog(1000, pd);
	}// showProgress

	/***
	 * Ping Google
	 * Start a browser if the page contains a (log-in) "redirect="
	 */
    public static boolean pingConnection( Activity context, String url ) {
    	boolean urlAvailable = false;
		if ( ensureConnected(context) ){
			showProgressDialog( context );
			HttpClient httpclient = new DefaultHttpClient();
			try {
			    HttpResponse response = httpclient.execute( new HttpGet( url ) );
				StatusLine statusLine = response.getStatusLine();
				debug(2, "statusLine is "+statusLine );

				// HTTP status is OK even if not logged in to NTNU
				//Toast.makeText( context, "Status-line is "+statusLine, Toast.LENGTH_LONG).show();
				if( statusLine.getStatusCode() == HttpStatus.SC_OK ) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					String responseString = out.toString();
					if ( responseString.contains( "redirect=" ) ) {	// Connection to url should be checked.
						debug(2, "Redirect detected for url: "+url );
						//Toast.makeText( context, "Mismatched url: "+url, Toast.LENGTH_LONG).show();
					}else{
						urlAvailable = true;
					}// if redirect page, else probably OK
				}else{//if status OK, else: Closes the connection on failure
					response.getEntity().getContent().close();
				}//if httpStatus OK, else close

				//Start browser to log in
				if ( ! urlAvailable ) {
					//throw new IOException( statusLine.getReasonPhrase() );

					//String activity = Thread.currentThread().getStackTrace()[3].getClassName();
					Toast.makeText( context, "Web access needed! Are you logged in?", Toast.LENGTH_LONG).show();
					//Uri uri = Uri.parse( url +"#"+ context.getClass().getCanonicalName() );
					Uri uri = Uri.parse( url +"?activity="+ context.getClass().getCanonicalName() );
					debug(0, "Pinging magic url: "+uri );
					debug(0, " Need the web for uri: "+uri );
					context.startActivityForResult( new Intent(Intent.ACTION_VIEW, uri ), REQUEST_KILL_BROWSER );
					//urlAvailable=true;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IllegalStateException e){	// Caused by bad url for example, missing http:// etc. Can still use cached maps...
				urlAvailable=false;
				debug(0, "Missing http:// in "+url+" ?" );
			} catch (IOException e) { // e.g. UnknownHostException // try downloading db's from the Web, catch (and print) exceptions
				e.printStackTrace();
				urlAvailable=false;
			}
		} // if not already loaded once before
		return urlAvailable;
	}// pingConnection

	/**
     * Display a dialog that user has no Internet connection
     * Code from: http://osdir.com/ml/Android-Developers/2009-11/msg05044.html
     */
	public static void showNoConnectionDialog( final Context myContext, final String msg, final String cancelButtonStr, final Intent cancelIntent ) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
		builder.setCancelable(true);
		if ( msg == "" ){
		    builder.setMessage( "No Connection" );
		}else{
		    builder.setMessage( msg );
		}
		builder.setTitle( "No Connection" );
		builder.setPositiveButton( "Settings", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		        myContext.startActivity( new Intent(Settings.ACTION_WIRELESS_SETTINGS) );
		    }
		} );

		String cancelText = cancelButtonStr;
		if ( cancelText == ""){
			cancelText = "Cancel";
		}
		builder.setNegativeButton( cancelText, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if ( cancelIntent != null ){
					if (myContext instanceof Activity){
						((Activity) myContext).startActivityForResult( cancelIntent, CityExplorer.REQUEST_LOCATION );
					}else{
						debug(-1, "This is not an Activity!!" );
					}
					dialog.dismiss();
		    	}
				return;
		    }
		} );

		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
		    public void onCancel(DialogInterface dialog) {
		    	if ( myContext == null ){
		    		debug(0, "OOOPS!");
		    	}else{
		    		Toast.makeText( myContext, "CANCELLED!", Toast.LENGTH_LONG).show();
					if (cancelIntent != null){
						myContext.startActivity( cancelIntent );
					}
		    	}
		        return;
		    }
		} );

		builder.show();
		//DATACONNECTION_NOTIFIED = true;
	} // showNoConnectionDialog

	/***
	 * @param time	In milliseconds
	 * @param d
	 */
	public static void timerDelayRemoveDialog(long time, final Dialog d){
	    new Handler().postDelayed(new Runnable() {
	        public void run(){
	        	if (d!= null){
	        		debug(2, "d is "+ d );
	        		d.dismiss();
	        	}
	        }
	    }, time);
	}//timerDelayRemoveDialog

}// class RunetimeActivity
