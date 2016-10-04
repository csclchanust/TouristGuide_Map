package chan.eddie.touristguide;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.graphics.drawable.BitmapDrawable;
import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;

public class RestMapFragment extends Fragment implements
	GoogleMap.OnInfoWindowClickListener,
	UserLocator.OnLocationListener,	OnClickListener {

    // Container Activity must implement this interface
	private OnRestMarkerTapListener mCallbackRest = null;
    public interface OnRestMarkerTapListener {
    	public void OnRestMarkerTap(Marker m, int index);
    }

	private OnFriendMarkerTapListener mCallbackFriend = null;
    public interface OnFriendMarkerTapListener {
    	public void OnFriendMarkerTap(Marker m, int index);
    }
    
    protected static ArrayList<RestaurantInfo> lastRestList = null;
    protected static ArrayList<FriendInfo> lastFriendList = null;
	
    protected View view;
    protected MapView mapView;
    protected GoogleMap mMap;
	protected UserLocator locator;
    protected CheckBox cbGPS, cb3Dim, cbSatellite;
    
    protected HashMap<Marker, Integer> mapRestMakerIndex = new HashMap<Marker, Integer>();
	protected SparseArray<Marker> mapRestIndexMarker = new SparseArray<Marker>();
    protected HashMap<Marker, Integer> mapFriendMakerIndex = new HashMap<Marker, Integer>();
	protected SparseArray<Marker> mapFriendIndexMarker = new SparseArray<Marker>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // init the GPS / network positioning
        locator = new UserLocator(getActivity());
        locator.setOnLocationListener(this);
        
		// Initializes the Google Maps Android API so that its classes are ready for use
		try {
			MapsInitializer.initialize(getActivity());
		} catch (GooglePlayServicesNotAvailableException e) {
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("RestMapFragment","onCreateView");
		
    	// onCreateView will be called when this fragment is being added
    	// by FragmentManager, so no need to re-initiate all the view
    	// component
		if (view == null) {
			view = inflater.inflate(R.layout.map_fragment, container, false);
			mapView = (MapView)view.findViewById(R.id.mapview);
			mapView.onCreate(savedInstanceState);
			mMap = mapView.getMap();
			mMap.setOnInfoWindowClickListener(this);
			
			updateRestMarker(lastRestList);
			updateFriendMarker(lastFriendList);

			cbGPS = (CheckBox)view.findViewById(R.id.checkboxGPS);
			cbGPS.setChecked(true);
			cbGPS.setOnClickListener(this);
			cb3Dim = (CheckBox)view.findViewById(R.id.checkBox3Dim);
			cb3Dim.setOnClickListener(this);
			cbSatellite = (CheckBox)view.findViewById(R.id.checkBoxSatellite);
			cbSatellite.setOnClickListener(this);
		}
		return view;
	}

	public void setOnRestMarkerTapListener(OnRestMarkerTapListener listener) {
		mCallbackRest = listener;
	}

	public void setOnFriendMarkerTapListener(OnFriendMarkerTapListener listener) {
		mCallbackFriend = listener;
	}

	public void updateRestMarker(ArrayList<RestaurantInfo> list) {
		// clear the old restaurant marker
		for (int i=0; i<mapRestIndexMarker.size(); i++)
			mapRestIndexMarker.valueAt(i).remove();
		mapRestMakerIndex.clear();
		mapRestIndexMarker.clear();

		if (list != null) {
			// refresh the restaurant marker
			for (int i=0; i<list.size(); i++) {
				RestaurantInfo info = list.get(i);
				Marker marker = mMap.addMarker(new MarkerOptions()
					.position(info.getCoordinate())
					.title(info.getTitle())
					.snippet(info.getAddress())
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
				mapRestMakerIndex.put(marker, i);
				mapRestIndexMarker.put(i, marker);
			}

			// set focus to first restaurant
			if (list.size() > 0) {
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(list.get(0).getCoordinate(), 16),
						1000, null);
			}
		}
		lastRestList = list;
	}
	
	public void updateFriendMarker(ArrayList<FriendInfo> list) {
		// clear the old friend marker
		for (int i=0; i<mapFriendIndexMarker.size(); i++)
			mapFriendIndexMarker.valueAt(i).remove();
		mapFriendMakerIndex.clear();
		mapFriendIndexMarker.clear();
		
		if (list != null) {
			// refresh the friend marker
			for (int i = 0; i < list.size(); i++) {
				FriendInfo info = list.get(i);
				Marker marker = null;
				if (i == 0) { // current user point on index 0
					marker = mMap.addMarker(new MarkerOptions()
						.position(info.point)
						.title(info.name)
						.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
				} else {
					marker = mMap.addMarker(new MarkerOptions()
						.position(info.point)
						.title(info.name)
						.snippet(info.contact)
						.icon(BitmapDescriptorFactory
						.fromBitmap(((BitmapDrawable) info.photo).getBitmap())));
				}
				mapFriendMakerIndex.put(marker, i);
				mapFriendIndexMarker.put(i, marker);
			}
		}
		lastFriendList = list;
	}
	
	public void updateFriendInfo(FriendInfo info, int index) {
		Marker marker = mapFriendIndexMarker.get(index);
		if (marker != null) {
			marker.setTitle(info.name);
			marker.setPosition(info.point);
			marker.setSnippet(info.contact);
		} else
			Log.d("RestMapFragment", "updateFriendInfo() marker object is null!");
	}
	
	public void onLocationChanged(Location location) {
		Log.d("RestMapFragment", "onLocationChanged");
		if (lastFriendList != null && lastFriendList.size() > 0) {
			// current user index in friend data is 0
			FriendInfo user = lastFriendList.get(0);
			user.setLocation(location.getLatitude(), location.getLongitude());
			updateFriendInfo(user, 0);
		}
	}
	
	public void setFocusItem(int index) {
		Marker marker = mapRestIndexMarker.get(index);
		if (marker != null) {
			marker.showInfoWindow();
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16),
					1000, null);
		} else
			Log.d("RestMapFragment", "setFocusItem() marker object is null!");
	}

	public void onInfoWindowClick(Marker m) {
		Integer index = mapRestMakerIndex.get(m);
		if (index != null && mCallbackRest != null) {
			mCallbackRest.OnRestMarkerTap(m, index);
			return;
		}

		index = mapFriendMakerIndex.get(m);
		if (index != null && mCallbackFriend != null) {
			mCallbackFriend.OnFriendMarkerTap(m, index);
			return;
		}
		
		Log.d("RestMapFragment", "onInfoWindowClick() no marker tap event triggered!");
	}
	
	public void onClick(View v) {
		if (v == cbGPS) {
			if (cbGPS.isChecked())
				locator.enableLocationUpdate();
			else
				locator.disableLocationUpdate();
		} else if (v == cb3Dim) {
			CameraPosition currentPos = mMap.getCameraPosition(); 
			CameraPosition cameraPosition = currentPos;
			if (cb3Dim.isChecked()) {
				// Creates a CameraPosition from the builder
				cameraPosition = new CameraPosition.Builder().target(currentPos.target)  // Sets the center of the map to Mountain View
					.zoom(17)                    // Sets the zoom
					.bearing(currentPos.bearing) // Sets the orientation of the camera to the original bearing
					.tilt(45)                    // Sets the tilt of the camera to 45 degrees
					.build();
			} else {
				cameraPosition = new CameraPosition.Builder().target(currentPos.target)  // Sets the center of the map to Mountain View
					.zoom(currentPos.zoom)       // Sets the zoom
					.bearing(currentPos.bearing) // Sets the orientation of the camera to the original bearing
					.tilt(0)                     // Sets the tilt of the camera to 0 degree
					.build(); 
			}
			mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		} else if (v == cbSatellite) {
			if (cbSatellite.isChecked())
				mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); // Satellite maps with no labels
			else
				mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); // Basic maps
		}
	}

	@Override
	public void onResume() {
		Log.d("RestMapFragment","onResume");
		
		// start the location listener to locate current user location
		if (cbGPS.isChecked())
			locator.enableLocationUpdate();

		mapView.onResume();
		super.onResume();
	}

	@Override
	public void onPause() {
		Log.d("RestMapFragment","onPause");
		
    	// release resource for location manager
		// when this activity is going to background
		// stop the location listener to locate current user location
		locator.disableLocationUpdate();
		
		mapView.onPause();
		super.onPause();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroyView() {
		Log.d("RestMapFragment", "onDestroyView");
		super.onDestroyView();
	}
}
