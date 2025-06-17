package com.services.userservice.services;

import com.services.userservice.dtos.AddressDTO;
import com.services.userservice.dtos.CreateAddressRequest;
import com.services.userservice.dtos.UpdateAddressRequest;
import com.services.userservice.exceptions.UserNotFound;
import com.services.userservice.models.Address;
import com.services.userservice.models.User;
import com.services.userservice.repositories.AddressRepository;
import com.services.userservice.repositories.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepo userRepository;

    public AddressService(AddressRepository addressRepository, UserRepo userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    private AddressDTO convertToDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setStreetAddress(address.getStreetAddress());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setCountry(address.getCountry());
        dto.setZipCode(address.getZipCode());
        dto.setDefault(address.isDefault());
        dto.setLabel(address.getLabel());
        dto.setAdditionalInfo(address.getAdditionalInfo());
        return dto;
    }

    public List<AddressDTO> getUserAddresses(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("User not found with email: " + email));
        return addressRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressDTO createAddress(String email, CreateAddressRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("User not found with email: " + email));

        Address address = new Address();
        address.setUser(user);
        address.setStreetAddress(request.getStreetAddress());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setZipCode(request.getZipCode());
        address.setDefault(request.isDefault());
        address.setLabel(request.getLabel());
        address.setAdditionalInfo(request.getAdditionalInfo());

        // If this is set as default, unset any other default addresses
        if (request.isDefault()) {
            addressRepository.findByUser(user).stream()
                    .filter(Address::isDefault)
                    .forEach(addr -> {
                        addr.setDefault(false);
                        addressRepository.save(addr);
                    });
        }

        return convertToDTO(addressRepository.save(address));
    }

    @Transactional
    public AddressDTO updateAddress(Long addressId, String email, UpdateAddressRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("User not found with email: " + email));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found with id: " + addressId));

        // Verify the address belongs to the user
        if (!address.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Address does not belong to user");
        }

        if (request.getStreetAddress() != null)
            address.setStreetAddress(request.getStreetAddress());
        if (request.getCity() != null)
            address.setCity(request.getCity());
        if (request.getState() != null)
            address.setState(request.getState());
        if (request.getCountry() != null)
            address.setCountry(request.getCountry());
        if (request.getZipCode() != null)
            address.setZipCode(request.getZipCode());
        if (request.getLabel() != null)
            address.setLabel(request.getLabel());
        if (request.getAdditionalInfo() != null)
            address.setAdditionalInfo(request.getAdditionalInfo());

        // Handle default address change
        if (request.isDefault() && !address.isDefault()) {
            addressRepository.findByUser(user).stream()
                    .filter(Address::isDefault)
                    .forEach(addr -> {
                        addr.setDefault(false);
                        addressRepository.save(addr);
                    });
            address.setDefault(true);
        }

        return convertToDTO(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long addressId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("User not found with email: " + email));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found with id: " + addressId));

        // Verify the address belongs to the user
        if (!address.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Address does not belong to user");
        }

        addressRepository.delete(address);
    }

    public AddressDTO getAddress(Long addressId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("User not found with email: " + email));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found with id: " + addressId));

        // Verify the address belongs to the user
        if (!address.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Address does not belong to user");
        }

        return convertToDTO(address);
    }
}