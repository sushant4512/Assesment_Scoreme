package com.assignment.assignment.service;

import com.assignment.assignment.model.PdfDocument;
import com.assignment.assignment.repository.PdfRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
public class PdfService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path storageDir = Paths.get("storage");

    public PdfService() throws IOException {
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }
    }

    // Upload and segment PDF
    public PdfDocument uploadAndSegmentPdf(MultipartFile file, int cuts) throws IOException {
        // Save original PDF
        String originalFileName = file.getOriginalFilename();
        Path originalPath = storageDir.resolve(UUID.randomUUID().toString() + "_" + originalFileName);
        Files.copy(file.getInputStream(), originalPath, StandardCopyOption.REPLACE_EXISTING);

        // Create PdfDocument entry
        PdfDocument document = new PdfDocument();
        document.setOriginalFileName(originalFileName);
        document.setStoragePath(originalPath.toString());
        document.setNumberOfCuts(cuts);
        document.setSegmentedPaths(new ArrayList<>());
        PdfDocument savedDocument = pdfRepository.save(document);

        // Process segmentation
        List<Path> segments = segmentPdf(originalPath.toFile(), cuts, savedDocument.getId());
        List<String> segmentPaths = segments.stream()
                .map(Path::toString)
                .collect(Collectors.toList());
        savedDocument.setSegmentedPaths(segmentPaths);
        pdfRepository.save(savedDocument);

        return savedDocument;
    }

    // Segment PDF based on whitespace
    private List<Path> segmentPdf(File pdfFile, int cuts, Long documentId) throws IOException {
        PDDocument document = PDDocument.load(pdfFile);
        PDFTextStripper stripper = new PDFTextStripper();

        // Extract text and positions
        stripper.setSortByPosition(true);
        String text = stripper.getText(document);





        // Placeholder: Let's assume each page is a separate segment
        int totalPages = document.getNumberOfPages();
        int segments = Math.min(cuts + 1, totalPages); // Ensure we don't have more segments than pages
        List<Path> segmentPaths = new ArrayList<>();

        // Calculate pages per segment
        int pagesPerSegment = totalPages / segments;
        int remainingPages = totalPages % segments;

        int startPage = 1;
        for (int i = 0; i < segments; i++) {
            int endPage = startPage + pagesPerSegment - 1;
            if (remainingPages > 0) {
                endPage += 1;
                remainingPages -= 1;
            }
            PDDocument segmentDoc = new PDDocument();
            for (int p = startPage; p <= endPage; p++) {
                segmentDoc.addPage(document.getPage(p - 1));
            }
            String segmentFileName = "segment_" + documentId + "_" + (i + 1) + ".pdf";
            Path segmentPath = storageDir.resolve(segmentFileName);
            segmentDoc.save(segmentPath.toFile());
            segmentDoc.close();
            segmentPaths.add(segmentPath);
            startPage = endPage + 1;
        }
        document.close();
        return segmentPaths;
    }

    // Get segmented PDFs as a zip
    public byte[] getSegmentedPdfsAsZip(Long id) throws IOException {
        Optional<PdfDocument> optional = pdfRepository.findById(id);
        if (!optional.isPresent()) {
            throw new FileNotFoundException("PDF Document not found");
        }
        PdfDocument document = optional.get();
        List<String> segmentPaths = document.getSegmentedPaths();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        for (String pathStr : segmentPaths) {
            Path path = Paths.get(pathStr);
            ZipEntry entry = new ZipEntry(path.getFileName().toString());
            zos.putNextEntry(entry);
            Files.copy(path, zos);
            zos.closeEntry();
        }
        zos.close();
        return baos.toByteArray();
    }

    // Get PdfDocument
    public Optional<PdfDocument> getPdfDocument(Long id) {
        return pdfRepository.findById(id);
    }

    // Update segmentation details
    public PdfDocument updateSegmentation(Long id, int newCuts) throws IOException {
        Optional<PdfDocument> optional = pdfRepository.findById(id);
        if (!optional.isPresent()) {
            throw new FileNotFoundException("PDF Document not found");
        }
        PdfDocument document = optional.get();
        document.setNumberOfCuts(newCuts);

        // Delete existing segments
        List<String> existingSegments = document.getSegmentedPaths();
        for (String pathStr : existingSegments) {
            Files.deleteIfExists(Paths.get(pathStr));
        }
        document.setSegmentedPaths(new ArrayList<>());

        // Re-segment the PDF
        Path originalPath = Paths.get(document.getStoragePath());
        List<Path> newSegments = segmentPdf(originalPath.toFile(), newCuts, document.getId());
        List<String> newSegmentPaths = newSegments.stream()
                .map(Path::toString)
                .collect(Collectors.toList());
        document.setSegmentedPaths(newSegmentPaths);
        pdfRepository.save(document);

        return document;
    }

    // Delete PDF and its segments
    public void deletePdf(Long id) throws IOException {
        Optional<PdfDocument> optional = pdfRepository.findById(id);
        if (!optional.isPresent()) {
            throw new FileNotFoundException("PDF Document not found");
        }
        PdfDocument document = optional.get();
        // Delete original PDF
        Files.deleteIfExists(Paths.get(document.getStoragePath()));
        // Delete segments
        for (String pathStr : document.getSegmentedPaths()) {
            Files.deleteIfExists(Paths.get(pathStr));
        }
        // Delete metadata
        pdfRepository.delete(document);
    }
}