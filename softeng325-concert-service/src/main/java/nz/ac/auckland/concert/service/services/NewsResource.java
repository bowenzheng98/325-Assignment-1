package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.service.domain.jpa.NewsItem;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/news")
public class NewsResource {

    private List<AsyncResponse> subscriptions;

    @GET
    public void subscribe(@Suspended AsyncResponse response) {
        subscriptions.add(response);
    }

    @POST
    public javax.ws.rs.core.Response sendNews(NewsItem newsItem) {
        for (AsyncResponse response : subscriptions) {
            response.resume(newsItem.convertToDTO());
        }
        subscriptions.clear();
        return Response.ok().build();
    }
}
