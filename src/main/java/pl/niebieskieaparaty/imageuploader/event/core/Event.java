package pl.niebieskieaparaty.imageuploader.event.core;

public record Event(
        String eventId,
        String date,
        String username,
        String title
) {
    public static Event of(String eventId,
                           String date,
                           String username,
                           String title) {
        return new Event(eventId, date, username, title);
    }
}