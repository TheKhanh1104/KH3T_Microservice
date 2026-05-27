package fit.iuh.kh3tshopbe.dto.response;

public class IntrospectResponse {
    private boolean valid;

    public IntrospectResponse() {}

    public IntrospectResponse(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    // Manual Builder
    public static IntrospectResponseBuilder builder() {
        return new IntrospectResponseBuilder();
    }

    public static class IntrospectResponseBuilder {
        private boolean valid;

        public IntrospectResponseBuilder valid(boolean valid) {
            this.valid = valid;
            return this;
        }

        public IntrospectResponse build() {
            return new IntrospectResponse(valid);
        }
    }
}
