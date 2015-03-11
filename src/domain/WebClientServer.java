package domain;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.List;
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
     * MongoDB client that we can get pending insertions from when a new
     *   connection is established.
     */
    private MongoDBClient mongoDbClient;

    /**
     * constructor instantiates a web client server.
     *
     * @param port port to listen for connections on.
     * @param gpsRecordsManager reference to the gpsRecordsManager to register
     *   for GPS updates from.
     *
     * @throws UnknownHostException
     */
    public WebClientServer(int port, GpsRecordManager gpsRecordsManager, MongoDBClient mongoDbClient) throws UnknownHostException
    {
        super(new InetSocketAddress(port));
        this.clients = new LinkedHashSet<>();
        this.mongoDbClient = mongoDbClient;
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
        clients.add(conn);

        // send new WebClient all pending insertions
        List<GpsRecord> records = mongoDbClient.getPendingRecords();
        for(GpsRecord record : records)
        {
            conn.send(toWebRecord(record).toString());
        }
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
}
