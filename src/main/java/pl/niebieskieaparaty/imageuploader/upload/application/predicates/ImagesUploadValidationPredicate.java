package pl.niebieskieaparaty.imageuploader.upload.application.predicates;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Header;
import pl.niebieskieaparaty.imageuploader.upload.core.UploadedData;

import java.util.List;

@ApplicationScoped
public class ImagesUploadValidationPredicate {

    public boolean isValidImageAmount(final List<UploadedData> uploadedDataList,
                                      final @Header("imagesAmount") Integer expectedAmount) {
        return expectedAmount.compareTo(uploadedDataList.size()) == 0;
    }
}
