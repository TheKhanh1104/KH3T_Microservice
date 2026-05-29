package fit.iuh.kh3tshopbe.shared.dto.response;

public class CategoryRevenueResponse {
    private String name;
    private long value;
    private double revenue;
    private String color;

    public CategoryRevenueResponse() {}
    public CategoryRevenueResponse(String name, long value, double revenue, String color) {
        this.name = name; this.value = value;
        this.revenue = revenue; this.color = color;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getValue() { return value; }
    public void setValue(long value) { this.value = value; }
    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
