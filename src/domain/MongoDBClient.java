package domain;

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
     * database objects...
     */
    private MongoClientURI uri;
    private MongoClient client;
    private DB mongoDb;
    private DBCollection locations;

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
        locations = null;
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
            locations = mongoDb.getCollection("locations");
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
            locations = null;
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
            // insert record into database
            locations.insert(toDbRecord(gpsRecord));
        }
    }
    
    /**
     * invoked when Android Clients connect. Not needed by MongoDBClient.
     */
    @Override
    public void onClientConnected(String clientId){}
    
    /**
     * invoked when Android Clients disconnect. Not needed by MongoDBClient.
     */
    @Override
    public void onClientDisconnected(String clientId){}
}
