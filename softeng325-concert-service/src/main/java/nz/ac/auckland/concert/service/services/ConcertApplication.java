package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.service.domain.jpa.Concert;
import nz.ac.auckland.concert.service.domain.jpa.Seat;
import nz.ac.auckland.concert.service.util.TheatreUtility;
import nz.ac.auckland.concert.utility.TheatreLayout;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Application;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationPath("/services")
public class ConcertApplication extends Application {

    public static final int RESERVATION_EXPIRY_TIME = 5;

    private Set<Object> singletons = new HashSet<>();
    private Set<Class<?>> classes = new HashSet<>();

    public ConcertApplication() {
        singletons.add(new PersistenceManager());
        classes.add(ConcertResource.class);
        classes.add(NewsResource.class);
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            TypedQuery<Concert> concertQuery = em.createQuery("SELECT c FROM Concert c", Concert.class);
            List<Concert> concerts = concertQuery.getResultList();
            for (Concert concert : concerts){
                for (LocalDateTime dateTime : concert.getDates()){
                    for (SeatRow row : TheatreLayout.getRowsForPriceBand(PriceBand.PriceBandA)){
                        for (int i = 1; i < TheatreLayout.getNumberOfSeatsForRow(row); i++){
                            Seat seat = new Seat(row, new SeatNumber(i), concert, PriceBand.PriceBandA, dateTime);
                            em.persist(seat);
                        }
                    }
                    for (SeatRow row : TheatreLayout.getRowsForPriceBand(PriceBand.PriceBandB)){
                        for (int i = 1; i < TheatreLayout.getNumberOfSeatsForRow(row); i++){
                            Seat seat = new Seat(row, new SeatNumber(i), concert, PriceBand.PriceBandB, dateTime);
                            em.persist(seat);
                        }
                    }
                    for (SeatRow row : TheatreLayout.getRowsForPriceBand(PriceBand.PriceBandC)){
                        for (int i = 1; i < TheatreLayout.getNumberOfSeatsForRow(row); i++){
                            Seat seat = new Seat(row, new SeatNumber(i), concert, PriceBand.PriceBandC, dateTime);
                            em.persist(seat);
                        }
                    }
                }
                em.flush();
            }
            tx.commit();
        } catch (ProcessingException e){
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()){
                em.close();
            }
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
