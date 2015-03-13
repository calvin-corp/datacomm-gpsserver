package domain;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * the GPSRecordManager class is used to take care of GPS updates, and notifying
 *   all registered observers.
 *
 * @author Eric Tsang
 */
public class GpsRecordManager
{
    private Set<GpsUpdateListener> registeredListeners;

    /**
     * instantiates a new GPS record manager.
     */
    public GpsRecordManager()
    {
        registeredListeners = new LinkedHashSet<>();
    }

    /**
     * interface for a GPS update listener that can be registered with this
     *   class.
     */
    public interface GpsUpdateListener
    {
        public void onGpsUpdate(GpsRecord gpsRecord);
        public void onClientConnected(String clientId);
        public void onClientDisconnected(String clientId);
    }

    /**
     * adds a GPS update listener to the GPS record manager. when a GPS update
     *   is added to the manager, then the registered listeners will be
     *   notified.
     *
     * @param listener reference to the listener to register.
     */
    public void registerListener(GpsUpdateListener listener)
    {
        registeredListeners.add(listener);
    }

    /**
     * removes a GPS update listener to the GPS record manager; the listener
     *   will no longer be notified of GPS updates.
     *
     * @param listener reference to the listener to unregister.
     */
    public void unregisterListener(GpsUpdateListener listener)
    {
        registeredListeners.remove(listener);
    }

    /**
     * dispatches a GPS update to all registered GPS update listeners.
     *
     * @param gpsRecord the new GPS record.
     */
    public void dispatchGpsUpdate(GpsRecord gpsRecord)
    {
        for (GpsUpdateListener listsner : registeredListeners)
        {
            listsner.onGpsUpdate(gpsRecord);
        }
    }
    
    /**
     * dispatches an Android Client connection to all registered listeners
     * 
     * @param clientId the id of the client that has connected
     */
    public void dispatchConnection(String clientId)
    {
        for (GpsUpdateListener listsner : registeredListeners)
        {
            listsner.onClientConnected(clientId);
        }
    }
    
    /**
     * dispatches an Android Client disconnection to all registered listeners
     * 
     * @param clientId the id of the client that has disconnected
     */
    public void dispatchDisconnection(String clientId)
    {
        for (GpsUpdateListener listsner : registeredListeners)
        {
            listsner.onClientDisconnected(clientId);
        }
    }
}
