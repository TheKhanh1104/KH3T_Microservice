package fit.iuh.kh3tgateway.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterFilter implements GlobalFilter, Ordered {

    private static final int LOGIN_LIMIT = 5;
    private static final int API_LIMIT = 120;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        if (isBypassPath(path) || method == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String clientIp = resolveClientIp(exchange);
        String bucketKey = clientIp + (isLoginPath(path) ? "::login" : "::api");
        Bucket bucket = cache.computeIfAbsent(bucketKey, key -> createBucket(isLoginPath(path)));

        if (!bucket.tryConsume(1)) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
            byte[] bytes = "{\"code\":429,\"message\":\"Too many requests. Please try again later.\"}".getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        }

        return chain.filter(exchange);
    }

    private Bucket createBucket(boolean loginPath) {
        int limit = loginPath ? LOGIN_LIMIT : API_LIMIT;
        return Bucket.builder()
                .addLimit(Bandwidth.classic(limit, Refill.intervally(limit, WINDOW)))
                .build();
    }

    private boolean isLoginPath(String path) {
        return path.endsWith("/auth/login");
    }

    private boolean isBypassPath(String path) {
        return path.startsWith("/actuator")
                || path.startsWith("/eureka")
                || path.startsWith("/public")
                || "/".equals(path)
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/webjars")
                || path.startsWith("/favicon.ico");
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    @Override
    public int getOrder() {
        return -110;
    }
}
