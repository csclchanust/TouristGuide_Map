package chan.eddie.touristguide;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.Marker;

import android.app.Activity;
import android.app.LoaderManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

public class MainActivity extends Activity implements
	UserLocator.OnLocationListener,
	LoaderManager.LoaderCallbacks<ArrayList<RestaurantInfo>>,
	// implements the following listener so this main activity
	// can control all the action flow
	SearchPrefFragment.OnSharedPreferenceChangeListener,
	RestListFragment.OnItemClickListener,
	RestMapFragment.OnFriendMarkerTapListener,
	RestMapFragment.OnRestMarkerTapListener,
	RestDetailFragment.OnClickListener,
	AlertDialogFragment.OnClickListener {

	public static final int LOADER_ID = 0;
	public static final String TAG_MAP_FRAGMENT = "rest_map";
	public static final String TAG_LIST_FRAGMENT = "rest_list";
	public static final String TAG_SEARCH_FRAGMENT = "rest_search";
	public static final String TAG_DETAIL_FRAGMENT = "rest_detail";
	public static final String TAG_DIALOG_FRAGMENT = "rest_dialog";
	public static final int VC_DIALOG_ID = 1;
	
	protected Menu rootMenu;
    protected RestMapFragment mapFragment;
    protected RestListFragment listFragment;
    protected SearchPrefFragment prefFragment;
    protected RestDetailFragment detailFragment;
    protected boolean isShowList = true;
    protected boolean isOnIntent = false;
    protected long lastBackPressTime = 0;
    protected Toast toast;
    
    protected ArrayList<RestaurantInfo> lastRestData;
    protected ArrayList<FriendInfo> friendData;
    protected FriendInfo friendSelected;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createView();
        
		// hard code some friend overlays for ease of presentation
 		if (friendData == null) {
 	 		friendData = new ArrayList<FriendInfo>();
 	 		// first one is current user location, hard code for ease of presentation
			// Please refer map samples for getting user current location 
			friendData.add(new FriendInfo(getResources().getDrawable(R.drawable.my_icon),
					22.285488, 114.156131, "Current Location", null));
			// friend info
			friendData.add(new FriendInfo(getResources().getDrawable(R.drawable.head1),
					22.28656, 114.151855, "John", "john@gmail.com"));
			friendData.add(new FriendInfo(getResources().getDrawable(R.drawable.head2),
					22.288084, 114.158463, "Mary", "mary@gmail.com"));
			friendData.add(new FriendInfo(getResources().getDrawable(R.drawable.head3),
					22.28182, 114.158077, "Peter", "peter@gmail.com"));
 		}
    }
	
	protected void createView() {
        setContentView(R.layout.activity_main);
        
        prefFragment = new SearchPrefFragment();
        prefFragment.setOnSharedPreferenceChangeListener(this);
        mapFragment = new RestMapFragment();
        mapFragment.setOnFriendMarkerTapListener(this);
        mapFragment.setOnRestMarkerTapListener(this);
 		listFragment = new RestListFragment();
 		listFragment.setOnItemClickListener(this);
 		detailFragment = new RestDetailFragment();
 		detailFragment.setOnClickListener(this);
 		
        FragmentTransaction ft = getFragmentManager().beginTransaction();
 		ft.add(R.id.container, mapFragment, TAG_MAP_FRAGMENT);
 		ft.add(R.id.container, listFragment, TAG_LIST_FRAGMENT);
 		ft.commit();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	rootMenu = menu;
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
        	case R.id.meun_search:
	            FragmentTransaction ft = getFragmentManager().beginTransaction();
	            ft.remove(mapFragment);
	            ft.remove(listFragment);
	            ft.add(R.id.container, prefFragment, TAG_SEARCH_FRAGMENT);
	            ft.addToBackStack(null);
	            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	            ft.commit();
	            return true;
	            
        	case R.id.meun_list:
        		if (isShowList) {
        			isShowList = false;
        			getFragmentManager().beginTransaction().hide(listFragment).commit();
        		} else {
        			isShowList = true;
        			getFragmentManager().beginTransaction().show(listFragment).commit();
        		}
        		return true;
	    }
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		Log.d("MainActivity","onStart");
	 	// In real sample, friend info should be loaded
	 	// dynamically same as restaurant data
		mapFragment.updateFriendMarker(friendData);

		// start background data loader for restaurant info
 		getLoaderManager().initLoader(LOADER_ID, null, this);
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.d("MainActivity","onResume");
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.d("MainActivity","onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.d("MainActivity","onStop");
		super.onStop();
	}

	public void onLocationChanged(Location location) {
		Log.d("MainActivity", "onLocationChanged");
		if (friendData != null && friendData.size() > 0) {
			// current user index in friend data is 0
			FriendInfo user = friendData.get(0);
			user.setLocation(location.getLatitude(), location.getLongitude());
			mapFragment.updateFriendInfo(user, 0);
		}
	}

	public Loader<ArrayList<RestaurantInfo>> onCreateLoader(int id, Bundle args) {
		Log.d("MainActivity", "onCreateLoader");
		Toast.makeText(this, "Loading restaurant data...", Toast.LENGTH_SHORT).show();
		return new RestInfoLoader(this);
	}

	public void onLoadFinished(Loader<ArrayList<RestaurantInfo>> loader, ArrayList<RestaurantInfo> data) {
		// A switch-case is useful when dealing with multiple Loaders/IDs
		switch (loader.getId()) {
		case LOADER_ID:
			Log.d("MainActivity", "onLoadFinished");
			Toast.makeText(this, "Loading completed", Toast.LENGTH_SHORT).show();
			lastRestData = data;
			mapFragment.updateRestMarker(data);
			listFragment.updateList(data);
			break;
		}
	}

	public void onLoaderReset(Loader<ArrayList<RestaurantInfo>> loader) {
		Log.d("MainActivity", "onLoaderReset");
	}

	// this event handler will be invoked when search preference is changed
	// in SearchPrefFragment
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d("MainActivity", "onSharedPreferenceChanged");
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}

	// this event handler will be invoked when the item in the restaurant list
	// is clicked in RestListFragment
	public void onItemClick(AdapterView<?> l, View v, int position, long id) {
		Log.d("MainActivity", "onItemClick position = " + position);
		mapFragment.setFocusItem(position);
	}
	

	public void OnRestMarkerTap(Marker m, int index) {
		Log.d("MainActivity", "OnRestMarkerTap index = "+index);
		
		// set the current user info and current tagged restaurant info
		// to detail fragment first
		// For simplicity, friend data with index 0 is current user info
		detailFragment.setDetailInfo(friendData.get(0), lastRestData.get(index));

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove(mapFragment);
        ft.remove(listFragment);
        ft.add(R.id.container, detailFragment, TAG_DETAIL_FRAGMENT);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
	}

	public void OnFriendMarkerTap(Marker m, int index) {
		Log.d("MainActivity", "OnFriendMarkerTap index = "+index);
		
		m.hideInfoWindow();
		friendSelected = friendData.get(index);
		// skip current user overlay tap as no contact
		if (friendSelected.contact == null)
			return;
		
		String[] callList = {"Skype", "GTalk", "Google+ Hangout"};
		String contact = friendSelected.name + " (" + friendSelected.contact + ")";
		AlertDialogFragment dialog = AlertDialogFragment.newInstance(VC_DIALOG_ID,
	            "Video call to " + contact + " ?", callList);
		dialog.setOnClickListener(this);
		dialog.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
	}
	
	public void onAlertDialogClick(int dialogId, int result, int option) {
		Log.d("MainActivity", "onAlertDialogClick dialogId="+dialogId+", result="+result+", option="+option);
		
		if (dialogId == VC_DIALOG_ID && result == AlertDialogFragment.CLICK_OK && friendSelected != null) {
			Intent intent = null;
			if (option == 0) { // Skype
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse("skype:"+friendSelected.contact));
			} else if (option == 1) { // GTalk
				Uri uri = new Uri.Builder().scheme("xmpp").authority("gtalk").query("call;type=video").appendPath(friendSelected.contact).build();
				intent = new Intent(Intent.ACTION_SENDTO, uri);
			} else if (option == 2) { // Google+ Hangout
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://talkgadget.google.com/hangouts/extras/talk.google.com/myhangout"));
			}
			
			if (intent != null)
				onOnNewIntent(intent);
		}
	}

	public boolean hasFragmentStack() {
	    if (getFragmentManager().findFragmentByTag(TAG_SEARCH_FRAGMENT) != null) {
	    	Log.d("MainActivity", "Search fragment found");
	    	return true;
	    } else if (getFragmentManager().findFragmentByTag(TAG_DETAIL_FRAGMENT) != null) {
	    	Log.d("MainActivity", "detail fragment found");
	    	return true;
	    }
	    return false;
	}
	
	@Override
	public void onBackPressed() {
		if (!hasFragmentStack()) {
			if (this.lastBackPressTime < System.currentTimeMillis() - 4000) {
				toast = Toast.makeText(this, "Press back again to close this app",
						Toast.LENGTH_SHORT);
				toast.show();
				this.lastBackPressTime = System.currentTimeMillis();
			} else {
				// if back button is pressed again within 4 sec, quit the app
				if (toast != null)
					toast.cancel();
				finish(); // quit the app
			}
		} else {
			super.onBackPressed();
		}
	}

	public void onOnNewIntent(Intent intent) {
		Log.d("MainActivity", "onOnNewIntent");
		if (intent != null) {
			final PackageManager packageManager = this.getPackageManager();
			List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
			if (list.size() > 0) {
				// invoke the target intent if package found
				// set the onIntent flag to stop screen rotation handling
				isOnIntent = true;
				startActivityForResult(intent, 0);
				return;
			}
			// if not package for the target action found, display dialog
			Toast.makeText(this, "Package for the action not found, please install it!", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("MainActivity", "onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);
		// intent returned, so screen rotation handling is resumed
		isOnIntent = false;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    
	    // only handle screen rotate on active view only
	    // as it is too complex under intent stack
	    if (isOnIntent) {
			Log.d("MainActivity", "onConfigurationChanged - skipped");
	    	return;
	    }
	    
		Log.d("MainActivity", "onConfigurationChanged");

	    // pop from fragment stack if the fragment is stacked
	    if (hasFragmentStack()) {
	    	getFragmentManager().popBackStack();
	    	// run the pop action immediately 
	    	getFragmentManager().executePendingTransactions();
	    }
	    
	    // remove other two fragments and run immediately
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        // need to follow stack pop up sequence for removal
        ft.remove(listFragment);
        ft.remove(mapFragment);
        ft.commit();
        getFragmentManager().executePendingTransactions();
        
        // rebuild the layout according to the orientation layout
        // portrait : res\layout\activity-main.xml
        // landscape: res\layout-land\activity-main.xml
        setContentView(R.layout.activity_main);
        // rebuild the menu, first cleanup then construct
        rootMenu.clear();
        getMenuInflater().inflate(R.menu.activity_main, rootMenu);
        
        // rebuild the fragments according to the orientation layout
        prefFragment = new SearchPrefFragment();
        prefFragment.setOnSharedPreferenceChangeListener(this);
        mapFragment = new RestMapFragment();
        mapFragment.setOnFriendMarkerTapListener(this);
        mapFragment.setOnRestMarkerTapListener(this);
 		listFragment = new RestListFragment();
 		listFragment.setOnItemClickListener(this);
 		
 		// construct the new interface by fragment manager and run immediately
 		FragmentTransaction ftNew = getFragmentManager().beginTransaction();
 		ftNew.add(R.id.container, mapFragment, TAG_MAP_FRAGMENT);
 		ftNew.add(R.id.container, listFragment, TAG_LIST_FRAGMENT);
 		ftNew.commit();
 		getFragmentManager().executePendingTransactions();

 		// add the data to list view
 		listFragment.updateList(lastRestData);

 		// hide the list fragment if it is not displayed in the last view 
 		if (!isShowList) {
 	 		FragmentTransaction ftNew2 = getFragmentManager().beginTransaction();
 			ftNew2.hide(listFragment);
 	 		ftNew2.commit();
 	 		getFragmentManager().executePendingTransactions();
 		}

	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	        Toast.makeText(this, "Landscape view", Toast.LENGTH_SHORT).show();
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	        Toast.makeText(this, "Portrait view", Toast.LENGTH_SHORT).show();
	    }
	}
}
