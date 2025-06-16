package pl.niebieskieaparaty.imageuploader.event.adapter.primary.rest;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import pl.niebieskieaparaty.imageuploader.event.application.route.EventRouteApi;

@ApplicationScoped
@Slf4j
class EventRestRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        restConfiguration()
                .component("platform-http")
                .bindingMode(RestBindingMode.json)
                .host("localhost")
                .port(8080)
                .contextPath("/api")
                .apiProperty("cors", "true")
                .enableCORS(true)
                .dataFormatProperty("prettyPrint", "true")
                .dataFormatProperty("json.in.prettyPrint", "true")
                .dataFormatProperty("json.out.prettyPrint", "true");

        rest("/events")
            .get()
                .produces("application/json")
                .to(EventRouteApi.DIRECT_GET_EVENTS);
    }
}
