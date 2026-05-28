package fit.iuh.kh3tgateway.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.Date;

public final class JwtUtil {

    private JwtUtil() {
    }

    public static boolean validateToken(String token, String secret) throws JOSEException {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            MACVerifier verifier = new MACVerifier(secret.getBytes());
            boolean verified = jwt.verify(verifier);
            if (!verified) return false;
            Date exp = jwt.getJWTClaimsSet().getExpirationTime();
            if (exp != null && exp.before(new Date())) return false;
            return true;
        } catch (ParseException e) {
            throw new JOSEException("Invalid JWT format", e);
        }
    }
}
