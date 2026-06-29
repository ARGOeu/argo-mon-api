package org.grnet.status.mappers;

import org.grnet.status.dtos.encrypt.EncryptResponseDto;
import org.grnet.status.dtos.general.ExistResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GeneralMapper {

    GeneralMapper INSTANCE = Mappers.getMapper(GeneralMapper.class);

    @Named("toEncryptResponse")
    @Mapping(target = "secret", source = "encryptedValue")
    EncryptResponseDto toEncryptResponse(String encryptedValue);

    @Named("toExistResponse")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "exist", source = "exist")
    ExistResponseDto toExistResponse(String name, Boolean exist);
}
