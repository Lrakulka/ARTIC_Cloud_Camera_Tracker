package tracker.camera.cloud.artic.artic_cloud_camera_tracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    // App configuration. Used in TrackerWebSocket.
    // You can read about ARTIC WebSocket here:
    // https://developer.artik.cloud/documentation/api-reference/websockets-api.html#device-channel-websocket
    public static final String WEB_SOCKET_URL = "wss://api.artik.cloud/v1.1/websocket?ack=true";
    // Device ID and TIKEN you can find here: https://artik.cloud/my/devices in menu Device Info
    public static final String DEVICE_ID = "0b5cfbecdfa04fbfa8b7a331c85fb6a0";
    public static final String DEVICE_TOKEN = "e40252caca6c4b85af45a266f5be82a6";
    //Text View for user messages
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
    }

    public void onClick(View v) {
        TrackerWebSocket trackerWebSocket = new TrackerWebSocket(DEVICE_ID,
                DEVICE_TOKEN, WEB_SOCKET_URL, textView);
        trackerWebSocket.connect();

        /*try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        trackerWebSocket.sendRegisterMessage();*/
    }
}
