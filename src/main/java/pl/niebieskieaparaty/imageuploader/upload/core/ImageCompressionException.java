package pl.niebieskieaparaty.imageuploader.upload.core;

public class ImageCompressionException extends UploadException {
    public ImageCompressionException(String message) {
        super(message);
    }

    public ImageCompressionException(String message, Throwable cause) {
        super(message, cause);
    }
}