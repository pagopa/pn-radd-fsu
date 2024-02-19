package it.pagopa.pn.radd.utils;

import it.pagopa.pn.radd.exception.PnZipException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ZipUtilsTest {


    @Test
    void extractPdfFromZipOkWithOnlyPDF() {
        byte[] zipFile = getFile("zip/zip-with-pdf.zip");

        byte[] pdfFile = ZipUtils.extractPdfFromZip(zipFile);
        assertThat(readPdf(pdfFile)).isTrue();

    }

    @Test
    void extractPdfFromZipOkWithPDFAndXML() {
        byte[] zipFile = getFile("zip/zip-with-pdf-and-xml.zip");

        byte[] pdfFile = ZipUtils.extractPdfFromZip(zipFile);
        assertThat(readPdf(pdfFile)).isTrue();

    }

    @Test
    void extractPdfFromZipKoBecauseNoPdfInZip() {
        byte[] zipFile = getFile("zip/zip-with-xml.zip");

        assertThatExceptionOfType(PnZipException.class).isThrownBy(() -> ZipUtils.extractPdfFromZip(zipFile));
    }



    private byte[] getFile(String file) {
        try {
            return new ClassPathResource(file).getInputStream().readAllBytes();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean readPdf(byte[] pdfFiles) {
        try(PDDocument pdf = PDDocument.load(pdfFiles)) {
            System.out.println(pdf.getNumberOfPages());
            return true;
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

}