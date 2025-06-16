package pl.niebieskieaparaty.imageuploader.security.adapter.primary.mvc;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Path("/login")
@Slf4j
@RequiredArgsConstructor
class LoginPage {

    private final Template login;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("error") boolean error,
                                @QueryParam("logout") boolean logout) {
        log.info("logout param: {}", logout);
        log.info("error param: {}", error);
        return login.data("error", error)
                .data("logout", logout);
    }
}

