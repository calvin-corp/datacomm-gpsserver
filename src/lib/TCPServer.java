package lib;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class TCPServer
{
    /**
     * the server's ServerSocket used to accept connection requests.
     */
    private final ServerSocket serverSock;

    /**
     * handle to the accept thread running on this server, accepting new
     *   connections.
     */
    private AcceptThread acceptThread;

    /**
     * set of running CommThreads being managed by this server.
     */
    private Set<CommThread> commThreads;

    //////////////////////
    // public interface //
    //////////////////////

    /**
     * 
     * @param serverPort
     * @throws IOException
     */
    public TCPServer(int serverPort) throws IOException
    {
        serverSock = new ServerSocket(serverPort);
    }

    /**
     * starts the server, and makes it listening for connections on the passed
     *   port.
     */
    public void start()
    {
        if(acceptThread == null)
        {
            acceptThread = new AcceptThread(serverSock);
            commThreads = new LinkedHashSet<>();
            acceptThread.start();
        }
    }

    /**
     * stops the server, and closes all connections to it.
     */
    public void stop()
    {
        if(acceptThread != null)
        {
            acceptThread.cancel();
            for(CommThread commThread : commThreads)
            {
                commThread.cancel();
            }
            acceptThread = null;
        }
    }

    /////////////////////////
    // protected interface //
    /////////////////////////

    /**
     * callback invoked when a new connection is established with the server.
     *
     * @param conn socket that is created to communicate with the new
     *   conneciton.
     */
    protected abstract void onOpen(Socket conn);

    /**
     * callback invoked when a message from a connection is received.
     *
     * @param conn socket that the message was received from.
     * @param msg message received from the socket.
     */
    protected abstract void onMessage(Socket conn, String msg);

    /**
     * callback invoked when the socket is closed by either the server, or the
     *   client.
     *
     * @param conn socket that was closed.
     * @param remote true if the socket was closed by the remote host; false
     *   otherwise.
     */
    protected abstract void onClose(Socket conn, boolean remote);

    //////////////////
    // AcceptThread //
    //////////////////

    /**
     * the AcceptThread is the thread that's used to accept incoming connection
     *   requests, and pass them off to connected threads.
     */
    private class AcceptThread extends Thread
    {
        /**
         * socket that the AcceptThread is supposed to listen to.
         */
        private ServerSocket svrSock;

        /**
         * instantiates a new AcceptThread that will listen to the passes server
         *   socket once the thread is started.
         *
         * @param  svrSock ServerSocket to listen to.
         */
        public AcceptThread(ServerSocket svrSock)
        {
            this.svrSock = svrSock;
            setName("AcceptThread "+svrSock.getLocalPort());
        }

        /**
         * the threaded method.
         *
         * continuously accepts connections, and invokes the onOpen callback
         *   whenever a new connection is established.
         */
        @Override
        public synchronized void run()
        {

            // accept new connections, and call callbacks
            while(true)
            {
                try
                {
                    Socket conn = svrSock.accept();
                    onOpen(conn);
                    CommThread commThread = new CommThread(conn);
                    commThread.start();
                }
                catch (IOException e)
                {
                    // notify all threads waiting for this thread to terminate
                    notifyAll();
                    break;
                }
            }
        }

        /**
         * cancels the accept thread.
         */
        public void cancel()
        {
            try
            {
                svrSock.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    ////////////////
    // CommThread //
    ////////////////

    /**
     * the CommThread is used to listen to connections, and invoke callbacks
     *   when one is received, or if the socket is closed. When the socket is
     *   closed, the thread will terminate.
     */
    private class CommThread extends Thread
    {
        /**
         * socket that's connected to a client.
         */
        private Socket sock;

        /**
         * constructs a new CommThread object that is used to read from the
         *   passed socket connection.
         *
         * @param  connection socket that this thread is supposed to listen to.
         */
        public CommThread(Socket connection)
        {
            this.sock = connection;
            setName("CommThread "+sock.getRemoteSocketAddress());
        }

        /**
         * starts the thread, and registers it with the server's set of active
         *   communication threads.
         */
        @Override
        public void start()
        {
            super.start();
            commThreads.add(CommThread.this);
        }

        /**
         * the threaded method.
         *
         * continuously reads from the socket, and invokes the onMessage
         *   callback whenever a new message is received from the connection.
         *   will call the onClose callback when the socket closes.
         */
        @Override
        public void run()
        {
            DataInputStream is;

            // get the socket's input stream so we can read from it.
            try
            {
                is = new DataInputStream(sock.getInputStream());
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            // continuously read from the connection, and invoke onMessage, or
            // invoke onClose when the socket is closed.
            while(true)
            {
                try
                {
                    // read from the socket & invoke onMessage callback
                    String msg = is.readUTF();
                    onMessage(sock,msg);
                }
                catch (SocketException e)
                {
                    // socket closed by local host
                    onClose(sock,false);
                    break;
                }
                catch (IOException e)
                {
                    // socket closed by remote host
                    onClose(sock,true);
                    break;
                }
            }

            // release resources
            try
            {
                is.close();
                sock.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            // remove self from set of active CommThreads
            commThreads.remove(CommThread.this);
        }

        /**
         * cancels, and ends the thread.
         */
        public void cancel()
        {
            try
            {
                sock.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
