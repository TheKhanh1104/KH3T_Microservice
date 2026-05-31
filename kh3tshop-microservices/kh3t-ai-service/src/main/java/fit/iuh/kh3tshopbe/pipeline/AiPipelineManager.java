package fit.iuh.kh3tshopbe.pipeline;

import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Service
public class AiPipelineManager {

    private final List<AiFilter> filters;

    public AiPipelineManager(List<AiFilter> filters) {
        this.filters = filters.stream()
                .sorted(Comparator.comparingInt(AiFilter::getOrder))
                .toList();
    }

    public AiContext execute(String userPrompt, java.util.Map<String, Object> initialMetadata) {
        AiContext context = AiContext.builder()
                .prompt(userPrompt)
                .systemContext(new StringBuilder())
                .metadata(new java.util.HashMap<>(initialMetadata))
                .build();

        for (AiFilter filter : filters) {
            if (context.isShouldStop()) break;
            filter.execute(context);
        }

        return context;
    }
}
