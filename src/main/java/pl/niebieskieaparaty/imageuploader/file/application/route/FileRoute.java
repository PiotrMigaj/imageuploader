package pl.niebieskieaparaty.imageuploader.file.application.route;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import pl.niebieskieaparaty.imageuploader.file.application.processor.FileCleanupProcessor;
import pl.niebieskieaparaty.imageuploader.file.application.processor.FileSetupProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class FileRoute extends RouteBuilder {

    private final FileSetupProcessor fileSetupProcessor;
    private final FileCleanupProcessor fileCleanupProcessor;

    @Override
    public void configure() throws Exception {

        from(FileRouteApi.DIRECT_PROCESS_SELECTED_IMAGES_IN_FOLDER)
                .routeId("processSelectedImagesInFolderRoute")
                .log("Received file selection request: ${body}")
                .process(fileSetupProcessor)
                .to(FileRouteApi.DIRECT_BATCH_MOVE_IMAGES)
                .log("Body ${body}");

        from(FileRouteApi.DIRECT_BATCH_MOVE_IMAGES)
            .routeId("batchMoveImagesRoute")
            .log("Starting batch move for ${body.size()} files")
            .setProperty("originalFileList", body())
            .setProperty("movedFiles", constant(Collections.synchronizedList(new ArrayList<String>())))
            .split(body()).shareUnitOfWork()
                .log("Processing image: ${body}")
                .setHeader("sourceFilePath", body()) // e.g. subfolder/image.jpg
                .pollEnrich()
                    .simple("file:${exchangeProperty.sourceDirectory}?fileName=${header.sourceFilePath}"
                            + "&noop=false&idempotent=false")
                    .timeout(10000)
                .choice()
                    .when(body().isNotNull())
                        .setHeader("CamelFileName", simple("${file:name}"))
                        .log("File ${header.sourceFilePath} found, moving to ${exchangeProperty.targetDirectory}"
                                + "/${header.CamelFileName}")
                        .toD("file:${exchangeProperty.targetDirectory}?fileName=${header.CamelFileName}"
                                + "&tempPrefix=.tmp")
                        .process(exchange -> {
                            @SuppressWarnings("unchecked")
                            final var movedFiles = (List<String>) exchange.getProperty("movedFiles");
                            movedFiles.add(exchange.getIn().getHeader("CamelFileName", String.class));
                        })
                        .log("Successfully moved: ${header.sourceFilePath}")
                    .otherwise()
                        .log("File ${header.sourceFilePath} not found or not accessible")
                .end()
            .end()
            .log("Batch move completed")
            .to(FileRouteApi.DIRECT_CLEANUP_AND_VERIFY);

        from(FileRouteApi.DIRECT_CLEANUP_AND_VERIFY)
            .routeId("cleanupAndVerifyRoute")
            .log("Starting cleanup and verification")
            .process(fileCleanupProcessor)
            .choice()
                .when(header("verificationStatus").isEqualTo("SUCCESS"))
                    .log("Verification successful: moved ${header.movedCount} files. ${header.cleanupMessage}")
                .when(header("verificationStatus").isEqualTo("CLEANUP_FAILED"))
                    .log("Files moved but cleanup failed: ${header.cleanupError}")
                .otherwise()
                    .log("Verification failed: expected ${header.expectedCount}, actual ${header.actualCount}")
            .end()
            .log("Cleanup and verification completed");
    }
}
