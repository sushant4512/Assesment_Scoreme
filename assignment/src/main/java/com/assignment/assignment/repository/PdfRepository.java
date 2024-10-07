package com.assignment.assignment.repository;

import com.assignment.assignment.model.PdfDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PdfRepository extends JpaRepository<PdfDocument, Long> {
}
