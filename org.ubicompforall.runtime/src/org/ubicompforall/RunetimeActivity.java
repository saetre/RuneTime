package org.ubicompforall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ubicompforall.runetime.R;
import android.app.Activity;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class RunetimeActivity extends Activity {
	// private static final String TAG = "RuneTime";
	private static final String TAG = "cityTime";
	public static final int DEBUG = 1;

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
	}// onCreate

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
		String httpGet = "http://busstjener.idi.ntnu.no/busstuc/oracle?q=";
		//String httpGet = "busstjener.idi.ntnu.no/busstuc/oracle?q=";
		String text;
		try {
			text = connect(httpGet + URLEncoder.encode(address, "utf-8") );
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
	}// updateScreenLocation(latitude, longitude)

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
	}// updateScreenLocation(latitude, longitude)

}// class RunetimeActivity
