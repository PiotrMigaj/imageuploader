package pl.niebieskieaparaty.imageuploader.selection.application.route;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.ddb.Ddb2Constants;
import org.apache.camel.component.aws2.ddb.Ddb2Operations;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import pl.niebieskieaparaty.imageuploader.file.application.route.FileRouteApi;
import pl.niebieskieaparaty.imageuploader.file.core.FileSelectionRequest;
import pl.niebieskieaparaty.imageuploader.selection.application.processor.GetAllBlockedSelectionsProcessor;
import pl.niebieskieaparaty.imageuploader.selection.application.processor.GetSelectedImageNamesProcessor;
import pl.niebieskieaparaty.imageuploader.selection.core.SelectedImages;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ApplicationScoped
@RequiredArgsConstructor
public class SelectionRoute extends RouteBuilder {

    private final GetAllBlockedSelectionsProcessor getAllBlockedSelectionsProcessor;
    private final GetSelectedImageNamesProcessor getSelectedImageNamesProcessor;

    @ConfigProperty(name = "aws.access-key")
    String accessKey;

    @ConfigProperty(name = "aws.secret-key")
    String secretKey;

    @ConfigProperty(name = "aws.region", defaultValue = "eu-central-1")
    String region;

    @SuppressWarnings("checkstyle:AvoidInlineConditionals")
    @Override
    public void configure() throws Exception {

        final var client = DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        getContext().getRegistry().bind("selectionCustomDynamoClient", client);

        from(SelectionRouteApi.DIRECT_GET_ALL_BLOCKED_SELECTIONS)
                .routeId("getAllBlockedSelectionsRoute")
                .process(exchange -> {
                    final Map<String, software.amazon.awssdk.services.dynamodb.model.Condition> conditionMap = new HashMap<>();
                    conditionMap.put("blocked", software.amazon.awssdk.services.dynamodb.model.Condition.builder()
                            .comparisonOperator(ComparisonOperator.EQ)
                            .attributeValueList(AttributeValue.builder().bool(true).build())
                            .build());
                    exchange.getIn().setHeader(Ddb2Constants.OPERATION, Ddb2Operations.Scan);
                    exchange.getIn().setHeader(Ddb2Constants.SCAN_FILTER, conditionMap);
                    exchange.getIn().setHeader(Ddb2Constants.CONSISTENT_READ, true);
                })
                .toF("aws2-ddb://%s?amazonDDBClient=#selectionCustomDynamoClient", "Selection")
                .process(getAllBlockedSelectionsProcessor);

        from(SelectionRouteApi.DIRECT_PROCESS_SELECTION)
                .routeId("processSelection")
                .log("Processing selection with: ${body}")
                .setHeader("selectionId", simple("${body.selectionId}"))
                .setHeader("directoryPath", simple("${body.directoryPath}"))
                .to(SelectionRouteApi.DIRECT_GET_SELECTED_IMAGE_NAMES)
                .setBody(exchange -> {
                    final var baseDirectoryPath = exchange.getIn().getHeader("directoryPath", String.class).trim();
                    final var selectedImages = exchange.getIn().getBody(SelectedImages.class);
                    return FileSelectionRequest.of(baseDirectoryPath, selectedImages.selectedImages());
                })
                .to(FileRouteApi.DIRECT_PROCESS_SELECTED_IMAGES_IN_FOLDER)
                .process(exchange -> {
                    @SuppressWarnings("unchecked")
                    final var movedFiles = (java.util.List<String>) exchange.getIn().getBody();
                    final var filesList = movedFiles != null ? movedFiles : List.<String>of();
                    final var filesCount = filesList.size();
                    
                    final var response = Map.of(
                        "message", "Selection processed successfully",
                        "movedFiles", filesList,
                        "count", filesCount
                    );
                    exchange.getIn().setBody(response);
                });

        from(SelectionRouteApi.DIRECT_GET_SELECTED_IMAGE_NAMES)
                .routeId("getSelectedImageNamesRoute")
                .process(exchange -> {
                    final String selectionId = exchange.getIn().getHeader("selectionId", String.class);
                    final Map<String, software.amazon.awssdk.services.dynamodb.model.Condition> conditionMap = new HashMap<>();
                    conditionMap.put("selectionId", software.amazon.awssdk.services.dynamodb.model.Condition.builder()
                            .comparisonOperator(ComparisonOperator.EQ)
                            .attributeValueList(AttributeValue.builder().s(selectionId).build())
                            .build());

                    exchange.getIn().setHeader(Ddb2Constants.OPERATION, Ddb2Operations.Scan);
                    exchange.getIn().setHeader(Ddb2Constants.SCAN_FILTER, conditionMap);
                    exchange.getIn().setHeader(Ddb2Constants.CONSISTENT_READ, true);
                })
                .toF("aws2-ddb://%s?amazonDDBClient=#selectionCustomDynamoClient", "Selection")
                .process(getSelectedImageNamesProcessor);
    }
}
