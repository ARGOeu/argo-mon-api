package org.grnet.status.mappers;

import org.grnet.status.dtos.encrypt.EncryptResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface EncryptMapper {

    EncryptMapper INSTANCE = Mappers.getMapper(EncryptMapper.class);

    @Named("toEncryptResponse")
    @Mapping(target = "secret", source = "encryptedValue")
    EncryptResponseDto toEncryptResponse(String encryptedValue);
}
