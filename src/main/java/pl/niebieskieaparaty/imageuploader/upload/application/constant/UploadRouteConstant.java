package pl.niebieskieaparaty.imageuploader.upload.application.constant;

import lombok.Getter;

public interface UploadRouteConstant {

    String EVENT_ID = "eventId";

    String USERNAME = "username";

    String CAMEL_FILE_NAME = "CamelFileName";

    String FILE_NAME = "fileName";

    String COMPRESSED_FILE_NAME = "compressedFileName";

    String COMPRESSED_FILE_WIDTH = "compressedFileWidth";

    String COMPRESSED_FILE_HEIGHT = "compressedFileHeight";

    String ORIGINAL_FILE_OBJECT_KEY = "originalFileObjectKey";

    String ORIGINAL_FILE_PRESIGNED_URL = "originalFilePresignedUrl";

    String COMPRESSED_FILE_OBJECT_KEY = "compressedFileObjectKey";

    String COMPRESSED_FILE_PRESIGNED_URL = "compressedFilePresignedUrl";

    String PRESIGN_DATE_TIME = "presignDateTime";

    @Getter
    enum UploadType {
        ORIGINAL("original"),
        COMPRESSED("compressed");

        private final String uploadType;

        UploadType(final String uploadType) {
            this.uploadType = uploadType;
        }
    }
}
