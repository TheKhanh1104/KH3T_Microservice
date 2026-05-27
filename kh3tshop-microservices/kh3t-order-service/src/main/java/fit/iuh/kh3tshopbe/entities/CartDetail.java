package fit.iuh.kh3tshopbe.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "cart_detail")
public class CartDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_detail_id")
    private int id;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "price_at_time")
    private double price_at_time;

    @Column(name = "subtotal")
    private double subtotal;

    @Column(name = "is_selected")
    private boolean isSelected;

    @Column(name = "create_at")
    @Temporal(TemporalType.DATE)
    private Date createAt;

    @Column(name = "update_at")
    @Temporal(TemporalType.DATE)
    private Date updateAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    @JsonIgnore
    private Cart cart;

    @Column(name = "product_id")
    private int productId;

    @Column(name = "size_detail_id")
    private int sizeDetailId;

    public CartDetail() {}

    public CartDetail(int id, int quantity, double price_at_time, double subtotal, boolean isSelected, Date createAt, Date updateAt, Cart cart, int productId, int sizeDetailId) {
        this.id = id;
        this.quantity = quantity;
        this.price_at_time = price_at_time;
        this.subtotal = subtotal;
        this.isSelected = isSelected;
        this.createAt = createAt;
        this.updateAt = updateAt;
        this.cart = cart;
        this.productId = productId;
        this.sizeDetailId = sizeDetailId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice_at_time() { return price_at_time; }
    public void setPrice_at_time(double price_at_time) { this.price_at_time = price_at_time; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    public Date getCreateAt() { return createAt; }
    public void setCreateAt(Date createAt) { this.createAt = createAt; }

    public Date getUpdateAt() { return updateAt; }
    public void setUpdateAt(Date updateAt) { this.updateAt = updateAt; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getSizeDetailId() { return sizeDetailId; }
    public void setSizeDetailId(int sizeDetailId) { this.sizeDetailId = sizeDetailId; }
}
