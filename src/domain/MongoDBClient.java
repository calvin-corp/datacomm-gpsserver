package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class MongoDBClient implements GpsRecordManager.GpsUpdateListener
{
    /**
     * keys in JSON objects that are sent to the Mongo database.
     */
    public static final String JSON_KEY_ID        = "id";
    public static final String JSON_KEY_LAT       = "lat";
    public static final String JSON_KEY_LON       = "lon";
    public static final String JSON_KEY_IP        = "ip";
    public static final String JSON_KEY_SPEED     = "speed";
    public static final String JSON_KEY_ALTITUDE  = "altitude";
    public static final String JSON_KEY_TIMESTAMP = "timestamp";

    private MongoClientURI uri;
    private MongoClient client;
    private DB mongoDb;
    private Timer timer;
    private UpdateDbTask updateDbTask;
    private List<BasicDBObject> pendingRecords;

    public MongoDBClient()
    {
        uri = new MongoClientURI("mongodb://android-app:somersault@ds033087.mongolab.com:33087/locations");
        client = null;
        mongoDb = null;
        timer = new Timer(true);
        pendingRecords = Collections.synchronizedList(
                new ArrayList<BasicDBObject>());
        updateDbTask = new UpdateDbTask();

        timer.scheduleAtFixedRate(updateDbTask, 1000, 30000);
    }

    //////////////////////
    // public interface //
    //////////////////////

    @SuppressWarnings("deprecation")
    public void connect()
    {
        if(!isConnected())
        {
            client = new MongoClient(uri);
            mongoDb = client.getDB(uri.getDatabase());
        }
    }

    public void disconnect()
    {
        if(isConnected())
        {
            client.close();
            mongoDb = null;
        }
    }

    public boolean isConnected()
    {
        return mongoDb != null;
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
        if(isConnected())
        {
            // create the database record
            BasicDBObject record = new BasicDBObject();
            record.put(JSON_KEY_ID, gpsRecord.getDeviceId());
            record.put(JSON_KEY_LAT, gpsRecord.getLat());
            record.put(JSON_KEY_LON, gpsRecord.getLng());
            record.put(JSON_KEY_IP, gpsRecord.getDeviceIp());
            record.put(JSON_KEY_SPEED, gpsRecord.getSpeed());
            record.put(JSON_KEY_ALTITUDE, gpsRecord.getAltitude());
            record.put(JSON_KEY_TIMESTAMP, gpsRecord.getSamplingTime());

            // insert record into database
            pendingRecords.add(record);
        }
    }

    private class UpdateDbTask extends TimerTask
    {

        @Override
        public void run()
        {
            if(pendingRecords.size() > 0)
            {
                System.out.println("Writing to database...");

                // get the database collection
                DBCollection locations = mongoDb.getCollection("locations");

                // insert pending records into the database
                locations.insert(pendingRecords);

                // clear pending records so they won't be inserted again
                pendingRecords.clear();
            }
        }
    }
}
