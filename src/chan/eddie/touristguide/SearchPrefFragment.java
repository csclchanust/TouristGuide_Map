package chan.eddie.touristguide;

import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SearchPrefFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	
	// callback of activity on search preference change
	private OnSharedPreferenceChangeListener mCallback = null;
	
	// the search criteria key value in preference
	public static final String keyType = "pref_rest_type";
	public static final String keyPrice = "pref_price";
	public static final String keyRating = "pref_rating";
	public static final String keyDistrict = "pref_district";
	
	private boolean isConfirmed;
	private String lastType, lastPrice, lastRating, lastDistrict;
	
    // Container Activity must implement this interface
    public interface OnSharedPreferenceChangeListener {
    	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.search_preference);
        
        // this fragment has own option menu
        setHasOptionsMenu(true);

        isConfirmed = false;

        // save the current preference to local variables in case of roll back
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        lastType = sp.getString(keyType, getString(R.string.pref_default_rest_type));
        lastPrice = sp.getString(keyPrice, getString(R.string.pref_default_price));
        lastRating = sp.getString(keyRating, getString(R.string.pref_default_rating));
        lastDistrict = sp.getString(keyDistrict, getString(R.string.pref_default_district));
        
        // Set the summary of each preference to the selected value 
		Map<String, ?> keys = sp.getAll();
		for (Map.Entry<String, ?> entry : keys.entrySet()) {
		    findPreference(entry.getKey()).setSummary(entry.getValue().toString()); 
		}
		
		// add event handler for preference change
        sp.registerOnSharedPreferenceChangeListener(this);
    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// remove all the menu item
		menu.clear();
		// display the own menu
		inflater.inflate(R.menu.search_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	public void setOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		mCallback = listener;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
        	case R.id.meun_search_ok:
        		// set the flag so the preference will not roll back
        		isConfirmed = true;
        		// quit the current fragment by popping up the last
        		// fragment from stack
        		getFragmentManager().popBackStack();
        		return true;
	    }
		return super.onOptionsItemSelected(item);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// Event handler when preference changed
	    Preference pref = findPreference(key);
	    if (pref instanceof ListPreference) {
	    	ListPreference lp = (ListPreference) pref; 
	        lp.setSummary(lp.getValue());
	    }
	}

	@Override
	public void onDestroy() {
		Log.d("SearchPrefFragment", "onDestroy");
		if (!isConfirmed) {
			// Roll back the setting if not click the confirm button
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
			SharedPreferences.Editor ed = sp.edit();
			ed.putString(keyType, lastType);
			ed.putString(keyPrice, lastPrice);
			ed.putString(keyRating, lastRating);
			ed.putString(keyDistrict, lastDistrict);
			ed.commit();
		} else if (mCallback != null) {
			// confirm to do new search and invoke the listener
			mCallback.onSharedPreferenceChanged(PreferenceManager.getDefaultSharedPreferences(getActivity()), "");
		}
		super.onDestroy();
	}

}
