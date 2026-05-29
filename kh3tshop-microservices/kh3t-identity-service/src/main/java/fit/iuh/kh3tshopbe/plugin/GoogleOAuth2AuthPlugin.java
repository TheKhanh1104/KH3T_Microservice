package fit.iuh.kh3tshopbe.plugin;

import fit.iuh.kh3tshopbe.dto.request.AuthenticationRequest;
import fit.iuh.kh3tshopbe.entities.Account;
import fit.iuh.kh3tshopbe.exception.AppException;
import fit.iuh.kh3tshopbe.exception.ErrorCode;
import fit.iuh.kh3tshopbe.repository.AccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoogleOAuth2AuthPlugin implements AuthenticationPlugin {

    AccountRepository accountRepository;

    @Override
    public boolean supports(String loginType) {
        return "GOOGLE".equalsIgnoreCase(loginType);
    }

    @Override
    public Account authenticate(AuthenticationRequest request) {
        // In a real OAuth2 implementation, request.getPassword() would contain the OAuth2 ID token / Access token
        // and we would call Google APIs to verify it.
        // For demonstration/academic presentation, we look up the user by email/username.
        Account user = accountRepository.findByUsername(request.getUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new AppException(ErrorCode.User_Not_Authenticated);
        }

        // Mock verification: check if password contains dummy token or simulate success
        return user;
    }
}
