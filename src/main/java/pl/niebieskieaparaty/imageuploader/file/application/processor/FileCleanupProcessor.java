package pl.niebieskieaparaty.imageuploader.file.application.processor;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@ApplicationScoped
@Slf4j
public class FileCleanupProcessor implements Processor {

    private static final String CAMEL_SUBFOLDER = ".camel";

    @Override
    public void process(final Exchange exchange) throws Exception {
        @SuppressWarnings("unchecked")
        final var originalFiles = (List<String>) exchange.getProperty("originalFileList");
        @SuppressWarnings("unchecked")
        final var movedFiles = (List<String>) exchange.getProperty("movedFiles");
        final String sourceDir = exchange.getProperty("sourceDirectory", String.class);

        // Verify all files were moved successfully
        final boolean allFilesMoved = movedFiles.size() == originalFiles.size();

        if (allFilesMoved) {
            final var sourcePath = java.nio.file.Paths.get(sourceDir);
            final var camelPath = sourcePath.resolve(CAMEL_SUBFOLDER);

            try {
                // Clean up .camel directory if it exists
                if (Files.exists(camelPath)) {
                    cleanupCamelDirectory(camelPath, exchange);
                } else {
                    exchange.getIn().setHeader("cleanupMessage", "No .camel directory found to clean up");
                }

                exchange.getIn().setBody(movedFiles);
                exchange.getIn().setHeader("verificationStatus", "SUCCESS");
                exchange.getIn().setHeader("movedCount", movedFiles.size());

            } catch (Exception e) {
                exchange.getIn().setHeader("cleanupError", e.getMessage());
                exchange.getIn().setHeader("verificationStatus", "CLEANUP_FAILED");
            }
        } else {
            exchange.getIn().setHeader("verificationStatus", "INCOMPLETE_MOVE");
            exchange.getIn().setHeader("expectedCount", originalFiles.size());
            exchange.getIn().setHeader("actualCount", movedFiles.size());
        }
    }

    private void cleanupCamelDirectory(final Path camelPath, final Exchange exchange) throws Exception {
        try (Stream<Path> paths = Files.walk(camelPath)) {
            final var deletedFiles = new ArrayList<String>();
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            deletedFiles.add(path.toString());
                        } catch (Exception e) {
                            log.error("Failed to delete: {} - {}", path, e.getMessage());
                        }
                    });
            exchange.getIn().setHeader("cleanupMessage",
                    "Cleaned up .camel directory - deleted " + deletedFiles.size() + " items");
        }
    }
}