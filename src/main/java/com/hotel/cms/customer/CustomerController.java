package com.hotel.cms.customer;

import com.hotel.cms.access.CurrentUserService;
import com.hotel.cms.common.ApiResponse;
import com.hotel.cms.customer.dto.CustomerRequest;
import com.hotel.cms.exception.NotFoundException;
import com.hotel.cms.user.UserAccount;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerRepository repo;
    private final CurrentUserService currentUserService;

    public CustomerController(CustomerRepository repo, CurrentUserService currentUserService) {
        this.repo = repo;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ApiResponse<List<Customer>> list(Principal principal, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size) {
        UserAccount user = currentUserService.get(principal);
        List<Customer> customers = currentUserService.isSuperAdmin(user)
                ? repo.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending())).getContent()
                : repo.findCustomersByOwnerUserId(user.getId());
        return ApiResponse.ok("Customers", customers);
    }

    @PostMapping
    public ApiResponse<Customer> create(@Valid @RequestBody CustomerRequest r) {
        Customer c = new Customer();
        apply(c, r);
        return ApiResponse.ok("Customer created", repo.save(c));
    }

    @PutMapping("/{id}")
    public ApiResponse<Customer> update(@PathVariable String id, @Valid @RequestBody CustomerRequest r) {
        Customer c = repo.findById(id).orElseThrow(() -> new NotFoundException("Customer not found"));
        apply(c, r);
        return ApiResponse.ok("Customer updated", repo.save(c));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Customer> delete(@PathVariable String id) {
        Customer c = repo.findById(id).orElseThrow(() -> new NotFoundException("Customer not found"));
        c.setActive(false);
        return ApiResponse.ok("Customer disabled", repo.save(c));
    }

    private void apply(Customer c, CustomerRequest r) {
        c.setFullName(r.fullName());
        c.setEmail(r.email());
        c.setPhone(r.phone());
        c.setGender(r.gender());
        c.setAddress(r.address());
    }
}
