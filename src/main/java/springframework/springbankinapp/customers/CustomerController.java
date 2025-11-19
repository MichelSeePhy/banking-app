package springframework.springbankinapp.customers;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {


    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDto> createOrganisationCustomer(@Valid @RequestBody CreateCustomerDto customer) {
        var createdCustomer = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<?> updateCustomer(
            @Valid @RequestBody UpdateCustomerDto updateRequest,
            @PathVariable Long customerId) {
        customerService.updateCustomer(customerId, updateRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{customerId}/members")
    public ResponseEntity<?> addMemberToCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody AddMemberDto addMemberDto) {
        customerService.addMemberToOrgCustomer(customerId, addMemberDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{customerId}/members/{memberId}")
    public ResponseEntity<?> removeMemberFromCustomer(
            @PathVariable Long customerId,
            @PathVariable Long memberId) {
        customerService.removeMemberFromOrganization(customerId, memberId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping("/{customerId}")
    public CustomerResponseDto getCustomer(@PathVariable Long customerId) {
        return customerService.getCustomerById(customerId);
    }


    @GetMapping
    public List<CustomerResponseDto> getAllCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Type type
    ){
        return customerService.getAllCustomers(name, type);
    }


}
