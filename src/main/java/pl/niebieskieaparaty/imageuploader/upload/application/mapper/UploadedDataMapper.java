package pl.niebieskieaparaty.imageuploader.upload.application.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import pl.niebieskieaparaty.imageuploader.upload.core.UploadedData;

import java.util.Map;

@ApplicationScoped
public class UploadedDataMapper {

    public UploadedData fromMap(final Map<String, String> map) {
        return UploadedData.createNew(
                map.get("fileName"),
                map.get("eventId"),
                map.get("username"),
                map.get("originalFileObjectKey"),
                map.get("originalFilePresignedUrl"),
                map.get("compressedFileObjectKey"),
                map.get("compressedFilePresignedUrl")
        );
    }
}
