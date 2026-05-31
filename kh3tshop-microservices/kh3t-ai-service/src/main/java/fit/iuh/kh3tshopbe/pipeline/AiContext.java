package fit.iuh.kh3tshopbe.pipeline;

import lombok.Builder;
import lombok.Data;
import java.util.Map;
import java.util.HashMap;

@Data
@Builder
public class AiContext {
    private String prompt;
    private String rawResponse;
    private String finalResponse;
    private StringBuilder systemContext;
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    private boolean shouldStop;
    private String errorMessage;

    public void appendSystemContext(String info) {
        if (systemContext == null) systemContext = new StringBuilder();
        systemContext.append(info).append("\n");
    }
}
