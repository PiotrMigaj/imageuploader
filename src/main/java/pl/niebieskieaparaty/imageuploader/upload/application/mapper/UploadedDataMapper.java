package pl.niebieskieaparaty.imageuploader.upload.application.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import pl.niebieskieaparaty.imageuploader.upload.core.UploadedData;

import java.util.Map;

import static pl.niebieskieaparaty.imageuploader.upload.application.constant.UploadRouteConstant.*;

@ApplicationScoped
public class UploadedDataMapper {

    public UploadedData fromMap(final Map<String, String> map) {
        return UploadedData.createNew(
                map.get(FILE_NAME),
                map.get(COMPRESSED_FILE_NAME),
                map.get(EVENT_ID),
                map.get(USERNAME),
                map.get(ORIGINAL_FILE_OBJECT_KEY),
                map.get(ORIGINAL_FILE_PRESIGNED_URL),
                map.get(COMPRESSED_FILE_OBJECT_KEY),
                map.get(COMPRESSED_FILE_PRESIGNED_URL),
                map.get(COMPRESSED_FILE_WIDTH),
                map.get(COMPRESSED_FILE_HEIGHT)
        );
    }
}
