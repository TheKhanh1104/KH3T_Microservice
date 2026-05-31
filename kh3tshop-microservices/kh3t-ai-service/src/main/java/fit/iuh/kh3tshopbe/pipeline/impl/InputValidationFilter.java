package fit.iuh.kh3tshopbe.pipeline.impl;

import fit.iuh.kh3tshopbe.pipeline.AiContext;
import fit.iuh.kh3tshopbe.pipeline.AiFilter;
import org.springframework.stereotype.Component;

@Component
public class InputValidationFilter implements AiFilter {
    @Override
    public void execute(AiContext context) {
        if (context.getPrompt() == null || context.getPrompt().isBlank()) {
            context.setFinalResponse("Em chưa nhận được câu hỏi, anh/chị vui lòng gửi lại nhé!");
            context.setShouldStop(true);
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
