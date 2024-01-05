package com.example.studysnaps.Repositories;

import com.example.studysnaps.entities.PDFDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Repository
public interface PDFDocumentRepository extends JpaRepository<PDFDocument, Integer> {
    Optional<PDFDocument> findByDocId(Integer id);

}