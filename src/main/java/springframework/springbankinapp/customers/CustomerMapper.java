package springframework.springbankinapp.customers;


import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResponseDto toCustomerResponseDto(Customer customer);

    Customer toEntity(CreateCustomerDto createCustomerDto);

    void updateEntityFromDto(UpdateCustomerDto updateCustomerDto, @MappingTarget Customer customer);

    List<CustomerResponseDto> toCustomerResponseList(List<Customer> customers);
}
