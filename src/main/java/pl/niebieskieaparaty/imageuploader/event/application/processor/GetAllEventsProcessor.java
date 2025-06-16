package pl.niebieskieaparaty.imageuploader.event.application.processor;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.aws2.ddb.Ddb2Constants;
import pl.niebieskieaparaty.imageuploader.event.core.Event;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Slf4j
public class GetAllEventsProcessor implements Processor {

    private static final String CAMEL_AWS_DDB_ITEMS = "CamelAwsDdbItems";
    private static final String EVENT_ID = "eventId";
    private static final String DATE = "date";
    private static final String USERNAME = "username";
    private static final String TITLE = "title";

    @Override
    public void process(Exchange exchange) throws Exception {
        var items = exchange.getIn().getHeader(CAMEL_AWS_DDB_ITEMS);
        if (items == null) {
            items = exchange.getIn().getHeader(Ddb2Constants.ITEMS);
        }

        if (items != null) {
            log.info("Found items: {}", items);
            final var ddbItems = (List<Map<String, AttributeValue>>) items;
            final var events = new ArrayList<Event>();
            for (var item : ddbItems) {
                events.add(Event.of(
                        item.getOrDefault(EVENT_ID, AttributeValue.builder().s("").build()).s(),
                        item.getOrDefault(DATE, AttributeValue.builder().s("").build()).s(),
                        item.getOrDefault(USERNAME, AttributeValue.builder().s("").build()).s(),
                        item.getOrDefault(TITLE, AttributeValue.builder().s("").build()).s()
                ));
            }
            exchange.getMessage().setBody(events);
        } else {
            log.warn("No items found in any header");
            exchange.getMessage().setBody(List.of());
        }
    }
}
