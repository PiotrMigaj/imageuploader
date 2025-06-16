package pl.niebieskieaparaty.imageuploader.gallery.application.route;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.ddb.Ddb2Operations;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import pl.niebieskieaparaty.imageuploader.gallery.application.processor.DynamoGalleryItemProcessor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import static org.apache.camel.component.aws2.ddb.Ddb2Constants.OPERATION;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
class GalleryRoute extends RouteBuilder {

    private final DynamoGalleryItemProcessor dynamoGalleryItemProcessor;

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

        getContext().getRegistry().bind("galleryCustomDynamoClient", client);

        from(GalleryRouteApi.DIRECT_SAVE_UPLOADED_DATA_IN_DYNAMODB).routeId("saveUploadedDataInDynamoDb")
                .process(dynamoGalleryItemProcessor)
                .log("Uploading data to dynamoDb: ${body}")
                .setHeader(OPERATION, constant(Ddb2Operations.PutItem))
                .to("aws2-ddb://GalleriesCamel?amazonDDBClient=#galleryCustomDynamoClient");
    }
}
