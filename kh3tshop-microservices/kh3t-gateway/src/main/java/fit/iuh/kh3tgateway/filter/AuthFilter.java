package fit.iuh.kh3tgateway.filter;

import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;
import fit.iuh.kh3tgateway.util.JwtUtil;

import java.nio.charset.StandardCharsets;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Value("${security.jwt.secret:changeme}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        // Exclude public endpoints
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange.getResponse(), 1005, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        try {
            if (!JwtUtil.validateToken(token, jwtSecret)) {
                return unauthorized(exchange.getResponse(), 1008, "Invalid or expired token");
            }
        } catch (JOSEException e) {
            return unauthorized(exchange.getResponse(), 1006, "Token validation error");
        }

        return chain.filter(exchange);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator") || 
               path.startsWith("/eureka") || 
               path.startsWith("/public") || 
               path.equals("/") ||
               path.contains("/auth/login") || 
               path.contains("/auth/introspect") || 
               path.contains("/auth/refresh") ||
               path.contains("/auth/forgot-password") || 
               path.contains("/auth/reset-password") ||
               path.contains("/accounts") ||
               path.contains("/products") ||
               path.contains("/categories") ||
               path.contains("/sizes") ||
               path.contains("/size-details") ||
               path.contains("/v1/payment") ||
               path.contains("/chat");
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, int code, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        String jsonResponse = String.format("{\"code\":%d, \"message\":\"%s\"}", code, message);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -100; // run before other filters
    }
}
