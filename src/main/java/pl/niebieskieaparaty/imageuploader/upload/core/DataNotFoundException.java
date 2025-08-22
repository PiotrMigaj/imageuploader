package pl.niebieskieaparaty.imageuploader.upload.core;

public class DataNotFoundException extends UploadException {
    public DataNotFoundException(String message) {
        super(message);
    }

    public DataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}