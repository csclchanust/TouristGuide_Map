package chan.eddie.touristguide;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class RestInfoLoader extends AsyncTaskLoader<ArrayList<RestaurantInfo>> {
	
	private final static String DATA_DELIMITER = "^";

	private ArrayList<RestaurantInfo> mData;
	
	public RestInfoLoader(Context context) {
		super(context);
	}
	
	public RestaurantInfo parseData(String line) {
		if (line.length() < 4)
			return null;
		
		ArrayList<String> tokens = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(line, DATA_DELIMITER);
		while (st.hasMoreElements()) {
			tokens.add((String)st.nextElement());
		}
		
		if (tokens.size() < 8)
			return null;
		
		RestaurantInfo info = new RestaurantInfo();
		info.setCoordinate(tokens.get(2), tokens.get(3));
		info.setTitle(tokens.get(1));
		info.setSubTitle(tokens.get(6));
		info.setPrice(tokens.get(4));
		info.setRestType(tokens.get(5));
		info.setAddress(tokens.get(7));
		info.setRating(tokens.get(8));
		return info;
	}

	@Override
	public ArrayList<RestaurantInfo> loadInBackground() {
		// prepare the URL query first
		Context context = getContext();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String queryType = sp.getString(SearchPrefFragment.keyType, context.getString(R.string.pref_default_rest_type));
        String queryPrice = sp.getString(SearchPrefFragment.keyPrice, context.getString(R.string.pref_default_price));
        String queryRating = sp.getString(SearchPrefFragment.keyRating, context.getString(R.string.pref_default_rating));
        String queryDistrict = sp.getString(SearchPrefFragment.keyDistrict, context.getString(R.string.pref_default_district));
        String queryUrl = context.getString(R.string.rest_data_url);
        String requestUrl = String.format("%s?%s=%s&%s=%s&%s=%s&%s=%s", queryUrl,
        		RestaurantInfo.KEY_REST_TYPE, queryType,
        		RestaurantInfo.KEY_PRICE, queryPrice,
        		RestaurantInfo.KEY_RATING, queryRating,
        		RestaurantInfo.KEY_DISTRICT, queryDistrict).replace(" ", "%20");
        Log.d("RestInfoLoader", requestUrl);
		
        // return data of restaurant info array list
		ArrayList<RestaurantInfo> data = new ArrayList<RestaurantInfo>();
		
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(new URI(requestUrl));
			HttpResponse response = client.execute(request);
			BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer();
			String line;
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
				
				RestaurantInfo info = parseData(line);
				if (info != null)
					data.add(info);
			}
			in.close();
			Log.d("RestInfoLoader", sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public void deliverResult(ArrayList<RestaurantInfo> data) {
		if (isReset()) {
			onReleaseResources(data);
			return;
		}
		
		ArrayList<RestaurantInfo> oldData = mData;
		mData = data;
		
		if (isStarted()) {
			super.deliverResult(data);
		}
		
		// Invalidate the old data as we don't need it any more.
		if (oldData != null && oldData != data) {
			onReleaseResources(oldData);
		}
		
	}

	@Override 
	protected void onStartLoading() { 
		if (mData != null) {
			deliverResult(mData); 
		}

		if (takeContentChanged() || mData == null) { 
			forceLoad(); 
		} 
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}
	
	@Override
	protected void onReset() {
		// Ensure the loader has been stopped.
		onStopLoading();
		
		// At this point we can release the resources associated with 'mData'.
		if (mData != null) {
			onReleaseResources(mData);
			mData = null;
		}
	}

	@Override
	public void onCanceled(ArrayList<RestaurantInfo> data) {
		// Attempt to cancel the current asynchronous load.
		super.onCanceled(data);
		
		// The load has been canceled, so we should release the resources
		// associated with 'data'.
		onReleaseResources(data);
	}
	
	protected void onReleaseResources(ArrayList<RestaurantInfo> data) {    
		// For a simple List, there is nothing to do. For something like a Cursor, we     
		// would close it in this method. All resources associated with the Loader    
		// should be released here.  
	}
}
