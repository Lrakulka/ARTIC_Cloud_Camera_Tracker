package tracker.camera.cloud.artic.artic_cloud_camera_tracker;

import android.os.AsyncTask;
import android.widget.TextView;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by List on 9/25/2016.
 * Operations with WebSocket
 */

public class TrackerWebSocket {
    private String deviceId;
    private String deviceToken;
    private String webSocketURL;
    // Established to true when passed registration
    private boolean isWebSocketReady;
    // Protect from creating the same AsynkTask more than one time
    private CountDownLatch messageLatch;
    // Created WebSocket session
    private Session session;
    // Show information to user
    private TextView textView;


    // Create JSON request message for registration
    private JSONObject reqMessage;
    // Common response message
    private JSONObject message;

    /**
     * Constructor sets socket parameters
     * @param deviceId - id of device
     * @param deviceToken - token of device
     * @param webSocketURL - webSocket URL
     */
    public TrackerWebSocket(String deviceId, String deviceToken,
                            String webSocketURL, TextView textView) {
        this.deviceId = deviceId;
        this.deviceToken = deviceToken;
        this.webSocketURL = webSocketURL;
        this.textView = textView;

        reqMessage = new JSONObject();
        message = new JSONObject();
        try {
            reqMessage.put("type", "register");
            reqMessage.put("sdid", deviceId);
            reqMessage.put("Authorization", "bearer " + deviceToken);
            reqMessage.put("cid", System.currentTimeMillis());
            message.put("sdid", deviceId);
            message.put("cid", System.currentTimeMillis());
            message.put("type", "message");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Execute asyncTask which trying to connect to ARTIC Cloud
     */
    public void connect() {
        // Create Thread to execute registration operation
        final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    messageLatch = new CountDownLatch(1);
                    final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
                    ClientManager client = ClientManager.createClient();
                    client.connectToServer(new Endpoint() {
                         @Override
                         public void onOpen(Session session, EndpointConfig config) {
                             session.addMessageHandler(new MessageHandler.Whole<String>() {

                                 @Override
                                 public void onMessage(String message) {
                                     try {
                                         webSocketListener(new JSONObject(message));
                                     } catch (JSONException e) {
                                         e.printStackTrace();
                                     }
                                     messageLatch.countDown();
                                 }
                             });
                             sendRegisterMessage(session);
                         }
                    }, cec, new URI(webSocketURL));
                    messageLatch.await(100, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        asyncTask.execute();
    }

    /**
     * Send coordinates of device location to ARTIC Cloud
     * @param x - the coordinate of vector X
     * @param y - the coordinate of vector Y
     */
    public void sendCoordinateLocation(double x, double y) {
        if (isWebSocketReady) {
            sendMessage("{\"coordinateX\":\"" + x + "\",\"coordinateY\":\"" + y + "\"}");
        }
    }

    /**
     * Close WebSocket session
     */
    public void closeSession() {
        if (session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        isWebSocketReady = false;
    }

    /**
     * Status of webSocket session
     * @return true if session open, false if not
     */
    public boolean isSessionOpen() {
        return session.isOpen();
    }

    /**
     * Return status of socket
     * @return true if socket ready on false if not
     */
    public boolean isWebSocketReady() {
        return isWebSocketReady;
    }

    /**
     * Send register message to ARTIC Cloud.
     * Don't forget to connect to cloud
     * You need to call isWebSocketReady() to know when socket ready
     */
    private void sendRegisterMessage(Session session) {
        try {
            session.getBasicRemote().sendText(reqMessage.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.session = session;
    }

    /**
     * Method processes all incoming messages
     * @param jsonMessage
     */
    private void webSocketListener(JSONObject jsonMessage) {
        // Ping message
        try {
            if (jsonMessage.getString("type").equals("ping")) {
                pingListener(jsonMessage.getLong("ts"));
                return;
            };
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        // Registration response message
        try {
            int code = jsonMessage.getJSONObject("data").getInt("code");
            if (code == 200) {
                isWebSocketReady = true;
                setText("Connected. Happy work");
            } else {
                setText("Error code " + code);
            }
            return;
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        // Action message
        try {
            if (jsonMessage.getString("type").equals("action")) {
                executeAction(jsonMessage.getJSONObject("data").getJSONArray("actions")
                        .optJSONObject(0).getString("name"));
                return;
            }
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        // Error message
        try {
            setText("Error code " + jsonMessage.getJSONObject("error").getString("code"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Execute action which received from ARTIC Cloud
     * @param actionName name of the action
     */
    private void executeAction(String actionName) {
        // Device Actions
        switch (actionName) {
            // Send response to ARTIC cloud when makes photo
            case "makePhoto" :
                System.out.println("makePhoto");
                sendMessage("{\"makePhoto\":\"" + true + "\"}");
                break;
            // Send response to ARTIC cloud when starts video
            case "setOnVideo" :
                System.out.println("setOnVideo");
                sendMessage("{\"startVideo\":\"" + true + "\"}");
                break;
            // Send response to ARTIC cloud when stops video
            case "setOffVideo" :
                System.out.println("setOffVideo");
                sendMessage("{\"stopVideo\":\"" + true + "\"}");
                break;
        }
    }

    /**
     * Send message to ARTIC Cloud
     */
    private void sendMessage(String data) {
        if (session.isOpen()) {
            try {
                message.put("data", data);
                session.getBasicRemote().sendText(message.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Executed when gets ping message from ARTIC Cloud
     * Normally ARTIC Cloud sending ping message every 30 seconds
     * @param pingTime - time in milliseconds when was send ping message
     */
    private void pingListener(long pingTime) {
        setText("Last ping message received " +
                (System.currentTimeMillis() - pingTime) + " millsec ago");
    }

    // TextView not changing from non main thread
    private void setText(final String text) {
        textView.post(new Runnable(){
            @Override
            public void run(){
                textView.setText(text);
            }
        });
    }
}
