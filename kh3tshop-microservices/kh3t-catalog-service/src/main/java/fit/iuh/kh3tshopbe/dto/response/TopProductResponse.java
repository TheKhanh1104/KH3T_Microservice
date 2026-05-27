package fit.iuh.kh3tshopbe.dto.response;

public class TopProductResponse {
    private String name;
    private String category;
    private int sales;
    private double revenue;
    private String trend;
    private String img;

    public TopProductResponse() {}

    public TopProductResponse(String name, String category, int sales, double revenue, String trend, String img) {
        this.name = name;
        this.category = category;
        this.sales = sales;
        this.revenue = revenue;
        this.trend = trend;
        this.img = img;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getSales() { return sales; }
    public void setSales(int sales) { this.sales = sales; }

    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }

    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }

    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }
}
