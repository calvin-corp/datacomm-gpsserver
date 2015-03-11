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

    /**
     * time interval between insertions after the initial insertion of
     *   pendingRecords into the database.
     */
    public static final long DB_INSERT_INTERVAL = 60000;

    /**
     * initial delay before the first batch of pendingRecords are inserted into
     *   the database. it must be non-zero, as it takes time for the application
     *   to initially connect with the database.
     */
    public static final long DB_INSERT_DELAY    = 60000;

    /**
     * database objects...
     */
    private MongoClientURI uri;
    private MongoClient client;
    private DB mongoDb;

    /**
     * Timer used to schedule the instance's UpdateDbTask which inserts the
     *   pendingRecords at fixed intervals.
     */
    private Timer timer;

    /**
     * list of pending GpsRecords to insert into the database, so that we don't
     *   pound the database with insertion requests.
     */
    private List<GpsRecord> pendingRecords;

    //////////////////
    // constructors //
    //////////////////

    /**
     * instantiates a MongoDB client.
     */
    public MongoDBClient()
    {
        // initialize instance variables
        uri = new MongoClientURI("mongodb://android-app:somersault@ds033087."
                +"mongolab.com:33087/locations");
        client = null;
        mongoDb = null;
        timer = new Timer(true);
        pendingRecords = Collections.synchronizedList(
                new ArrayList<GpsRecord>());

        // schedule the updateDbTask to regularly insert all pending records
        // into the database at fixed intervals; the UpdateDbTask will
        // reschedule itself after its initial execution.
        timer.schedule(new UpdateDbTask(), DB_INSERT_DELAY);
    }

    //////////////////////
    // public interface //
    //////////////////////

    /**
     * connects this instance to the database.
     */
    @SuppressWarnings("deprecation")
    public void connect()
    {
        if(!isConnected())
        {
            client = new MongoClient(uri);
            mongoDb = client.getDB(uri.getDatabase());
        }
    }

    /**
     * disconnects this instance from the database.
     */
    public void disconnect()
    {
        if(isConnected())
        {
            client.close();
            mongoDb = null;
        }
    }

    /**
     * returns true if the object is connected; false otherwise.
     *
     * @return true if the object is connected; false otherwise.
     */
    public boolean isConnected()
    {
        return mongoDb != null;
    }

    /**
     * returns a list of records that are being buffered, and have yet to be
     *   inserted into the database.
     *
     * @return a list of records that have yet to be inserted into the database.
     */
    public List<GpsRecord> getPendingRecords()
    {
        return pendingRecords;
    }

    ///////////////////////
    // private interface //
    ///////////////////////

    /**
     * converts a GpsRecord object into a MongoDB object that can be inserted
     *   into the MongoDB database.
     *
     * @param gpsRecord record to convert into a MongoDB consumable record.
     *
     * @return a MongoDB consumable record.
     */
    private static BasicDBObject toDbRecord(GpsRecord gpsRecord)
    {
        BasicDBObject record = new BasicDBObject();
        record.put(JSON_KEY_ID, gpsRecord.getDeviceId());
        record.put(JSON_KEY_LAT, gpsRecord.getLat());
        record.put(JSON_KEY_LON, gpsRecord.getLng());
        record.put(JSON_KEY_IP, gpsRecord.getDeviceIp());
        record.put(JSON_KEY_SPEED, gpsRecord.getSpeed());
        record.put(JSON_KEY_ALTITUDE, gpsRecord.getAltitude());
        record.put(JSON_KEY_TIMESTAMP, gpsRecord.getSamplingTime());

        return record;
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
            // insert record into pending records list
            pendingRecords.add(gpsRecord);
        }
    }

    /////////////////////////////////
    // UpdateDbTask implementation //
    /////////////////////////////////

    /**
     * the UpdateDbTask is scheduled once in the beginning to insert all pending
     *   records into the database. after it is scheduled once, it will
     *   reschedule itself forever. this is done in case the timer tasks takes
     *   unusually longer to complete execution, and then run again while the
     *   first task is still trying to complete.
     *
     * @author Eric Tsang
     *
     */
    private class UpdateDbTask extends TimerTask
    {

        /**
         * the run method inserts pending records into the database, and
         *   reschedules a new instance of itself.
         */
        @Override
        public void run()
        {
            if(pendingRecords.size() > 0)
            {
                System.out.println("Writing to database...");

                // copy pending records to array, and prepare them for insertion
                GpsRecord[] gpsRecords = new GpsRecord[pendingRecords.size()];
                pendingRecords.toArray(gpsRecords);
                ArrayList<BasicDBObject> dbRecords = new ArrayList<>();
                for(GpsRecord gpsRecord : gpsRecords)
                {
                    dbRecords.add(toDbRecord(gpsRecord));
                }

                // clear pending records so they won't be inserted again
                pendingRecords.clear();

                // get the database collection
                DBCollection locations = mongoDb.getCollection("locations");

                // insert converted pending records into the database
                locations.insert(dbRecords);

                // reschedule for the future
                timer.schedule(new UpdateDbTask(), DB_INSERT_INTERVAL);

                // free some memory~
                timer.purge();

                System.out.println("Database operation complete...");
            }
        }
    }
}
