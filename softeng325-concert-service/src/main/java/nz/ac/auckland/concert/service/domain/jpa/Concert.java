package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name="CONCERTS")
@Access(AccessType.FIELD)
public class Concert {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "CONCERT_DATES")
    private Set<LocalDateTime> dates;

    @ElementCollection
    @CollectionTable(name = "CONCERT_TARIFS")
    @MapKeyEnumerated(EnumType.STRING)
    private Map<PriceBand, BigDecimal> tariff;

    @ManyToMany
    @JoinTable(name="CONCERT_PERFORMER")
    private Set<Performer> performers;

    public Concert(){}

    public Concert(String title, Set<LocalDateTime> dates, Map<PriceBand,
            BigDecimal> tariff, Set<Performer> performers){
        this.title = title;
        this.dates = dates;
        this.tariff = tariff;
        this.performers = performers;
    }

    public ConcertDTO convertToDTO(){
        Set<Long> performerIDs = new HashSet<>();
        for (Performer p : performers){
            performerIDs.add(p.getId());
        }
        return new ConcertDTO(this.id, this.title, this.dates, this.tariff, performerIDs);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<LocalDateTime> getDates() {
        return dates;
    }

    public void setDates(Set<LocalDateTime> dates) {
        this.dates = dates;
    }

    public Map<PriceBand, BigDecimal> getTariff() {
        return tariff;
    }

    public void setTariff(Map<PriceBand, BigDecimal> tariff) {
        this.tariff = tariff;
    }

    public Set<Performer> getPerformerIds() {
        return this.performers;
    }

    public void setPerformerIds(Set<Performer> performerIds) {
        this.performers = performerIds;
    }
}
