package pl.niebieskieaparaty.imageuploader.file.application.processor;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import pl.niebieskieaparaty.imageuploader.file.core.FileSelectionRequest;

import java.nio.file.Files;
import java.nio.file.Paths;

@ApplicationScoped
public class FileSetupProcessor implements Processor {

    private static final String DOT = ".";

    @ConfigProperty(name = "selectedImages.subfolder.name")
    String subfolderName;

    @ConfigProperty(name = "selectedImages.extension")
    String extension;

    @Override
    public void process(final Exchange exchange) throws Exception {

        final var fileSelectionRequest = exchange.getIn().getBody(FileSelectionRequest.class);
        try {
            // Create target subfolder
            final var targetPath = Paths.get(fileSelectionRequest.baseDirectoryPath(), subfolderName);
            Files.createDirectories(targetPath);

            // Set properties for the file copying routes
            exchange.setProperty("sourceDirectory", fileSelectionRequest.baseDirectoryPath());
            exchange.setProperty("targetDirectory", targetPath.toString());

            // Pass the list of files to split
            final var imageNamesWithExtension = fileSelectionRequest.imageNames().stream()
                    .map(imageName -> imageName + DOT + extension)
                    .toList();

            exchange.getIn().setBody(imageNamesWithExtension);

        } catch (Exception ex) {
            exchange.setProperty("processingError", ex.getMessage());
            throw ex;
        }
    }
}
