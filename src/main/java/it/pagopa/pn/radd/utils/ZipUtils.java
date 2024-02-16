package it.pagopa.pn.radd.utils;

import it.pagopa.pn.radd.exception.PnZipException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {

    private ZipUtils() {}

    /**
     * Utility method that extracts a PDF from a ZIP given as input
     * @param zipData The ZIP file in bytearray format
     * @return The PDF file in bytearray format
     */
    public static byte[] extractPdfFromZip(byte[] zipData) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipData);
             ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream)) {

            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().endsWith(".pdf")) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }
                    return byteArrayOutputStream.toByteArray();
                }
            }
        } catch (IOException e) {
            throw new PnZipException("Failed to extract PDF from ZIP", e);
        }

        throw new PnZipException("PDF not found in the ZIP file");
    }
}