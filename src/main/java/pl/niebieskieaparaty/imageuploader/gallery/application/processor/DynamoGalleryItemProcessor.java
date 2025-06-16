package pl.niebieskieaparaty.imageuploader.gallery.application.processor;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.aws2.ddb.Ddb2Constants;
import pl.niebieskieaparaty.imageuploader.upload.core.UploadedData;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static pl.niebieskieaparaty.imageuploader.upload.application.constant.UploadRouteConstant.*;

@ApplicationScoped
public class DynamoGalleryItemProcessor implements Processor {

    @Override
    public void process(final Exchange exchange) throws Exception {
        final var uploadedData = exchange.getIn().getBody(UploadedData.class);
        final var dynamoItem = toDynamoGalleryItem(uploadedData);
        exchange.getIn().setHeader(Ddb2Constants.ITEM, dynamoItem);
    }

    private Map<String, AttributeValue> toDynamoGalleryItem(final UploadedData data) {
        final var item = new HashMap<String, AttributeValue>();
        item.put(FILE_NAME, AttributeValue.fromS(data.fileName()));
        item.put(COMPRESSED_FILE_NAME, AttributeValue.fromS(data.compressedFileName()));
        item.put(EVENT_ID, AttributeValue.fromS(data.eventId()));
        item.put(USERNAME, AttributeValue.fromS(data.username()));
        item.put(ORIGINAL_FILE_OBJECT_KEY, AttributeValue.fromS(data.originalFileObjectKey()));
        item.put(ORIGINAL_FILE_PRESIGNED_URL, AttributeValue.fromS(data.originalFilePresignedUrl()));
        item.put(COMPRESSED_FILE_OBJECT_KEY, AttributeValue.fromS(data.compressedFileObjectKey()));
        item.put(COMPRESSED_FILE_PRESIGNED_URL, AttributeValue.fromS(data.compressedFilePresignedUrl()));
        item.put(COMPRESSED_FILE_WIDTH, AttributeValue.fromS(data.compressedFileWidth()));
        item.put(COMPRESSED_FILE_HEIGHT, AttributeValue.fromS(data.compressedFileHeight()));
        item.put(PRESIGN_DATE_TIME, AttributeValue.fromS(
                data.presignDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        );
        return item;
    }
}
