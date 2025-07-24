package pl.niebieskieaparaty.imageuploader.selection.core;

public record SelectionProcessRequest(
        String selectionId,
        String directoryPath,
        String directoryName
) {
}
