package fit.iuh.kh3tgateway.filter;

import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterFilter implements GlobalFilter, Ordered {

    private static final int LIMIT = 5; // 5 requests
    private static final long WINDOW_MS = 60_000L; // per minute

    private static class Window {
        long windowStart;
        int count;
    }

    private final Map<String, Window> cache = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if ("/api/auth/login".equals(path)) {
            String ip = exchange.getRequest().getRemoteAddress() != null ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
            long now = Instant.now().toEpochMilli();
            Window w = cache.computeIfAbsent(ip, k -> { Window nw = new Window(); nw.windowStart = now; nw.count = 0; return nw; });
            synchronized (w) {
                if (now - w.windowStart > WINDOW_MS) {
                    w.windowStart = now;
                    w.count = 0;
                }
                if (w.count >= LIMIT) {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    byte[] bytes = "{\"code\":429,\"message\":\"Too many requests\"}".getBytes(StandardCharsets.UTF_8);
                    return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
                }
                w.count++;
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -90;
    }
}
