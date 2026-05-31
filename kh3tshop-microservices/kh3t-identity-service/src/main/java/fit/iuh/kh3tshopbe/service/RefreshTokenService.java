package fit.iuh.kh3tshopbe.service;

import fit.iuh.kh3tshopbe.entities.RefreshToken;
import fit.iuh.kh3tshopbe.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private static final long TTL_MILLIS = 7L * 24 * 60 * 60 * 1000; // 7 days

    @Transactional
    public void storeRefreshToken(String username, String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByUsername(username)
                .orElse(new RefreshToken());
        
        refreshToken.setUsername(username);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(new Date(System.currentTimeMillis() + TTL_MILLIS));
        
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public boolean validateAndRotate(String username, String token, String newToken) {
        Optional<RefreshToken> existingOpt = refreshTokenRepository.findByUsername(username);
        if (existingOpt.isPresent()) {
            RefreshToken existing = existingOpt.get();
            if (existing.getToken().equals(token) && existing.getExpiryDate().after(new Date())) {
                existing.setToken(newToken);
                existing.setExpiryDate(new Date(System.currentTimeMillis() + TTL_MILLIS));
                refreshTokenRepository.save(existing);
                return true;
            }
        }
        return false;
    }

    public boolean validate(String username, String token) {
        return refreshTokenRepository.findByUsername(username)
                .map(t -> t.getToken().equals(token) && t.getExpiryDate().after(new Date()))
                .orElse(false);
    }
}
