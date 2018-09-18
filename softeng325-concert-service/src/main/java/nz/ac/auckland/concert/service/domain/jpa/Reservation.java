package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="RESERVATIONS")
@Access(AccessType.FIELD)
public class Reservation {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    private User user;

    @OneToMany
    @JoinColumn
    private Set<Seat> seats = new HashSet<>();

    @Enumerated
    private PriceBand priceBand;

    @ManyToOne
    private Concert concert;

    private LocalDateTime concertDate;

    private boolean booked = false;

    private LocalDateTime reservationTimeStamp;

    public Reservation(){}

    public Reservation(User user, PriceBand priceband, Concert concert, LocalDateTime concertDate,
                       LocalDateTime reservationTimeStamp){
        this.user = user;
        this.priceBand = priceband;
        this.concert = concert;
        this.concertDate = concertDate;
        this.reservationTimeStamp = reservationTimeStamp;
    }

    public BookingDTO convertToBookingDTO(){
        Set<SeatDTO> seatDTOs = new HashSet<>();
        for (Seat seat : this.seats){
            seatDTOs.add(seat.convertToDTO());
        }
        return new BookingDTO(this.concert.getId(), this.concert.getTitle(),
                this.concertDate, seatDTOs, this.priceBand);
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Seat> getSeats() {
        return seats;
    }

    public void setSeats(Set<Seat> seats) {
        this.seats = seats;
    }

    public PriceBand getPriceBand() {
        return priceBand;
    }

    public void setPriceBand(PriceBand priceBand) {
        this.priceBand = priceBand;
    }

    public Concert getConcert() {
        return concert;
    }

    public void setConcert(Concert concert) {
        this.concert = concert;
    }

    public LocalDateTime getConcertDate() {
        return concertDate;
    }

    public void setConcertDate(LocalDateTime concertDate) {
        this.concertDate = concertDate;
    }

    public boolean getBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    public LocalDateTime getReservationTimeStamp() {
        return reservationTimeStamp;
    }

    public void setReservationTimeStamp(LocalDateTime reservationTimeStamp) {
        this.reservationTimeStamp = reservationTimeStamp;
    }
}
