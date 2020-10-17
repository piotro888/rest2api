package eu.piotro.test2api.tcp;

import eu.piotro.test2api.api.APIForwarder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
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
    private static final int defaultMaxQueueSize = 100;
    private static final int defaultRateLimit = 10;

    /**
     * Create server with custom parameters
     * @param port inet port number
     * @param forwarder APIForwarder to route HTTP requests
     * @param maxThreads maximum number of connections to handle simultaneously
     * @param minKeepThreads minimum number of handler threads to keep (more - faster reaction to traffic changes)
     * @param inactiveWorkerTimeout time in seconds to keep inactive handlers over minimum limit
     * @param readTimeout time in milliseconds to timeout request reading
     * @param maxQueueSize maximum number of waiting connections
     * @param rateLimit maximum number of connections from one IP address per second
     * @throws IOException if I/O error when creating ServerSocket ex. cannot bind to port
     */
    public Server (int port, APIForwarder forwarder, int maxThreads, int minKeepThreads, int inactiveWorkerTimeout, int readTimeout, int maxQueueSize, int rateLimit) throws IOException {
        serverSocket = new ServerSocket(port);
        executor = new ServerExecutor(maxThreads, minKeepThreads, inactiveWorkerTimeout, maxQueueSize);
        this.forwarder = forwarder;
        this.readTimeout = readTimeout;
        this.rateLimit = rateLimit;
        clearAndSchedule();
    }

    /**
     * Create server with default parameters
     * @param port inet port number
     * @param forwarder APIForwarder to route HTTP requests
     * @throws IOException if I/O error when creating ServerSocket ex. cannot bind to port
     */
    public Server (int port, APIForwarder forwarder) throws IOException {
        this(port, forwarder, defaultMaxThreads, defaultMinKeepThreads, defaultInactiveWorkerTimeout, defaultReadTimeout, defaultMaxQueueSize, defaultRateLimit);
    }

    /**
     * Listen for new connection and execute it asynchronously in future
     */
    public void accept(){
        try {
            Socket acceptedSocket = serverSocket.accept();
            System.out.println(acceptedSocket);

            if(!checkRateLimit(acceptedSocket))
                return;

            executor.execute(new ConnectionHandler(acceptedSocket, forwarder, scheduledExecutor, readTimeout));

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private boolean checkRateLimit(Socket socket){
        String ip = socket.getInetAddress().getHostAddress();
        if(rateLimitMap.containsKey(ip)){
            int cnt = rateLimitMap.get(ip);
            if(cnt > rateLimit){
                try {
                    String response = "HTTP/1.1 429 Too Many Requests\r\nContent-Length: 0\r\n\r\n";
                    socket.getOutputStream().write(response.getBytes(), 0, response.length());
                    socket.getOutputStream().flush();
                    socket.close();
                } catch (IOException ignored) {}
                return false;
            }
            rateLimitMap.replace(ip, cnt, cnt+1);
        } else {
            rateLimitMap.put(ip, 1);
        }
        return true;
    }

    private void clearAndSchedule(){
        rateLimitMap.clear();
        scheduledExecutor.schedule(this::clearAndSchedule, 1, TimeUnit.SECONDS);
    }

    private final Executor executor;
    private final ServerSocket serverSocket;
    private final APIForwarder forwarder;
    private final int readTimeout;
    private final int rateLimit;
    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    private final HashMap<String, Integer> rateLimitMap = new HashMap<>();
}
