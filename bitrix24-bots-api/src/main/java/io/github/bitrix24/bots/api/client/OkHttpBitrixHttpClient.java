package io.github.bitrix24.bots.api.client;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Lightweight, high-performance {@link BitrixHttpClient} implementation based on OkHttp.
 */
@Slf4j
public class OkHttpBitrixHttpClient implements BitrixHttpClient {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final Pattern WEBHOOK_URL_MASK_PATTERN =
            Pattern.compile("(/rest/\\d+/)([^/?#]+)(/?)", Pattern.CASE_INSENSITIVE);

    private final OkHttpClient client;
    private final boolean ownsClient;

    public OkHttpBitrixHttpClient() {
        this(builder());
    }

    public OkHttpBitrixHttpClient(OkHttpClient client) {
        this.client = Objects.requireNonNull(client, "client cannot be null");
        this.ownsClient = false;
    }

    private OkHttpBitrixHttpClient(Builder builder) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(builder.connectTimeout)
                .readTimeout(builder.readTimeout)
                .writeTimeout(builder.writeTimeout)
                .connectionPool(builder.connectionPool);

        if (builder.enableLogging) {
            clientBuilder.addInterceptor(new LoggingInterceptor());
        }

        for (Interceptor interceptor : builder.interceptors) {
            clientBuilder.addInterceptor(interceptor);
        }

        this.client = clientBuilder.build();
        this.ownsClient = true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public OkHttpClient getClient() {
        return client;
    }

    @Override
    public BitrixHttpResponse executePost(String url, String jsonBody, Map<String, String> headers) throws IOException {
        Objects.requireNonNull(url, "url cannot be null");
        String payload = jsonBody != null ? jsonBody : "{}";

        RequestBody body = RequestBody.create(payload, JSON_MEDIA_TYPE);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body)
                .header("Accept", "application/json");

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    requestBuilder.header(entry.getKey(), entry.getValue());
                }
            }
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            int statusCode = response.code();
            String responseBody = "";

            ResponseBody responsePayload = response.body();
            if (responsePayload != null) {
                responseBody = responsePayload.string();
            }

            Map<String, List<String>> responseHeaders = toHeadersMap(response.headers());

            return BitrixHttpResponse.builder()
                    .statusCode(statusCode)
                    .body(responseBody)
                    .headers(responseHeaders)
                    .build();
        }
    }

    @Override
    public void close() {
        if (ownsClient) {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
            if (client.cache() != null) {
                try {
                    client.cache().close();
                } catch (IOException e) {
                    log.warn("Failed to close OkHttp cache: {}", e.getMessage());
                }
            }
        }
    }

    private Map<String, List<String>> toHeadersMap(Headers headers) {
        if (headers == null || headers.size() == 0) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> map = new HashMap<>();
        for (String name : headers.names()) {
            map.put(name, headers.values(name));
        }
        return Collections.unmodifiableMap(map);
    }

    static String maskUrl(String url) {
        if (url == null) {
            return null;
        }
        return WEBHOOK_URL_MASK_PATTERN.matcher(url).replaceAll("$1***$3");
    }

    private static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (!log.isDebugEnabled()) {
                return chain.proceed(request);
            }

            String maskedUrl = maskUrl(request.url().toString());
            long startNanos = System.nanoTime();
            log.debug("Bitrix24 REST Request: {} {}", request.method(), maskedUrl);

            try {
                Response response = chain.proceed(request);
                long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
                log.debug("Bitrix24 REST Response: {} {} ({} ms)", response.code(), maskedUrl, tookMs);
                return response;
            } catch (IOException e) {
                long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
                log.warn("Bitrix24 REST Request failed: {} {} ({} ms): {}", request.method(), maskedUrl, tookMs, e.getMessage());
                throw e;
            }
        }
    }

    public static class Builder {
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout = Duration.ofSeconds(30);
        private Duration writeTimeout = Duration.ofSeconds(30);
        private ConnectionPool connectionPool = new ConnectionPool(5, 5, TimeUnit.MINUTES);
        private boolean enableLogging = true;
        private final List<Interceptor> interceptors = new ArrayList<>();

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = Objects.requireNonNull(connectTimeout, "connectTimeout cannot be null");
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = Objects.requireNonNull(readTimeout, "readTimeout cannot be null");
            return this;
        }

        public Builder writeTimeout(Duration writeTimeout) {
            this.writeTimeout = Objects.requireNonNull(writeTimeout, "writeTimeout cannot be null");
            return this;
        }

        public Builder connectionPool(ConnectionPool connectionPool) {
            this.connectionPool = Objects.requireNonNull(connectionPool, "connectionPool cannot be null");
            return this;
        }

        public Builder enableLogging(boolean enableLogging) {
            this.enableLogging = enableLogging;
            return this;
        }

        public Builder addInterceptor(Interceptor interceptor) {
            this.interceptors.add(Objects.requireNonNull(interceptor, "interceptor cannot be null"));
            return this;
        }

        public OkHttpBitrixHttpClient build() {
            return new OkHttpBitrixHttpClient(this);
        }
    }
}
