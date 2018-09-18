package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.types.Genre;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="PERFORMERS")
@Access(AccessType.FIELD)
public class Performer {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String imageName;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    @ManyToMany
    private Set<Concert> concerts;

    public Performer(){}

    public Performer(String name, String imageName, Genre genre, Set<Concert> concerts){
        this.name = name;
        this.imageName = imageName;
        this.genre = genre;
        this.concerts = concerts;
    }

    public PerformerDTO convertToDTO(){
        Set<Long> concertIds = new HashSet<>();
        for (Concert c : concerts){
            concertIds.add(c.getId());
        }
        return new PerformerDTO(this.id, this.name, this.imageName, this.genre, concertIds);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public Set<Concert> getConcerts() {
        return concerts;
    }

    public void setConcerts(Set<Concert> concerts) {
        this.concerts = concerts;
    }
}
