package pl.niebieskieaparaty.imageuploader.upload.adapter.primary.rest;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import pl.niebieskieaparaty.imageuploader.upload.application.route.UploadRouteApi;

@ApplicationScoped
@Slf4j
class UploadRestRoute extends RouteBuilder {

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

        rest("/uploads")
            .post()
                .bindingMode(RestBindingMode.off)
                .consumes("multipart/form-data")
                .produces("text/plain")
                .param()
                    .name("eventId")
                    .type(RestParamType.formData)
                    .dataType("string")
                    .required(true)
                .endParam()
                .param()
                    .name("username")
                    .type(RestParamType.formData)
                    .dataType("string")
                    .required(true)
                .endParam()
                .param()
                    .name("file")
                    .type(RestParamType.formData)
                    .dataType("file")
                    .required(true)
                .endParam()
                .to(UploadRouteApi.DIRECT_UPLOAD_IMAGE_TO_BUCKETS);
    }
}
