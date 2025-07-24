package pl.niebieskieaparaty.imageuploader.file.core;

import java.util.List;

public record FileSelectionRequest(String baseDirectoryPath, List<String> imageNames) {

    public static FileSelectionRequest of(final String baseDirectoryPath, final List<String> imageNames) {
        return new FileSelectionRequest(baseDirectoryPath, imageNames);
    }
}
