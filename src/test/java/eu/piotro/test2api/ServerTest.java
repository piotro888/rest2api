package eu.piotro.test2api;

import eu.piotro.test2api.api.APIForwarder;
import eu.piotro.test2api.tcp.Server;
import org.junit.*;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.Assert.*;

public class ServerTest {

    @BeforeClass
    public static void setUp() throws Exception {
        APIForwarder forwarder = new APIForwarder();
        forwarder.registerClassStatic(TestHandlersStatic.class);
        forwarder.registerClass(new TestHandlers());

        Server server = new Server(1234, forwarder, 2, 1, 100, 100, 100, 10);
        serverThread = new Thread(()->{
            while (!Thread.currentThread().isInterrupted()) {
                server.accept();
            }
        });
        serverThread.start();
        client = HttpClient.newHttpClient();
    }

    @AfterClass
    public static void tearDown() {
        serverThread.interrupt();
    }

    @Test
    public void testGetStatic() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1234/"))
                .build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(httpResponse.statusCode(), 200);
        assertEquals(httpResponse.body(), "OK");

        request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1234/static2"))
                .build();

        httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(httpResponse.statusCode(), 200);
        assertEquals(httpResponse.body(), "OK");
    }

    @Test
    public void testGetDynamic() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1234/d1"))
                .build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(httpResponse.statusCode(), 200);
        assertEquals(httpResponse.body(), "OK");
    }

    @Test
    public void testTimeOut() throws Exception {
        Socket socket = new Socket("localhost", 1234);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Thread.sleep(200);
        assertEquals(reader.readLine(), "HTTP/1.1 408 Request Timeout");
    }

    @Test
    public void testNotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1234/404"))
                .build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(httpResponse.statusCode(), 404);
    }

    @Test
    public void rateLimitTest() throws Exception {
        for(int i=0; i<12; i++) {
            Socket socket = new Socket("localhost", 1234);
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            writer.write("\n\n");
            writer.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            if (reader.readLine().equals("HTTP/1.1 429 Too Many Requests")) {
                socket.close();
                Thread.sleep(1000);
                return;
            }
            socket.close();
        }
        fail();
        Thread.sleep(1000);
    }

    @Test
    public void testBadRequest() throws Exception {
        Socket socket = new Socket("localhost", 1234);
        PrintWriter writer = new PrintWriter(socket.getOutputStream());
        writer.write("badrequest\n\n");
        writer.flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        assertEquals(reader.readLine(), "HTTP/1.1 400 Bad Request");
        socket.close();

        socket = new Socket("localhost", 1234);
        writer = new PrintWriter(socket.getOutputStream());
        writer.write("GET /e500 HTTP/1.1 \n\n");
        writer.flush();
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        assertEquals(reader.readLine(), "HTTP/1.1 500 Internal Server Error");
        socket.close();

        socket = new Socket("localhost", 1234);
        writer = new PrintWriter(socket.getOutputStream());
        writer.write("POST /relay HTTP/1.1\n\n");
        writer.flush();
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        assertEquals(reader.readLine(), "HTTP/1.1 411 Length Required");
        socket.close();
    }

    @Test
    public void testInternalError() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1234/404"))
                .build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(httpResponse.statusCode(), 404);
    }

    @Test
    public void testPost() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1234/relay"))
                .POST(HttpRequest.BodyPublishers.ofString("relay!"))
                .build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(httpResponse.statusCode(), 200);
        assertEquals(httpResponse.body(), "relay!");
    }

    @Test
    public void testMultiMethod() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1234/mm"))
                .PUT(HttpRequest.BodyPublishers.ofString("put"))
                .build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(httpResponse.statusCode(), 405);

        request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1234/mm"))
                .POST(HttpRequest.BodyPublishers.ofString("post"))
                .build();
        httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(httpResponse.statusCode(), 200);
        assertEquals(httpResponse.body(), "OK_POST");

        request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1234/mm"))
                .build();
        httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(httpResponse.statusCode(), 200);
        assertEquals(httpResponse.body(), "OK_GET");
    }

    @Test
    public void testHttpException() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1234/httpex"))
                .build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(httpResponse.statusCode(), 499);
    }

    private static Thread serverThread;
    private static HttpClient client;
}
