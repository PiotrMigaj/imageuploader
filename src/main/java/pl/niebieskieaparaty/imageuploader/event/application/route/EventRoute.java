package pl.niebieskieaparaty.imageuploader.event.application.route;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import pl.niebieskieaparaty.imageuploader.event.application.processor.GetAllEventsProcessor;
import pl.niebieskieaparaty.imageuploader.event.application.processor.UpdateEventWithCamelGalleryProcessor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@ApplicationScoped
@RequiredArgsConstructor
class EventRoute extends RouteBuilder {

    private final GetAllEventsProcessor getAllEventsProcessor;

    private final UpdateEventWithCamelGalleryProcessor updateEventWithCamelGalleryProcessor;

    private final DynamoDbClient dynamoDbClient;

    @Override
    public void configure() throws Exception {

        getContext().getRegistry().bind("eventCustomDynamoClient", dynamoDbClient);

        from(EventRouteApi.DIRECT_GET_EVENTS)
                .routeId("getEventsRoute")
                .to("aws2-ddb://Events?operation=Scan&consistentRead=true&amazonDDBClient=#eventCustomDynamoClient")
                .process(getAllEventsProcessor);

        from(EventRouteApi.DIRECT_UPDATE_EVENT_WITH_CAMEL_GALLERY)
                .routeId("updateEventWithCamelGallery")
                .process(updateEventWithCamelGalleryProcessor)
                .to("aws2-ddb://Events?amazonDDBClient=#eventCustomDynamoClient")
                .log("Updated camelGallery=true for eventId=${header.eventId}");
    }
}