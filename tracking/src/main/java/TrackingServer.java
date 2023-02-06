import org.jboss.resteasy.reactive.ResponseStatus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/tracking")
public class TrackingServer {

    @GET
    @Path("/o")
    @ResponseStatus(200)
    @Produces(MediaType.TEXT_PLAIN)
    public String open() {
        System.out.println("o");
        return "Hello from RESTEasy Reactive";
    }
    @GET
    @Path("/c")
    @ResponseStatus(200)
    @Produces(MediaType.TEXT_PLAIN)
    public String click() {
        System.out.println("c");
        return "Hello from RESTEasy Reactive";
    }

}