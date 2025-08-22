package pl.niebieskieaparaty.imageuploader.upload.application.processor;

import io.github.mojtabaJ.cwebp.CWebp;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import pl.niebieskieaparaty.imageuploader.upload.configuration.ImageCompressionConfiguration;
import pl.niebieskieaparaty.imageuploader.upload.core.ImageCompressionException;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import static pl.niebieskieaparaty.imageuploader.upload.application.constant.UploadRouteConstant.*;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class JpegToWebpImageCompressor implements Processor {

    private final ImageCompressionConfiguration config;

    @Override
    public void process(final Exchange exchange) throws Exception {
        File tempInputFile = null;
        File tempOutputFile = null;

        try {
            final var inputBytes = exchange.getIn().getBody(byte[].class);
            if (inputBytes == null) {
                throw new ImageCompressionException("Input image bytes are null");
            }

            final var originalFileName = exchange.getIn().getHeader("CamelFileName", String.class);
            if (originalFileName == null || originalFileName.isBlank()) {
                throw new ImageCompressionException("Original file name header is missing");
            }

            tempInputFile = File.createTempFile(config.getTempFilePrefix() + "_input", ".tmp");
            try (FileOutputStream fos = new FileOutputStream(tempInputFile)) {
                fos.write(inputBytes);
            }

            final var image = ImageIO.read(tempInputFile);
            if (image == null) {
                throw new ImageCompressionException("Input bytes are not a valid image");
            }

            final var dimensions = calculateNewDimensions(image.getWidth(), image.getHeight());
            tempOutputFile = File.createTempFile(config.getTempFilePrefix() + "_output", "." + config.getFormat());

            compressImage(tempInputFile, tempOutputFile, dimensions);

            final var compressedBytes = Files.readAllBytes(tempOutputFile.toPath());
            final var compressedFileName = generateCompressedFileName(originalFileName);

            setExchangeHeaders(exchange, compressedBytes, compressedFileName, dimensions);

            log.info("Compressed image from {}x{} to {}x{}, output size: {} bytes, output file name: {}",
                    image.getWidth(), image.getHeight(), dimensions.width(), dimensions.height(),
                    compressedBytes.length, compressedFileName);

        } catch (IOException e) {
            throw new ImageCompressionException("Failed to process image compression", e);
        } finally {
            cleanupTempFiles(tempInputFile, tempOutputFile);
        }
    }

    private Dimensions calculateNewDimensions(int originalWidth, int originalHeight) {
        if (originalWidth <= config.getMaxWidth()) {
            return new Dimensions(originalWidth, originalHeight);
        }

        final float scale = (float) config.getMaxWidth() / originalWidth;
        final int newWidth = config.getMaxWidth();
        final int newHeight = Math.round(originalHeight * scale);

        return new Dimensions(newWidth, newHeight);
    }

    private void compressImage(File inputFile, File outputFile, Dimensions dimensions) throws IOException {
        try {
            new CWebp()
                    .input(inputFile.getAbsolutePath())
                    .output(outputFile.getAbsolutePath())
                    .resize(dimensions.width(), dimensions.height())
                    .quality(config.getQuality())
                    .execute();
        } catch (Exception e) {
            throw new IOException("CWebp compression failed", e);
        }
    }

    private String generateCompressedFileName(String originalFileName) {
        final String fileExtension = "." + config.getFormat();
        return originalFileName.contains(".")
                ? originalFileName.substring(0, originalFileName.lastIndexOf('.')) + fileExtension
                : originalFileName + fileExtension;
    }

    private void setExchangeHeaders(Exchange exchange, byte[] compressedBytes, String fileName, Dimensions dimensions) {
        exchange.getIn().setBody(compressedBytes);
        exchange.getIn().setHeader(COMPRESSED_FILE_NAME, fileName);
        exchange.getIn().setHeader(COMPRESSED_FILE_WIDTH, dimensions.width());
        exchange.getIn().setHeader(COMPRESSED_FILE_HEIGHT, dimensions.height());
    }

    private void cleanupTempFiles(File... files) {
        for (File file : files) {
            if (file != null && file.exists()) {
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (IOException e) {
                    log.warn("Failed to delete temporary file: {}", file.getAbsolutePath(), e);
                }
            }
        }
    }

    private record Dimensions(int width, int height) {
    }
}

