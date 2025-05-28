package utils;

import com.typesafe.config.ConfigFactory;
import controllers.ShortFormatPreSentenceReportController;
import controllers.base.EncryptedFormFactory;
import data.ShortFormatPreSentenceReportData;
import interfaces.DocumentStore;
import interfaces.PdfGenerator;

public class TestableWizardController extends ShortFormatPreSentenceReportController {

    public TestableWizardController(PdfGenerator pdfGenerator, DocumentStore documentStore) {

        super(
                null,
                null,
                ConfigFactory.load(),
                null,
                null,
                new EncryptedFormFactory(null, null, null, null),
                pdfGenerator,
                documentStore,
                null,
                null,
                null,
                null);
    }

    public void testStoreReport(ShortFormatPreSentenceReportData data) {

        nextPage(data);
    }
}
