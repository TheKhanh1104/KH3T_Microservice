package fit.iuh.kh3tshopbe.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fit.iuh.kh3tshopbe.enums.PaymentMethod;
import fit.iuh.kh3tshopbe.enums.StatusOrdering;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private int id;

    @Column(name = "order_code", nullable = false, unique = true)
    private String orderCode;

    @Column(name = "note")
    private String note;

    @Column(name = "order_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_ordering")
    private StatusOrdering statusOrder;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_trading_id")
    private CustomerTrading customerTrading;

    @JsonIgnore
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    @OneToOne(mappedBy = "order")
    private Invoice invoice;

    @Column(name = "account_id")
    private int accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    public Order() {}

    public Order(int id, String orderCode, String note, Date orderDate, StatusOrdering statusOrder, CustomerTrading customerTrading, List<OrderDetail> orderDetails, Invoice invoice, int accountId, PaymentMethod paymentMethod) {
        this.id = id;
        this.orderCode = orderCode;
        this.note = note;
        this.orderDate = orderDate;
        this.statusOrder = statusOrder;
        this.customerTrading = customerTrading;
        this.orderDetails = orderDetails;
        this.invoice = invoice;
        this.accountId = accountId;
        this.paymentMethod = paymentMethod;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public StatusOrdering getStatusOrder() { return statusOrder; }
    public void setStatusOrder(StatusOrdering statusOrder) { this.statusOrder = statusOrder; }

    public CustomerTrading getCustomerTrading() { return customerTrading; }
    public void setCustomerTrading(CustomerTrading customerTrading) { this.customerTrading = customerTrading; }

    public List<OrderDetail> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetail> orderDetails) { this.orderDetails = orderDetails; }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
}
