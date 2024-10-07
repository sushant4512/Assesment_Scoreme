package com.assignment.assignment.controller;

import com.assignment.assignment.model.PdfDocument;
import com.assignment.assignment.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/pdfs")
public class PdfController {

    @Autowired
    private PdfService pdfService;

    // Upload PDF and initiate segmentation
    @PostMapping
    public ResponseEntity<PdfDocument> uploadPdf(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("cuts") int cuts) {
        try {
            PdfDocument document = pdfService.uploadAndSegmentPdf(file, cuts);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Retrieve segmented PDFs (as a zip or individual links)
    @GetMapping("/{id}/segments")
    public ResponseEntity<?> getSegments(@PathVariable Long id) {
        try {
            byte[] zipBytes = pdfService.getSegmentedPdfsAsZip(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=segments.zip")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipBytes);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Retrieve metadata
    @GetMapping("/{id}/metadata")
    public ResponseEntity<PdfDocument> getMetadata(@PathVariable Long id) {
        Optional<PdfDocument> document = pdfService.getPdfDocument(id);
        return document.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update segmentation details
    @PutMapping("/{id}")
    public ResponseEntity<PdfDocument> updateSegmentation(@PathVariable Long id,
                                                          @RequestParam("cuts") int cuts) {
        try {
            PdfDocument updatedDocument = pdfService.updateSegmentation(id, cuts);
            return ResponseEntity.ok(updatedDocument);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete processed PDF
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePdf(@PathVariable Long id) {
        try {
            pdfService.deletePdf(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}