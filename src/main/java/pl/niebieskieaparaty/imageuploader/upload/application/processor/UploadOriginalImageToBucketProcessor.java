package pl.niebieskieaparaty.imageuploader.upload.application.processor;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.attachment.AttachmentMessage;

@ApplicationScoped
@Slf4j
public class UploadOriginalImageToBucketProcessor implements Processor {

    private static final String ORIGINAL_PATH = "%s/%s/images/original/%s";

    @Override
    public void process(final Exchange exchange) throws Exception {
        // Extract form fields from headers or body
        final var eventId = exchange.getIn().getHeader("eventId", String.class);
        final var username = exchange.getIn().getHeader("username", String.class);

        if (eventId == null || username == null) {
            throw new IllegalArgumentException("Missing required fields: eventId and username.");
        }

        log.info("Uploading file for user '{}' and event '{}'", username, eventId);

        // Handle the file attachment
        final var attachmentMessage = exchange.getIn(AttachmentMessage.class);

        if (attachmentMessage.getAttachments().isEmpty()) {
            throw new IllegalArgumentException("No file uploaded.");
        }

        final var attachment = attachmentMessage.getAttachments().entrySet().iterator().next();
        final var dataHandler = attachment.getValue();

        var originalFileName = dataHandler.getName();
        if (originalFileName == null || originalFileName.isEmpty()) {
            originalFileName = attachment.getKey();
        }

        // Build S3 key (folder path + filename)
        final var s3Key = String.format(ORIGINAL_PATH, username, eventId, originalFileName);

        // Set S3 headers
        exchange.getIn().setHeader("CamelAwsS3Key", s3Key);

        // Set file content as body
        exchange.getIn().setBody(dataHandler.getInputStream());
    }
}
