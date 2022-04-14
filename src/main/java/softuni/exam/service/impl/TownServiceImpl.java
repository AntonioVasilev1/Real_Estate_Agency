package softuni.exam.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.ImportTownDTO;
import softuni.exam.models.entity.Town;
import softuni.exam.repository.TownRepository;
import softuni.exam.service.TownService;

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
public class TownServiceImpl implements TownService {
    private final TownRepository townRepository;
    private final Gson gson;
    private final Validator validator;
    private final ModelMapper modelMapper;

    public TownServiceImpl(TownRepository townRepository) {
        this.townRepository = townRepository;
        this.gson = new GsonBuilder().create();
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.modelMapper = new ModelMapper();
    }

    @Override
    public boolean areImported() {
        return this.townRepository.count() > 0;
    }

    @Override
    public String readTownsFileContent() throws IOException {
        Path path = Path.of("src/main/resources/files/json/towns.json");
        return Files.readString(path);
    }

    @Override
    public String importTowns() throws IOException {
        String json = this.readTownsFileContent();

        ImportTownDTO[] importTownDTOS = this.gson.fromJson(json, ImportTownDTO[].class);

        List<String> result = new ArrayList<>();

        for (ImportTownDTO importTownDTO : importTownDTOS) {
            Set<ConstraintViolation<ImportTownDTO>> validationErrors =
                    this.validator.validate(importTownDTO);

            if (validationErrors.isEmpty()) {
                Optional<Town> optTown =
                        this.townRepository.findByTownName(importTownDTO.getTownName());

                if (optTown.isPresent()) {
                    result.add("Invalid town");
                } else {
                    Town town = this.modelMapper.map(importTownDTO, Town.class);
                    this.townRepository.save(town);
                    result.add(String.format("Successfully imported town %s - %d", town.getTownName(), town.getPopulation()));
                }
            } else {
                result.add("Invalid town");
            }
        }
        return String.join("\n", result);
    }
}
