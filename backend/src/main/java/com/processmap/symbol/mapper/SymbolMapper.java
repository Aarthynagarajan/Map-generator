package com.processmap.symbol.mapper;

import com.processmap.dto.SymbolResponseDTO;
import com.processmap.symbol.entity.Symbol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SymbolMapper {
    @Mapping(target = "domain", expression = "java(symbol.getDomain().name())")
    SymbolResponseDTO toResponseDTO(Symbol symbol);
}
