package fit.iuh.kh3tshopbe.pipeline.impl;

import fit.iuh.kh3tshopbe.pipeline.AiContext;
import fit.iuh.kh3tshopbe.pipeline.AiFilter;
import org.springframework.stereotype.Component;

@Component
public class OutputFormattingFilter implements AiFilter {
    @Override
    public void execute(AiContext context) {
        if (context.getRawResponse() != null) {
            String formatted = context.getRawResponse();
            
            // Hậu xử lý: Ví dụ xóa các markdown thừa hoặc thêm câu chào cuối
            if (!formatted.contains("🙏") && !formatted.contains("😊")) {
                formatted += " 😊";
            }
            
            context.setFinalResponse(formatted);
        }
    }

    @Override
    public int getOrder() {
        return 4;
    }
}
