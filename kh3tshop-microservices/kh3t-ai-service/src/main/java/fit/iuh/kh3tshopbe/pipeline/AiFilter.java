package fit.iuh.kh3tshopbe.pipeline;

public interface AiFilter {
    void execute(AiContext context);
    
    default int getOrder() {
        return 0;
    }
}
