package fit.iuh.kh3tshopbe.dto.response;

public class AuthenticationResponse {
    private boolean isAuthenticated;
    private String token;

    public AuthenticationResponse() {}

    public AuthenticationResponse(boolean isAuthenticated, String token) {
        this.isAuthenticated = isAuthenticated;
        this.token = token;
    }

    public boolean isAuthenticated() { return isAuthenticated; }
    public void setAuthenticated(boolean isAuthenticated) { this.isAuthenticated = isAuthenticated; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    // Manual Builder
    public static AuthenticationResponseBuilder builder() {
        return new AuthenticationResponseBuilder();
    }

    public static class AuthenticationResponseBuilder {
        private boolean isAuthenticated;
        private String token;

        public AuthenticationResponseBuilder isAuthenticated(boolean isAuthenticated) {
            this.isAuthenticated = isAuthenticated;
            return this;
        }

        public AuthenticationResponseBuilder token(String token) {
            this.token = token;
            return this;
        }

        public AuthenticationResponse build() {
            return new AuthenticationResponse(isAuthenticated, token);
        }
    }
}
