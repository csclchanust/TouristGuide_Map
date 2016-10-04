package chan.eddie.touristguide;

import java.util.HashMap;

import com.google.android.gms.maps.model.LatLng;

public class RestaurantInfo extends HashMap<String,String> {
	private static final long serialVersionUID = 1L;
	
	public static final String KEY_COORDINATE = "coordinate";
	public static final String KEY_TITLE = "title";
	public static final String KEY_SUBTITLE = "subtitle";
	public static final String KEY_PRICE = "price";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_REST_TYPE = "type";
	public static final String KEY_RATING = "rating";
	public static final String KEY_DISTRICT = "district";
	
	private LatLng point;
	
	// setter methods
	public LatLng setCoordinate(String latitude, String longitude) {
		point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
		return point;
	}
	
	public String setTitle(String title) {
		return (String)put(KEY_TITLE, title);
	}
	
	public String setSubTitle(String subTitle) {
		return (String)put(KEY_SUBTITLE, subTitle);
	}

	public String setPrice(String price) {
		return (String)put(KEY_PRICE, price);
	}
	
	public String setAddress(String addr) {
		return (String)put(KEY_ADDRESS, addr);
	}
	
	public String setRestType(String restType) {
		return (String)put(KEY_REST_TYPE, restType);
	}
	
	public String setRating(String rating) {
		return (String)put(KEY_RATING, rating);
	}
	
	// getter methods
	public LatLng getCoordinate() {
		return point;
	}
	
	public String getTitle() {
		return (String)get(KEY_TITLE);
	}
	
	public String getSubStitle() {
		return (String)get(KEY_SUBTITLE);
	}
	
	public String getPrice() {
		return (String)get(KEY_PRICE);
	}
	
	public String getRestType() {
		return (String)get(KEY_REST_TYPE);
	}
	
	public String getRating() {
		return (String)get(KEY_RATING);
	}
	
	public String getAddress() {
		return (String)get(KEY_ADDRESS);
	}
}
