package fit.iuh.kh3tshopbe.pipeline.impl;

import fit.iuh.kh3tshopbe.pipeline.AiContext;
import fit.iuh.kh3tshopbe.pipeline.AiFilter;
import fit.iuh.kh3tshopbe.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LlmProcessorFilter implements AiFilter {

    private final GeminiService geminiService;

    @Override
    public void execute(AiContext context) {
        // Tạo prompt cuối cùng kết hợp từ user prompt và system context (RAG)
        String finalPrompt = "CONTEXT HỆ THỐNG:\n" + context.getSystemContext().toString() + 
                             "\n\nCÂU HỎI NGƯỜI DÙNG: " + context.getPrompt();
        
        try {
            // Gọi qua GeminiService (giữ lại cơ chế fallback đã có của bạn)
            String response = geminiService.generateRawText(finalPrompt);
            context.setRawResponse(response);
        } catch (Exception e) {
            context.setErrorMessage("Lỗi khi gọi AI: " + e.getMessage());
            context.setShouldStop(true);
        }
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
