package pl.niebieskieaparaty.imageuploader.upload.application.processor;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import pl.niebieskieaparaty.imageuploader.upload.application.constant.UploadRouteConstant.UploadType;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.HashMap;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
public class S3PresignUrlProcessor implements Processor {

    private final S3Presigner presigner;

    @Override
    public void process(final Exchange exchange) throws Exception {
        final String bucket = exchange.getIn().getHeader("CamelAwsS3BucketName", String.class);
        final String key = exchange.getIn().getHeader("CamelAwsS3Key", String.class);
        final UploadType uploadType = exchange.getIn().getHeader("uploadType", UploadType.class);

        if (bucket == null || key == null || uploadType == null) {
            log.warn("Missing headers: bucket={}, key={}, uploadType={}", bucket, key, uploadType);
            throw new IllegalArgumentException("Required S3 headers are missing");
        }

        final var request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        final var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofDays(7))
                .getObjectRequest(request)
                .build();

        final String presignedUrl = presigner.presignGetObject(presignRequest).url().toString();

        log.info("Presigned URL generated [{}]: {}", uploadType, presignedUrl);

        final var result = new HashMap<String, String>();
        result.put(uploadType.getUploadType() + "FileObjectKey", key);
        result.put(uploadType.getUploadType() + "FilePresignedUrl", presignedUrl);

        exchange.getIn().setBody(result);
    }
}
