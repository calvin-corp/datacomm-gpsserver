package domain;

import java.net.Socket;

import org.json.JSONObject;

import lib.Client;

/**
 * keeps track of a connection with an Android clients, and associated state
 *   information.
 *
 * sends updates to the GpsRecordManager when a GPS update is received from the
 *   client.
 *
 * @author Eric Tsang
 */
public class AndroidClient implements Client
{
    /**
     * keys in JSON objects that are received from the android clients.
     */
    public static final String JSON_KEY_ID        = "id";
    public static final String JSON_KEY_LAT       = "lat";
    public static final String JSON_KEY_LON       = "lon";
    public static final String JSON_KEY_SPEED     = "speed";
    public static final String JSON_KEY_ALTITUDE  = "altitude";
    public static final String JSON_KEY_TIMESTAMP = "timestamp";

    /**
     * reference to a gpsRecordsManager to send GPS updates to.
     */
    private GpsRecordManager gpsRecordsManager;

    /**
     * contains the mac address of the android device used to uniquely identify
     *   it on the server.
     */
    private String androidId;

    /**
     * socket used to communicate with the remote android device. here, we're
     *   only interested in it because we need to put it into the gpsRecords.
     */
    private Socket socket;

    //////////////////
    // constructors //
    //////////////////

    /**
     * instantiates a new AndroidClient object which is used to communicate and
     *   keep track with the Android client.
     *
     * @param  gpsRecordsManager reference to a gpsRecordsManager to send GPS
     *   updates to.
     */
    public AndroidClient(Socket socket, GpsRecordManager gpsRecordsManager)
    {
        this.socket = socket;
        this.gpsRecordsManager = gpsRecordsManager;
    }

    //////////////////////
    // public interface //
    //////////////////////

    /**
     * invoked when the connection to the Android client is terminated.
     *
     * @param remote true if the connection was terminated by the remote host;
     *   false otherwise.
     */
    @Override
    public void onClose(boolean remote)
    {
        if (androidId != null)
        {
            gpsRecordsManager.dispatchDisconnection(androidId);
        }
        
        gpsRecordsManager = null;
    }

    /**
     * invoked when a message from this Android client has been received. it
     *   parses messages from Android client, and save it if it's metadata, or
     *   creates and sends a gps update to the GpsRecordManager if it is a GPS
     *   update.
     *
     * @param msg message received from the Android client.
     */
    @Override
    public void onMessage(String msg)
    {
        JSONObject json = new JSONObject(msg);

        // parse message from client
        if(json.has(JSON_KEY_ID))
        {
            // message is data about device; save the data...
            androidId = json.getString(JSON_KEY_ID);
            gpsRecordsManager.dispatchConnection(androidId);
        }
        else
        {
            // message is GPS update; update the database
            GpsRecord gpsRecord = fromAndroidRecord(this,json);
            gpsRecordsManager.dispatchGpsUpdate(gpsRecord);
        }
    }

    ///////////////////////
    // private interface //
    ///////////////////////

    /**
     * converts a JSON record received from the remote android device into a
     *   GpsRecord.
     *
     * @param  client the AndroidClient requesting the conversion. will be
     *   getting the id, and socket fields from this object.
     * @param  json JSON object received from the remote client.
     *
     * @return a GpsRecord instance that encapsulates the data of a GPS update.
     */
    private static GpsRecord fromAndroidRecord(AndroidClient client, JSONObject json)
    {
        return new GpsRecord(
                client.androidId,
                client.socket,
                json.getLong(JSON_KEY_TIMESTAMP),
                json.getDouble(JSON_KEY_LAT),
                json.getDouble(JSON_KEY_LON),
                json.getDouble(JSON_KEY_ALTITUDE),
                json.getDouble(JSON_KEY_SPEED));
    }
}
