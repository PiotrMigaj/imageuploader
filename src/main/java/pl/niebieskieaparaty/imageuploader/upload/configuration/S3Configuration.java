package pl.niebieskieaparaty.imageuploader.upload.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@ApplicationScoped
@Slf4j
public class S3Configuration {

    @ConfigProperty(name = "aws.access-key")
    String accessKey;

    @ConfigProperty(name = "aws.secret-key")
    String secretKey;

    @ConfigProperty(name = "aws.region", defaultValue = "eu-central-1")
    String region;

    @Produces
    @ApplicationScoped
    public S3Presigner s3Presigner() {
        log.info("Initializing S3Presigner with region: {}", region);
        try {
            final var presigner = S3Presigner.builder()
                    .region(Region.of(region))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(accessKey, secretKey)
                            )
                    )
                    .build();
            log.info("S3Presigner initialized successfully.");
            return presigner;
        } catch (Exception e) {
            log.error("Failed to initialize S3Presigner", e);
            throw e;
        }
    }

    void cleanup(@Disposes final S3Presigner presigner) {
        log.info("Closing S3Presigner...");
        try {
            presigner.close();
            log.info("S3Presigner closed successfully.");
        } catch (Exception e) {
            log.warn("Error while closing S3Presigner", e);
        }
    }
}
