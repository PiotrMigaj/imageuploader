package pl.niebieskieaparaty.imageuploader.upload.application.processor;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.io.ByteArrayInputStream;

@Slf4j
@ApplicationScoped
public class UploadCompressedImageToBucketProcessor implements Processor {

    private static final String COMPRESSED_PATH = "%s/%s/images/compressed/%s";

    @Override
    public void process(final Exchange exchange) throws Exception {

        final var eventId = exchange.getIn().getHeader("eventId", String.class);
        final var username = exchange.getIn().getHeader("username", String.class);

        if (eventId == null || username == null) {
            throw new IllegalArgumentException("Missing required fields: eventId and username.");
        }

        final var webpBytes = exchange.getIn().getBody(byte[].class);
        if (webpBytes == null || webpBytes.length == 0) {
            throw new IllegalArgumentException("No converted image data available in message body");
        }

        var originalFileName = exchange.getIn().getHeader("compressedFileName", String.class);
        if (originalFileName == null || originalFileName.isBlank()) {
            originalFileName = "converted-image.webp";
        } else {
            originalFileName = originalFileName.replaceAll("\\.[^.]+$", "") + ".webp";
        }

        final var s3Key = String.format(COMPRESSED_PATH, username, eventId, originalFileName);

        exchange.getIn().setHeader("CamelAwsS3Key", s3Key);

        exchange.getIn().setBody(new ByteArrayInputStream(webpBytes));

        log.info("Uploading converted image to S3 with key: {}", s3Key);
    }
}
