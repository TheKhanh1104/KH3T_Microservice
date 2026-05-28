package fit.iuh.kh3tshopbe.mapper;

import fit.iuh.kh3tshopbe.dto.request.AccountRequest;
import fit.iuh.kh3tshopbe.dto.request.CustomerRequest;
import fit.iuh.kh3tshopbe.dto.response.AccountResponse;
import fit.iuh.kh3tshopbe.dto.response.CustomerResponse;
import fit.iuh.kh3tshopbe.entities.Account;
import fit.iuh.kh3tshopbe.entities.Customer;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AccountMapperImpl implements AccountMapper {

    @Override
    public Account toAccount(AccountRequest accountRequest) {
        if ( accountRequest == null ) {
            return null;
        }

        Account.AccountBuilder account = Account.builder();

        if ( accountRequest.getCustomer() != null ) {
            account.customer( customerRequestToCustomer( accountRequest.getCustomer() ) );
        }
        if ( accountRequest.getUsername() != null ) {
            account.username( accountRequest.getUsername() );
        }
        if ( accountRequest.getPassword() != null ) {
            account.password( accountRequest.getPassword() );
        }
        if ( accountRequest.getRole() != null ) {
            account.role( accountRequest.getRole() );
        }
        if ( accountRequest.getStatusLogin() != null ) {
            account.statusLogin( accountRequest.getStatusLogin() );
        }

        return account.build();
    }

    @Override
    public void updateAccountFromRequest(AccountRequest accountRequest, Account account) {
        if ( accountRequest == null ) {
            return;
        }

        if ( accountRequest.getCustomer() != null ) {
            if ( account.getCustomer() == null ) {
                account.setCustomer( new Customer() );
            }
            customerRequestToCustomer1( accountRequest.getCustomer(), account.getCustomer() );
        }
        else {
            account.setCustomer( null );
        }
        if ( accountRequest.getUsername() != null ) {
            account.setUsername( accountRequest.getUsername() );
        }
        else {
            account.setUsername( null );
        }
        if ( accountRequest.getPassword() != null ) {
            account.setPassword( accountRequest.getPassword() );
        }
        else {
            account.setPassword( null );
        }
        if ( accountRequest.getRole() != null ) {
            account.setRole( accountRequest.getRole() );
        }
        else {
            account.setRole( null );
        }
        if ( accountRequest.getStatusLogin() != null ) {
            account.setStatusLogin( accountRequest.getStatusLogin() );
        }
        else {
            account.setStatusLogin( null );
        }
    }

    @Override
    public AccountResponse toAccountResponse(Account account) {
        if ( account == null ) {
            return null;
        }

        AccountResponse.AccountResponseBuilder accountResponse = AccountResponse.builder();

        if ( account.getCreateAt() != null ) {
            accountResponse.createAt( account.getCreateAt() );
        }
        if ( account.getCustomer() != null ) {
            accountResponse.customer( customerToCustomerResponse( account.getCustomer() ) );
        }
        accountResponse.id( account.getId() );
        if ( account.getRole() != null ) {
            accountResponse.role( account.getRole() );
        }
        if ( account.getStatusLogin() != null ) {
            accountResponse.statusLogin( account.getStatusLogin() );
        }
        if ( account.getUpdateAt() != null ) {
            accountResponse.updateAt( account.getUpdateAt() );
        }
        if ( account.getUsername() != null ) {
            accountResponse.username( account.getUsername() );
        }

        return accountResponse.build();
    }

    protected Customer customerRequestToCustomer(CustomerRequest customerRequest) {
        if ( customerRequest == null ) {
            return null;
        }

        Customer customer = new Customer();

        if ( customerRequest.getFullName() != null ) {
            customer.setFullName( customerRequest.getFullName() );
        }
        if ( customerRequest.getPhoneNumber() != null ) {
            customer.setPhoneNumber( customerRequest.getPhoneNumber() );
        }
        if ( customerRequest.getEmail() != null ) {
            customer.setEmail( customerRequest.getEmail() );
        }
        if ( customerRequest.getGender() != null ) {
            customer.setGender( customerRequest.getGender() );
        }
        if ( customerRequest.getDateOfBirth() != null ) {
            customer.setDateOfBirth( customerRequest.getDateOfBirth() );
        }

        return customer;
    }

    protected void customerRequestToCustomer1(CustomerRequest customerRequest, Customer mappingTarget) {
        if ( customerRequest == null ) {
            return;
        }

        if ( customerRequest.getFullName() != null ) {
            mappingTarget.setFullName( customerRequest.getFullName() );
        }
        else {
            mappingTarget.setFullName( null );
        }
        if ( customerRequest.getPhoneNumber() != null ) {
            mappingTarget.setPhoneNumber( customerRequest.getPhoneNumber() );
        }
        else {
            mappingTarget.setPhoneNumber( null );
        }
        if ( customerRequest.getEmail() != null ) {
            mappingTarget.setEmail( customerRequest.getEmail() );
        }
        else {
            mappingTarget.setEmail( null );
        }
        if ( customerRequest.getGender() != null ) {
            mappingTarget.setGender( customerRequest.getGender() );
        }
        else {
            mappingTarget.setGender( null );
        }
        if ( customerRequest.getDateOfBirth() != null ) {
            mappingTarget.setDateOfBirth( customerRequest.getDateOfBirth() );
        }
        else {
            mappingTarget.setDateOfBirth( null );
        }
    }

    protected CustomerResponse customerToCustomerResponse(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        CustomerResponse.CustomerResponseBuilder customerResponse = CustomerResponse.builder();

        if ( customer.getDateOfBirth() != null ) {
            customerResponse.dateOfBirth( customer.getDateOfBirth() );
        }
        if ( customer.getEmail() != null ) {
            customerResponse.email( customer.getEmail() );
        }
        if ( customer.getFullName() != null ) {
            customerResponse.fullName( customer.getFullName() );
        }
        if ( customer.getGender() != null ) {
            customerResponse.gender( customer.getGender() );
        }
        customerResponse.id( customer.getId() );
        if ( customer.getPhoneNumber() != null ) {
            customerResponse.phoneNumber( customer.getPhoneNumber() );
        }

        return customerResponse.build();
    }
}
