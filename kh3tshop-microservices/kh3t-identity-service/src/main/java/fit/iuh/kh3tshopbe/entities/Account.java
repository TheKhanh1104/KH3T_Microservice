package fit.iuh.kh3tshopbe.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fit.iuh.kh3tshopbe.enums.Role;
import fit.iuh.kh3tshopbe.enums.StatusLogin;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "account")
public class Account {
    @Id
    @Column(name = "login_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(name = "create_at")
    private Date createAt;

    @Column(name = "update_at")
    private Date updateAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_login")
    private StatusLogin statusLogin;

    @Column(name = "otp_code")
    private String otpCode;

    @Column(name = "otp_expiry")
    private Date otpExpiry;

    @JsonIgnore
    @OneToMany(mappedBy = "account")
    private List<Address> addresses;

    public Account() {}

    public Account(int id, Customer customer, String username, String password, Role role, Date createAt, Date updateAt, StatusLogin statusLogin, List<Address> addresses, String otpCode, Date otpExpiry) {
        this.id = id;
        this.customer = customer;
        this.username = username;
        this.password = password;
        this.role = role;
        this.createAt = createAt;
        this.updateAt = updateAt;
        this.statusLogin = statusLogin;
        this.addresses = addresses;
        this.otpCode = otpCode;
        this.otpExpiry = otpExpiry;
    }

    // Manual Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Date getCreateAt() { return createAt; }
    public void setCreateAt(Date createAt) { this.createAt = createAt; }
    public Date getUpdateAt() { return updateAt; }
    public void setUpdateAt(Date updateAt) { this.updateAt = updateAt; }
    public StatusLogin getStatusLogin() { return statusLogin; }
    public void setStatusLogin(StatusLogin statusLogin) { this.statusLogin = statusLogin; }
    public List<Address> getAddresses() { return addresses; }
    public void setAddresses(List<Address> addresses) { this.addresses = addresses; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public Date getOtpExpiry() { return otpExpiry; }
    public void setOtpExpiry(Date otpExpiry) { this.otpExpiry = otpExpiry; }

    // Manual Builder
    public static AccountBuilder builder() {
        return new AccountBuilder();
    }

    public static class AccountBuilder {
        private int id;
        private Customer customer;
        private String username;
        private String password;
        private Role role;
        private Date createAt;
        private Date updateAt;
        private StatusLogin statusLogin;
        private List<Address> addresses;
        private String otpCode;
        private Date otpExpiry;

        public AccountBuilder id(int id) {
            this.id = id;
            return this;
        }

        public AccountBuilder customer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public AccountBuilder username(String username) {
            this.username = username;
            return this;
        }

        public AccountBuilder password(String password) {
            this.password = password;
            return this;
        }

        public AccountBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public AccountBuilder createAt(Date createAt) {
            this.createAt = createAt;
            return this;
        }

        public AccountBuilder updateAt(Date updateAt) {
            this.updateAt = updateAt;
            return this;
        }

        public AccountBuilder statusLogin(StatusLogin statusLogin) {
            this.statusLogin = statusLogin;
            return this;
        }

        public AccountBuilder addresses(List<Address> addresses) {
            this.addresses = addresses;
            return this;
        }

        public AccountBuilder otpCode(String otpCode) {
            this.otpCode = otpCode;
            return this;
        }

        public AccountBuilder otpExpiry(Date otpExpiry) {
            this.otpExpiry = otpExpiry;
            return this;
        }

        public Account build() {
            return new Account(id, customer, username, password, role, createAt, updateAt, statusLogin, addresses, otpCode, otpExpiry);
        }
    }
}
