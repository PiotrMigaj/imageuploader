package pl.niebieskieaparaty.imageuploader.selection.adapter.primary.rest;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import pl.niebieskieaparaty.imageuploader.selection.application.route.SelectionRouteApi;
import pl.niebieskieaparaty.imageuploader.selection.core.SelectionProcessRequest;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class SelectionRestRoute extends RouteBuilder {

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

        rest("/selections/process")
                .post()
                    .type(SelectionProcessRequest.class)
                    .outType(String.class)
                    .to(SelectionRouteApi.DIRECT_PROCESS_SELECTION);
    }
}