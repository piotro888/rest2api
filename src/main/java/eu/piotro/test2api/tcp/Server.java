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
    private static final int defaultMaxThreads = 10;
    private static final int defaultMinKeepThreads = 1;
    private static final int defaultInactiveWorkerTimeout = 10;
    private static final int defaultReadTimeout = 5000;

    /**
     * Create server with custom parameters
     * @param port inet port number
     * @param forwarder APIForwarder to route HTTP requests
     * @param maxThreads maximum number of connections to handle simultaneously
     * @param minKeepThreads minimum number of handler threads to keep (more - faster reaction to traffic changes)
     * @param inactiveWorkerTimeout time in seconds to keep inactive handlers over minimum limit
     * @param readTimeout time in milliseconds to timeout request reading
     * @throws IOException if I/O error when creating ServerSocket ex. cannot bind to port
     */
    public Server (int port, APIForwarder forwarder, int maxThreads, int minKeepThreads, int inactiveWorkerTimeout, int readTimeout) throws IOException {
        serverSocket = new ServerSocket(port);
        executor = new ServerExecutor(maxThreads, minKeepThreads, inactiveWorkerTimeout);
        this.forwarder = forwarder;
        this.readTimeout = readTimeout;
    }

    /**
     * Create server
     * @param port inet port number
     * @param forwarder APIForwarder to route HTTP requests
     * @throws IOException if I/O error when creating ServerSocket ex. cannot bind to port
     */
    public Server (int port, APIForwarder forwarder) throws IOException {
        this(port, forwarder, defaultMaxThreads, defaultMinKeepThreads, defaultInactiveWorkerTimeout, defaultReadTimeout);
    }

    /**
     * Listen for new connection and execute it asynchronously in future
     */
    public void accept(){
        try {
            Socket acceptedSocket = serverSocket.accept();
            System.out.println(acceptedSocket);

            executor.execute(new ConnectionHandler(acceptedSocket, forwarder, timeoutExecutor, readTimeout));

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private final Executor executor;
    private final ServerSocket serverSocket;
    private final APIForwarder forwarder;
    private final int readTimeout;
    private final ScheduledExecutorService timeoutExecutor = Executors.newSingleThreadScheduledExecutor();
}
