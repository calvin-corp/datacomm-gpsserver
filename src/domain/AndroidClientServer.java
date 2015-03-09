package domain;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

import lib.Client;
import lib.TCPServer;

/**
 * listens for new TCP connections from Android devices. when connections are
 *   made, a new client object is created to keep track of state information
 *   associated with the connection.
 *
 * when messages are received from the connections, or if they are closed, the
 *   information is multiplexed to the client objects that are keeping track of
 *   the connection's state information so they can be updated as necessary.
 *
 * @author Eric Tsang
 */
public class AndroidClientServer extends TCPServer
{
    /**
     * maps connection objects to their client objects. this map has an entry
     *   for each connection that is connected to the server at any moment.
     */
    private Map<Object,Client> clients;

    /**
     * reference to a GpsRecordManager to send GPS updates to.
     */
    private GpsRecordManager gpsRecordsManager;

    /**
     * instantiates a new AndroidClientServer object.
     *
     * @param  serverPort port to listen for connections for.
     * @param  gpsRecordsManager reference to a GpsRecordManager to send GPS
     *   updates to.
     *
     * @throws IOException
     */
    public AndroidClientServer(int serverPort, GpsRecordManager gpsRecordsManager) throws IOException
    {
        super(serverPort);
        this.clients = new LinkedHashMap<>();
        this.gpsRecordsManager = gpsRecordsManager;
    }

    //////////////////////////////
    // TCPServer implementation //
    //////////////////////////////

    /**
     * invoked when a new connection is established with the server. it creates
     *   a new entry in the clients map.
     *
     * @param conn socket object that is connected to the remote host that just
     *   connected.
     */
    @Override
    protected void onOpen(Socket conn)
    {
        clients.put(conn,new AndroidClient(gpsRecordsManager));
    }

    /**
     * invoked when a message is received from a connected remote host. forwards
     *   the received message to the client object.
     *
     * @param conn socket object that is connected to the remote host that sent
     *   the message.
     * @param msg message received from the remote host.
     */
    @Override
    protected void onMessage(Socket conn, String msg)
    {
        clients.get(conn).onMessage(msg);
    }

    /**
     * invoked when an connection to the server is ended. it
     *   removes the corresponding entry in the clients map.
     *
     * @param conn socket object that is connected to the remote host that just
     *   disconnected.
     * @param remote true if the connection was terminated by the remote host;
     *   false otherwise.
     */
    @Override
    protected void onClose(Socket conn, boolean remote)
    {
        clients.get(conn).onClose(remote);
        clients.remove(conn);
    }
}
