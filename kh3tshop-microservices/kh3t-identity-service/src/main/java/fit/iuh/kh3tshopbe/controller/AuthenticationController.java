package fit.iuh.kh3tshopbe.controller;

import com.nimbusds.jose.JOSEException;
import fit.iuh.kh3tshopbe.configuration.JwtUtil;
import fit.iuh.kh3tshopbe.dto.ResetPassword.ForgotPasswordRequest;
import fit.iuh.kh3tshopbe.dto.ResetPassword.ResetPasswordRequest;
import fit.iuh.kh3tshopbe.dto.request.AuthenticationRequest;
import fit.iuh.kh3tshopbe.dto.request.IntrospectRequest;
import fit.iuh.kh3tshopbe.dto.request.RefreshRequest;
import fit.iuh.kh3tshopbe.dto.response.ApiResponse;
import fit.iuh.kh3tshopbe.dto.response.AuthenticationResponse;
import fit.iuh.kh3tshopbe.dto.response.IntrospectResponse;
import fit.iuh.kh3tshopbe.entities.Account;
import fit.iuh.kh3tshopbe.exception.AppException;
import fit.iuh.kh3tshopbe.exception.ErrorCode;
import fit.iuh.kh3tshopbe.service.AccountService;
import fit.iuh.kh3tshopbe.service.AuthenticationService;
import fit.iuh.kh3tshopbe.service.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Date;
import java.util.Random;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    AccountService accountService;
    JwtUtil jwtUtil;
    EmailService emailService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request){
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) {
        var result = authenticationService.refreshTokens(request.getUsername(), request.getRefreshToken());
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        Account account = accountService.findAccountByCustomerEmail(forgotPasswordRequest.getEmail());
        if (account == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        
        account.setOtpCode(otp);
        account.setOtpExpiry(new Date(System.currentTimeMillis() + 5 * 60 * 1000));
        accountService.saveAccount(account);

        emailService.sendSimpleEmail(
                forgotPasswordRequest.getEmail(),
                "Reset Password OTP",
                "Your verification code is: " + otp + ". It will expire in 5 minutes."
        );
        
        return ApiResponse.<String>builder()
                .result("OTP has been sent to your email.")
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        Account account = accountService.findAccountByCustomerEmail(resetPasswordRequest.getEmail());
        if (account == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        if (account.getOtpCode() == null || !account.getOtpCode().equals(resetPasswordRequest.getOtp())) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        if (account.getOtpExpiry() == null || account.getOtpExpiry().before(new Date())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        account.setPassword(accountService.encodePassword(resetPasswordRequest.getNewPassword()));
        account.setOtpCode(null);
        account.setOtpExpiry(null);
        
        accountService.saveAccount(account);

        return ApiResponse.<String>builder()
                .code(1000)
                .result("Password has been reset successfully.")
                .build();
    }
}
