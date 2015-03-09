package domain;

import lib.Client;

public class AndroidClient implements Client
{
    private GpsRecordManager gpsRecordsManager;

    public AndroidClient(GpsRecordManager gpsRecordsManager)
    {
        this.gpsRecordsManager = gpsRecordsManager;
    }

    @Override
    public void onClose(boolean remote)
    {
        gpsRecordsManager = null;
    }

    @Override
    public void onMessage(String msg)
    {
        // TODO: parse messages from android client, and save it if it's
        //       metadata, or send the gps update to the GpsRecordManager
        //       otherwise.

        // GpsRecord gpsRecord = new GpsRecord();
        // gpsRecordsManager.dispatchGpsUpdate(gpsRecord);
    }
}
