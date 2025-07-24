package pl.niebieskieaparaty.imageuploader.selection.application.processor;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.aws2.ddb.Ddb2Constants;
import pl.niebieskieaparaty.imageuploader.selection.core.SelectedImages;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Slf4j
public class GetSelectedImageNamesProcessor implements Processor {

    private static final String CAMEL_AWS_DDB_ITEM = "CamelAwsDdbItem";
    private static final String SELECTED_IMAGES = "selectedImages";
    private static final String SELECTION_ID = "selectionId";

    @Override
    public void process(final Exchange exchange) throws Exception {
        log.info("=== GetSelectedImageNamesProcessor Debug ===");
        log.info("All headers: {}", exchange.getIn().getHeaders());
        
        // For Scan operation, we need to get the ITEMS (list) instead of ITEM (single)
        var items = exchange.getIn().getHeader("CamelAwsDdbItems");
        log.info("CamelAwsDdbItems header: {}", items);
        
        if (items == null) {
            items = exchange.getIn().getHeader(Ddb2Constants.ITEMS);
            log.info("Ddb2Constants.ITEMS header: {}", items);
        }

        if (items != null) {
            log.info("Found items: {}", items);
            final var ddbItems = (List<Map<String, AttributeValue>>) items;
            
            if (ddbItems.isEmpty()) {
                log.warn("Scan returned empty list - no matching records found");
                exchange.getMessage().setBody(new SelectedImages(List.of()));
                return;
            }
            
            // Take the first (and should be only) item since we're filtering by PK
            final var ddbItem = ddbItems.get(0);
            log.info("Processing first item: {}", ddbItem);
            
            // Get selectionId
            final String selectionId = ddbItem.getOrDefault(SELECTION_ID, AttributeValue.builder().s("").build()).s();
            
            // Parse selectedImages list
            final List<String> imageNames = new ArrayList<>();
            final AttributeValue selectedImagesAttr = ddbItem.get(SELECTED_IMAGES);
            
            if (selectedImagesAttr != null && selectedImagesAttr.l() != null) {
                for (AttributeValue imageValue : selectedImagesAttr.l()) {
                    if (imageValue.s() != null) {
                        imageNames.add(imageValue.s());
                    }
                }
            }
            
            log.info("Parsed {} selected images for selectionId: {}", imageNames.size(), selectionId);
            
            final var selectedImages = new SelectedImages(imageNames);
            exchange.getMessage().setBody(selectedImages);
        } else {
            log.error("No items found in DynamoDB Scan response!");
            log.error("This means the Scan operation returned null - record may not exist or filter is wrong");
            exchange.getMessage().setBody(new SelectedImages(List.of()));
        }
    }
}