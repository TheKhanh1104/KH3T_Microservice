package fit.iuh.kh3tshopbe.entities;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "customer_trading")
public class CustomerTrading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trading_id")
    private int id;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false)
    private String receiverPhone;

    @Column(name = "receiver_email")
    private String receiverEmail;

    @Column(name = "receiver_address", nullable = false)
    private String receiverAddress;

    @Column(name = "total_amount")
    private double totalAmount;

    @Column(name = "trading_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date tradingDate;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToOne(mappedBy = "customerTrading", fetch = FetchType.LAZY)
    private Order order;

    public CustomerTrading() {}

    public CustomerTrading(int id, String receiverName, String receiverPhone, String receiverEmail, String receiverAddress, double totalAmount, Date tradingDate, Date createdAt, Date updatedAt, Order order) {
        this.id = id;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.receiverEmail = receiverEmail;
        this.receiverAddress = receiverAddress;
        this.totalAmount = totalAmount;
        this.tradingDate = tradingDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.order = order;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }

    public String getReceiverEmail() { return receiverEmail; }
    public void setReceiverEmail(String receiverEmail) { this.receiverEmail = receiverEmail; }

    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public Date getTradingDate() { return tradingDate; }
    public void setTradingDate(Date tradingDate) { this.tradingDate = tradingDate; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
}
