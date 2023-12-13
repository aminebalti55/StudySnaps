package com.example.studysnaps.Repositories;

import com.example.studysnaps.entities.PDFDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PDFDocumentRepository extends JpaRepository<PDFDocument, Integer> {
}