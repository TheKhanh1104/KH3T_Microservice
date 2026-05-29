package fit.iuh.kh3tshopbe.plugin;

import fit.iuh.kh3tshopbe.dto.request.AuthenticationRequest;
import fit.iuh.kh3tshopbe.entities.Account;
import fit.iuh.kh3tshopbe.exception.AppException;
import fit.iuh.kh3tshopbe.exception.ErrorCode;
import fit.iuh.kh3tshopbe.repository.AccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UsernamePasswordAuthPlugin implements AuthenticationPlugin {

    AccountRepository accountRepository;
    PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(String loginType) {
        // Luôn hỗ trợ nếu loginType là PASSWORD hoặc bị rỗng
        return loginType == null || 
               loginType.trim().isEmpty() || 
               "PASSWORD".equalsIgnoreCase(loginType.trim());
    }

    @Override
    public Account authenticate(AuthenticationRequest request) {
        
        Account user = accountRepository.findByUsername(request.getUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.Password_Failed);
        }

        return user;
    }
}
