package Navigator;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by ytka on 18.03.18.
 */

public class Route {
    public String beginAddress;
    public String endAddress;

    public LatLng beginLocation;
    public LatLng endLocation;

    public Distance distance;
    public Duration duration;
    public List<LatLng> points;
}
