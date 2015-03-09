package domain;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

import lib.Client;
import lib.TCPServer;

public class AndroidClientServer extends TCPServer
{

    private Map<Object,Client> clients;
    private GpsRecordManager gpsRecordsManager;

    public AndroidClientServer(int serverPort, GpsRecordManager gpsRecordsManager) throws IOException
    {
        super(serverPort);
        this.clients = new LinkedHashMap<>();
        this.gpsRecordsManager = gpsRecordsManager;
    }

    @Override
    protected void onOpen(Socket conn)
    {
        clients.put(conn,new AndroidClient(gpsRecordsManager));
    }

    @Override
    protected void onMessage(Socket conn, String msg)
    {
        clients.get(conn).onMessage(msg);
    }

    @Override
    protected void onClose(Socket conn, boolean remote)
    {
        clients.get(conn).onClose(remote);
        clients.remove(conn);
    }
}
