package softuni.exam.models.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class ImportOfferDTO {

    @XmlElement
    @Positive
    private double price;

    @XmlElement
    @NotNull
    private AgentDTO agent;

    @XmlElement
    @NotNull
    private ApartmentDTO apartment;

    @NotNull
    private String publishedOn;

    public double getPrice() {
        return price;
    }

    public AgentDTO getAgent() {
        return agent;
    }

    public ApartmentDTO getApartment() {
        return apartment;
    }

    public String getPublishedOn() {
        return publishedOn;
    }
}
