package com.processmap.simulation.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmap.dto.ScenarioResponseDTO;
import com.processmap.simulation.entity.Scenario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class ScenarioMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "diagramId", source = "diagram.id")
    @Mapping(target = "stopperStates", expression = "java(mapStopperStates(scenario.getStopperStates()))")
    public abstract ScenarioResponseDTO toResponseDTO(Scenario scenario);

    protected Map<String, String> mapStopperStates(JsonNode stopperStates) {
        if (stopperStates == null) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.convertValue(stopperStates, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
