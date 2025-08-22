package pl.niebieskieaparaty.imageuploader.upload.adapter.secondary;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import pl.niebieskieaparaty.imageuploader.upload.application.port.secondary.ImageUploadedDataRepository;
import pl.niebieskieaparaty.imageuploader.upload.core.DataNotFoundException;
import pl.niebieskieaparaty.imageuploader.upload.core.EventId;
import pl.niebieskieaparaty.imageuploader.upload.core.UploadException;
import pl.niebieskieaparaty.imageuploader.upload.core.UploadedData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
class InMemoryImageUploadedDataRepository implements ImageUploadedDataRepository {

    private final Map<EventId, List<UploadedData>> repository = new ConcurrentHashMap<>();

    @Override
    public void saveUploadedData(final UploadedData uploadedData) {
        if (uploadedData == null || StringUtils.isBlank(uploadedData.eventId())) {
            throw new UploadException("UploadedData and eventId must not be null");
        }
        try {
            final var eventId = EventId.of(UUID.fromString(uploadedData.eventId()));
            final var uploadedDataList = repository
                    .computeIfAbsent(eventId, k -> Collections.synchronizedList(new ArrayList<>()));
            uploadedDataList.add(uploadedData);
        } catch (IllegalArgumentException e) {
            throw new UploadException("Invalid eventId format: " + uploadedData.eventId(), e);
        }
    }

    @Override
    public void deleteEntryForEventId(final EventId eventId) {
        if (eventId == null || eventId.eventId() == null) {
            throw new UploadException("EventId must not be null");
        }
        final var removedData = repository.remove(eventId);
        if (removedData == null) {
            throw new DataNotFoundException("No data found for eventId: " + eventId.eventId());
        }
    }

    @Override
    public List<UploadedData> getDataByEventId(final EventId eventId) {
        if (eventId == null || eventId.eventId() == null) {
            return Collections.emptyList();
        }
        final var uploadedDataList = repository.get(eventId);
        if (uploadedDataList == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(uploadedDataList);
    }
}
