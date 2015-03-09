package domain;

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
     * reference to a gpsRecordsManager to send GPS updates to.
     */
    private GpsRecordManager gpsRecordsManager;

    /**
     * instantiates a new AndroidClient object which is used to communicate and
     *   keep track with the Android client.
     *
     * @param  gpsRecordsManager reference to a gpsRecordsManager to send GPS
     *   updates to.
     */
    public AndroidClient(GpsRecordManager gpsRecordsManager)
    {
        this.gpsRecordsManager = gpsRecordsManager;
    }

    /**
     * invoked when the connection to the Android client is terminated.
     *
     * @param remote true if the connection was terminated by the remote host;
     *   false otherwise.
     */
    @Override
    public void onClose(boolean remote)
    {
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
        // TODO: parse messages from Android client, and save it if it's
        //       metadata, or send the gps update to the GpsRecordManager
        //       otherwise.

        // GpsRecord gpsRecord = new GpsRecord();
        // gpsRecordsManager.dispatchGpsUpdate(gpsRecord);
    }
}
