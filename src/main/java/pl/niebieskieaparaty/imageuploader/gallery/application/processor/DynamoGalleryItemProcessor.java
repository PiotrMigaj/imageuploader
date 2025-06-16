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
        item.put("fileName", AttributeValue.fromS(data.fileName()));
        item.put("eventId", AttributeValue.fromS(data.eventId()));
        item.put("username", AttributeValue.fromS(data.username()));
        item.put("originalFileObjectKey", AttributeValue.fromS(data.originalFileObjectKey()));
        item.put("originalFilePresignedUrl", AttributeValue.fromS(data.originalFilePresignedUrl()));
        item.put("compressedFileObjectKey", AttributeValue.fromS(data.compressedFileObjectKey()));
        item.put("compressedFilePresignedUrl", AttributeValue.fromS(data.compressedFilePresignedUrl()));
        item.put("presignDateTime", AttributeValue.fromS(
                data.presignDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        );
        return item;
    }
}
