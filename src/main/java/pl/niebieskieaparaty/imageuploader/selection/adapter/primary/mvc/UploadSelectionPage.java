package pl.niebieskieaparaty.imageuploader.selection.adapter.primary.mvc;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;


@Path("/upload-selection")
@RequiredArgsConstructor
@Slf4j
class UploadSelectionPage {

    private final Template uploadSelection;

    @ConfigProperty(name = "base-backend-path")
    String baseBackendPath;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        log.info("Base backend path: {}", baseBackendPath);
        return uploadSelection.data("baseBackendPath", baseBackendPath);
    }
}

