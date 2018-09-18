package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.dto.UserDTO;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="USERS")
public class User {

    @Id
    private String username;

    private String password;

    private String firstname;

    private String lastname;

    private String token;

    @OneToOne
    private CreditCard creditCard;

    private LocalDateTime newsTimeStamp;

    public User(){}

    public User(String username, String password, String firstname, String lastname){
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public LocalDateTime getNewsTimeStamp() {
        return newsTimeStamp;
    }

    public void setNewsTimeStamp(LocalDateTime newsTimeStamp) {
        this.newsTimeStamp = newsTimeStamp;
    }

    public UserDTO convertToDTO(){
        return new UserDTO(this.username, this.password, this.lastname, this.firstname);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }
}
