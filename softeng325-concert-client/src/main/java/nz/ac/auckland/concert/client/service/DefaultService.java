package nz.ac.auckland.concert.client.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.domain.jpa.NewsItem;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.print.Book;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

public class DefaultService implements ConcertService {

    private final static String WEB_SERVICE_URI = "http://localhost:10000/services/resource";

    private Cookie authenticationToken;

    // AWS S3 access credentials for concert images.
    private static final String AWS_ACCESS_KEY_ID = "AKIAJOG7SJ36SFVZNJMQ";
    private static final String AWS_SECRET_ACCESS_KEY = "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";

    // Name of the S3 bucket that stores images.
    private static final String AWS_BUCKET = "concert2.aucklanduni.ac.nz";

    // Download directory - a directory named "images" in the user's home
    // directory.
    private static final String FILE_SEPARATOR = System
            .getProperty("file.separator");
    private static final String ROOT_DIRECTORY = System
            .getProperty("user.dir");
    private static final String DOWNLOAD_DIRECTORY = ROOT_DIRECTORY
            + FILE_SEPARATOR + "images";

    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {
        Response response = null;
        Client client = ClientBuilder.newClient();
        try {
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/concerts")
                    .request().accept(MediaType.APPLICATION_XML);
            response = builder.get();
            Set<ConcertDTO> concerts;
            if (response.getStatus() == Response.Status.OK.getStatusCode()){
                concerts = response.readEntity(new GenericType<Set<ConcertDTO>>(){});
            } else {
                concerts = new HashSet<>();
            }
            return concerts;
        } catch (ProcessingException e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    @Override
    public Set<PerformerDTO> getPerformers() throws ServiceException {
        Response response = null;
        Client client = ClientBuilder.newClient();
        try {
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/performers")
                    .request().accept(MediaType.APPLICATION_XML);
            response = builder.get();
            Set<PerformerDTO> performers;
            if (response.getStatus() == Response.Status.OK.getStatusCode()){
                performers = response.readEntity(new GenericType<Set<PerformerDTO>>(){});
            } else {
                performers = new HashSet<>();
            }
            return performers;
        } catch (ProcessingException e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    @Override
    public UserDTO createUser(UserDTO newUser) throws ServiceException {
        Response response = null;
        Client client = ClientBuilder.newClient();
        try {
            Invocation.Builder builder = client.target(WEB_SERVICE_URI+"/user")
                    .request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
            response = builder.post(Entity.entity(newUser, MediaType.APPLICATION_XML));
            int responseStatus = response.getStatus();

            if (responseStatus == Response.Status.CREATED.getStatusCode()){
                authenticationToken = (Cookie) response.getCookies().values().toArray()[0];
            }

            if (response.getStatusInfo().getFamily() != SUCCESSFUL) {
                String message = response.readEntity(String.class);
                throw new ServiceException(message);
            }

            return response.readEntity(UserDTO.class);
        } catch (ProcessingException e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    @Override
    public UserDTO authenticateUser(UserDTO user) throws ServiceException {
        Response response = null;
        Client client = ClientBuilder.newClient();
        try {
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/login")
                    .request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
            response = builder.post(Entity.entity(user, MediaType.APPLICATION_XML));
            int responseStatus = response.getStatus();
            UserDTO returnUser = null;

            if (responseStatus == Response.Status.OK.getStatusCode()){
                returnUser = response.readEntity(UserDTO.class);
                authenticationToken = (Cookie) response.getCookies().values().toArray()[0];
            }

            if (response.getStatusInfo().getFamily() != SUCCESSFUL){
                String message = response.readEntity(String.class);
                throw new ServiceException(message);
            }
            return returnUser;
        } catch (ProcessingException e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    @Override
    public Image getImageForPerformer(PerformerDTO performer) throws ServiceException {
        try {
            String imageName = performer.getImageName();

            if (imageName == null || imageName.isEmpty()){
                throw new ServiceException(Messages.NO_IMAGE_FOR_PERFORMER);
            }
            // Create download directory if it doesn't already exist.
            File downloadDirectory = new File(DOWNLOAD_DIRECTORY);
            downloadDirectory.mkdir();
            File imageFile = new File(downloadDirectory, imageName);
            if (imageFile.exists()){
                return ImageIO.read(imageFile);
            }
            // Create an AmazonS3 object that represents a connection with the
            // remote S3 service.
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                    AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
            AmazonS3 s3 = AmazonS3ClientBuilder
                    .standard()
                    .withRegion(Regions.AP_SOUTHEAST_2)
                    .withCredentials(
                            new AWSStaticCredentialsProvider(awsCredentials))
                    .build();
            GetObjectRequest req = new GetObjectRequest(AWS_BUCKET, performer.getImageName());
            s3.getObject(req, imageFile);
            return ImageIO.read(imageFile);
        } catch (Exception e){
            e.printStackTrace();
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }
    }

    @Override
    public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequest) throws ServiceException {
        Response response = null;
        Client client = ClientBuilder.newClient();
        if (authenticationToken == null){
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }
        try {
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/reservations")
                    .request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
            response = builder.cookie("authenticationToken",authenticationToken.getValue())
                    .post(Entity.entity(reservationRequest, MediaType.APPLICATION_XML));
            if (response.getStatus() == Response.Status.OK.getStatusCode()){
                return response.readEntity(ReservationDTO.class);
            }
            if (response.getStatusInfo().getFamily() != SUCCESSFUL){
                String message = response.readEntity(String.class);
                throw new ServiceException(message);
            }
        } catch (ProcessingException e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
        return null;
    }

    @Override
    public void confirmReservation(ReservationDTO reservation) throws ServiceException {
        if (authenticationToken == null){
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }
        Client client = ClientBuilder.newClient();
        try {
            Invocation.Builder builder = client.target(WEB_SERVICE_URI+"/confirm")
                    .request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
            Response response = builder.cookie("authenticationToken", authenticationToken.getValue())
                    .post(Entity.entity(reservation, MediaType.APPLICATION_XML));
            if (response.getStatusInfo().getFamily() != SUCCESSFUL){
                String message = response.readEntity(String.class);
                throw new ServiceException(message);
            }
        } catch (ProcessingException e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    @Override
    public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
        if (authenticationToken == null){
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }
        Client client = ClientBuilder.newClient();
        try {
            Invocation.Builder builder = client.target(WEB_SERVICE_URI+"/creditcard")
                    .request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
            Response response = builder.cookie("authenticationToken", authenticationToken.getValue())
                    .post(Entity.entity(creditCard, MediaType.APPLICATION_XML));
            if (response.getStatusInfo().getFamily() != SUCCESSFUL){
                String message = response.readEntity(String.class);
                throw new ServiceException(message);
            }
        } catch (ProcessingException e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    @Override
    public Set<BookingDTO> getBookings() throws ServiceException {
        Response response = null;
        if (authenticationToken == null){
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }
        Set<BookingDTO> set = null;
        Client client = ClientBuilder.newClient();
        try {
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/bookings")
                    .request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
            response = builder.cookie("authenticationToken", authenticationToken.getValue()).get();
            if (response.getStatus() == Response.Status.OK.getStatusCode()){
                set = response.readEntity(new GenericType<Set<BookingDTO>>(){});
            }
            if (response.getStatusInfo().getFamily() != SUCCESSFUL) {
                String message = response.readEntity(String.class);
                throw new ServiceException(message);
            }
            return set;
        } catch (ProcessingException e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    public void subscribe(Consumer<NewsItemDTO> onNewsItem){
        Client client = ClientBuilder.newClient();
        try {
            final WebTarget target = client.target(WEB_SERVICE_URI + "/news");
            target.request().async().get(new InvocationCallback<NewsItemDTO>() {
                @Override
                public void completed(NewsItemDTO newsItem) {
                    onNewsItem.accept(newsItem);
                }

                @Override
                public void failed(Throwable throwable) {
                    throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
                }
            });
        } catch (Exception e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }
    }

    public void submitNews(NewsItemDTO newsItemDTO){
        Client client = ClientBuilder.newClient();
        try {
            Invocation.Builder builder = client.target(WEB_SERVICE_URI+"/news")
                    .request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
            Response response = builder.post(Entity.entity(newsItemDTO, MediaType.APPLICATION_XML));
        } catch (Exception e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }
    }
}
