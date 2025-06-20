package com.services.userservice.services;

import com.services.userservice.dtos.AddressDTO;
import com.services.userservice.models.Address;
import com.services.userservice.models.User;
import com.services.userservice.repositories.AddressRepository;
import com.services.userservice.repositories.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepo userRepo;

    @Autowired
    public AddressService(AddressRepository addressRepository, UserRepo userRepo) {
        this.addressRepository = addressRepository;
        this.userRepo = userRepo;
    }

    public List<AddressDTO> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AddressDTO getAddressById(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId);
        return address != null ? toDTO(address) : null;
    }

    @Transactional
    public AddressDTO addAddress(Long userId, AddressDTO dto) {
        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isEmpty())
            throw new RuntimeException("User not found");
        Address address = fromDTO(dto);
        address.setUser(userOpt.get());
        if (dto.isDefault()) {
            unsetDefaultForUser(userId);
            address.setDefault(true);
        }
        Address saved = addressRepository.save(address);
        return toDTO(saved);
    }

    @Transactional
    public AddressDTO updateAddress(Long userId, Long addressId, AddressDTO dto) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId);
        if (address == null)
            throw new RuntimeException("Address not found");

        if (dto.getStreetAddress() != null)
            address.setStreetAddress(dto.getStreetAddress());
        if (dto.getCity() != null)
            address.setCity(dto.getCity());
        if (dto.getState() != null)
            address.setState(dto.getState());
        if (dto.getCountry() != null)
            address.setCountry(dto.getCountry());
        if (dto.getZipCode() != null)
            address.setZipCode(dto.getZipCode());
        if (dto.getLabel() != null)
            address.setLabel(dto.getLabel());
        if (dto.getAdditionalInfo() != null)
            address.setAdditionalInfo(dto.getAdditionalInfo());

        // Only update default if explicitly set
        if (dto.isDefault()) {
            unsetDefaultForUser(userId);
            address.setDefault(true);
        }

        Address saved = addressRepository.save(address);
        return toDTO(saved);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId);
        if (address != null) {
            addressRepository.delete(address);
        }
    }

    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {

        Address address = addressRepository.findByIdAndUserId(addressId, userId);

        if (address != null) {
            unsetDefaultForUser(userId);
            address.setDefault(true);
            addressRepository.save(address);
        } else {
            throw new RuntimeException("Address not found");
        }
    }

    private void unsetDefaultForUser(Long userId) {
        List<Address> defaults = addressRepository.findByUserIdAndIsDefaultTrue(userId);
        for (Address addr : defaults) {
            addr.setDefault(false);
            addressRepository.save(addr);
        }
    }

    private AddressDTO toDTO(Address address) {
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

    private Address fromDTO(AddressDTO dto) {
        Address address = new Address();
        address.setStreetAddress(dto.getStreetAddress());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setCountry(dto.getCountry());
        address.setZipCode(dto.getZipCode());
        address.setDefault(dto.isDefault());
        address.setLabel(dto.getLabel());
        address.setAdditionalInfo(dto.getAdditionalInfo());
        return address;
    }
}