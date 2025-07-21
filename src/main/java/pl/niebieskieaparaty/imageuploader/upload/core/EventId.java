package pl.niebieskieaparaty.imageuploader.upload.core;

import java.util.UUID;

public record EventId(UUID eventId) {

    public static EventId of(final UUID eventId) {
        return new EventId(eventId);
    }
}
