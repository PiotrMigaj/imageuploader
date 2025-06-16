package pl.niebieskieaparaty.imageuploader.event.application.route;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import pl.niebieskieaparaty.imageuploader.event.application.processor.GetAllEventsProcessor;
import pl.niebieskieaparaty.imageuploader.event.application.processor.UpdateEventWithCamelGalleryProcessor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@ApplicationScoped
@RequiredArgsConstructor
class EventRoute extends RouteBuilder {

    private final GetAllEventsProcessor getAllEventsProcessor;

    private final UpdateEventWithCamelGalleryProcessor updateEventWithCamelGalleryProcessor;

    @ConfigProperty(name = "aws.access-key")
    String accessKey;

    @ConfigProperty(name = "aws.secret-key")
    String secretKey;

    @ConfigProperty(name = "aws.region", defaultValue = "eu-central-1")
    String region;

    @Override
    public void configure() throws Exception {

        final var client = DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        getContext().getRegistry().bind("eventCustomDynamoClient", client);

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