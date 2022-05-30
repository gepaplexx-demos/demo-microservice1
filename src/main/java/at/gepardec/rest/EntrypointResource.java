package at.gepardec.rest;

import at.gepardec.service.OrderedCallService;
import at.gepardec.service.ServiceCollector;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/call")
@ApplicationScoped
public class EntrypointResource {

    @Inject
    Logger Log;

    @ConfigProperty(name = "microservices.idletime")
    int idletime;

    @ConfigProperty(name = "microservices.sequence")
    String orderSequence;

    @Inject
    ServiceCollector serviceCollector;

    @GET
    @Path("/serviceBySequence")
    @Counted(name = "performedCalls", description = "How often the service has been called.")
    @Timed(name = "callsTimer", description = "A measure of how long it takes to perform the complete call.", unit = MetricUnits.MILLISECONDS)
    @Produces(MediaType.TEXT_PLAIN)
    public void callNextService(@QueryParam("orderSequence") String orderSequence,
                                @QueryParam("transactionID") UUID transactionID) {
        this.orderSequence = orderSequence;
        Log.info("Service 1 requesting call of next Service");
        if(orderSequence.length() == 0) {
            return;
        }
        char currentchar = parseOrderSequence();
        chooseActionByChar(currentchar, transactionID);

    }

    private void chooseActionByChar(char currentchar, UUID transactionID) {
        callService(orderSequence, transactionID);
    }

    private char parseOrderSequence() {

        char currentChar = orderSequence.charAt(0);
        orderSequence = orderSequence.substring(1);
        return currentChar;

    }

    public void callService(String orderSequence, UUID transactionID) {
        if (orderSequence.length() > 0) {
            Log.info("TransactionID: " + transactionID.toString() + " - Calling Random service");
            OrderedCallService orderedCallService = new OrderedCallService(serviceCollector.getServiceURLs());
            orderedCallService.callRandomServiceBySequence(orderSequence, transactionID);
        } else {
            Log.info("[" + transactionID.toString() + "]" + " Stopping RandomCallService...");
        }

    }
}
