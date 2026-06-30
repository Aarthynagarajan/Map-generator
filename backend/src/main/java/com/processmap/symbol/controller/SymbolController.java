package com.processmap.symbol.controller;

import com.processmap.common.ApiResponse;
import com.processmap.dto.SymbolResponseDTO;
import com.processmap.project.entity.Domain;
import com.processmap.symbol.entity.Symbol;
import com.processmap.symbol.mapper.SymbolMapper;
import com.processmap.symbol.repository.SymbolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/symbols")
@RequiredArgsConstructor
public class SymbolController {

    private final SymbolRepository symbolRepository;
    private final SymbolMapper symbolMapper;

    @GetMapping
    public ApiResponse<List<SymbolResponseDTO>> getSymbols(@RequestParam String domain) {
        Domain dom = Domain.fromString(domain);
        if (dom == null) {
            dom = Domain.INDUSTRIAL; // fallback
        }

        List<Symbol> symbols = symbolRepository.findByDomain(dom);
        List<SymbolResponseDTO> dtos = symbols.stream().map(symbolMapper::toResponseDTO).toList();
        return ApiResponse.of(dtos);
    }
}
