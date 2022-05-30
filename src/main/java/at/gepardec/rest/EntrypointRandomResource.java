package at.gepardec.rest;

import at.gepardec.service.RandomCallService;
import at.gepardec.service.ServiceCollector;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Random;
import java.util.UUID;

@Path("/call")
@ApplicationScoped
public class EntrypointRandomResource {

    @Inject
    Logger Log;

    @ConfigProperty(name = "microservices.idletime")
    int idletime;

    @ConfigProperty(name = "microservices.seed")
    Long seed;

    Random random;

    @Inject
    ServiceCollector serviceCollector;

    @PostConstruct
    void initRandom() {
        random = new Random(seed);
    }

    @GET
    @Path("/service")
    @Counted(name = "performedCalls", description = "How often the service has been called.")
    @Timed(name = "callsTimer", description = "A measure of how long it takes to perform the complete call.", unit = MetricUnits.MILLISECONDS)
    @Produces(MediaType.TEXT_PLAIN)
    public void callNextService(@QueryParam("ttl") int ttl,
                                @QueryParam("transactionID") UUID transactionID)
            throws InterruptedException {

        Thread.sleep(idletime);
        Log.info("Service 1 requesting call of next Service");
        callRandomService(ttl, transactionID);
    }

    public void callRandomService(int ttl, UUID transactionID) {
        if (ttl > 0) {
            Log.info("TransactionID: " + transactionID.toString() + " - Calling Random service");
            Log.info("ttl: " + (ttl - 1));
            RandomCallService randomCallService = new RandomCallService(serviceCollector.getServiceURLs(), random);
            randomCallService.callRandomService(--ttl, transactionID);
        } else {
            Log.info("[" + transactionID.toString() + "]" + " Stopping RandomCallService...");
        }

    }
}