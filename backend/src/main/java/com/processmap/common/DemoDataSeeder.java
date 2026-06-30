package com.processmap.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmap.history.entity.Diagram;
import com.processmap.history.repository.DiagramRepository;
import com.processmap.project.entity.Domain;
import com.processmap.project.entity.Project;
import com.processmap.project.repository.ProjectRepository;
import com.processmap.simulation.entity.Scenario;
import com.processmap.simulation.repository.ScenarioRepository;
import com.processmap.user.entity.User;
import com.processmap.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final DiagramRepository diagramRepository;
    private final ScenarioRepository scenarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("demo@processpro.io").isPresent()) {
            log.info("Demo user already seeded.");
            return;
        }

        log.info("Seeding Demo Mode Projects and Demo User...");

        // 1. Seed Demo User
        User demoUser = User.builder()
                .email("demo@processpro.io")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(com.processmap.user.entity.Role.USER)
                .build();
        demoUser = userRepository.save(demoUser);

        // 2. Seed Industrial Cooling Water System
        Project industrialProj = Project.builder()
                .user(demoUser)
                .name("Industrial Cooling Water System")
                .domain(Domain.INDUSTRIAL)
                .description("Example project modeling water loop dynamics, including reservoir, centrifugal pumps, check valves, storage tank, and control valves.")
                .build();
        industrialProj = projectRepository.save(industrialProj);

        String industrialGraph = """
        {
          "nodes": {
            "n1": { "id": "n1", "label": "Reservoir", "entityClass": "RESERVOIR", "symbolId": "RESERVOIR", "x": 100, "y": 100, "width": 80, "height": 60, "locked": false, "state": "open", "confidence": 0.98, "medium": "liquid", "tag": "TK-101", "aliases": [], "userConfirmRequired": false },
            "n2": { "id": "n2", "label": "Pump P-101", "entityClass": "CENTRIFUGAL_PUMP", "symbolId": "CENTRIFUGAL_PUMP", "x": 300, "y": 100, "width": 80, "height": 60, "locked": false, "state": "open", "confidence": 0.98, "medium": "liquid", "tag": "P-101", "aliases": [], "userConfirmRequired": false },
            "n3": { "id": "n3", "label": "Check Valve CV-101", "entityClass": "CHECK_VALVE", "symbolId": "CHECK_VALVE", "x": 500, "y": 100, "width": 80, "height": 60, "locked": false, "state": "open", "confidence": 0.98, "medium": "liquid", "tag": "CV-101", "aliases": [], "userConfirmRequired": false },
            "n4": { "id": "n4", "label": "Storage Tank T-102", "entityClass": "STORAGE_TANK", "symbolId": "STORAGE_TANK", "x": 700, "y": 100, "width": 120, "height": 100, "locked": false, "state": "open", "confidence": 0.98, "medium": "liquid", "tag": "T-102", "aliases": [], "userConfirmRequired": false }
          },
          "edges": {
            "e1": { "id": "e1", "from": "n1", "to": "n2", "medium": "liquid", "direction": "forward", "routePoints": [], "label": "Suction Line" },
            "e2": { "id": "e2", "from": "n2", "to": "n3", "medium": "liquid", "direction": "forward", "routePoints": [] },
            "e3": { "id": "e3", "from": "n3", "to": "n4", "medium": "liquid", "direction": "forward", "routePoints": [] }
          },
          "adjacency": {
            "n1": ["e1"],
            "n2": ["e2"],
            "n3": ["e3"]
          },
          "domain": "industrial"
        }
        """;

        Diagram indDiag = Diagram.builder()
                .project(industrialProj)
                .promptText("Water flows from reservoir into centrifugal pump P-101, then through check valve CV-101 to storage tank T-102.")
                .graphSnapshot(objectMapper.readTree(industrialGraph))
                .version(1)
                .build();
        indDiag = diagramRepository.save(indDiag);

        Scenario indScenario = Scenario.builder()
                .diagram(indDiag)
                .name("Normal Operation")
                .stopperStates(objectMapper.readTree("{\"n3\": \"open\"}"))
                .isDefault(true)
                .build();
        scenarioRepository.save(indScenario);

        // 3. Seed Electrical Power Distribution
        Project electricalProj = Project.builder()
                .user(demoUser)
                .name("Electrical Power Distribution")
                .domain(Domain.ELECTRICAL)
                .description("Example project modeling generator feed, transformer stepping, circuit breaker relays, and main panels.")
                .build();
        electricalProj = projectRepository.save(electricalProj);

        String electricalGraph = """
        {
          "nodes": {
            "n1": { "id": "n1", "label": "Generator G-1", "entityClass": "GENERATOR", "symbolId": "GENERATOR", "x": 100, "y": 100, "width": 80, "height": 60, "locked": false, "state": "open", "confidence": 0.98, "medium": "electrical", "tag": "G-1", "aliases": [], "userConfirmRequired": false },
            "n2": { "id": "n2", "label": "Breaker CB-1", "entityClass": "CIRCUIT_BREAKER", "symbolId": "CIRCUIT_BREAKER", "x": 100, "y": 250, "width": 80, "height": 60, "locked": false, "state": "on", "confidence": 0.98, "medium": "electrical", "tag": "CB-1", "aliases": [], "userConfirmRequired": false },
            "n3": { "id": "n3", "label": "Transformer XFMR-1", "entityClass": "TRANSFORMER", "symbolId": "TRANSFORMER", "x": 100, "y": 400, "width": 80, "height": 60, "locked": false, "state": "open", "confidence": 0.98, "medium": "electrical", "tag": "XFMR-1", "aliases": [], "userConfirmRequired": false },
            "n4": { "id": "n4", "label": "Panel Board SW-1", "entityClass": "PANEL_BOARD", "symbolId": "PANEL_BOARD", "x": 100, "y": 550, "width": 80, "height": 60, "locked": false, "state": "open", "confidence": 0.98, "medium": "electrical", "tag": "SW-1", "aliases": [], "userConfirmRequired": false }
          },
          "edges": {
            "e1": { "id": "e1", "from": "n1", "to": "n2", "medium": "electrical", "direction": "forward", "routePoints": [], "label": "Primary Feed" },
            "e2": { "id": "e2", "from": "n2", "to": "n3", "medium": "electrical", "direction": "forward", "routePoints": [] },
            "e3": { "id": "e3", "from": "n3", "to": "n4", "medium": "electrical", "direction": "forward", "routePoints": [] }
          },
          "adjacency": {
            "n1": ["e1"],
            "n2": ["e2"],
            "n3": ["e3"]
          },
          "domain": "electrical"
        }
        """;

        Diagram elecDiag = Diagram.builder()
                .project(electricalProj)
                .promptText("Power from generator feeds the step-down transformer XFMR-1 through circuit breaker CB-1 to main panel board SW-1.")
                .graphSnapshot(objectMapper.readTree(electricalGraph))
                .version(1)
                .build();
        elecDiag = diagramRepository.save(elecDiag);

        Scenario elecScenario = Scenario.builder()
                .diagram(elecDiag)
                .name("Bus Energized")
                .stopperStates(objectMapper.readTree("{\"n2\": \"on\"}"))
                .isDefault(true)
                .build();
        scenarioRepository.save(elecScenario);

        // 4. Seed Hydraulic Press System
        Project hydraulicProj = Project.builder()
                .user(demoUser)
                .name("Hydraulic Press System")
                .domain(Domain.HYDRAULIC)
                .description("Example hydraulic loop with high pressure pump HP-1, control valves, reservoirs, and cylinder actuators.")
                .build();
        hydraulicProj = projectRepository.save(hydraulicProj);

        String hydraulicGraph = """
        {
          "nodes": {
            "n1": { "id": "n1", "label": "Fluid Reservoir", "entityClass": "RESERVOIR_HYDRAULIC", "symbolId": "RESERVOIR_HYDRAULIC", "x": 100, "y": 100, "width": 80, "height": 60, "locked": false, "state": "open", "confidence": 0.98, "medium": "hydraulic", "tag": "RES-1", "aliases": [], "userConfirmRequired": false },
            "n2": { "id": "n2", "label": "Hydraulic Pump HP-1", "entityClass": "HYDRAULIC_PUMP", "symbolId": "HYDRAULIC_PUMP", "x": 300, "y": 100, "width": 80, "height": 60, "locked": false, "state": "open", "confidence": 0.98, "medium": "hydraulic", "tag": "HP-1", "aliases": [], "userConfirmRequired": false },
            "n3": { "id": "n3", "label": "Control Valve DCV-1", "entityClass": "DIRECTIONAL_CONTROL_VALVE", "symbolId": "DIRECTIONAL_CONTROL_VALVE", "x": 500, "y": 100, "width": 80, "height": 60, "locked": false, "state": "open", "confidence": 0.98, "medium": "hydraulic", "tag": "DCV-1", "aliases": [], "userConfirmRequired": false },
            "n4": { "id": "n4", "label": "Hydraulic Cylinder CYL-1", "entityClass": "CYLINDER", "symbolId": "CYLINDER", "x": 700, "y": 100, "width": 80, "height": 60, "locked": false, "state": "open", "confidence": 0.98, "medium": "hydraulic", "tag": "CYL-1", "aliases": [], "userConfirmRequired": false }
          },
          "edges": {
            "e1": { "id": "e1", "from": "n1", "to": "n2", "medium": "hydraulic", "direction": "forward", "routePoints": [], "label": "Suction" },
            "e2": { "id": "e2", "from": "n2", "to": "n3", "medium": "hydraulic", "direction": "forward", "routePoints": [] },
            "e3": { "id": "e3", "from": "n3", "to": "n4", "medium": "hydraulic", "direction": "forward", "routePoints": [] }
          },
          "adjacency": {
            "n1": ["e1"],
            "n2": ["e2"],
            "n3": ["e3"]
          },
          "domain": "hydraulic"
        }
        """;

        Diagram hydDiag = Diagram.builder()
                .project(hydraulicProj)
                .promptText("Fluid flows from reservoir, pressurized by hydraulic pump HP-1 to directional control valve DCV-1 to cylinder CYL-1.")
                .graphSnapshot(objectMapper.readTree(hydraulicGraph))
                .version(1)
                .build();
        hydDiag = diagramRepository.save(hydDiag);

        Scenario hydScenario = Scenario.builder()
                .diagram(hydDiag)
                .name("Normal Press Loop")
                .stopperStates(objectMapper.readTree("{\"n3\": \"open\"}"))
                .isDefault(true)
                .build();
        scenarioRepository.save(hydScenario);

        log.info("Demo Mode data seeding completed successfully.");
    }
}
