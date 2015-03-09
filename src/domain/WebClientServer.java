package domain;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
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
        gpsRecordsManager.registerListener(this);
    }

    ////////////////////////////////////
    // WebSocketServer implementation //
    ////////////////////////////////////

    /**
     * adds the connection to the set of connected connections.
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        clients.add(conn);
    }

    /**
     * removes the connection from the set of connected connections.
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote)
    {
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
        // create the json message to send to the web clients
        JSONObject json = new JSONObject();
        json.put(JSON_KEY_ID, gpsRecord.getDeviceId());
        json.put(JSON_KEY_LAT, gpsRecord.getLat());
        json.put(JSON_KEY_LON, gpsRecord.getLng());
        json.put(JSON_KEY_IP, gpsRecord.getDeviceIp());
        json.put(JSON_KEY_SPEED, gpsRecord.getSpeed());
        json.put(JSON_KEY_ALTITUDE, gpsRecord.getAltitude());
        json.put(JSON_KEY_TIMESTAMP, gpsRecord.getSamplingTime());

        // send messages to clients
        for(WebSocket client : clients)
        {
            client.send(json.toString());
        }
    }
}
