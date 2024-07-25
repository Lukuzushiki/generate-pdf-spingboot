package com.example.generate_pdf_spingboot.controller;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.generate_pdf_spingboot.model.PdfRequest;

@RestController
@RequestMapping("/api")
public class PdfController {
    @PostMapping("/generate-pdf")
    public ResponseEntity<byte[]> generatePdf(@RequestBody PdfRequest pdfRequest) {
        // Decode base64 image
        byte[] imageData = Base64.getDecoder().decode(pdfRequest.getImage().replace("data:image/png;base64,", ""));

        // Create a new PDF Document
        try (PDDocument document = new PDDocument()) {
            PDRectangle landscape = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            PDRectangle portrait = new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());
            PDPage page = new PDPage(landscape);
            document.addPage(page);

            // Create content stream to write PDF
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                PDImageXObject image = PDImageXObject.createFromByteArray(document, imageData, "image");
                contentStream.drawImage(image, 0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
            }

            // Covert PDF to byte Array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            byte[] pdfBytes = outputStream.toByteArray();

            // Return PDF as byteArray
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);
            httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\\\"output.pdf\\\"");
            return new ResponseEntity<>(pdfBytes, httpHeaders, HttpStatus.OK);
        } catch (Exception e) {
            // Log the exception (you can use a logging framework like SLF4J)
            e.printStackTrace();
            // Return an error response
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
