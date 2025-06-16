package pl.niebieskieaparaty.imageuploader.upload.core;

import java.time.LocalDateTime;

public record UploadedData(
        String fileName,
        String compressedFileName,
        String eventId,
        String username,
        String originalFileObjectKey,
        String originalFilePresignedUrl,
        String compressedFileObjectKey,
        String compressedFilePresignedUrl,
        String compressedFileWidth,
        String compressedFileHeight,
        LocalDateTime presignDateTime
) {
    public static UploadedData createNew(
            final String fileName,
            final String compressedFileName,
            final String eventId,
            final String username,
            final String originalFileObjectKey,
            final String originalFilePresignedUrl,
            final String compressedFileObjectKey,
            final String compressedFilePresignedUrl,
            final String compressedFileWidth,
            final String compressedFileHeight
    ) {
        return new UploadedData(
                fileName,
                compressedFileName,
                eventId,
                username,
                originalFileObjectKey,
                originalFilePresignedUrl,
                compressedFileObjectKey,
                compressedFilePresignedUrl,
                compressedFileWidth,
                compressedFileHeight,
                LocalDateTime.now()
        );
    }
}
