package pl.niebieskieaparaty.imageuploader.selection.adapter.primary.mvc;

import org.apache.camel.ProducerTemplate;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.niebieskieaparaty.imageuploader.selection.application.route.SelectionRouteApi;
import pl.niebieskieaparaty.imageuploader.selection.core.Selection;

import java.util.List;


@Path("/selection")
@RequiredArgsConstructor
@Slf4j
class SelectionPage {

    private final Template selection;

    private final ProducerTemplate producerTemplate;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        final var selections = (List<Selection>) producerTemplate
                .requestBody(SelectionRouteApi.DIRECT_GET_ALL_BLOCKED_SELECTIONS, null, List.class);
        log.info("Number of items in Selection table with blocked attribute set to true: {}", selections.size());
        return selection.data("selections", selections);
    }
}

