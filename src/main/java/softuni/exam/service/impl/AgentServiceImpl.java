package softuni.exam.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.ImportAgentDTO;
import softuni.exam.models.entity.Agent;
import softuni.exam.models.entity.Town;
import softuni.exam.repository.AgentRepository;
import softuni.exam.repository.TownRepository;
import softuni.exam.service.AgentService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AgentServiceImpl implements AgentService {
    private AgentRepository agentRepository;
    private TownRepository townRepository;
    private final Gson gson;
    private final Validator validator;
    private final ModelMapper modelMapper;

    public AgentServiceImpl(AgentRepository agentRepository, TownRepository townRepository) {
        this.agentRepository = agentRepository;
        this.townRepository = townRepository;
        this.gson = new GsonBuilder().create();
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.modelMapper = new ModelMapper();
    }

    @Override
    public boolean areImported() {
        return this.agentRepository.count() > 0;
    }

    @Override
    public String readAgentsFromFile() throws IOException {
        Path path = Path.of("src/main/resources/files/json/agents.json");
        return Files.readString(path);
    }

    @Override
    public String importAgents() throws IOException {
        String json = this.readAgentsFromFile();

        ImportAgentDTO[] importAgentDTOS = this.gson.fromJson(json, ImportAgentDTO[].class);

        List<String> result = new ArrayList<>();

        for (ImportAgentDTO dto : importAgentDTOS) {
            Set<ConstraintViolation<ImportAgentDTO>> validationErrors =
                    this.validator.validate(dto);

            if (validationErrors.isEmpty()) {
                Optional<Agent> optionalAgent =
                        this.agentRepository.findByFirstName(dto.getFirstName());

                if (optionalAgent.isPresent()) {
                    result.add("Invalid agent");
                } else {
                    Agent agent = this.modelMapper.map(dto, Agent.class);
                    Optional<Town> town = this.townRepository.findByTownName(dto.getTown());
                    agent.setTown(town.get());
                    this.agentRepository.save(agent);

                    result.add(String.format("Successfully imported agent - %s %s", agent.getFirstName(), agent.getLastName()));
                }
            } else {
                result.add("Invalid agent");
            }
        }
        return String.join("\n", result);
    }
}
