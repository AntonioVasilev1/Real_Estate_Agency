package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.ImportApartmentDTO;
import softuni.exam.models.dto.ImportApartmentRootDTO;
import softuni.exam.models.entity.Apartment;
import softuni.exam.models.entity.Town;
import softuni.exam.repository.ApartmentRepository;
import softuni.exam.repository.TownRepository;
import softuni.exam.service.ApartmentService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ApartmentServiceImpl implements ApartmentService {
    private final Path path = Path.of("src/main/resources/files/xml/apartments.xml");
    private ApartmentRepository apartmentRepository;
    private TownRepository townRepository;
    private final Validator validator;
    private final Unmarshaller unmarshaller;
    private ModelMapper modelMapper;

    public ApartmentServiceImpl(ApartmentRepository apartmentRepository, TownRepository townRepository) throws JAXBException {
        this.apartmentRepository = apartmentRepository;
        this.townRepository = townRepository;

        JAXBContext jaxbContext = JAXBContext.newInstance(ImportApartmentRootDTO.class);
        this.unmarshaller = jaxbContext.createUnmarshaller();

        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.modelMapper = new ModelMapper();
    }

    @Override
    public boolean areImported() {
        return this.apartmentRepository.count() > 0;
    }

    @Override
    public String readApartmentsFromFile() throws IOException {
        return Files.readString(this.path);
    }

    @Override
    public String importApartments() throws IOException, JAXBException {
        ImportApartmentRootDTO apartmentRootDTO = (ImportApartmentRootDTO) this.unmarshaller.unmarshal(
                new FileReader(this.path.toAbsolutePath().toString()));

        return apartmentRootDTO
                .getApartments()
                .stream()
                .map(this::importApartment)
                .collect(Collectors.joining("\n"));
    }

    private String importApartment(ImportApartmentDTO dto) {
        Set<ConstraintViolation<ImportApartmentDTO>> errors = this.validator.validate(dto);

        if (!errors.isEmpty()) {
            return "Invalid apartment";
        }

        Optional<Apartment> optional =
                this.apartmentRepository.findByTownTownNameAndArea(dto.getTown(), dto.getArea());

        if (optional.isPresent()) {
            return "Invalid apartment";
        }

        Apartment apartment = this.modelMapper.map(dto, Apartment.class);
        Optional<Town> town = this.townRepository.findByTownName(dto.getTown());

        apartment.setTown(town.get());
        this.apartmentRepository.save(apartment);

        return String.format("Successfully imported apartment %s - %.2f", apartment.getApartmentType(), apartment.getArea());
    }
}
