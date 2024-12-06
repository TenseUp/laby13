package edu.cis.Controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class SimpleServer implements HttpHandler {
    private HttpServer server;
    private SimpleServerRequestListener requestListener;
    private int port;

    public SimpleServer(SimpleServerRequestListener listener, int port) {
        this.requestListener = listener;
        this.port = port;
    }

    public static String getUriString(HttpExchange exchange) {
        return exchange.getRequestURI().toString();
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", this);
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestUrl = getUriString(exchange);
        Request request = Request.fromUrl(requestUrl);

        String response = requestListener.requestMade(request);

        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}

interface SimpleServerRequestListener {
    String requestMade(Request request);
}