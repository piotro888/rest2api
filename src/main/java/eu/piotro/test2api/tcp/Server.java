package eu.piotro.test2api.tcp;

import eu.piotro.test2api.api.APIForwarder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Socket server for HTTP
 * @since 0.1
 */
public class Server {
    private static final int maxThreads = 4;
    private static final int minKeepThreads = 1;
    private static final int inactiveWorkerTimeout = 10;

    /**
     * Create server with custom {@link ServerExecutor} parameters
     * @param port inet port number
     * @param forwarder APIForwarder to route HTTP requests
     * @param maxThreads maximum number of connections to handle simultaneously
     * @param minKeepThreads minimum number of handler threads to keep (more - faster reaction to traffic changes)
     * @param inactiveWorkerTimeout time in seconds to keep inactive handlers over minimum limit
     * @throws IOException if I/O error when creating ServerSocket ex. cannot bind to port
     */
    public Server (int port, APIForwarder forwarder, int maxThreads, int minKeepThreads, int inactiveWorkerTimeout) throws IOException {
        serverSocket = new ServerSocket(port);
        executor = new ServerExecutor(maxThreads, minKeepThreads, inactiveWorkerTimeout);
        this.forwarder = forwarder;
    }

    /**
     * Create server
     * @param port inet port number
     * @param forwarder APIForwarder to route HTTP requests
     * @throws IOException if I/O error when creating ServerSocket ex. cannot bind to port
     */
    public Server (int port, APIForwarder forwarder) throws IOException {
        this(port, forwarder, maxThreads, minKeepThreads, inactiveWorkerTimeout);
    }

    /**
     * Listen for new connection and execute it asynchronously in future
     */
    public void accept(){
        try {
            Socket acceptedSocket = serverSocket.accept();
            System.out.println(acceptedSocket);

            executor.execute(new ConnectionHandler(acceptedSocket, forwarder));

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private final Executor executor;
    private final ServerSocket serverSocket;
    private final APIForwarder forwarder;
}
