package fit.iuh.kh3tshopbe.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import fit.iuh.kh3tshopbe.dto.request.AuthenticationRequest;
import fit.iuh.kh3tshopbe.dto.request.IntrospectRequest;
import fit.iuh.kh3tshopbe.dto.response.AuthenticationResponse;
import fit.iuh.kh3tshopbe.dto.response.IntrospectResponse;
import fit.iuh.kh3tshopbe.entities.Account;
import fit.iuh.kh3tshopbe.entities.Customer;
import fit.iuh.kh3tshopbe.exception.AppException;
import fit.iuh.kh3tshopbe.exception.ErrorCode;
import fit.iuh.kh3tshopbe.plugin.AuthenticationPlugin;
import fit.iuh.kh3tshopbe.repository.AccountRepository;
import fit.iuh.kh3tshopbe.repository.CustomerRepository;
import java.util.List;
import java.util.UUID;
import fit.iuh.kh3tshopbe.service.RefreshTokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    AccountRepository accountRepository;
    PasswordEncoder passwordEncoder;
    EmailService emailService;
    JwtService jwtService;
    CustomerRepository customerRepository;
    RefreshTokenService refreshTokenService;
    List<AuthenticationPlugin> plugins;

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();

        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            var verified = signedJWT.verify(verifier);

            return IntrospectResponse.builder()
                    .valid(verified && expiryTime.after(new Date()))
                    .build();
        } catch (Exception e) {
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }
    }
    public AuthenticationResponse authenticate(AuthenticationRequest request){
        String loginType = (request.getLoginType() == null || request.getLoginType().isBlank()) 
                           ? "PASSWORD" : request.getLoginType().trim().toUpperCase();
        
        // DEBUG: Kiểm tra danh sách plugin tại đây
        if (plugins == null || plugins.isEmpty()) {
            throw new RuntimeException("Authentication System Error: No security plugins loaded.");
        }

        AuthenticationPlugin plugin = plugins.stream()
                .filter(p -> p.supports(loginType))
                .findFirst()
                .orElseThrow(() -> {
                    System.err.println("No plugin found for loginType: " + loginType);
                    return new AppException(ErrorCode.User_Not_Authenticated);
                });

        Account user = plugin.authenticate(request);

        var token = generationToken(user);
        var refresh = UUID.randomUUID().toString();
        refreshTokenService.storeRefreshToken(user.getUsername(), refresh);

        return AuthenticationResponse.builder()
            .isAuthenticated(true)
            .token(token)
            .refreshToken(refresh)
            .username(user.getUsername())
            .role(user.getRole() != null ? user.getRole().name() : "USER")
            .build();
    }

        public AuthenticationResponse refreshTokens(String username, String refreshToken){
        var user = accountRepository.findByUsername(username).orElseThrow(
            () -> new RuntimeException("User not found")
        );
        String newRefresh = UUID.randomUUID().toString();
        boolean ok = refreshTokenService.validateAndRotate(username, refreshToken, newRefresh);
        if(!ok){
            throw new RuntimeException("Invalid refresh token");
        }
        var newAccess = generationToken(user);
        return AuthenticationResponse.builder()
            .isAuthenticated(true)
            .token(newAccess)
            .refreshToken(newRefresh)
            .build();
        }

    private static final String SIGNER_KEY =
            "c8e09fddda9e192d16c485affabc61c9f4bca77a60c19d448f3a6e8475b9f0a4e0d1f69bca8d21f1123b8f0f8a0b8d12";
    private String generationToken(Account account) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(account.getUsername())
                    .issuer("kh3t-shop")
                    .issueTime(new Date())
                    .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                    .claim("scope", account.getRole() != null ? account.getRole().name() : "USER")
                    .build();

            Payload payload = new Payload(claimsSet.toJSONObject());
            JWSObject jwsObject = new JWSObject(header, payload);

            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));

            return jwsObject.serialize();
        } catch (Exception e) {
            System.err.println("Token Generation Error: " + e.getMessage());
            throw new AppException(ErrorCode.Token_Generation_Failed);
        }
    }




}
