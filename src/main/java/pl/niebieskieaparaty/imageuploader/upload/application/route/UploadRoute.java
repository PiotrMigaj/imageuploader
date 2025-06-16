package pl.niebieskieaparaty.imageuploader.upload.application.route;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import pl.niebieskieaparaty.imageuploader.event.application.route.EventRouteApi;
import pl.niebieskieaparaty.imageuploader.gallery.application.route.GalleryRouteApi;
import pl.niebieskieaparaty.imageuploader.upload.application.aggregator.UploadAggregationStrategy;
import pl.niebieskieaparaty.imageuploader.upload.application.mapper.UploadedDataMapper;
import pl.niebieskieaparaty.imageuploader.upload.application.processor.JpegToWebpImageCompressor;
import pl.niebieskieaparaty.imageuploader.upload.application.processor.S3PresignUrlProcessor;
import pl.niebieskieaparaty.imageuploader.upload.application.processor.UploadCompressedImageToBucketProcessor;
import pl.niebieskieaparaty.imageuploader.upload.application.processor.UploadOriginalImageToBucketProcessor;

import static pl.niebieskieaparaty.imageuploader.upload.application.constant.UploadRouteConstant.UploadType;

@ApplicationScoped
@RequiredArgsConstructor
class UploadRoute extends RouteBuilder {

    private final UploadOriginalImageToBucketProcessor uploadOriginalImageToBucketProcessor;

    private final UploadCompressedImageToBucketProcessor uploadCompressedImageToBucketProcessor;

    private final S3PresignUrlProcessor s3PresignUrlProcessor;

    private final JpegToWebpImageCompressor jpegToWebpImageCompressor;

    private final UploadAggregationStrategy uploadAggregationStrategy;

    private final UploadedDataMapper uploadedDataMapper;

    @ConfigProperty(name = "aws.bucket-name")
    String bucketName;

    @Override
    public void configure() throws Exception {

        from(UploadRouteApi.DIRECT_UPLOAD_IMAGE_TO_BUCKETS)
                .routeId("uploadImageToBuckets")
                .setHeader("CamelAwsS3BucketName", constant(bucketName))
                .multicast(uploadAggregationStrategy)
                    .to(UploadRouteApi.DIRECT_UPLOAD_IMAGE_TO_ORIGINAL_FOLDER,
                            UploadRouteApi.DIRECT_UPLOAD_IMAGE_TO_COMPRESSED_FOLDER)
                .end()
                .bean(uploadedDataMapper)
                .log("Body after uploading to S3 buckets: ${body}")
                .to(GalleryRouteApi.DIRECT_SAVE_UPLOADED_DATA_IN_DYNAMODB)
                .to(EventRouteApi.DIRECT_UPDATE_EVENT_WITH_CAMEL_GALLERY)
                .setBody(simple("File uploaded successfully"));

        from(UploadRouteApi.DIRECT_UPLOAD_IMAGE_TO_ORIGINAL_FOLDER)
                .routeId("uploadImageToOriginalFolder")
                .setHeader("uploadType", constant(UploadType.ORIGINAL))
                .process(uploadOriginalImageToBucketProcessor)
                .toD("aws2-s3://${header.CamelAwsS3BucketName}?autoCreateBucket=false")
                .process(s3PresignUrlProcessor);

        from(UploadRouteApi.DIRECT_UPLOAD_IMAGE_TO_COMPRESSED_FOLDER)
                .routeId("uploadImageToCompressedFolder")
                .process(jpegToWebpImageCompressor)
                .setHeader("uploadType", constant(UploadType.COMPRESSED))
                .process(uploadCompressedImageToBucketProcessor)
                .toD("aws2-s3://${header.CamelAwsS3BucketName}?autoCreateBucket=false")
                .process(s3PresignUrlProcessor);
    }
}
