package org.ubicompforall.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.TreeMap;

import android.content.Context;

public class BusstopFinder {

	//private Context ctx;
	private TreeMap<String, double[]> pois;
	TreeMap<Integer, String> distances;

	public BusstopFinder( Context ctx ){
		pois = new TreeMap<String, double[]>();
		distances = new TreeMap<Integer, String>();
		BufferedReader br;
		try {
			br = new BufferedReader( new InputStreamReader( ctx.getAssets().open ( "latLngName.txt" ), "UTF-8" ) );
			String text="";
			while ( (text = br.readLine()) != null){
				String[] latLngName = text.split("\t");
				pois.put( latLngName[2], new double[]{ Double.parseDouble(latLngName[0]), Double.parseDouble(latLngName[1]) } );
			}
			RunetimeActivity.debug(-1, "Getting tired after "+pois.size()+" busstops?" );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}//CONSTRUCTOR

	public String getBusstops( int limit, double latitude, double longitude) {
		String name="Offline";
		name = RunetimeActivity.getClosest( new double[]{latitude,longitude}, limit, pois, distances);
		return name;
	}
	
}//class BusstopFinder
