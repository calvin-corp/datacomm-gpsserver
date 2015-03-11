package main;

import java.io.IOException;

import domain.AndroidClientServer;
import domain.GpsRecordManager;
import domain.MongoDBClient;
import domain.WebClientServer;

public class Main
{
    public static final String USAGE = "usage: java "+Main.class.getName()
            +" [android_svr_port] [websocket_svr_port]";

    public static void main(String[] args) throws IOException, InterruptedException
    {
        int androidServerPort;
        int websocketServerPort;
        AndroidClientServer androidSvr;
        WebClientServer webSockSvr;
        GpsRecordManager gpsRecords;
        MongoDBClient mongoDBClnt;

        // parse command line arguments
        try
        {
            androidServerPort = 7000;//Integer.valueOf(args[0]);
            websocketServerPort = 7001;//Integer.valueOf(args[1]);
        }
        catch(Exception e)
        {
            System.out.println(USAGE);
            return;
        }

        // set up the servers
        try
        {
            gpsRecords = new GpsRecordManager();
            mongoDBClnt = new MongoDBClient();
            androidSvr = new AndroidClientServer(androidServerPort,gpsRecords);
            webSockSvr = new WebClientServer(websocketServerPort,gpsRecords,
                    mongoDBClnt);
            gpsRecords.registerListener(mongoDBClnt);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        // start the servers
        androidSvr.start();
        webSockSvr.start();
        mongoDBClnt.connect();

        // end the program when input is received
        System.in.read();
        androidSvr.stop();
        webSockSvr.stop();
        mongoDBClnt.disconnect();
    }
}
