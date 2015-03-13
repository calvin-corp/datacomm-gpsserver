package domain;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

/**
 * this server is used to serve web clients.
 *
 * users using web browsers to connect to the Apache web server to get a map
 *   that has markers that display where people currently are will also connect
 *   to this web server using a web socket. this server will send GPS updates to
 *   all connected clients whenever a GPS update is received.
 *
 * @author Eric Tsang
 *
 */
public class WebClientServer extends WebSocketServer implements GpsRecordManager.GpsUpdateListener
{
    /**
     * keys in JSON objects that are sent to the web clients.
     */
    public static final String JSON_MESSAGE_TYPE  = "msgType";
    public static final String JSON_KEY_DEVICES   = "devices";
    public static final String JSON_KEY_ID        = "id";
    public static final String JSON_KEY_LAT       = "lat";
    public static final String JSON_KEY_LON       = "lon";
    public static final String JSON_KEY_IP        = "ip";
    public static final String JSON_KEY_SPEED     = "speed";
    public static final String JSON_KEY_ALTITUDE  = "altitude";
    public static final String JSON_KEY_TIMESTAMP = "timestamp";

    /**
     * set containing all of the connected connections to web clients.
     */
    private Set<WebSocket> clients;
    private Set<String> trackedDevices;

    //////////////////
    // constructors //
    //////////////////

    /**
     * constructor instantiates a web client server.
     *
     * @param port port to listen for connections on.
     * @param gpsRecordsManager reference to the gpsRecordsManager to register
     *   for GPS updates from.
     *
     * @throws UnknownHostException
     */
    public WebClientServer(int port, GpsRecordManager gpsRecordsManager) throws UnknownHostException
    {
        super(new InetSocketAddress(port));
        this.clients = new LinkedHashSet<>();
        this.trackedDevices = new TreeSet<String>();
        gpsRecordsManager.registerListener(this);
    }

    ////////////////////////////////////
    // WebSocketServer implementation //
    ////////////////////////////////////

    /**
     * adds the connection to the set of connected connections, and sends them
     *   all pending insertions, because the web client can get the the previous
     *   records from the database.
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        System.out.println("WebSocket "+conn+" has connected");
        clients.add(conn);
        sendLiveDevices(conn);
    }

    /**
     * removes the connection from the set of connected connections.
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote)
    {
        System.out.println("WebSocket "+conn+" has disconnected");
        clients.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String msg)
    {
        // do nothing; should not receive messages!
    }

    @Override
    public void onError(WebSocket conn, Exception e)
    {
        // do nothing; errors shouldn't occur!
    }

    ///////////////////////
    // private interface //
    ///////////////////////
    
    /**
     * converts a GpsRecord into JSON form that the web client can consume.
     *
     * @param  gpsRecord GpSrecord object to convert.
     *
     * @return a GpsRecord in JSON form that the web client can consume.
     */
    public static JSONObject toWebRecord(GpsRecord gpsRecord)
    {
        JSONObject json = new JSONObject();
        json.put(JSON_KEY_ID, gpsRecord.getDeviceId());
        json.put(JSON_KEY_LAT, gpsRecord.getLat());
        json.put(JSON_KEY_LON, gpsRecord.getLng());
        json.put(JSON_KEY_IP, gpsRecord.getDeviceIp());
        json.put(JSON_KEY_SPEED, gpsRecord.getSpeed());
        json.put(JSON_KEY_ALTITUDE, gpsRecord.getAltitude());
        json.put(JSON_KEY_TIMESTAMP, gpsRecord.getSamplingTime());

        return json;
    }

    /**
     * send the list of currently live tracked devices to the web client
     * 
     * @param client the newly connected web client.
     */
    private void sendLiveDevices(WebSocket client)
    {
        // Create an array from the existing set of clients
        String[] liveDevices = new String[trackedDevices.size()];
        Iterator<String> itr = trackedDevices.iterator();
        
        for (int i = 0; itr.hasNext(); i++)
        {
            liveDevices[i] = itr.next();
        }
        
        // Construct JSON Object to send
        JSONObject json = new JSONObject();
        json.put(JSON_MESSAGE_TYPE, "setup");
        json.put(JSON_KEY_DEVICES, liveDevices);
        
        // Send the JSON to the new connection
        client.send(json.toString());
    }
    
    ////////////////////////////////////////
    // GpsRecordManager.GpsUpdateListener //
    ////////////////////////////////////////

    /**
     * invoked when gpsUpdates are made. forwards the updates to all connected
     *   web clients.
     */
    @Override
    public void onGpsUpdate(GpsRecord gpsRecord)
    {
        // send messages to clients
        for(WebSocket client : clients)
        {
            client.send(toWebRecord(gpsRecord).toString());
        }
    }
    
    /**
     * invoked when Android Clients connect. Forwards message to webclients.
     */
    @Override
    public void onClientConnected(String clientId)
    {
        // Save device ID
        trackedDevices.add(clientId);
        
        // Build JSON message to send to web clients
        JSONObject json = new JSONObject();
        json.put(JSON_MESSAGE_TYPE, "connected");
        json.put(JSON_KEY_ID, clientId);
        String message = json.toString();
        
        // send messages to clients
        for(WebSocket client : clients)
        {
            client.send(message);
        }
    }
    
    /**
     * invoked when Android Clients disconnect. Forwards message to webclients.
     */
    @Override
    public void onClientDisconnected(String clientId)
    {
        if (clientId == null)
            return;
        
        // Remove device ID
        trackedDevices.remove(clientId);
        
        // Build JSON message to send to web clients
        JSONObject json = new JSONObject();
        json.put(JSON_MESSAGE_TYPE, "disconnected");
        json.put(JSON_KEY_ID, clientId);
        String message = json.toString();
        
        // send messages to clients
        for(WebSocket client : clients)
        {
            client.send(message);
        }
    }
}
