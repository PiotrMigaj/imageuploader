package pl.niebieskieaparaty.imageuploader.selection.adapter.primary.rest;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import pl.niebieskieaparaty.imageuploader.selection.application.route.SelectionRouteApi;
import pl.niebieskieaparaty.imageuploader.selection.core.Selection;
import pl.niebieskieaparaty.imageuploader.selection.core.SelectionProcessRequest;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class UploadSelectionRestRoute extends RouteBuilder {

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

        rest("/selections")
                .get()
                .param()
                    .name("blocked")
                    .type(RestParamType.query)
                    .dataType("boolean")
                    .defaultValue("false")
                .endParam()
                .outType(List.class)
                .to("direct:getAllSelectionsDueToTheBlockedParam");

        from("direct:getAllSelectionsDueToTheBlockedParam")
                .choice()
                    .when(simple("${header.blocked} == 'true'"))
                        .to(SelectionRouteApi.DIRECT_GET_ALL_BLOCKED_SELECTIONS)
                    .otherwise()
                        .to(SelectionRouteApi.DIRECT_GET_ALL_NOT_BLOCKED_SELECTIONS)
                .end();
    }
}
