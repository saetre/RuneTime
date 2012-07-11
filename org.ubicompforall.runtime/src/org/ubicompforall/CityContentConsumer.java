package org.ubicompforall;

import java.util.TreeMap;

import org.ubicompforall.CityExplorer.data.SQLiteConnector;

//import org.ubicompforall.CityExplorer.data.CityContentProvider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;

public class CityContentConsumer { // implements DatabaseInterface{

	//Move to final static Contract class
	public static final String SCHEME = "content";
	public static final String AUTHORITY = "org.ubicompforall.CityExplorer.provider";
	public static final String POI_TABLE = "PoiTable";
	//private static final String _ID = "_ID";

	//public static final Uri CONTENT_URI = Uri( AUTHORITY, POI_TABLE );
	public static final Uri CONTENT_URI = new Uri.Builder().scheme(SCHEME).authority(AUTHORITY).appendPath(POI_TABLE).build();

	
	Context ctx;

	public CityContentConsumer( RunetimeActivity runetimeActivity ) {
		this.ctx = runetimeActivity;
	}//CONSTRUCTOR

	/***
	 * @return An array with all pois from the __current__ (?) database
	 */
	public TreeMap<String, Double[]>
	 getAllPois(){
		TreeMap<String, Double[]> pois = new TreeMap<String, Double[] >();
		String error = "";
		
		// A "projection" defines the columns that will be returned for each row
		String[] mProjection = {
		    //_ID,							// Contract class constant for the _ID column name
		    //UserDictionary.Words.LOCALE	// Contract class constant for the locale column name
		    SQLiteConnector.POI_NAME_COL,		// Contract class constant for the name column name
		    SQLiteConnector.LAT_COL,	// Contract class constant for the location column name
		    SQLiteConnector.LON_COL	// Contract class constant for the location column name
		};
		String mSelectionClause = "POI.address_id = ADDR._id";	// Defines a string to contain the selection clause
		String[] mSelectionArgs = null;	// Initializes an array to contain selection arguments
		String mSortClause = SQLiteConnector.POI_NAME_COL;	// Defines a string to contain the selection clause

		RunetimeActivity.debug(0, "Looking for "+CONTENT_URI );
		Cursor mCursor = null;
		try{
			mCursor = ctx.getContentResolver().query(
					//UserDictionary.Words.CONTENT_URI,   // The content URI of the words table == vnd.android.cursor.dir/vnd.google.userword
					//CONTENT_URI = org.ubicompforall.CityExplorer.provider/P,   // The content URI of the words table
					CONTENT_URI,   // The content URI of the words table
					mProjection,                        // The columns to return for each row
					mSelectionClause,                   // Selection criteria
					mSelectionArgs,                     // Selection criteria
					mSortClause );		// The sort order for the returned rows
	    }catch (SQLiteException e){
	    	e.printStackTrace();
	    	RunetimeActivity.debug(-1, e.getMessage() );
	    	error = e.getMessage();
	    	RunetimeActivity.debug(-1, "ERROR in POI_TABLE select "+mProjection[1]+" etc..." );
	    }//try - catch

		//pois.put("Cursor", mCursor.toString() );
		if (mCursor != null){
			//pois.put("Columns", mCursor.getColumnNames().toString() );
			//pois.put("Type", mCursor.getType() );

			//for (int i=0; mCursor.moveToNext(); i++){
			while( mCursor.moveToNext() ){
				pois.put( mCursor.getString(0), new Double[]{ mCursor.getDouble(1), mCursor.getDouble(2) } );
			}
		}else{
			RunetimeActivity.debug(-1, "NO Cursor! "+error );
		}
		return pois;
	}//getAllPois

//	@SuppressWarnings("unused")
//	private void insert() {
//		ContentValues mNewValues = new ContentValues();		// Defines an object to contain the new values to insert
//		/*
//		 * Sets the values of each column and inserts the word. The arguments to the "put"
//		 * method are "column name" and "value"
//		 */
//		mNewValues.put(UserDictionary.Words.APP_ID, "example.user");
//		mNewValues.put(UserDictionary.Words.LOCALE, "en_US");
//		mNewValues.put(UserDictionary.Words.WORD, "insert");
//		mNewValues.put(UserDictionary.Words.FREQUENCY, "100");
//
//		ctx.getContentResolver().insert(
//		    UserDictionary.Words.CONTENT_URI,   // the user dictionary content URI
//		    mNewValues                          // the values to insert
//		);
//	}//insert

}//class CityContentProvider
