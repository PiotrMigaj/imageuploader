package pl.niebieskieaparaty.imageuploader.upload.application.port.secondary;

import pl.niebieskieaparaty.imageuploader.upload.core.EventId;
import pl.niebieskieaparaty.imageuploader.upload.core.UploadedData;

import java.util.List;

public interface ImageUploadedDataRepository {

    void saveUploadedData(UploadedData uploadedData);

    void deleteEntryForEventId(EventId eventId);

    List<UploadedData> getDataByEventId(EventId eventId);
}
