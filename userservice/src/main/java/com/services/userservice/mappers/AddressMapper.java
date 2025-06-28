package com.services.userservice.mappers;


import com.services.userservice.dtos.UpdateAddressRequest;
import com.services.userservice.models.Address;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAddressFromDto(UpdateAddressRequest dto, @MappingTarget Address entity);
} 