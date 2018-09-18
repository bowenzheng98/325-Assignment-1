package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name="SEATS")
@IdClass(CompositeId.class)
@Access(AccessType.FIELD)
public class Seat{

    @Version
    private int version;

    @Id
    private SeatRow seatRow;

    @Id
    private SeatNumber seatNumber;

    @ManyToOne
    @Id
    private Concert concert;

    @Enumerated
    private PriceBand priceBand;

    @Id
    private LocalDateTime concertDate;

    private LocalDateTime reservationTimeStamp;

    @ManyToOne
    private Reservation reservation;

    public Seat(){}

    public Seat(SeatRow seatRow, SeatNumber seatNumber, Concert concert, PriceBand priceband, LocalDateTime concertDate){
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.concert = concert;
        this.priceBand = priceband;
        this.concertDate = concertDate;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public PriceBand getPriceBand() {
        return priceBand;
    }

    public void setPriceBand(PriceBand priceBand) {
        this.priceBand = priceBand;
    }

    public SeatDTO convertToDTO(){
        return new SeatDTO(seatRow, seatNumber);
    }

    public SeatRow getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(SeatRow seatRow) {
        this.seatRow = seatRow;
    }

    public SeatNumber getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(SeatNumber seatNumber) {
        this.seatNumber = seatNumber;
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

    public LocalDateTime getReservationTimeStamp() {
        return reservationTimeStamp;
    }

    public void setReservationTimeStamp(LocalDateTime reservationTimeStamp) {
        this.reservationTimeStamp = reservationTimeStamp;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

}

class CompositeId implements Serializable{

    private SeatNumber seatNumber;
    private SeatRow seatRow;
    private Concert concert;
    private LocalDateTime concertDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositeId that = (CompositeId) o;

        if (seatNumber != null ? !seatNumber.equals(that.seatNumber) : that.seatNumber != null) return false;
        if (seatRow != that.seatRow) return false;
        if (concert != null ? !concert.equals(that.concert) : that.concert != null) return false;
        return concertDate != null ? concertDate.equals(that.concertDate) : that.concertDate == null;
    }

    @Override
    public int hashCode() {
        int result = seatNumber != null ? seatNumber.hashCode() : 0;
        result = 31 * result + (seatRow != null ? seatRow.hashCode() : 0);
        result = 31 * result + (concert != null ? concert.hashCode() : 0);
        result = 31 * result + (concertDate != null ? concertDate.hashCode() : 0);
        return result;
    }
}
