package pl.niebieskieaparaty.imageuploader.upload.application.route;

public interface UploadRouteApi {
    String DIRECT_UPLOAD_IMAGE_TO_BUCKETS = "direct:uploadImageToBuckets";
    String DIRECT_UPLOAD_IMAGE_TO_ORIGINAL_FOLDER = "direct:uploadImageToOriginalFolder";
    String DIRECT_UPLOAD_IMAGE_TO_COMPRESSED_FOLDER = "direct:uploadImageToCompressedFolder";
    String COMPLETE_UPLOAD = "direct:completeUpload";
    String COMPLETE_UPLOAD_VALID_IMAGE_AMOUNT = "direct:completeUploadValidImageAmount";
    String COMPLETE_UPLOAD_NOT_VALID_IMAGE_AMOUNT = "direct:completeUploadNotValidImageAmount";
}
