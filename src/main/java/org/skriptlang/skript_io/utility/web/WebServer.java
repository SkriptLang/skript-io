package org.skriptlang.skript_io.utility.web;

import com.sun.net.httpserver.HttpServer;
import mx.kenzie.clockwork.io.DataTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class WebServer {

    public static final int DEFAULT_PORT = 80,
        DEFAULT_BACKLOG = 0, MAX_DATA_LENGTH = 64_000_000; // todo config option for these?
    protected static final Map<Integer, WebServer> servers = new HashMap<>();
    protected final int port;
    private final Map<URI, PostHandler> handlers;
    private HttpServer server;

    public WebServer(int port) {
        this.port = port;
        this.handlers = new HashMap<>();
    }

    public static @Nullable WebServer get(int port) {
        return servers.get(port);
    }

    public static @NotNull WebServer getOrCreate(int port) {
        WebServer current = servers.get(port);
        if (current != null) return current;
        WebServer server = new WebServer(port);
        servers.put(port, server);
        return server;
    }

    public void prepareIfNecessary() {
        if (server == null) this.prepare();
    }

    public void registerHandler(URI uri, PostHandler handler) {
        if (handlers.containsKey(uri) && server != null) server.removeContext(uri.toString());
        this.handlers.put(uri, handler);
        if (server == null) return;
        this.server.createContext(uri.toString(), handler);
    }

    public void closeHandler(URI uri) {
        if (!handlers.containsKey(uri)) return;
        this.handlers.remove(uri);
        if (server == null) return;
        this.server.removeContext(uri.toString());
    }

    public void prepare() {
        if (server != null) server.stop(0);
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), DEFAULT_BACKLOG);
            for (Map.Entry<URI, PostHandler> entry : handlers.entrySet()) {
                URI uri = entry.getKey();
                PostHandler handler = entry.getValue();
                this.server.createContext(uri.toString(), handler);
            }
            this.server.setExecutor(null);
            this.server.start();
        } catch (IOException ex) {
            SkriptIO.error(ex);
        }
    }

    public void closeAll() {
        SkriptIO.queue().queue(new DataTask() {
            @Override
            public void execute() throws IOException, InterruptedException {
                server.stop(10);
                handlers.clear();
            }
        });
    }

}
