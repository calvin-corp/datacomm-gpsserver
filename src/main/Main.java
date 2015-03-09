package main;

import java.io.IOException;

import domain.AndroidClientServer;
import domain.GpsRecordManager;
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

        // parse command line arguments
        try
        {
            androidServerPort = Integer.valueOf(args[0]);
            websocketServerPort = Integer.valueOf(args[1]);
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
            androidSvr = new AndroidClientServer(androidServerPort,gpsRecords);
            webSockSvr = new WebClientServer(websocketServerPort,gpsRecords);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        // start the servers
        androidSvr.start();
        webSockSvr.start();

        // end the program when input is received
        System.in.read();
        androidSvr.stop();
        webSockSvr.stop();
    }
}
//
//    public static class MyTCPServer extends TCPServer
//    {
//
//        public MyTCPServer(int serverPort) throws IOException
//        {
//            super(serverPort);
//        }
//
//        @Override
//        protected void onClose(Socket conn, boolean remote)
//        {
//            SocketAddress addr = conn.getRemoteSocketAddress();
//            System.out.println("client "+addr+" disconnected "+(remote?"by remote":"by server")+".");
//            
//        }
//
//        @Override
//        protected void onMessage(Socket conn, String msg)
//        {
//            SocketAddress addr = conn.getRemoteSocketAddress();
//            System.out.println(addr+": "+msg);
////            stop();
//
//            try
//            {
//                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
//                os.writeUTF("SEE YA LATER");
//            }
//            catch (IOException e)
//            {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        protected void onOpen(Socket conn)
//        {
//            SocketAddress addr = conn.getRemoteSocketAddress();
//            System.out.println("client "+addr+" has connected.");
//        }
//    }
