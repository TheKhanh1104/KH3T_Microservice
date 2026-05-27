package fit.iuh.kh3tshopbe.entities;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private int id;

    @Column(name = "total_quantity")
    private int totalQuantity;

    @Column(name = "total_amount")
    private double totalAmount;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created_at;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated_at;

    @Column(name = "customer_login")
    private int accountId;

    @OneToMany(mappedBy = "cart")
    private List<CartDetail> cart_details;

    public Cart() {}

    public Cart(int id, int totalQuantity, double totalAmount, Date created_at, Date updated_at, int accountId, List<CartDetail> cart_details) {
        this.id = id;
        this.totalQuantity = totalQuantity;
        this.totalAmount = totalAmount;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.accountId = accountId;
        this.cart_details = cart_details;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }

    public Date getUpdated_at() { return updated_at; }
    public void setUpdated_at(Date updated_at) { this.updated_at = updated_at; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public List<CartDetail> getCart_details() { return cart_details; }
    public void setCart_details(List<CartDetail> cart_details) { this.cart_details = cart_details; }
}
