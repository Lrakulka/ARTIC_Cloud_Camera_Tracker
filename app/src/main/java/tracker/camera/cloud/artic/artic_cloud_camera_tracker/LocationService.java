package tracker.camera.cloud.artic.artic_cloud_camera_tracker;

/**
 * Created by List on 9/25/2016.
 */

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import java.util.List;

/**
 * Get location of device and send it to Cloud
 */
public class LocationService {
    private TrackerWebSocket trackerWebSocket;
    private Context context;
    private Location location;
    // Parameter stop executing AsyncTask
    private volatile boolean start;

    public LocationService(TrackerWebSocket trackerWebSocket, Context context) {
        this.trackerWebSocket = trackerWebSocket;
        this.context = context;
    }

    /**
     * Sends location to Cloud every 30 seconds
     */
    public void start() {
        final LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        final List<String> providers = lm.getProviders(true);
        start = true;
        final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Location l = null;
                // Check, is permissions allowed
                if ((ActivityCompat.checkSelfPermission(context, Manifest.permission
                        .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                        (ActivityCompat.checkSelfPermission(context, Manifest.permission
                                .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    start = false;
                }
                /* Every 30 seconds checks coordinates if they changed
                        sent message to ARTIC Cloud with new coordinates */
                while (start) {
                    // Get coordinates
                    for (int i = providers.size() - 1; i >= 0; i--) {
                        l = lm.getLastKnownLocation(providers.get(i));
                        if (l != null) break;
                    }
                    // Check coordinates
                    if (location == null || location.equals(l)) {
                        trackerWebSocket.sendCoordinateLocation(l.getLatitude(), l.getLongitude());
                        location = l;
                    }
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                };
                return null;
            }
        };
        asyncTask.execute();
    }

    /**
     * Stops sending location
     */
    public void stop() {
        start = false;
    }
}
