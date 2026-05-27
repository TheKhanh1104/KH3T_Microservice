package fit.iuh.kh3tshopbe.dto.response;

import fit.iuh.kh3tshopbe.entities.Customer;
import fit.iuh.kh3tshopbe.enums.Role;
import fit.iuh.kh3tshopbe.enums.StatusLogin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AccountResponse {
    int id;
    CustomerResponse customer;
    String username;
    Role role;
    Date createAt;
    Date updateAt;
    StatusLogin statusLogin;

    // Manual Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public CustomerResponse getCustomer() { return customer; }
    public void setCustomer(CustomerResponse customer) { this.customer = customer; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Date getCreateAt() { return createAt; }
    public void setCreateAt(Date createAt) { this.createAt = createAt; }
    public Date getUpdateAt() { return updateAt; }
    public void setUpdateAt(Date updateAt) { this.updateAt = updateAt; }
    public StatusLogin getStatusLogin() { return statusLogin; }
    public void setStatusLogin(StatusLogin statusLogin) { this.statusLogin = statusLogin; }
}
