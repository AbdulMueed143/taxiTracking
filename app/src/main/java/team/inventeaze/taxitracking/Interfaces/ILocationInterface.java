package team.inventeaze.taxitracking.Interfaces;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by AbdulMueed on 12/19/2014.
 */
public interface ILocationInterface {
    public void onTrackerLocationChange(LatLng newlatlon, float accuracy);
}
