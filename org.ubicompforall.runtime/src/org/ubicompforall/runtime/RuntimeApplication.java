package org.ubicompforall.runtime;

import android.app.Application;
import android.util.Log;


public class RuntimeApplication extends Application {
	// private static final String TAG = "RuneTime";
	private static final String TAG = "cityTime";
	public static final int DEBUG = 1;

	public RuntimeApplication(){
		debug(-1, "Hello!" );
	}
	
	@Override
	public void onCreate(){
		debug(-1, "Or Here?" );
	}

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


} // class RuntimeApplication


