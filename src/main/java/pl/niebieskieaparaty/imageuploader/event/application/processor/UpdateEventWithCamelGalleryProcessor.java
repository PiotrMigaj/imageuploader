package pl.niebieskieaparaty.imageuploader.event.application.processor;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.aws2.ddb.Ddb2Constants;
import org.apache.camel.component.aws2.ddb.Ddb2Operations;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;

import java.util.HashMap;

@ApplicationScoped
public class UpdateEventWithCamelGalleryProcessor implements Processor {

    @Override
    public void process(final Exchange exchange) throws Exception {
        final var eventId = exchange.getIn().getHeader("eventId", String.class);
        if (eventId == null) {
            throw new IllegalArgumentException("Missing eventId in headers.");
        }

        final var keyMap = new HashMap<String, AttributeValue>();
        keyMap.put("eventId", AttributeValue.builder().s(eventId).build());

        final var updateMap = new HashMap<String, AttributeValueUpdate>();
        updateMap.put("camelGallery", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().bool(true).build()) // or .bool(true) if the field is boolean
                .action("PUT")
                .build());

        exchange.getIn().setHeader(Ddb2Constants.OPERATION, Ddb2Operations.UpdateItem);
        exchange.getIn().setHeader(Ddb2Constants.KEY, keyMap);
        exchange.getIn().setHeader(Ddb2Constants.UPDATE_VALUES, updateMap);
    }
}
