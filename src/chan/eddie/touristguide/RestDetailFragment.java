package chan.eddie.touristguide;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class RestDetailFragment extends Fragment implements OnClickListener {
	
	// callback of activity on intent action
	private OnClickListener mCallback = null;

	private final String mapUrl = "http://maps.google.com/maps?saddr=%.15f,%.15f(You)&daddr=%.15f,%.15f(%s)";
	
	// in real life, the video link should get from restaurant info
	//private final String videoUrl = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
	private final String videoUrl = "http://www.808.dk/pics/video/gizmo.mp4";
	
	private View view;
	private RestaurantInfo info;
	private FriendInfo user;
	private TextView textName;
	private TextView textAddr;
	private TextView textType;
	private TextView textPrice;
	private TextView textRating;
	private Button btnRoute;
	private Button btnVideo;
	
    // Container Activity must implement this interface
    public interface OnClickListener {
    	public void onOnNewIntent(Intent intent);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // this fragment has own option menu
        setHasOptionsMenu(true);
	}
	
	public void setOnClickListener(OnClickListener listener) {
		mCallback = listener;
	}

	public void setDetailInfo(FriendInfo user, RestaurantInfo info) {
		this.info = info;
		this.user = user;
	}
	
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
    	Log.d("RestDetailFragment","onCreateView");
    	
    	// onCreateView will be called when this fragment is being added
    	// by FragmentManager, so no need to re-initiate all the view
    	// component
    	if (view == null) {
        	view = inflater.inflate(R.layout.detail_fragment, container, false);
    	}
    	
        textName = (TextView)view.findViewById(R.id.textRestName);
        textAddr = (TextView)view.findViewById(R.id.textRestAddr);
        textType = (TextView)view.findViewById(R.id.textRestType);
        textPrice = (TextView)view.findViewById(R.id.textRestPrice);
        textRating = (TextView)view.findViewById(R.id.textRestRating);
        btnRoute = (Button)view.findViewById(R.id.btnRoute);
        btnVideo = (Button)view.findViewById(R.id.btnVideo);
        btnRoute.setOnClickListener(this);
        btnVideo.setOnClickListener(this);

    	return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// remove all the menu item
		menu.clear();
		// display the own menu
		inflater.inflate(R.menu.detail_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
        	case R.id.meun_detail_close:
        		// quit the current fragment by popping up the last
        		// fragment from stack
        		getFragmentManager().popBackStack();
        		return true;
	    }
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		// just for safe guard on view not initialized
		if (textName != null) {
			textName.setText(info.getTitle());
			textAddr.setText(info.getAddress());
			textType.setText(info.getRestType());
			textPrice.setText(info.getPrice());
			textRating.setText(info.getRating());
		}
		
		super.onStart();
	}

	@Override
	public void onPause() {
		Log.d("RestDetailFragment","onPause");
		super.onPause();
	}

	@Override
	public void onResume() {
		Log.d("RestDetailFragment","onResume");
		super.onResume();
	}
	
	@Override
	public void onDestroyView() {
		Log.d("RestDetailFragment", "onDestroyView");
		super.onDestroyView();
	}

	@SuppressLint("DefaultLocale")
	public void onClick(View v) {
		if (v == btnRoute) {
			String routeUri = String.format(mapUrl,
					user.point.latitude, user.point.longitude,
					info.getCoordinate().latitude, info.getCoordinate().longitude, info.getTitle()); 
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(routeUri));
			//startActivity(intent);
			// send the start intent action in main activity
			// for action flow handling
			if (mCallback != null)
				mCallback.onOnNewIntent(intent);
		} else if (v == btnVideo) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(videoUrl), "video/*");
			//startActivity(intent);
			// send the start intent action in main activity
			// for action flow handling
			if (mCallback != null)
				mCallback.onOnNewIntent(intent);
		}
	}
}
