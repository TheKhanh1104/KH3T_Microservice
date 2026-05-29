package fit.iuh.kh3tshopbe.dto.response;

public class AuthenticationResponse {
    private boolean isAuthenticated;
    private String token;
    private String refreshToken;
    private String username;
    private String role;

    public AuthenticationResponse() {}

    public AuthenticationResponse(boolean isAuthenticated, String token, String refreshToken, String username, String role) {
        this.isAuthenticated = isAuthenticated;
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
    }
    
    // Getters and Setters
    public boolean isAuthenticated() { return isAuthenticated; }
    public void setAuthenticated(boolean isAuthenticated) { this.isAuthenticated = isAuthenticated; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public static AuthenticationResponseBuilder builder() {
        return new AuthenticationResponseBuilder();
    }

    public static class AuthenticationResponseBuilder {
        private boolean isAuthenticated;
        private String token;
        private String refreshToken;
        private String username;
        private String role;

        public AuthenticationResponseBuilder isAuthenticated(boolean isAuthenticated) {
            this.isAuthenticated = isAuthenticated;
            return this;
        }

        public AuthenticationResponseBuilder token(String token) {
            this.token = token;
            return this;
        }

        public AuthenticationResponseBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public AuthenticationResponseBuilder username(String username) {
            this.username = username;
            return this;
        }

        public AuthenticationResponseBuilder role(String role) {
            this.role = role;
            return this;
        }

        public AuthenticationResponse build() {
            return new AuthenticationResponse(isAuthenticated, token, refreshToken, username, role);
        }
    }
}
