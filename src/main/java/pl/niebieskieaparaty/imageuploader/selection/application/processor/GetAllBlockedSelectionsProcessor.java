package pl.niebieskieaparaty.imageuploader.selection.application.processor;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.aws2.ddb.Ddb2Constants;
import pl.niebieskieaparaty.imageuploader.selection.core.Selection;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Slf4j
public class GetAllBlockedSelectionsProcessor implements Processor {

    private static final String CAMEL_AWS_DDB_ITEMS = "CamelAwsDdbItems";
    private static final String SELECTION_ID = "selectionId";
    private static final String BLOCKED = "blocked";
    private static final String EVENT_ID = "eventId";
    private static final String EVENT_TITLE = "eventTitle";
    private static final String USERNAME = "username";

    @Override
    public void process(final Exchange exchange) throws Exception {
        var items = exchange.getIn().getHeader(CAMEL_AWS_DDB_ITEMS);
        if (items == null) {
            items = exchange.getIn().getHeader(Ddb2Constants.ITEMS);
        }

        if (items != null) {
            log.info("Found items: {}", items);
            final var ddbItems = (List<Map<String, AttributeValue>>) items;
            final var selections = new ArrayList<Selection>();
            for (var item : ddbItems) {
                selections.add(new Selection(
                        item.getOrDefault(SELECTION_ID, AttributeValue.builder().s("").build()).s(),
                        item.getOrDefault(BLOCKED, AttributeValue.builder().bool(false).build()).bool(),
                        item.getOrDefault(EVENT_ID, AttributeValue.builder().s("").build()).s(),
                        item.getOrDefault(EVENT_TITLE, AttributeValue.builder().s("").build()).s(),
                        item.getOrDefault(USERNAME, AttributeValue.builder().s("").build()).s()
                ));
            }
            exchange.getMessage().setBody(selections);
        } else {
            log.warn("No items found in any header");
            exchange.getMessage().setBody(List.of());
        }
    }
}