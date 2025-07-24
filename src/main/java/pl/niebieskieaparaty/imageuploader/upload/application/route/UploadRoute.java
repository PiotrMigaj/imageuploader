package pl.niebieskieaparaty.imageuploader.upload.application.route;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import pl.niebieskieaparaty.imageuploader.event.application.route.EventRouteApi;
import pl.niebieskieaparaty.imageuploader.gallery.application.route.GalleryRouteApi;
import pl.niebieskieaparaty.imageuploader.upload.application.aggregator.UploadAggregationStrategy;
import pl.niebieskieaparaty.imageuploader.upload.application.mapper.UploadedDataMapper;
import pl.niebieskieaparaty.imageuploader.upload.application.port.secondary.ImageUploadedDataRepository;
import pl.niebieskieaparaty.imageuploader.upload.application.predicates.ImagesUploadValidationPredicate;
import pl.niebieskieaparaty.imageuploader.upload.application.processor.JpegToWebpImageCompressor;
import pl.niebieskieaparaty.imageuploader.upload.application.processor.S3PresignUrlProcessor;
import pl.niebieskieaparaty.imageuploader.upload.application.processor.UploadCompressedImageToBucketProcessor;
import pl.niebieskieaparaty.imageuploader.upload.application.processor.UploadOriginalImageToBucketProcessor;
import pl.niebieskieaparaty.imageuploader.upload.core.CompleteUploadRequest;
import pl.niebieskieaparaty.imageuploader.upload.core.EventId;

import java.util.UUID;

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

    private final ImageUploadedDataRepository imageUploadedDataRepository;

    private final ImagesUploadValidationPredicate imagesUploadValidationPredicate;

    @ConfigProperty(name = "aws.bucket-name")
    String bucketName;

    @Override
    public void configure() throws Exception {

        from(UploadRouteApi.DIRECT_UPLOAD_IMAGE_TO_BUCKETS)
                .routeId("uploadImageToBuckets")
                .setHeader("CamelAwsS3BucketName", constant(bucketName))
                .multicast(uploadAggregationStrategy).parallelProcessing()
                .to(UploadRouteApi.DIRECT_UPLOAD_IMAGE_TO_ORIGINAL_FOLDER)
                .to(UploadRouteApi.DIRECT_UPLOAD_IMAGE_TO_COMPRESSED_FOLDER)
                .end()
                .bean(uploadedDataMapper)
                .log("Body after uploading to S3 buckets: ${body}")
                .bean(imageUploadedDataRepository, "saveUploadedData")
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


        from(UploadRouteApi.COMPLETE_UPLOAD)
                .routeId("completeUpload")
                .setHeader("eventId").jsonpath("$.eventId")
                .setHeader("imagesAmount").jsonpath("$.imagesAmount")
                .setBody(exchange -> {
                    final var eventId = exchange.getIn().getBody(CompleteUploadRequest.class).eventId();
                    return EventId.of(UUID.fromString(eventId));
                })
                .bean(imageUploadedDataRepository, "getDataByEventId")
                .choice()
                    .when().method(imagesUploadValidationPredicate, "isValidImageAmount")
                        .to(UploadRouteApi.COMPLETE_UPLOAD_VALID_IMAGE_AMOUNT)
                    .otherwise()
                        .to(UploadRouteApi.COMPLETE_UPLOAD_NOT_VALID_IMAGE_AMOUNT)
                .end();

        from(UploadRouteApi.COMPLETE_UPLOAD_VALID_IMAGE_AMOUNT)
                .split(body()).parallelProcessing()
                .to(GalleryRouteApi.DIRECT_SAVE_UPLOADED_DATA_IN_DYNAMODB)
                .end()
                .to(EventRouteApi.DIRECT_UPDATE_EVENT_WITH_CAMEL_GALLERY)
                .setBody(exchange -> {
                    final var eventId = exchange.getIn().getHeader("eventId", String.class);
                    return EventId.of(UUID.fromString(eventId));
                })
                .bean(imageUploadedDataRepository, "deleteEntryForEventId")
                .setBody(simple("Total upload of files completed successfully for eventId: ${body}."));

        from(UploadRouteApi.COMPLETE_UPLOAD_NOT_VALID_IMAGE_AMOUNT)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setBody(simple("Images amount from request (${header.imagesAmount}) differs from actual uploaded images count"));


    }
}
