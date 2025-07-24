package pl.niebieskieaparaty.imageuploader.upload.adapter.primary.mvc;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Path("/")
@RequiredArgsConstructor
@Slf4j
class UploadPage {

    private final Template index;

    @ConfigProperty(name = "base-backend-path")
    String baseBackendPath;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        log.info("Base backend path: {}", baseBackendPath);
        return index.instance().data("baseBackendPath", baseBackendPath);
    }
}

