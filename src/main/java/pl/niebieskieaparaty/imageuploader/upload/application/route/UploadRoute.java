package pl.niebieskieaparaty.imageuploader.upload.application.route;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import pl.niebieskieaparaty.imageuploader.gallery.application.route.GalleryRouteApi;
import pl.niebieskieaparaty.imageuploader.upload.application.aggregator.UploadAggregationStrategy;
import pl.niebieskieaparaty.imageuploader.upload.application.mapper.UploadedDataMapper;
import pl.niebieskieaparaty.imageuploader.upload.application.processor.S3PresignUrlProcessor;
import pl.niebieskieaparaty.imageuploader.upload.application.processor.UploadImageToBucketProcessor;

import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor
class UploadRoute extends RouteBuilder {

    private static final String ORIGINAL_PATH = "%s/%s/images/original/%s";

    private static final String ORIGINAL = "original";

    private static final String COMPRESSED_PATH = "%s/%s/images/compressed/%s";

    private static final String COMPRESSED = "compressed";

    private final UploadImageToBucketProcessor uploadImageToBucketProcessor;

    private final S3PresignUrlProcessor s3PresignUrlProcessor;

    private final UploadAggregationStrategy uploadAggregationStrategy;

    private final UploadedDataMapper uploadedDataMapper;

    @Override
    public void configure() throws Exception {

        from(UploadRouteApi.DIRECT_UPLOAD_IMAGE_TO_BUCKETS)
                .routeId("uploadImageToBuckets")
                .multicast(uploadAggregationStrategy)
                    .to(UploadRouteApi.DIRECT_UPLOAD_IMAGE_TO_ORIGINAL_FOLDER,
                            UploadRouteApi.DIRECT_UPLOAD_IMAGE_TO_COMPRESSED_FOLDER)
                .end()
                .bean(uploadedDataMapper)
                .log("Body after uploading to S3 buckets: ${body}")
                .to(GalleryRouteApi.DIRECT_SAVE_UPLOADED_DATA_IN_DYNAMODB)
                .setBody(simple("File uploaded successfully"));

        from(UploadRouteApi.DIRECT_UPLOAD_IMAGE_TO_ORIGINAL_FOLDER)
                .routeId("uploadImageToOriginalFolder")
                .setHeader("bucketPath", constant(ORIGINAL_PATH))
                .setHeader("uploadType", constant(ORIGINAL))
                .process(uploadImageToBucketProcessor)
                .to("aws2-s3://niebieskie-aparaty-test-upload?autoCreateBucket=false")
                .process(s3PresignUrlProcessor);

        from(UploadRouteApi.DIRECT_UPLOAD_IMAGE_TO_COMPRESSED_FOLDER)
                .routeId("uploadImageToCompressedFolder")
                .setHeader("bucketPath", constant(COMPRESSED_PATH))
                .setHeader("uploadType", constant(COMPRESSED))
                .process(uploadImageToBucketProcessor)
                .to("aws2-s3://niebieskie-aparaty-test-upload?autoCreateBucket=false")
                .process(s3PresignUrlProcessor);
    }
}
