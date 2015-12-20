package org.openenergymonitor.emoncmsapp4;


import org.openenergymonitor.emoncmsapp4.MainActivity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class ViewAndUpdatePreferencesActivity extends FragmentActivity {
	private static final String RD_API_KEY = "api_key";
	private static final String SERVER_URL = "server_url";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(MainActivity.class.getName(), "setContentView(R.menu.user_prefs_fragment)");
		setContentView(R.menu.user_prefs_fragment);
	}

	// Fragment that displays the username preference
	public static class UserPreferenceFragment extends PreferenceFragment {

		protected static final String TAG = "UserPrefsFragment";
		private OnSharedPreferenceChangeListener mListener;
		private Preference mUrlServerPreference;
		private Preference mApiKeyPreference;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			Log.i(MainActivity.class.getName(), "addPreferencesFromResource(R.xml.user_prefs)");
			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.user_prefs);

			mUrlServerPreference = (Preference) getPreferenceManager()
					.findPreference(SERVER_URL);
			mApiKeyPreference = (Preference) getPreferenceManager()
					.findPreference(RD_API_KEY);

			// Attach a listener to update summary when the host address changes
			mListener = new OnSharedPreferenceChangeListener() {
				@Override
				public void onSharedPreferenceChanged(
						SharedPreferences sharedPreferences, String key) {
					mUrlServerPreference.setSummary(sharedPreferences.getString(
							SERVER_URL, "None Set"));
					mApiKeyPreference.setSummary(sharedPreferences.getString(
							RD_API_KEY, "None Set"));
					//mDscPassPreference.setSummary(sharedPreferences.getString(
					//		DSCPASS, "None Set"));
				}
			};

			// Get SharedPreferences object managed by the PreferenceManager for
			// this Fragment
			SharedPreferences prefs = getPreferenceManager().getSharedPreferences();

			// Register a listener on the SharedPreferences object
			prefs.registerOnSharedPreferenceChangeListener(mListener);

			// Invoke callback manually to display the current host address
			mListener.onSharedPreferenceChanged(prefs, SERVER_URL);
			mListener.onSharedPreferenceChanged(prefs, RD_API_KEY);
		}

	}
}
