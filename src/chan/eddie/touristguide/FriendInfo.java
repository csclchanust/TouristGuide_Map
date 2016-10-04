package chan.eddie.touristguide;

import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.model.LatLng;

public class FriendInfo {
	
	Drawable photo;
	public LatLng point;
	public String name;
	public String contact;
	
	FriendInfo(Drawable photo, double latitude, double longitude, String name, String contact) {
		this.photo = photo;
		point = new LatLng(latitude, longitude);
		this.name = name;
		this.contact = contact;
	}
	
	void setLocation(double latitude, double longitude) {
		point = new LatLng(latitude, longitude);
	}
}
