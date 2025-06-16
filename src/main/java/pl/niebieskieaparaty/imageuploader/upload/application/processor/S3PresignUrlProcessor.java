package pl.niebieskieaparaty.imageuploader.upload.application.processor;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
@Slf4j
public class S3PresignUrlProcessor implements Processor {

    @ConfigProperty(name = "aws.access-key")
    String accessKey;

    @ConfigProperty(name = "aws.secret-key")
    String secretKey;

    @ConfigProperty(name = "aws.region", defaultValue = "eu-central-1")
    String region;

    @Override
    public void process(final Exchange exchange) throws Exception {
        // Generate pre-signed URL after upload
        final var bucket = exchange.getIn().getHeader("CamelAwsS3BucketName", String.class);
        final var key = exchange.getIn().getHeader("CamelAwsS3Key", String.class);
        final var uploadType = exchange.getIn().getHeader("uploadType", String.class);

        // Get or create the S3Presigner (could be injected or created per use)
        final var presigner = S3Presigner.builder()
                .region(Region.of(region)) // match your region
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();

        final var request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        final var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofDays(7)) // Set expiration
                .getObjectRequest(request)
                .build();

        final var presignedUrl = presigner.presignGetObject(presignRequest).url().toString();

        log.info("Presigned URL generated [{}]: {}", uploadType, presignedUrl);

        final var result = new HashMap<String, String>();
        result.put(uploadType + "FileObjectKey", key);
        result.put(uploadType + "FilePresignedUrl", presignedUrl);

        exchange.getIn().setBody(result); // structured response for aggregation

        presigner.close();
    }
}
