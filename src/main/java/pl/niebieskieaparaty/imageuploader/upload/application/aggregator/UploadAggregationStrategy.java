package pl.niebieskieaparaty.imageuploader.upload.application.aggregator;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class UploadAggregationStrategy implements AggregationStrategy {
    @Override
    public Exchange aggregate(final Exchange oldExchange, final Exchange newExchange) {
        final Map<String, Object> aggregatedResult;
        if (oldExchange == null) {
            aggregatedResult = new HashMap<>();
        } else {
            aggregatedResult = oldExchange.getIn().getBody(Map.class);
        }
        final Map<String, Object> newResult = newExchange.getIn().getBody(Map.class);
        if (newResult != null) {
            aggregatedResult.putAll(newResult);
        }
        if (oldExchange == null) {
            final var eventId = newExchange.getIn().getHeader("eventId", String.class);
            final var username = newExchange.getIn().getHeader("username", String.class);
            final var fileName = newExchange.getIn().getHeader("CamelFileName", String.class);
            aggregatedResult.put("eventId", eventId);
            aggregatedResult.put("username", username);
            aggregatedResult.put("fileName", fileName);
        }
        newExchange.getIn().setBody(aggregatedResult);
        return newExchange;
    }
}
