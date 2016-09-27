package tracker.camera.cloud.artic.artic_cloud_camera_tracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    // App configuration. Used in TrackerWebSocket.
    // You can read about ARTIK WebSocket here:
    // https://developer.artik.cloud/documentation/api-reference/websockets-api.html#device-channel-websocket
    public static final String WEB_SOCKET_URL = "wss://api.artik.cloud/v1.1/websocket?ack=true";
    // Device ID and TIKEN you can find here: https://artik.cloud/my/devices in menu Device Info
    public static final String DEVICE_ID = "<YOUR DEVICE ID>";
    public static final String DEVICE_TOKEN = "<YOUR DEVICE TOKEN>";
    //Text View for user messages
    private TextView textView;
    // Video service
    private VideoService videoService;
    // Web Socket service
    private TrackerWebSocket trackerWebSocket;
    // Coordinates service
    private LocationService locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        videoService = new VideoService(this);
        trackerWebSocket = new TrackerWebSocket(DEVICE_ID,
                DEVICE_TOKEN, WEB_SOCKET_URL, textView, videoService);
        locationService = new LocationService(trackerWebSocket, this);
    }

    public void doId(View v) {
        trackerWebSocket.connect();
        locationService.start();
    }

    public void stopDoIt(View v) {
        locationService.stop();
        trackerWebSocket.closeSession();
        textView.setText("Session closed");
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoService.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoService.pause();
    }
}
