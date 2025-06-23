package com.services.userservice.repositories;

import com.services.userservice.models.Address;
import com.services.userservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);

    List<Address> findByUserId(Long userId);

    Address findByIdAndUserId(Long id, Long userId);

    List<Address> findByUserIdAndIsDefaultTrue(Long userId);

    void deleteByUserId(Long userId);
}