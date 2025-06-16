package pl.niebieskieaparaty.imageuploader.upload.application.processor;

import io.github.mojtabaJ.cwebp.CWebp;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import static pl.niebieskieaparaty.imageuploader.upload.application.constant.UploadRouteConstant.*;

@Slf4j
@ApplicationScoped
public class JpegToWebpImageCompressor implements Processor {

    @Override
    public void process(final Exchange exchange) throws Exception {

        final var inputBytes = exchange.getIn().getBody(byte[].class);
        if (inputBytes == null) {
            throw new IllegalArgumentException("Input image bytes are null");
        }

        final var originalFileName = exchange.getIn().getHeader("CamelFileName", String.class);
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Original file name header is missing");
        }

        final var tempInputFile = File.createTempFile("input", ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tempInputFile)) {
            fos.write(inputBytes);
        }

        final var image = ImageIO.read(tempInputFile);
        if (image == null) {
            tempInputFile.delete();
            throw new IllegalArgumentException("Input bytes are not a valid image");
        }

        final int maxWidth = 2500;
        int newWidth = image.getWidth();
        int newHeight = image.getHeight();

        if (newWidth > maxWidth) {
            final float scale = (float) maxWidth / newWidth;
            newWidth = maxWidth;
            newHeight = Math.round(image.getHeight() * scale);
        }

        final var tempOutputFile = File.createTempFile("output", ".webp");

        new CWebp()
                .input(tempInputFile.getAbsolutePath())
                .output(tempOutputFile.getAbsolutePath())
                .resize(newWidth, newHeight)
                .quality(85)
                .execute();

        final var webpBytes = Files.readAllBytes(tempOutputFile.toPath());

        tempInputFile.delete();
        tempOutputFile.delete();

        exchange.getIn().setBody(webpBytes);

        final var compressedFileName = originalFileName.contains(".")
                ? originalFileName.substring(0, originalFileName.lastIndexOf('.')) + ".webp"
                : originalFileName + ".webp";

        exchange.getIn().setHeader(COMPRESSED_FILE_NAME, compressedFileName);
        exchange.getIn().setHeader(COMPRESSED_FILE_WIDTH, newWidth);
        exchange.getIn().setHeader(COMPRESSED_FILE_HEIGHT, newHeight);

        log.info("Compressed image from {}x{} to {}x{}, output size: {} bytes, output file name: {}",
                image.getWidth(), image.getHeight(), newWidth, newHeight, webpBytes.length, compressedFileName);
    }
}
