package chan.eddie.touristguide;

import java.util.HashMap;
import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class RestListFragment extends Fragment implements OnItemClickListener {

	private static final String[] COL_SHOW = {RestaurantInfo.KEY_TITLE, RestaurantInfo.KEY_ADDRESS};
	private static final int[] COL_INDEX = {android.R.id.text1, android.R.id.text2};

	// callback of activity on item click
	private OnItemClickListener mCallback = null;

	View view;
	protected ListView listView;
	protected SimpleAdapter adapter;
	protected ArrayList<HashMap<String,String>> list;

	
    // Container Activity must implement this interface
    public interface OnItemClickListener {
    	public void onItemClick(AdapterView<?> l, View v, int position, long id);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
    	Log.d("RestListFragment","onCreateView");
    	
    	// onCreateView will be called when this fragment is being added
    	// by FragmentManager, so no need to re-initiate all the view
    	// component
    	if (view == null) {
        	view = inflater.inflate(R.layout.list_fragment, container, false);
        	listView = (ListView)view.findViewById(R.id.rest_list);

        	list = new ArrayList<HashMap<String,String>>();
        	adapter = new SimpleAdapter(getActivity(), list, 
        			android.R.layout.simple_list_item_2, COL_SHOW, COL_INDEX);
        	listView.setAdapter(adapter);
        	listView.setOnItemClickListener(this);
    	}
    	return view;
	}
    
    public void setOnItemClickListener(OnItemClickListener listener) {
    	mCallback = listener;
    }

	public void onItemClick(AdapterView<?> l, View v, int position, long id) {
		Log.d("RestListFragment", "Item clicked: " + id);
		v.setSelected(true); 
		if (mCallback != null)
			mCallback.onItemClick(l, v, position, id);
	}
	
	public void updateList(ArrayList<RestaurantInfo> data) {
		list.clear();
		list.addAll(data);
		// tell the list view to refresh after data changed
		adapter.notifyDataSetChanged();
		// move the view to the top of the list
		listView.setSelectionAfterHeaderView();
	}
	
	public void setSelectedItem(int index) {
		listView.setSelection(index);
	}

	@Override
	public void onPause() {
		Log.d("RestListFragment","onPause");
		super.onPause();
	}

	@Override
	public void onResume() {
		Log.d("RestListFragment","onResume");
		super.onResume();
	}
	
	@Override
	public void onDestroyView() {
		Log.d("RestListFragment", "onDestroyView");
		super.onDestroyView();
	}
}
