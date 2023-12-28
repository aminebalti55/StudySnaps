package com.example.studysnaps.controllers;


import com.example.studysnaps.Repositories.QuizRepository;
import com.example.studysnaps.services.PdfDocumentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/Pdf")
public class PdfDocumentController {

    @Autowired
    PdfDocumentService pdfDocumentService;
    @Autowired
    QuizRepository quizRepository;



    @PostMapping(value = "/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadPDF(@RequestParam("file") MultipartFile file,
                                                         Authentication authentication) {
        try {
            // Extract text from the uploaded PDF
            String pdfText = pdfDocumentService.extractTextFromPDF(file);

            String textLanguage = "English";

            // Get the current user details from the UserDetailsService
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userEmail = userDetails.getUsername();

            // Generate quizzes and answers based on the PDF content
            Map<String, Object> quizzesAndAnswers = pdfDocumentService.generateQuizzesAndAnswers(pdfText, textLanguage,userEmail);

            // Create and save entities based on the extracted information
            pdfDocumentService.saveQuizzesAndAnswersToDatabase(quizzesAndAnswers, pdfText, textLanguage, userEmail);

            return ResponseEntity.ok(quizzesAndAnswers);
        } catch (IOException e) {
            e.printStackTrace();

            // In case of an error, return an error map in the response body
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error processing the PDF file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}




