package pl.niebieskieaparaty.imageuploader.upload.application.aggregator;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import java.util.HashMap;
import java.util.Map;

import static pl.niebieskieaparaty.imageuploader.upload.application.constant.UploadRouteConstant.*;

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
            final var eventId = newExchange.getIn().getHeader(EVENT_ID, String.class);
            final var username = newExchange.getIn().getHeader(USERNAME, String.class);
            final var fileName = newExchange.getIn().getHeader(CAMEL_FILE_NAME, String.class);
            aggregatedResult.put(EVENT_ID, eventId);
            aggregatedResult.put(USERNAME, username);
            aggregatedResult.put(FILE_NAME, fileName);
        }
        addHeaderIfExists(COMPRESSED_FILE_NAME, newExchange, aggregatedResult);
        addHeaderIfExists(COMPRESSED_FILE_WIDTH, newExchange, aggregatedResult);
        addHeaderIfExists(COMPRESSED_FILE_HEIGHT, newExchange, aggregatedResult);
        newExchange.getIn().setBody(aggregatedResult);
        return newExchange;
    }

    private void addHeaderIfExists(final String headerName, final Exchange exchange, final Map<String, Object> aggregatedResult) {
        final var headerValue = exchange.getIn().getHeader(headerName, String.class);
        if (headerValue != null && !headerValue.isBlank()) {
            aggregatedResult.put(headerName, headerValue);
        }
    }
}
