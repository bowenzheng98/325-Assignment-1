package nz.ac.auckland.concert.service.services;

import com.sun.org.apache.regexp.internal.RE;
import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.service.domain.jpa.*;
import nz.ac.auckland.concert.service.util.TheatreUtility;

import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.valueOf;
import static nz.ac.auckland.concert.common.message.Messages.*;
import static nz.ac.auckland.concert.service.services.ConcertApplication.RESERVATION_EXPIRY_TIME;

@Path("/resource")
@Produces(APPLICATION_XML)
@Consumes(APPLICATION_XML)
public class ConcertResource {

    @GET
    @Path("/concerts")
    public Response retrieveConcerts(){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            TypedQuery<Concert> concertQuery = em.createQuery("SELECT c FROM Concert c", Concert.class);
            List<Concert> concerts = concertQuery.getResultList();
            ResponseBuilder builder;
            if (concerts.isEmpty()) {
                builder = Response.status(404);
            } else {
                Set<ConcertDTO> concertDTOS = new HashSet<>();
                for(Concert concert : concerts){
                    ConcertDTO concertDTO = concert.convertToDTO();
                    concertDTOS.add(concertDTO);
                }
                GenericEntity<Set<ConcertDTO>> entity =  new GenericEntity<Set<ConcertDTO>>(concertDTOS){};
                builder = Response.ok().entity(entity);
            }
            tx.commit();
            return builder.build();
        } catch (Exception e){
            return Response.serverError().build();
        } finally {
            if (em != null && em.isOpen()){
                em.close();
            }
        }
    }

    @GET
    @Path("/performers")
    public Response retrievePerformers(){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            TypedQuery<Performer> performerQuery = em.createQuery("SELECT p FROM Performer p", Performer.class);
            List<Performer> performers = performerQuery.getResultList();
            ResponseBuilder builder;
            if (performers.isEmpty()){
                builder = Response.status(404);
            } else {
                List<PerformerDTO> performerDTOS = new ArrayList<>();
                for (Performer performer : performers){
                    PerformerDTO performerDTO = performer.convertToDTO();
                    performerDTOS.add(performerDTO);
                }
                GenericEntity<List<PerformerDTO>> entity = new GenericEntity<List<PerformerDTO>>(performerDTOS){};
                builder = Response.ok().entity(entity);
            }
            tx.commit();
            return builder.build();
        } catch (Exception e){
            return Response.serverError().build();
        } finally {
            if (em != null && em.isOpen()){
                em.close();
            }
        }
    }

    @POST
    @Path("/user")
    public Response createUser(UserDTO user){
        try {
            List<String> userDetails = new ArrayList<>();
            userDetails.add(user.getFirstname());
            userDetails.add(user.getLastname());
            userDetails.add(user.getPassword());
            userDetails.add(user.getUsername());
            for (String s : userDetails) {
                if (s == null) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(CREATE_USER_WITH_MISSING_FIELDS).build();
                }
            }
            EntityManager em = PersistenceManager.instance().createEntityManager();
            try {
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                if (em.find(User.class, user.getUsername()) != null){
                    return Response.status(Response.Status.BAD_REQUEST).entity(CREATE_USER_WITH_NON_UNIQUE_NAME).build();
                }
                User newUser = new User(user.getUsername(), user.getPassword(),user.getFirstname(), user.getLastname());
                UUID token = UUID.randomUUID();
                newUser.setToken(token.toString());
                em.persist(newUser);
                tx.commit();
                return Response.created(URI.create("/user/"+ newUser.getUsername()))
                        .entity(user).cookie(new NewCookie("authenticationToken", token.toString())).build();
            } catch (Exception e){
                e.printStackTrace();
                return Response.serverError().build();
            } finally {
                if (em != null && em.isOpen()){
                    em.close();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/login")
    public Response authenticateUser(UserDTO user){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            User databaseUser = em.find(User.class, user.getUsername());
            if (user.getUsername() == null){
                return Response.status(Response.Status.BAD_REQUEST).entity(AUTHENTICATE_USER_WITH_MISSING_FIELDS).build();
            } else if (user.getPassword() == null){
                return Response.status(Response.Status.BAD_REQUEST).entity(AUTHENTICATE_USER_WITH_MISSING_FIELDS).build();
            } else if (databaseUser == null){
                return Response.status(Response.Status.NOT_FOUND).entity(AUTHENTICATE_NON_EXISTENT_USER).build();
            } else if (!databaseUser.getPassword().equals(user.getPassword())){
                return Response.status(Response.Status.BAD_REQUEST).entity(AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD).build();
            }
            if (databaseUser.getToken() == null){
                UUID token = UUID.randomUUID();
                databaseUser.setToken(token.toString());
            }
            UserDTO returnUser = databaseUser.convertToDTO();
            tx.commit();
            return Response.ok().entity(returnUser)
                    .cookie(new NewCookie("authenticationToken", databaseUser.getToken())).build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.serverError().build();
        } finally {
            if (em != null && em.isOpen()){
                em.close();
            }
        }
    }

    @POST
    @Path("/reservations")
    public Response reserveSeats(ReservationRequestDTO reservationRequest, @CookieParam("authenticationToken") Cookie cookie){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            if ((Integer) reservationRequest.getNumberOfSeats() == null || reservationRequest.getConcertId() == null ||
                    reservationRequest.getDate() == null || reservationRequest.getSeatType() == null){
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(RESERVATION_REQUEST_WITH_MISSING_FIELDS).build();
            }
            if (cookie == null){
                return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHENTICATED_REQUEST).build();
            }
            TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u WHERE u.token=:token", User.class)
                    .setParameter("token", cookie.getValue());
            User user = userQuery.getSingleResult();
            if (user == null){
                return Response.status(Response.Status.BAD_REQUEST).entity(BAD_AUTHENTICATON_TOKEN).build();
            }
            TypedQuery<Concert> concertQuery = em.createQuery("SELECT c FROM Concert c WHERE c.id=:id", Concert.class)
                    .setParameter("id", reservationRequest.getConcertId());
            Concert concert = concertQuery.getSingleResult();
            if (!concert.getDates().contains(reservationRequest.getDate())){
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE).build();
            }

            TypedQuery<Seat> seatQuery = em.createQuery("SELECT s FROM Seat s WHERE s.concert.id=:concertId " +
                    "AND s.concertDate=:concertDate AND s.priceBand=:priceBand", Seat.class)
                    .setParameter("concertId", reservationRequest.getConcertId())
                    .setParameter("concertDate", reservationRequest.getDate())
                    .setParameter("priceBand", reservationRequest.getSeatType())
                    .setLockMode(LockModeType.OPTIMISTIC);
            List<Seat> seats = seatQuery.getResultList();

            if (seats.isEmpty() || seats.size() < reservationRequest.getNumberOfSeats()){
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION).build();
            }

            Set<Seat> availableSeats = TheatreUtility.getSpecificAvailableSeats(reservationRequest.getNumberOfSeats(),
                    seats);

            Set<Seat> unavailableSeats = new HashSet<>();
            for (Seat seat : availableSeats){
                Reservation reservation = seat.getReservation();
                if (reservation != null) {
                    if (reservation.getBooked() && reservation.getReservationTimeStamp()
                            .isBefore(LocalDateTime.now().minusSeconds(RESERVATION_EXPIRY_TIME))) {
                        seat.setReservation(null);
                    } else {
                        unavailableSeats.add(seat);
                    }
                }
            }
            availableSeats.removeAll(unavailableSeats);
            List<Seat> seatList = new ArrayList<>(availableSeats);
            Set<Seat> reservableSeats = TheatreUtility.findAvailableSeats(reservationRequest.getNumberOfSeats(),seatList);
            Reservation newReservation = new Reservation(user, reservationRequest.getSeatType(),
                    concert, reservationRequest.getDate(), LocalDateTime.now());
            em.persist(newReservation);
            Set<SeatDTO> seatDTOS = new HashSet<>();
            for (Seat seat : reservableSeats){
                seat.setReservation(newReservation);
                seat.setReservationTimeStamp(newReservation.getReservationTimeStamp());
                newReservation.getSeats().add(seat);
                seatDTOS.add(seat.convertToDTO());
            }
            tx.commit();
            em.close();

            ReservationDTO reservationDTO = new ReservationDTO(newReservation.getId(), reservationRequest, seatDTOS);
            return Response.ok().entity(reservationDTO).build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.serverError().build();
        } finally {
            if (em != null && em.isOpen()){
                em.close();
            }
        }
    }

    @POST
    @Path("/confirm")
    public Response confirmReservation(ReservationDTO reservation, @CookieParam("authenticationToken") Cookie cookie){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        if (cookie == null){
        return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHENTICATED_REQUEST).build();
        }
        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u WHERE u.token=:token", User.class)
                    .setParameter("token", cookie.getValue());
            User user = userQuery.getSingleResult();
            if (user == null){
                return Response.status(Response.Status.BAD_REQUEST).entity(BAD_AUTHENTICATON_TOKEN).build();
            }
            if (user.getCreditCard() == null){
                return Response.status(Response.Status.BAD_REQUEST).entity(CREDIT_CARD_NOT_REGISTERED).build();
            }
            TypedQuery<Reservation> reservationQuery = em
                    .createQuery("SELECT r FROM Reservation r WHERE r.id=:rid", Reservation.class)
                    .setParameter("rid", reservation.getId());
            Reservation r = reservationQuery.getSingleResult();
            if (r.getReservationTimeStamp().isBefore(LocalDateTime.now().minusSeconds(RESERVATION_EXPIRY_TIME))){
                return Response.status(Response.Status.BAD_REQUEST).entity(EXPIRED_RESERVATION).build();
            }
            r.setBooked(true);
            tx.commit();
            return Response.ok().build();
        } catch (Exception e){
            return Response.serverError().build();
        }
    }


    @POST
    @Path("/creditcard")
    public Response addCreditCard(CreditCardDTO creditCard, @CookieParam("authenticationToken") Cookie cookie){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        if (cookie == null){
            return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHENTICATED_REQUEST).build();
        }
        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u WHERE u.token=:token", User.class)
                    .setParameter("token", cookie.getValue());
            User user = userQuery.getSingleResult();
            if (user == null){
                return Response.status(Response.Status.BAD_REQUEST).entity(BAD_AUTHENTICATON_TOKEN).build();
            }
            CreditCard card = new CreditCard(creditCard.getType(), creditCard.getName(), creditCard.getNumber(),
                    creditCard.getExpiryDate());
            user.setCreditCard(card);
            em.persist(card);
            tx.commit();
            return Response.ok().build();
        } catch (Exception e){
            return Response.serverError().build();
        } finally {
            if (em != null && em.isOpen()){
                em.close();
            }
        }
    }

    @GET
    @Path("/bookings")
    public Response getAllBookings(@CookieParam("authenticationToken") Cookie cookie){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        if (cookie == null){
            return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHENTICATED_REQUEST).build();
        }
        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u WHERE u.token=:token", User.class)
                    .setParameter("token", cookie.getValue());
            User user = userQuery.getSingleResult();
            if (user == null){
                return Response.status(Response.Status.BAD_REQUEST).entity(BAD_AUTHENTICATON_TOKEN).build();
            }
            TypedQuery<Reservation> reservationQuery = em.createQuery("SELECT r FROM Reservation r WHERE " +
                    "r.user.token=:token", Reservation.class).setParameter("token", cookie.getValue());
            List<Reservation> reservations = reservationQuery.getResultList();
            Set<BookingDTO> bookings = new HashSet<>();
            for (Reservation reservation :  reservations){
                if (reservation.getBooked()){
                    BookingDTO a = reservation.convertToBookingDTO();
                    bookings.add(reservation.convertToBookingDTO());
                }
            }
            GenericEntity<Set<BookingDTO>> entity = new GenericEntity<Set<BookingDTO>>(bookings){};
            tx.commit();
            return Response.ok().entity(entity).build();
        } catch (Exception e){
            return Response.serverError().build();
        } finally {
            if (em != null && em.isOpen()){
                em.close();
            }
        }
    }

}
