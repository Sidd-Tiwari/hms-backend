package com.hotel.cms.customer;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByUserAccountId(String userAccountId);

    @Query("select distinct c from Customer c join Booking b on b.customer.id = c.id where b.hotel.ownerUser.id = :ownerUserId order by c.createdAt desc")
    List<Customer> findCustomersByOwnerUserId(@Param("ownerUserId") String ownerUserId);

    @Query("select count(distinct c.id) from Customer c join Booking b on b.customer.id = c.id where b.hotel.ownerUser.id = :ownerUserId")
    long countCustomersByOwnerUserId(@Param("ownerUserId") String ownerUserId);
}
