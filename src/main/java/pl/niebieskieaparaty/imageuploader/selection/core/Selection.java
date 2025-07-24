package pl.niebieskieaparaty.imageuploader.selection.core;

public record Selection(
        String selectionId,
        Boolean blocked,
        String eventId,
        String eventTitle,
        String username
) {
}
