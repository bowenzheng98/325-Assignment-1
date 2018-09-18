package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="CREDITCARDS")
@Access(AccessType.FIELD)
public class CreditCard {

    @Enumerated
    private CreditCardDTO.Type type;

    private String name;

    @Id
    private String number;

    private LocalDate expiryDate;

    public CreditCard(){}

    public CreditCard(CreditCardDTO.Type type, String name, String number, LocalDate expiryDate){
        this.type = type;
        this.name = name;
        this.number = number;
        this.expiryDate = expiryDate;
    }

    public CreditCardDTO.Type getType() {
        return type;
    }

    public void setType(CreditCardDTO.Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
}
