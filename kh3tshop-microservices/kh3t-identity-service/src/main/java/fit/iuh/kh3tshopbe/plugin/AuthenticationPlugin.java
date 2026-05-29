package fit.iuh.kh3tshopbe.plugin;

import fit.iuh.kh3tshopbe.dto.request.AuthenticationRequest;
import fit.iuh.kh3tshopbe.entities.Account;

public interface AuthenticationPlugin {
    /**
     * Checks if this plugin supports the given authentication type.
     *
     * @param loginType the authentication type (e.g. "PASSWORD", "GOOGLE", etc.)
     * @return true if supported, false otherwise
     */
    boolean supports(String loginType);

    /**
     * Performs authentication logic and returns the authenticated Account.
     *
     * @param request the authentication request credentials
     * @return the authenticated Account
     */
    Account authenticate(AuthenticationRequest request);
}
