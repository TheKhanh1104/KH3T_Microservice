package fit.iuh.kh3tshopbe.shared.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fit.iuh.kh3tshopbe.shared.enums.Status;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private int id;

    @Column(name = "product_name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private double price;

    @Column(name = "cost_price")
    private double costPrice;

    @Column(name = "unit")
    private String unit;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "image_url_front")
    private String imageUrlFront;

    @Column(name = "image_url_back")
    private String imageUrlBack;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "brand")
    private String brand;

    @Column(name = "rating")
    private double rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category")
    private Category category;

    @Column(name = "discount_amount")
    private double discountAmount;

    @Column(name = "material")
    private String material;

    @Column(name = "form")
    private String form;

    @Enumerated(EnumType.STRING)
    private Status status;

    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<SizeDetail> sizeDetails;

    public Product() {}

    public Product(int id, String name, String description, double price, double costPrice,
                   String unit, int quantity, String imageUrlFront, String imageUrlBack,
                   Date createdAt, Date updatedAt, String brand, double rating,
                   Category category, double discountAmount, String material,
                   String form, Status status, List<SizeDetail> sizeDetails) {
        this.id = id; this.name = name; this.description = description;
        this.price = price; this.costPrice = costPrice; this.unit = unit;
        this.quantity = quantity; this.imageUrlFront = imageUrlFront;
        this.imageUrlBack = imageUrlBack; this.createdAt = createdAt;
        this.updatedAt = updatedAt; this.brand = brand; this.rating = rating;
        this.category = category; this.discountAmount = discountAmount;
        this.material = material; this.form = form; this.status = status;
        this.sizeDetails = sizeDetails;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getImageUrlFront() { return imageUrlFront; }
    public void setImageUrlFront(String imageUrlFront) { this.imageUrlFront = imageUrlFront; }
    public String getImageUrlBack() { return imageUrlBack; }
    public void setImageUrlBack(String imageUrlBack) { this.imageUrlBack = imageUrlBack; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public String getForm() { return form; }
    public void setForm(String form) { this.form = form; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public List<SizeDetail> getSizeDetails() { return sizeDetails; }
    public void setSizeDetails(List<SizeDetail> sizeDetails) { this.sizeDetails = sizeDetails; }

    public static ProductBuilder builder() { return new ProductBuilder(); }

    public static class ProductBuilder {
        private int id; private String name; private String description;
        private double price; private double costPrice; private String unit;
        private int quantity; private String imageUrlFront; private String imageUrlBack;
        private Date createdAt; private Date updatedAt; private String brand;
        private double rating; private Category category; private double discountAmount;
        private String material; private String form; private Status status;
        private List<SizeDetail> sizeDetails;

        public ProductBuilder id(int id) { this.id = id; return this; }
        public ProductBuilder name(String name) { this.name = name; return this; }
        public ProductBuilder description(String description) { this.description = description; return this; }
        public ProductBuilder price(double price) { this.price = price; return this; }
        public ProductBuilder costPrice(double costPrice) { this.costPrice = costPrice; return this; }
        public ProductBuilder unit(String unit) { this.unit = unit; return this; }
        public ProductBuilder quantity(int quantity) { this.quantity = quantity; return this; }
        public ProductBuilder imageUrlFront(String imageUrlFront) { this.imageUrlFront = imageUrlFront; return this; }
        public ProductBuilder imageUrlBack(String imageUrlBack) { this.imageUrlBack = imageUrlBack; return this; }
        public ProductBuilder createdAt(Date createdAt) { this.createdAt = createdAt; return this; }
        public ProductBuilder updatedAt(Date updatedAt) { this.updatedAt = updatedAt; return this; }
        public ProductBuilder brand(String brand) { this.brand = brand; return this; }
        public ProductBuilder rating(double rating) { this.rating = rating; return this; }
        public ProductBuilder category(Category category) { this.category = category; return this; }
        public ProductBuilder discountAmount(double discountAmount) { this.discountAmount = discountAmount; return this; }
        public ProductBuilder material(String material) { this.material = material; return this; }
        public ProductBuilder form(String form) { this.form = form; return this; }
        public ProductBuilder status(Status status) { this.status = status; return this; }
        public ProductBuilder sizeDetails(List<SizeDetail> sizeDetails) { this.sizeDetails = sizeDetails; return this; }
        public Product build() {
            return new Product(id, name, description, price, costPrice, unit, quantity,
                    imageUrlFront, imageUrlBack, createdAt, updatedAt, brand, rating,
                    category, discountAmount, material, form, status, sizeDetails);
        }
    }
}
