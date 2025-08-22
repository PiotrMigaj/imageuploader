package pl.niebieskieaparaty.imageuploader.upload.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Getter
public class ImageCompressionConfiguration {

    @ConfigProperty(name = "image.compression.max-width", defaultValue = "2500")
    int maxWidth;

    @ConfigProperty(name = "image.compression.quality", defaultValue = "85")
    int quality;

    @ConfigProperty(name = "image.compression.format", defaultValue = "webp")
    String format;

    @ConfigProperty(name = "image.compression.temp-file-prefix", defaultValue = "imageuploader")
    String tempFilePrefix;
}