package Navigator;

import java.util.List;

/**
 * Created by ytka on 18.03.18.
 */

public interface DirectionFinderListener {
    void onStartBuild();
    void onBuild(List<Route> Route);
}
