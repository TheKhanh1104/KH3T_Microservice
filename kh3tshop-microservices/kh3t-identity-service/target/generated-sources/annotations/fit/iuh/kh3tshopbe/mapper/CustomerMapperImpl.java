package fit.iuh.kh3tshopbe.mapper;

import fit.iuh.kh3tshopbe.dto.request.CustomerRequest;
import fit.iuh.kh3tshopbe.dto.response.CustomerResponse;
import fit.iuh.kh3tshopbe.entities.Account;
import fit.iuh.kh3tshopbe.entities.Customer;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.4 (Oracle Corporation)"
)
@Component
public class CustomerMapperImpl implements CustomerMapper {

    @Override
    public Customer toCustomer(CustomerRequest customerRequest) {
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

    @Override
    public void updateCustomerFromRequest(CustomerRequest customerRequest, Customer customer) {
        if ( customerRequest == null ) {
            return;
        }

        if ( customerRequest.getFullName() != null ) {
            customer.setFullName( customerRequest.getFullName() );
        }
        else {
            customer.setFullName( null );
        }
        if ( customerRequest.getPhoneNumber() != null ) {
            customer.setPhoneNumber( customerRequest.getPhoneNumber() );
        }
        else {
            customer.setPhoneNumber( null );
        }
        if ( customerRequest.getEmail() != null ) {
            customer.setEmail( customerRequest.getEmail() );
        }
        else {
            customer.setEmail( null );
        }
        if ( customerRequest.getGender() != null ) {
            customer.setGender( customerRequest.getGender() );
        }
        else {
            customer.setGender( null );
        }
        if ( customerRequest.getDateOfBirth() != null ) {
            customer.setDateOfBirth( customerRequest.getDateOfBirth() );
        }
        else {
            customer.setDateOfBirth( null );
        }
    }

    @Override
    public CustomerResponse toCustomerResponse(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        CustomerResponse.CustomerResponseBuilder customerResponse = CustomerResponse.builder();

        customerResponse.accountId( customerAccountId( customer ) );
        customerResponse.id( customer.getId() );
        if ( customer.getFullName() != null ) {
            customerResponse.fullName( customer.getFullName() );
        }
        if ( customer.getPhoneNumber() != null ) {
            customerResponse.phoneNumber( customer.getPhoneNumber() );
        }
        if ( customer.getEmail() != null ) {
            customerResponse.email( customer.getEmail() );
        }
        if ( customer.getGender() != null ) {
            customerResponse.gender( customer.getGender() );
        }
        if ( customer.getDateOfBirth() != null ) {
            customerResponse.dateOfBirth( customer.getDateOfBirth() );
        }

        return customerResponse.build();
    }

    private int customerAccountId(Customer customer) {
        if ( customer == null ) {
            return 0;
        }
        Account account = customer.getAccount();
        if ( account == null ) {
            return 0;
        }
        int id = account.getId();
        return id;
    }
}
