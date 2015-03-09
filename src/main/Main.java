package main;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

import lib.TCPServer;

public class Main
{
    public static void main(String[] args)
    {
        MyTCPServer svr;
        try
        {
            svr = new MyTCPServer(7000);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        System.out.println("Server started on port 7000");
        svr.start();
    }

    public static class MyTCPServer extends TCPServer
    {

        public MyTCPServer(int serverPort) throws IOException
        {
            super(serverPort);
        }

        @Override
        protected void onClose(Socket conn, boolean remote)
        {
            SocketAddress addr = conn.getRemoteSocketAddress();
            System.out.println("client "+addr+" disconnected "+(remote?"by remote":"by server")+".");
            
        }

        @Override
        protected void onMessage(Socket conn, String msg)
        {
            SocketAddress addr = conn.getRemoteSocketAddress();
            System.out.println(addr+": "+msg);
//            stop();

            try
            {
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeUTF("SEE YA LATER");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        protected void onOpen(Socket conn)
        {
            SocketAddress addr = conn.getRemoteSocketAddress();
            System.out.println("client "+addr+" has connected.");
        }
    }
}
