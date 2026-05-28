package fit.iuh.kh3tshopbe.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {

    private final StringRedisTemplate redis;
    private static final Duration TTL = Duration.ofDays(7);

    public RefreshTokenService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private String keyFor(String username) {
        return "refresh:" + username;
    }

    public void storeRefreshToken(String username, String token) {
        redis.opsForValue().set(keyFor(username), token, TTL.toMillis(), TimeUnit.MILLISECONDS);
    }

    public boolean validateAndRotate(String username, String token, String newToken) {
        String key = keyFor(username);
        String existing = redis.opsForValue().get(key);
        if (Objects.equals(existing, token)) {
            // rotate: overwrite with new token and TTL
            redis.opsForValue().set(key, newToken, TTL.toMillis(), TimeUnit.MILLISECONDS);
            return true;
        }
        return false;
    }

    public boolean validate(String username, String token) {
        String existing = redis.opsForValue().get(keyFor(username));
        return Objects.equals(existing, token);
    }

}
