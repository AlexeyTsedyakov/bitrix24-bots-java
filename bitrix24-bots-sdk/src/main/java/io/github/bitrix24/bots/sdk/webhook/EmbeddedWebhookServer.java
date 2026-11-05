package io.github.bitrix24.bots.sdk.webhook;

import com.sun.net.httpserver.HttpServer;
import io.github.bitrix24.bots.sdk.config.WebhookConfig;
import io.github.bitrix24.bots.sdk.dispatcher.BotEventDispatcher;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lightweight embedded HTTP server powered by the standard JDK {@link HttpServer}.
 * Enables running a Bitrix24 webhook receiver out of the box with zero third-party servlet dependencies.
 */
@Slf4j
public class EmbeddedWebhookServer implements AutoCloseable {

    private final WebhookConfig config;
    private final WebhookHandler handler;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private HttpServer server;
    private ExecutorService executor;

    /**
     * Constructs embedded webhook server with configuration and dispatcher.
     *
     * @param config webhook server configuration
     * @param dispatcher event dispatcher
     */
    public EmbeddedWebhookServer(WebhookConfig config, BotEventDispatcher dispatcher) {
        this(config, new WebhookHandler(dispatcher));
    }

    /**
     * Constructs embedded webhook server with configuration and handler.
     *
     * @param config webhook server configuration
     * @param handler webhook handler
     */
    public EmbeddedWebhookServer(WebhookConfig config, WebhookHandler handler) {
        this.config = config != null ? config : WebhookConfig.builder().build();
        this.handler = Objects.requireNonNull(handler, "handler cannot be null");
    }

    /**
     * Starts the embedded HTTP server.
     *
     * @return this instance
     * @throws IOException if server fails to bind to port
     */
    public synchronized EmbeddedWebhookServer start() throws IOException {
        if (running.compareAndSet(false, true)) {
            InetSocketAddress address = new InetSocketAddress(config.getHost(), config.getPort());
            server = HttpServer.create(address, config.getBacklog());
            server.createContext(config.getPath(), handler);

            executor = Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "bitrix24-webhook-http");
                t.setDaemon(true);
                return t;
            });
            server.setExecutor(executor);
            server.start();

            log.info("Embedded Bitrix24 webhook server started at http://{}:{}{}",
                    config.getHost(), getPort(), config.getPath());
        } else {
            log.warn("EmbeddedWebhookServer is already running");
        }
        return this;
    }

    /**
     * Stops the embedded HTTP server with default 1 second graceful delay.
     */
    public synchronized void stop() {
        stop(1);
    }

    /**
     * Stops the embedded HTTP server with specified delay.
     *
     * @param delaySeconds maximum seconds to wait for active exchanges to complete
     */
    public synchronized void stop(int delaySeconds) {
        if (running.compareAndSet(true, false)) {
            log.info("Stopping embedded Bitrix24 webhook server (port: {})", getPort());
            if (server != null) {
                server.stop(delaySeconds);
                server = null;
            }
            if (executor != null) {
                executor.shutdownNow();
                executor = null;
            }
        }
    }

    /**
     * Checks if the embedded server is currently listening.
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Returns the actual bound port (useful when port 0 is used for ephemeral allocation).
     *
     * @return bound port or configured port
     */
    public int getPort() {
        if (server != null && server.getAddress() != null) {
            return server.getAddress().getPort();
        }
        return config.getPort();
    }

    /**
     * Returns the configured webhook path.
     *
     * @return URL path
     */
    public String getPath() {
        return config.getPath();
    }

    /**
     * Returns the full listening URL.
     *
     * @return URL string
     */
    public String getUrl() {
        return "http://" + config.getHost() + ":" + getPort() + config.getPath();
    }

    /**
     * Returns the underlying {@link WebhookHandler}.
     *
     * @return webhook handler
     */
    public WebhookHandler getHandler() {
        return handler;
    }

    @Override
    public void close() {
        stop();
    }
}
